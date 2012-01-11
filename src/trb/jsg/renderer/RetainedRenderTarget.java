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

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.Util;

import trb.jsg.RenderTarget;
import trb.jsg.Texture;
import trb.jsg.peers.*;

class RetainedRenderTarget implements RenderTargetPeer, NativeResource {

	/** The opengl fbo id */
	public IntBuffer fboId = BufferUtils.createIntBuffer(1);

	/** Depth buffer id */
	private IntBuffer depthbufferId = BufferUtils.createIntBuffer(1);
	
	/** What fbo color attachments to render */
	private IntBuffer drawBuffers; 

	/** The peers render target */
	public RenderTarget renderTarget;
	
	/**
	 * 
	 * @param renderTarget
	 */
	public RetainedRenderTarget(RenderTarget renderTarget) {
		this.renderTarget = renderTarget;
		drawBuffers = BufferUtils.createIntBuffer(Math.min(RetainedSceneGraph.maxDrawBuffers, renderTarget.getColorAttachments().length));
		for (int i=0; i<drawBuffers.limit(); i++) {
			int id = renderTarget.getColorAttachments()[i] == null ? GL_NONE : (GL_COLOR_ATTACHMENT0_EXT + i);
			drawBuffers.put(i, id);
		}
	}

	/**
	 * Implements SimpleNativeResource
	 */
	public void destroyNativeResource() {
		System.out.println(getClass().getSimpleName()+".destroyNativeResource()");
		if (depthbufferId.get(0) > 0) {
			depthbufferId.rewind();
			glDeleteRenderbuffersEXT(depthbufferId);
			System.out.println("SimpleRenderTargetPeer destroy depth buffer id="+depthbufferId.get(0));
		}
		if (fboId.get(0) > 0) {
			fboId.rewind();
			glDeleteFramebuffersEXT(fboId);
			System.out.println("SimpleRenderTargetPeer destroy fbo buffer id="+fboId.get(0));
		}
		for (Texture colorAttachment : renderTarget.getColorAttachments()) {
			if (colorAttachment.nativePeer != null) {
				RetainedTexture peer = (RetainedTexture) colorAttachment.nativePeer;
				peer.parents.remove(renderTarget);
			}
		}
	}

	/**
	 * Implements SimpleNativeResource
	 */
	public void updateNativeResource() {
		System.out.println(getClass().getSimpleName()+".updateNativeResource()");
		if (fboId.get(0) <= 0) {
			glGenFramebuffersEXT(fboId);
			glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId.get(0));
			
			if (renderTarget.isAttachDepthBuffer()) {
				// add depth buffer
				glGenRenderbuffersEXT(depthbufferId);
				glBindRenderbufferEXT(GL_RENDERBUFFER_EXT, depthbufferId.get(0));
				glRenderbufferStorageEXT(GL_RENDERBUFFER_EXT, GL_DEPTH_COMPONENT, renderTarget.getWidth(), renderTarget.getHeight());
				glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, depthbufferId.get(0));
			}
			
			for (Texture colorAttachment : renderTarget.getColorAttachments()) {
				if (colorAttachment != null) {
					// add a texture
					RetainedTexture peer = (RetainedTexture) colorAttachment.nativePeer;
					if (peer == null) {
						System.out.println("texture netivePeer is null");
						// first time texture is added
						peer = new RetainedTexture(colorAttachment);
						peer.updateNativeResource();
						colorAttachment.nativePeer = peer;
					} else {
						System.out.println("texture netivePeer texture id is "+peer.getTextureId());
						if (peer.getTextureId() <= 0) {
							peer.updateNativeResource();
							System.out.println("After updateNativeResource() texture netivePeer texture id is "+peer.getTextureId());
						}
					}
					peer.parents.add(renderTarget);
					if (renderTarget.isGenerateMipMap()) {
						GLState.glActiveTextureWrapper(GL13.GL_TEXTURE0);
						GLState.glBindTextureWrapper(GL_TEXTURE_2D, peer.getTextureId());
						glGenerateMipmapEXT(GL_TEXTURE_2D);
					}
					
					glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, peer.getTextureId(), 0);
				}
			}
            int frameBufferState = glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT);
			if (GL_FRAMEBUFFER_COMPLETE_EXT != frameBufferState) {
                String description = "";
                if (GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT == frameBufferState) {
                    description = "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT";
                } else if (GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT == frameBufferState) {
                    description = "GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT";
                } else if (GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT == frameBufferState) {
                    description = "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT";
                } else if (GL_FRAMEBUFFER_UNSUPPORTED_EXT == frameBufferState) {
                    description = "GL_FRAMEBUFFER_UNSUPPORTED_EXT";
                }
				throw new RuntimeException("Framebuffer not complete. " + description);
			}
			Util.checkGLError();
		}
	}
	
	/**
	 * Gets the draw buffers to use.
	 * @return the draw buffers
	 */
	public IntBuffer getDrawBuffers() {
		return drawBuffers;
	}
}
