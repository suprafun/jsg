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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.Util;

import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;

import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.peers.*;
import trb.jsg.util.SGUtil;

/**
 * Texture peer.
 * 
 * @author tombr
 *
 */
class RetainedTexture implements TexturePeer, NativeResource {

	/** List of shapes that references this Shader */
	public ArrayList<Object> parents = new ArrayList<Object>();
	
	/** Set to true when texture data is changed and cleared when texture is uploaded */
	public boolean isDataDirty = true;
	
	/** Set to true when texture state is changed and cleared when texture state is set */
	public boolean isStateDirty = true;

	/** The peers texture */
	public Texture texture;

	/** The opengl texture id */
	private IntBuffer textureId = BufferUtils.createIntBuffer(1);

	/**
	 * 
	 * @param texture
	 */
	public RetainedTexture(Texture texture) {
		this.texture = texture;
	}

	/**
	 * Implements SimpleNativeResource.
	 */
	public void updateNativeResource() {
		if (isDataDirty || isStateDirty) {
			for (int i=0; i<parents.size(); i++) {
				Object obj = parents.get(i);
				if (obj instanceof Shape) {
					((RetainedShape) ((Shape)obj).nativePeer).textureStateHash = -1;
				}
			}
		}
		
		if (isDataDirty) {
			isDataDirty = false;
			//System.out.println(getClass().getSimpleName()+" update data");

			if (textureId.get(0) <= 0) {
				textureId.rewind();
				glGenTextures(textureId);
				System.out.println(this+" SimpleTexturePeer create opengl texture id="+textureId.get(0));
			}
			int id = textureId.get(0);
			int target = texture.getType().get();
			GLState.glActiveTextureWrapper(GL13.GL_TEXTURE0);
			GLState.glBindTextureWrapper(target, id);

			int w = SGUtil.roundPow2(texture.getWidth());
			int h = SGUtil.roundPow2(texture.getHeight());
			int d = SGUtil.roundPow2(texture.getDepth());
			boolean isPow2 = false;
			
			switch (texture.getType()) {
			case TEXTURE_1D:
				System.err.println(getClass().getSimpleName()+" ERROR: GL_TEXTURE_1D not supported");
				if (w == texture.getWidth()) {
					isPow2 = true;
				}
				break;
			case TEXTURE_2D:
				if (w == texture.getWidth() && h == texture.getHeight()) {
					isPow2 = true;
					if (texture.getLevelCount(0) <= 1) {
						int mipmapVal = texture.getGenerateMipMaps() ? GL_TRUE : GL_FALSE;
						glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, mipmapVal);
					}
					for (int levelIdx = 0; levelIdx < texture.getLevelCount(0); levelIdx++) {
						ByteBuffer pixels = texture.getPixels(0, levelIdx);
						pixels.rewind();
						//System.out.println("glTexImage2D "+levelIdx+" w="+w+" h="+h);
						glTexImage2D(GL_TEXTURE_2D, levelIdx, texture.getInternalFormat()
								, w, h
								, 0, texture.getFormat().get(), GL_UNSIGNED_BYTE, pixels);
						w = Math.max(1, w >> 1);
						h = Math.max(1, h >> 1);
					}
				}
				break;
			case TEXTURE_3D:
				System.err.println(getClass().getSimpleName()+" ERROR: GL_TEXTURE_3D not supported");
				if (w == texture.getWidth() && h == texture.getHeight() && d == texture.getDepth()) {
					isPow2 = true;
				}
				break;
			case TEXTURE_CUBE_MAP:
				isPow2 = true;
				for (int sideIdx=0; sideIdx<6; sideIdx++) {
					int cubew = w;
					int cubeh = h;
					for (int levelIdx = 0; levelIdx < texture.getLevelCount(sideIdx); levelIdx++) {
						ByteBuffer pixels = texture.getPixels(sideIdx, levelIdx);
						pixels.rewind();
						int sideTarget = GL_TEXTURE_CUBE_MAP_POSITIVE_X+sideIdx;
						Util.checkGLError();
						glTexImage2D(sideTarget, levelIdx, texture.getInternalFormat()
								, cubew, cubeh
								, 0, texture.getFormat().get(), GL_UNSIGNED_BYTE, pixels);
						Util.checkGLError();
						cubew = Math.max(1, cubew >> 1);
						cubeh = Math.max(1, cubeh >> 1);
						int mipmapVal = texture.getGenerateMipMaps() ? GL_TRUE : GL_FALSE;
						//System.err.println("glTexParameteri "+ sideTarget+" GL_GENERATE_MIPMAP "+mipmapVal);
						//glTexParameteri(target, GL_GENERATE_MIPMAP, mipmapVal);
						Util.checkGLError();
					}
				}
				break;
			default:
				Thread.dumpStack();
				break;
			}
			
			if (!isPow2) {
				System.err.println("Non power of two textures not supported at the moment w="+w+" h="+h+" d="+d
						+" "+texture.getWidth()+" "+texture.getHeight()+" "+texture.getDepth());
			}
		}

		if (isStateDirty) {
			System.out.println(getClass().getSimpleName()+" update state");
			isStateDirty = false;
			if (textureId.get(0) <= 0) {
				textureId.rewind();
				glGenTextures(textureId);
				System.out.println(this+" SimpleTexturePeer create opengl texture id="+textureId.get(0));
			}
			int target = texture.getType().get();
			int id = textureId.get(0);
			GLState.glActiveTextureWrapper(GL13.GL_TEXTURE0);
			GLState.glBindTextureWrapper(target, id);
			glTexParameteri(target, GL_TEXTURE_MAG_FILTER, texture.getMagFilter().get());
			glTexParameteri(target, GL_TEXTURE_MIN_FILTER, texture.getMinFilter().get());
			glTexParameterf(target, GL_TEXTURE_MAX_ANISOTROPY_EXT, texture.getMaxAnisotropy());
			if (target != GL_TEXTURE_CUBE_MAP) {
				glTexParameteri(target, GL_TEXTURE_WRAP_S, texture.getWrapS().get());
				glTexParameteri(target, GL_TEXTURE_WRAP_T, texture.getWrapT().get());
				glTexParameteri(target, GL_TEXTURE_WRAP_R, texture.getWrapT().get());
			}
		}
	}

	/**
	 * Implements SimpleNativeResource.
	 */
	public void destroyNativeResource() {
		if (textureId.get(0) > 0) {
			textureId.rewind();
			glDeleteTextures(textureId);
			System.out.println(this+" SimpleTexturePeer destroy opengl texture id="+textureId.get(0));
		}
	}

	/**
	 * 
	 * @return
	 */
	public int getTextureId() {
		return textureId.get(0);
	}

	/**
	 * Implements TexturePeer.
	 */
	public void textureDataChanged(Texture texture) {
		isDataDirty = true;
		for (Object parent : parents) {
			if (parent instanceof Shape) {
				RetainedShape shapePeer = (RetainedShape) ((Shape)parent).nativePeer;
				shapePeer.textureDataChanged(this);
			}
		}
	}

	/**
	 * Implements TexturePeer.
	 */
	public void textureStateChanged(Texture texture) {
		isStateDirty = true;
		for (Object parent : parents) {
			if (parent instanceof Shape) {
				RetainedShape shapePeer = (RetainedShape) ((Shape)parent).nativePeer;
				shapePeer.textureStateChanged(this);
			}
		}
	}
//	
//	/**
//	 * Scales the texture to the specified size.
//	 * @return the scaled image in a ByteBuffer
//	 * @param newWidth
//	 * @param newHeight
//	 * @return
//	 */
//	public ByteBuffer scale(int newWidth, int newHeight) {
//		BufferedImage scaledImage = new BufferedImage(newWidth, newHeight);
//	}
}
