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

package trb.jsg;

import java.io.Serializable;

import trb.jsg.peers.RenderTargetPeer;

/**
 * RenderTarget is used to render to one or more textures. In GLSL 
 * gl_FragData[] can be used to write to the different textures. The index is
 * the same in GLSL as the colorAttachment list. How many are available depends
 * on the card and can be queried by SimpleSceneGraphPeer.maxColorAttachments 
 * and SimpleSceneGraphPeer.maxDrawBuffers.
 * <p>
 * The size of all the textures has to be the same as the render target size.
 * <p>
 * If automatic mipmap generation is needed, it should be turned off in the
 * texture objects and on in the render target.
 * 
 * @author tombr
 *
 */
public class RenderTarget implements Serializable {

	private static final long serialVersionUID = 0L;

	/** The size of the render target.*/
	private int width;

	/** The size of the render target.*/
	private int height;
	
	/** Whether or not depth buffer will be attached */
	private boolean attachDepthBuffer = true;
	
	/** Whether or not to generate mipmaps */
	private boolean generateMipMap = false;
	
	/** List of color attachments */
	private Texture[] colorAttachments = new Texture[0];
	
	/** The native peer. Don't touch unless your creating a renderer. */
	transient public RenderTargetPeer nativePeer;
	
	/**
	 * Construct a render target with the specified size, flags and textures. 
	 * @param width the width
	 * @param height the height
	 * @param attachDepthBuffer true to attach a depth buffer
	 * @param generateMipMap true to generate mipmaps
	 * @param colorAttachments the textures to render to
	 */
	public RenderTarget(int width, int height, boolean attachDepthBuffer
			, boolean generateMipMap, Texture... colorAttachments) {
		this.width = width;
		this.height = height;
		this.colorAttachments = colorAttachments;
		this.generateMipMap = generateMipMap;
		this.attachDepthBuffer = attachDepthBuffer;
	}

	/**
	 * Gets the width.
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the height.
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Checks depth buffer will be attached.
	 * @return the attachDepthBuffer flag
	 */
	public boolean isAttachDepthBuffer() {
		return attachDepthBuffer;
	}

	/**
	 * Checks if mipmap will be generated.
	 * @return the generateMipMap flag
	 */
	public boolean isGenerateMipMap() {
		return generateMipMap;
	}

	/**
	 * Gets the list of color attachments.
	 * @return the colorAttachments
	 */
	public Texture[] getColorAttachments() {
		return colorAttachments;
	}
}
