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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.Serializable;
import java.nio.*;

import org.lwjgl.BufferUtils;

import trb.jsg.enums.Format;
import trb.jsg.enums.MagFilter;
import trb.jsg.enums.MinFilter;
import trb.jsg.enums.TextureType;
import trb.jsg.enums.Wrap;
import trb.jsg.peers.TexturePeer;

/**
 * A texture that can be shared amongst shapes.
 * <p>
 * The texture can have 1, 2 or 3 dimension or be a cube map. Utility classes 
 * for the different types is located in trb.sg.util.texture. 
 * <p>
 * Mipmaps can be provided by the application or be generated automaticly.
 * 
 * @author tombr
 *
 */
public class Texture implements Serializable {

	private static final long serialVersionUID = 0L;

	/** Counter that increased every time a Texture is created */
	private static int nextStateId = 1;
	
	/** Unique id that is not the opengl id */
	public int stateId = nextStateId++;
	
	private TextureType type = TextureType.TEXTURE_2D;
	
	/** The number of color components to use internaly by opengl. Must be 
	 * 1, 2, 3, or 4, or one of the following symbolic constants:
        GL_ALPHA,
        GL_ALPHA4,
        GL_ALPHA8,
        GL_ALPHA12,
        GL_ALPHA16,
        GL_COMPRESSED_ALPHA,
        GL_COMPRESSED_LUMINANCE,
        GL_COMPRESSED_LUMINANCE_ALPHA,
        GL_COMPRESSED_INTENSITY,
        GL_COMPRESSED_RGB,
        GL_COMPRESSED_RGBA,
        GL_DEPTH_COMPONENT,
        GL_DEPTH_COMPONENT16,
        GL_DEPTH_COMPONENT24,
        GL_DEPTH_COMPONENT32,
        GL_LUMINANCE,
        GL_LUMINANCE4,
        GL_LUMINANCE8,
        GL_LUMINANCE12,
        GL_LUMINANCE16,
        GL_LUMINANCE_ALPHA,
        GL_LUMINANCE4_ALPHA4,
        GL_LUMINANCE6_ALPHA2,
        GL_LUMINANCE8_ALPHA8,
        GL_LUMINANCE12_ALPHA4,
        GL_LUMINANCE12_ALPHA12,
        GL_LUMINANCE16_ALPHA16,
        GL_INTENSITY,
        GL_INTENSITY4,
        GL_INTENSITY8,
        GL_INTENSITY12,
        GL_INTENSITY16,
        GL_R3_G3_B2,
        GL_RGB,
        GL_RGB4,
        GL_RGB5,
        GL_RGB8,
        GL_RGB10,
        GL_RGB12,
        GL_RGB16,
        GL_RGBA,
        GL_RGBA2,
        GL_RGBA4,
        GL_RGB5_A1,
        GL_RGBA8,
        GL_RGB10_A2,
        GL_RGBA12,
        GL_RGBA16,
        GL_SLUMINANCE,
        GL_SLUMINANCE8,
        GL_SLUMINANCE_ALPHA,
        GL_SLUMINANCE8_ALPHA8,
        GL_SRGB,
        GL_SRGB8,
        GL_SRGB_ALPHA, or 
        GL_SRGB8_ALPHA8.
     */
	private int internalFormat = 4;

	/** The texture data */
	private int width;
	private int height;
	private int depth;
	
	/** Specifies the width of the border. Must be either 0 or 1. */
	private int border = 0;
	
	private Format format = Format.RGB;
	
	/** The pixel data indexed on (side, level). Side is only by CUBE_MAP */
	private ByteBuffer[][] pixels;
	
	/** True to automaticly generate mipmaps */
	private boolean generateMipMaps = false;
	
	private Wrap wrapS = Wrap.REPEAT;
	private Wrap wrapT = Wrap.REPEAT;
	private Wrap wrapR = Wrap.REPEAT;
	
	private MagFilter magFilter = MagFilter.LINEAR;
	
	private MinFilter minFilter = MinFilter.LINEAR;
	
	/** Texture object's maximum degree of anisotropy */
	private float maxAnisotropy = 1f;

	/** The native peer. Don't touch. */
	transient public TexturePeer nativePeer;
	
    public Texture() {
    }

    public Texture(TextureType type, int internalFormat, int width, int height
            , int depth, Format format, ByteBuffer[][] pixels, boolean generateMipMaps) {
        this.type = type;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.internalFormat = internalFormat;
        this.format = format;
        this.pixels = pixels;
        this.generateMipMaps = generateMipMaps;
    }
	
	/**
	 * Sets the texture data.
	 * @param width the width of the texture
	 * @param height the height of the texture
	 * @param format the format of the data
	 * @param pixels the pixels
	 * @param generateMipMaps true to auto generate mipmaps
	 */
	public void setTextureData(TextureType type, int internalFormat, int width
			, int height, int depth, Format format, ByteBuffer[][] pixels
			, boolean generateMipMaps) {
		this.type = type;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.internalFormat = internalFormat;
		this.format = format;
		this.pixels = pixels;
		this.generateMipMaps = generateMipMaps;
		if (nativePeer != null) {
			nativePeer.textureDataChanged(this, null);
		}
	}
	
	/**
	 * Gets the number of levels for the specified cube side.
	 * @param cubeSide index of the cube side. Must be 0 if not a cube map
	 * @return the number of mipmap levels
	 */
	public int getLevelCount(int cubeSide) {
		return pixels[cubeSide].length;
	}
	
	/**
	 * Gets a reference to the texture pixels. pixelsChanged() must be called
	 * after changing pixels for the changes to take effect. 
	 * @param cubeSide index of the cube side. Must be 0 if not a cube map.
	 * @param level the mipmap level
	 * @return a reference to the texture data
	 */
	public ByteBuffer getPixels(int cubeSide, int level) {
		return pixels[cubeSide][level];
	}

	/**
	 * Gets a reference to the texture pixels. pixelsChanged() must be called
	 * after changing pixels for the changes to take effect. 
	 * @return a reference to the texture data
	 */
	public ByteBuffer[][] getPixels() {
		return pixels;
	}
	
	/**
	 * Notify the renderer that the pixels has changed.
     * @param dirtyRects list of dirty rectangles to update. Everything is
     *        updated if nothing is provided.
	 */
	public void pixelsChanged(Rectangle... dirtyRects) {
		if (nativePeer != null) {
			nativePeer.textureDataChanged(this, dirtyRects);
		}
	}

	/**
	 * Gets the internal format.
	 * @return the internalFormat
	 */
	public int getInternalFormat() {
		return internalFormat;
	}

	/**
	 * Gets the width
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
	 * Gets the depth.
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Gets the border.
	 * @return the border
	 */
	public int getBorder() {
		return border;
	}

	/**
	 * Gets the format of the data stored in the byte buffer(s).
	 * @return the format
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Checks if mipmaps is automaticly generated.
	 * @return the generateMipMaps flag
	 */
	public boolean getGenerateMipMaps() {
		return generateMipMaps;
	}

	/**
	 * Sets the minify filter.
	 * @param minFilter the minFilter
	 */
	public void setMinFilter(MinFilter minFilter) {
		if (this.minFilter != minFilter) {
			this.minFilter = minFilter;
			if (nativePeer != null) {
				nativePeer.textureStateChanged(this);
			}
		}
	}

	/**
	 * Gets the minify filter.
	 * @return the minFilter
	 */
	public MinFilter getMinFilter() {
		return minFilter;
	}

	/**
	 * Sets the magnify filter.
	 * @param magFilter the magFilter
	 */
	public void setMagFilter(MagFilter magFilter) {
		if (this.magFilter != magFilter) {
			this.magFilter = magFilter;
			if (nativePeer != null) {
				nativePeer.textureStateChanged(this);
			}
		}
	}

	/**
	 * Gets the magnify filter.
	 * @return the magFilter
	 */
	public MagFilter getMagFilter() {
		return magFilter;
	}

	/**
	 * Sets maximum degree of anisotropy.
	 * @param maxAnisotropy the maximum degree of anisotropy
	 */
	public void setMaxAnisotropy(float maxAnisotropy) {
		if (this.maxAnisotropy != maxAnisotropy) {
			this.maxAnisotropy = maxAnisotropy;
			if (nativePeer != null) {
				nativePeer.textureStateChanged(this);
			}
		}
	}

	/**
	 * Gets maximum degree of anisotropy.
	 * @return maximum degree of anisotropy
	 */
	public float getMaxAnisotropy() {
		return maxAnisotropy;
	}

	/**
	 * Sets the wrap parameter for texture coordinate s.
	 * @param wrapS the wrap parameter
	 */
	public void setWrapS(Wrap wrapS) {
		if (this.wrapS != wrapS) {
			this.wrapS = wrapS;
			if (nativePeer != null) {
				nativePeer.textureStateChanged(this);
			}
		}
	}

	/**
	 * Gets the wrap parameter for texture coordinate s.
	 * @return the s wrap parameter
	 */
	public Wrap getWrapS() {
		return wrapS;
	}

	/**
	 * Sets the wrap parameter for texture coordinate t.
	 * @param wrapT the wrap parameter
	 */
	public void setWrapT(Wrap wrapT) {
		if (this.wrapT != wrapT) {
			this.wrapT = wrapT;
			if (nativePeer != null) {
				nativePeer.textureStateChanged(this);
			}
		}
	}

	/**
	 * Gets the wrap parameter for texture coordinate t.
	 * @return the t wrap parameter
	 */
	public Wrap getWrapT() {
		return wrapT;
	}

	/**
	 * Sets the wrap parameter for texture coordinate r.
	 * @param wrapR the wrap parameter
	 */
	public void setWrapR(Wrap wrapR) {
		if (this.wrapR != wrapR) {
			this.wrapR = wrapR;
			if (nativePeer != null) {
				nativePeer.textureStateChanged(this);
			}
		}
	}

	/**
	 * Gets the wrap parameter for texture coordinate r.
	 * @return the r wrap parameter
	 */
	public Wrap getWrapR() {
		return wrapR;
	}

	/**
	 * Gets the texture type.
	 * @return the type
	 */
	public TextureType getType() {
		return type;
	}
	/**
	 * Serializable.
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(type);
		out.writeInt(internalFormat);
		out.writeInt(width);
		out.writeInt(height);
		out.writeInt(depth);
		out.writeInt(border);
		out.writeObject(format);
		out.writeBoolean(generateMipMaps);
		out.writeObject(wrapS);
		out.writeObject(wrapT);
		out.writeObject(wrapR);
		out.writeObject(magFilter);
		out.writeObject(minFilter);
		out.writeFloat(maxAnisotropy);
		out.writeBoolean(pixels != null);
		if (pixels != null) {
			out.writeInt(pixels.length);
			for (int i=0; i<pixels.length; i++) {
				out.writeInt(pixels[i].length);
				for (int j=0; j<pixels[i].length; j++) {
					out.writeObject(VertexData.toArray(pixels[i][j].rewind()));
				}
			}
		}
	}
	
	/**
	 * Serializable.
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		stateId = nextStateId++;
		type = (TextureType) in.readObject();
		internalFormat = in.readInt();
		width = in.readInt();
		height = in.readInt();
		depth = in.readInt();
		border = in.readInt();
		format = (Format) in.readObject();
		generateMipMaps = in.readBoolean();
		wrapS = (Wrap) in.readObject();
		wrapT = (Wrap) in.readObject();
		wrapR = (Wrap) in.readObject();
		magFilter = (MagFilter) in.readObject();
		minFilter = (MinFilter) in.readObject();
		maxAnisotropy = in.readFloat();
		boolean hasPixels = in.readBoolean();
		if (hasPixels) {
			pixels = new ByteBuffer[in.readInt()][];
			for (int i=0; i<pixels.length; i++) {
				pixels[i] = new ByteBuffer[in.readInt()];
				for (int j=0; j<pixels.length; j++) {
					byte[] data = (byte[]) in.readObject();
					pixels[i][j] = BufferUtils.createByteBuffer(data.length);
					pixels[i][j].put(data).rewind();
					System.out.println("Read texture of length: " + data.length + " level="+j);
				}				
			}
		}
	}
}
