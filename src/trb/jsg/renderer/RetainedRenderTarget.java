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

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.Util;
import trb.jsg.DepthBuffer;

import trb.jsg.RenderTarget;
import trb.jsg.Texture;
import trb.jsg.peers.*;

class RetainedRenderTarget implements RenderTargetPeer, NativeResource {

	/** The opengl fbo id */
	public IntBuffer fboId = BufferUtils.createIntBuffer(1);
	
	/** What fbo color attachments to render */
	private IntBuffer drawBuffers; 

	/** The peers render target */
	public RenderTarget renderTarget;
	
	public RetainedRenderTarget(RenderTarget renderTarget) {
		this.renderTarget = renderTarget;
        if (renderTarget.getColorAttachments().length > RetainedSceneGraph.maxDrawBuffers) {
            System.err.println(renderTarget.getColorAttachments().length
                    + " is more color attachments than are supported: " + RetainedSceneGraph.maxDrawBuffers);
        }
		drawBuffers = BufferUtils.createIntBuffer(Math.min(RetainedSceneGraph.maxDrawBuffers, renderTarget.getColorAttachments().length));
		for (int i=0; i<drawBuffers.limit(); i++) {
			int id = renderTarget.getColorAttachments()[i] == null ? GL_NONE : (GL_COLOR_ATTACHMENT0 + i);
			drawBuffers.put(i, id);
		}
	}

	/**
	 * Implements NativeResource
	 */
	public void destroyNativeResource() {
		//System.out.println(getClass().getSimpleName()+".destroyNativeResource()");
        DepthBuffer depthBuffer = renderTarget.getDepthBuffer();
        if (depthBuffer != null && depthBuffer.nativePeer instanceof RetainedDepthBuffer) {
            RetainedDepthBuffer retainedDepthBuffer = (RetainedDepthBuffer) depthBuffer.nativePeer;
            retainedDepthBuffer.parents.remove(renderTarget);
            if (retainedDepthBuffer.parents.isEmpty()) {
                IntBuffer id = retainedDepthBuffer.depthBufferId;
                id.rewind();
                glDeleteRenderbuffers(id);
                depthBuffer.nativePeer = null;
                //System.out.println(this + " destroy depth buffer id=" + id.get(0));
            }
        }
		if (fboId.get(0) > 0) {
			fboId.rewind();
			glDeleteFramebuffers(fboId);
			//System.out.println(this + " destroy fbo buffer id="+fboId.get(0));
		}
		for (Texture colorAttachment : renderTarget.getColorAttachments()) {
			if (colorAttachment.nativePeer != null) {
				RetainedTexture peer = (RetainedTexture) colorAttachment.nativePeer;
				peer.parents.remove(renderTarget);
			}
		}
	}

	/**
	 * Implements NativeResource
	 */
	public void updateNativeResource() {
		//System.err.println(this+".updateNativeResource()");
		if (fboId.get(0) <= 0) {
			glGenFramebuffers(fboId);
			glBindFramebuffer(GL_FRAMEBUFFER, fboId.get(0));

            DepthBuffer depthBuffer = renderTarget.getDepthBuffer();
            if (depthBuffer != null) {
                if (depthBuffer.nativePeer == null) {
                    depthBuffer.nativePeer = new RetainedDepthBuffer();
                }
                if (depthBuffer.nativePeer instanceof RetainedDepthBuffer) {
                    RetainedDepthBuffer retainedDepthBuffer = (RetainedDepthBuffer) depthBuffer.nativePeer;
                    retainedDepthBuffer.parents.add(renderTarget);
                    IntBuffer id = retainedDepthBuffer.depthBufferId;
                    if (id.get(0) <= 0) {
                        id.rewind();
                        glGenRenderbuffers(id);
                        glBindRenderbuffer(GL_RENDERBUFFER, id.get(0));
                        glRenderbufferStorage(GL_RENDERBUFFER, depthBuffer.format, renderTarget.getWidth(), renderTarget.getHeight());
                    } else {
                        glBindRenderbuffer(GL_RENDERBUFFER, id.get(0));
                    }
                    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, id.get(0));
                    if (depthBuffer.format == GL_DEPTH24_STENCIL8 || depthBuffer.format == GL_DEPTH32F_STENCIL8) {
                        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_ATTACHMENT, GL_RENDERBUFFER, id.get(0));
                    }
                }
            }

            Texture[] textures = renderTarget.getColorAttachments();
            for (int i=0; i<textures.length; i++) {
                Texture colorAttachment = textures[i];
				if (colorAttachment != null) {
					// add a texture
					RetainedTexture peer = (RetainedTexture) colorAttachment.nativePeer;
					if (peer == null) {
						//System.out.println("texture netivePeer is null");
						// first time texture is added
						peer = new RetainedTexture(colorAttachment);
						peer.updateNativeResource();
						colorAttachment.nativePeer = peer;
					} else {
						//System.out.println("texture netivePeer texture id is "+peer.getTextureId());
						if (peer.getTextureId() <= 0) {
							peer.updateNativeResource();
							//System.out.println("After updateNativeResource() texture netivePeer texture id is "+peer.getTextureId());
						}
					}
					peer.parents.add(renderTarget);
					if (renderTarget.isGenerateMipMap()) {
						GLState.glActiveTextureWrapper(GL13.GL_TEXTURE0);
						GLState.glBindTextureWrapper(GL_TEXTURE_2D, peer.getTextureId());
						glGenerateMipmap(GL_TEXTURE_2D);
					}
					
					glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i
                            , colorAttachment.getType().get(), peer.getTextureId(), 0);
				}
			}
            int frameBufferState = glCheckFramebufferStatus(GL_FRAMEBUFFER);
			if (GL_FRAMEBUFFER_COMPLETE != frameBufferState) {
                String description = "";
                if (GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT == frameBufferState) {
                    description = "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
                } else if (GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT == frameBufferState) {
                    description = "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
                } else if (GL_FRAMEBUFFER_UNSUPPORTED == frameBufferState) {
                    description = "GL_FRAMEBUFFER_UNSUPPORTED";
                } else {
                    description = "Unknown";
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
