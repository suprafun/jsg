/*
 * Copyright (c) 2008 Java Scene Graph
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
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
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

import static org.lwjgl.opengl.EXTFramebufferObject.GL_MAX_COLOR_ATTACHMENTS_EXT;
import static org.lwjgl.opengl.GL11.glGetInteger;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.peers.*;

/**
 * Wraps a SceneGraph.
 * 
 * @author tombr
 */
class RetainedSceneGraph implements SceneGraphPeer {
	
	public static final boolean SAFE_MODE = false; 
	
	/** List of native resource that needs to be updated at the next render. */
	public ArrayList<NativeResource> updateList = new ArrayList<NativeResource>();
	
	/** List of native resource that needs to be destroyed at the next render. */
	public ArrayList<NativeResource> destroyList = new ArrayList<NativeResource>();
	
	/** The wrapped scene graph */
	public SceneGraph sceneGraph;
	
	/** The number of available color attachments. Is set after constructing a 
	 * SimpleSceneGraphPeer. */
	public static int maxColorAttachments = -1;
	
	/**
	 * How many draw buffers we can render to at the same time.
	 */
	public static int maxDrawBuffers = -1;

	/**
	 * Constructs a SimpleSceneGraph that is the native peer of the specified
	 * scenegraph. Adds all passes in the specified scenegraph to this peer.
	 * @param sceneGraph the scenegraph to wrap
	 */
	public RetainedSceneGraph(SceneGraph sceneGraph) {
		this.sceneGraph = sceneGraph;
		sceneGraph.nativePeer = this;
		
		GLState.init();
		
		IntBuffer intBuffer = BufferUtils.createIntBuffer(16);
		glGetInteger(GL_MAX_COLOR_ATTACHMENTS_EXT, intBuffer);
		maxColorAttachments = intBuffer.get(0);
		glGetInteger(GL20.GL_MAX_DRAW_BUFFERS, intBuffer);
		maxDrawBuffers = intBuffer.get(0);
		
//		http://www.gamedev.net/reference/programming/features/fbo2/page5.asp
//		GLenum buffers[] = { GL_COLOR_ATTACHMENT0_EXT, GL_COLOR_ATTACHMENT1_EXT };
//		glDrawBuffers(2, buffers);
		
		
		for (int passIdx=0; passIdx<sceneGraph.getRenderPassCount(); passIdx++) {
			RenderPass pass = sceneGraph.getRenderPass(passIdx);
			passAdded(pass);
		}
	}
	/**
	 * Implements ScenenGraphListener.
	 */
	public void passAdded(RenderPass renderPass) {
		if (renderPass.nativePeer != null) {
			throw new RuntimeException("RenderPass already has a native peer");
		}
		
		renderPass.nativePeer = new RetainedRenderPass(renderPass, this);
		for (int shapeIdx=0; shapeIdx<renderPass.getShapeCount(); shapeIdx++) {
			Shape shape = renderPass.getShape(shapeIdx);
			renderPass.nativePeer.shapeAdded(shape);
		}
		
		renderPass.nativePeer.renderTargetChanged(null, renderPass.getRenderTarget());		
	}
	
	/**
	 * Implements ScenenGraphListener.
	 */
	public void passRemoved(RenderPass renderPass) {
		for (int shapeIdx=0; shapeIdx<renderPass.getShapeCount(); shapeIdx++) {
			Shape shape = renderPass.getShape(shapeIdx);
			renderPass.nativePeer.shapeRemoved(shape);
		}

		renderPass.nativePeer.renderTargetChanged(renderPass.getRenderTarget(), null);		
		
		renderPass.nativePeer = null;
	}

	/**
	 * Renders the SceneGraph.
	 */
	public void render() {
		GLState.validateState();
		
		// update native resources that has been added or changed the last frame
		for (NativeResource resource : updateList) {
			resource.updateNativeResource();
		}
		updateList.clear();
		
		// destroy native resource that has been removed or deleted the last frame
		for (NativeResource resource : destroyList) {
			resource.destroyNativeResource();
		}
		destroyList.clear();
		
		// render the render passes in order
		for (int passIdx=0; passIdx<sceneGraph.getRenderPassCount(); passIdx++) {
			((RetainedRenderPass) sceneGraph.getRenderPass(passIdx).nativePeer).render();
		}
	}
}
