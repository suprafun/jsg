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

package trb.jsg.util;

import java.nio.ByteBuffer;
import java.util.List;
import javax.vecmath.Tuple3f;

import org.lwjgl.BufferUtils;
import trb.jsg.Texture;
import trb.jsg.enums.Format;
import trb.jsg.enums.MagFilter;
import trb.jsg.enums.MinFilter;
import trb.jsg.enums.TextureType;

public class SGUtil {
	
	/**
	 * Converts a String name to a 0 terminated ByteBuffer
	 * @param name the name
	 * @return the ByteBuffer
	 */
	public static ByteBuffer nameToByteBuffer(String name) {
		ByteBuffer buf = BufferUtils.createByteBuffer(name.length()+1);
		buf.put(name.getBytes()).rewind();
		return buf;
	}

	
	/**
	 * Rounds to the nearest power of two.
	 * @param val the value to round
	 * @return the rounded value
	 */
	public static int roundPow2(int val) {
		if (val == 1) {
			return 1;
		}
		int pow2Size = 1;
		while (pow2Size <= (val - (pow2Size / 2))) {
			pow2Size *= 2;
		}
		
		return pow2Size;
	}

    public static Texture createTexture(int internalFormat, int w, int h) {
        ByteBuffer[][] pixels = {{BufferUtils.createByteBuffer(w * h * 4)}};
        Texture baseTexture = new Texture(TextureType.TEXTURE_2D, internalFormat, w, h, 0, Format.RGBA, pixels, false, false);
        baseTexture.setMagFilter(MagFilter.NEAREST);
        baseTexture.setMinFilter(MinFilter.NEAREST);
        return baseTexture;
    }

    public static float[] toFloats(List<? extends Tuple3f> list) {
        float[] floats = new float[list.size()*3];
        for (int i=0; i<list.size(); i++) {
            Tuple3f t = list.get(i);
            floats[i*3+0] = t.x;
            floats[i*3+1] = t.y;
            floats[i*3+2] = t.z;
        }
        return floats;
    }

    public static int[] createIndices(int vertexCount) {
        int[] indices = new int[vertexCount];
        for (int i=0; i<indices.length; i++) {
            indices[i] = i;
        }
        return indices;
    }

    public static int[] createTriangleStripIndices(int vertexCount) {
        int[] newIndices = new int[(vertexCount - 2) * 3];
        int dstIdx = 0;
        for (int idx = 2; idx < vertexCount; idx++) {
            if ((idx & 1) == 1) {
                newIndices[dstIdx++] = idx;
                newIndices[dstIdx++] = idx - 1;
                newIndices[dstIdx++] = idx - 2;
            } else {
                newIndices[dstIdx++] = idx;
                newIndices[dstIdx++] = idx - 2;
                newIndices[dstIdx++] = idx - 1;
            }
        }
        return newIndices;
    }

    public static int[] convertIndicesFromTriangleStripToTriangles(int[] indices) {
        int[] newIndices = new int[(indices.length - 2) * 3];
        int dstIdx = 0;
        for (int idx = 2; idx < indices.length; idx++) {
            if ((idx & 1) == 1) {
                newIndices[dstIdx++] = idx;
                newIndices[dstIdx++] = idx - 1;
                newIndices[dstIdx++] = idx - 2;
            } else {
                newIndices[dstIdx++] = idx;
                newIndices[dstIdx++] = idx - 2;
                newIndices[dstIdx++] = idx - 1;
            }
        }
        return newIndices;
    }
}
