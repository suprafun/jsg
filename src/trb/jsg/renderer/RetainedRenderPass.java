/*
 * Copyright (c) 2008-2012 Java Scene Graph
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'Java Scene Graph' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package trb.jsg.renderer;

import static org.lwjgl.opengl.ARBDrawBuffers.*;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;

import javax.vecmath.Color4f;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.Util;

import trb.jsg.*;
import trb.jsg.peers.*;
import trb.jsg.util.GLUtils;

class RetainedRenderPass implements RenderPassPeer {
	
	/** The frame index that starts at 0 increased every frame */
	public int frameIdx = 0;
	
	/** The peers RenderPass */
	public RenderPass renderPass;
	
	/** A reference to the renderer */
	public RetainedSceneGraph sceneGraphPeer;
	
	/** The slow renderer is used if the shape can not be rendered by the other renderers */
	//private SlowRenderer slowRenderer = new SlowRenderer(this);
	
	/** The StateSortRenderer is used if there shapes can be state sorted */
	private StateSortRenderer renderer = new StateSortRenderer(this);
	

	/**
	 * Constructs a SimpleRendererPassPeer that back the specified RenderPass.
	 * @param renderPass the RenderPass
	 * @param renderer the renderer
	 */
	public RetainedRenderPass(RenderPass renderPass, RetainedSceneGraph renderer) {
		this.renderPass = renderPass;
		this.sceneGraphPeer = renderer;
	}
	
	/**
	 * Implements RenderPassPeer.
	 */
	public void shapeAdded(Shape shape) {
		if (shape.nativePeer != null) {
			throw new RuntimeException("Shape already has a native peer");
		}
		RetainedShape peer = new RetainedShape(shape);
		shape.nativePeer = peer;
		peer.shaderChanged(null, shape.getState().getShader());
		peer.vertexDataChanged(null, shape.getVertexData());
		int[] activeUnits = shape.getState().getActiveUnits();
		for (int i=0; i<activeUnits.length; i++) {
			peer.textureChanged(null, shape.getState().getUnit(activeUnits[i]).getTexture());
		}		
		
		peer.renderer = renderer;
		renderer.add(peer);
		//slowRenderer.shapes.add(peer);
	}

	/**
	 * Implements RenderPassPeer.
	 */
	public void shapeRemoved(Shape shape) {
		RetainedShape peer = (RetainedShape) shape.nativePeer;
		
		// remove shape from its renderer
		if (peer.renderer == renderer) {
			renderer.remove(peer);
		}
		
		peer.shaderChanged(shape.getState().getShader(), null);
		peer.vertexDataChanged(shape.getVertexData(), null);
		int[] activeUnits = shape.getState().getActiveUnits();
		for (int i=0; i<activeUnits.length; i++) {
			peer.textureChanged(shape.getState().getUnit(activeUnits[i]).getTexture(), null);
		}
		shape.nativePeer = null;
	}
	
	/**
	 * The render target has changed. 
	 * @param oldTarget the previous render target
	 * @param newTarget the new render target
	 */
	public void renderTargetChanged(RenderTarget oldTarget, RenderTarget newTarget) {
		if (oldTarget != null && oldTarget.nativePeer != null) {
			sceneGraphPeer.destroyList.add((NativeResource) oldTarget.nativePeer);
			oldTarget.nativePeer = null;
		}
		if (newTarget != null) {
			if (newTarget.nativePeer != null) {
				throw new RuntimeException("RenderTarget has a NativePeer");
			}
			
			newTarget.nativePeer = new RetainedRenderTarget(newTarget);
			sceneGraphPeer.updateList.add((NativeResource) newTarget.nativePeer);
		}
	}
	
	/**
	 * Renders the specified RenderPass.
	 */
	public void render() {
		Util.checkGLError();
		glPushAttrib(GL_VIEWPORT_BIT);
		RenderTarget renderTarget = renderPass.getRenderTarget();
		if (renderTarget != null) {
			RetainedRenderTarget renderTargetPeer = (RetainedRenderTarget) renderTarget.nativePeer;
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, renderTargetPeer.fboId.get(0));
			glDrawBuffersARB(renderTargetPeer.getDrawBuffers());
			glViewport(0, 0, renderTarget.getWidth(), renderTarget.getHeight());
		} else {
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
			glDrawBuffer(GL_BACK);
		}
		
		Util.checkGLError();
		
		// clear the buffers using the clear value
		int clearMask = renderPass.getClearMask();
		if ((clearMask & GL_COLOR_BUFFER_BIT) != 0) {
			Color4f clearColor = renderPass.getClearColor();
			glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
		}
		if ((clearMask & GL_DEPTH_BUFFER_BIT) != 0) {
            GLState.applyDepthMaskDif(true);
			glClearDepth(renderPass.getClearDepth());
		}
		if ((clearMask & GL_STENCIL_BUFFER_BIT) != 0) {
			glClearStencil(renderPass.getClearStencil());
		}
		glClear(renderPass.getClearMask());
		
		View view = renderPass.getView();
		view.updateWorldPlanes();
		
		glMatrixMode(GL_PROJECTION);
		GLUtils.loadMatrix(view.getProjectionMatrix());

		glMatrixMode(GL_MODELVIEW);		
		
		renderer.render();
        //slowRenderer.render(new Matrix4f());

		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
		glPopAttrib();
		
		frameIdx++;
	}
}
