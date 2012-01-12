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

package trb.jsg.util;

import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB;
import static org.lwjgl.opengl.ARBShaderObjects.glGetInfoLogARB;
import static org.lwjgl.opengl.ARBShaderObjects.glGetObjectParameterARB;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.vecmath.Matrix4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;

public class GLUtils {
	
	/** Temp var */ 
	private static FloatBuffer matrixBuf = BufferUtils.createFloatBuffer(16);
	private static IntBuffer getIntBuf = BufferUtils.createIntBuffer(16);
	private static FloatBuffer getFloatBuf = BufferUtils.createFloatBuffer(16);
	
	/**
	 * Loads a Matrix4f into opengl.
	 * @param m4f the matrix to load
	 */
	public static void loadMatrix(Matrix4f m4f) {
		matrixBuf.rewind();
		matrixBuf.put(m4f.m00).put(m4f.m10).put(m4f.m20).put(m4f.m30);
		matrixBuf.put(m4f.m01).put(m4f.m11).put(m4f.m21).put(m4f.m31);
		matrixBuf.put(m4f.m02).put(m4f.m12).put(m4f.m22).put(m4f.m32);
		matrixBuf.put(m4f.m03).put(m4f.m13).put(m4f.m23).put(m4f.m33);
		matrixBuf.rewind();
		GL11.glLoadMatrix(matrixBuf);
	}
	
	/**
	 * Loads a Matrix4f into opengl.
	 * @param m4f the matrix to load
	 */
	public static void loadMatrix2(Matrix4f m4f) {
		GL11.glTranslated(0, 0, 0.0001f);
		GL11.glRotatef(0.000001f, 1, 0, 0);
		GL11.glRotatef(0.000001f, 0, 1, 0);
		GL11.glRotatef(0.000001f, 0, 0, 1);
		GL11.glScaled(1, 1, 1);
	}

	/**
	 * Same as glGetInteger.
	 */
	public static int getInteger(int id) {
		getIntBuf.rewind();
		getIntBuf.clear();
		GL11.glGetInteger(id, getIntBuf);
		getIntBuf.rewind();
		return getIntBuf.get(0);
	}

	/**
	 * Same as glGetInteger.
	 */
	public static int getTexEnv(int cap) {
		getIntBuf.rewind();
		getIntBuf.clear();
		GL11.glGetTexEnv(GL11.GL_TEXTURE_ENV, cap, getIntBuf);
		getIntBuf.rewind();
		return getIntBuf.get(0);
	}

	/**
	 * Same as glGetInteger.
	 */
	public static float getFloat(int id) {
		getFloatBuf.rewind();
		getFloatBuf.clear();
		GL11.glGetFloat(id, getFloatBuf);
		getFloatBuf.rewind();
		return getFloatBuf.get(0);
	}
	
	public static int getTexGen(int coord) {
		getIntBuf.rewind();
		getIntBuf.clear();
		GL11.glGetTexGen(coord, GL11.GL_TEXTURE_GEN_MODE, getIntBuf);
		getIntBuf.rewind();
		return getIntBuf.get(0);
	}
	
	/**
	 * Checks if a shader has compiled.
	 * @param objectId the vertex or fragment shader id
	 * @return true if compile is ok
	 */
	public static boolean isCompileOk(int objectId) {
		glGetObjectParameterARB(objectId, GL_OBJECT_COMPILE_STATUS_ARB, getIntBuf);
		return getIntBuf.get(0) == 1;
	}
	
	/**
	 * Checks if a program has linked.
	 * @param programId the program id
	 * @return true if program linked successfully
	 */
	public static boolean isLinkOk(int objectId) {
		glGetObjectParameterARB(objectId, GL_OBJECT_LINK_STATUS_ARB, getIntBuf);
		return getIntBuf.get(0) == 1;
	}
	
	public static void printLogInfo(int obj) {
		glGetObjectParameterARB(obj, GL_OBJECT_INFO_LOG_LENGTH_ARB, getIntBuf);
 
		int length = getIntBuf.get(0);
		System.out.println("Info log length:"+length);
		if (length > 0) {
			// We have some info we need to output.
			ByteBuffer infoLog = BufferUtils.createByteBuffer(length);
			glGetInfoLogARB(obj, getIntBuf, infoLog);
			byte[] infoBytes = new byte[length];
			infoLog.get(infoBytes);
			String out = new String(infoBytes);
 
			System.out.println("Info log:\n"+out);
		}
 
		Util.checkGLError();
	}	
}
