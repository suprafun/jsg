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

package trb.jsg;

import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import org.lwjgl.BufferUtils;
//import org.lwjgl.util.vector.Matrix2f;

import trb.jsg.util.SGUtil;

/**
 * Uniform shader value that holds one or more elements of a specified type of 
 * Uniform. Example of passing two vec2 with values (0, 1) and (2, 3):<br>
 * new Uniform("name", Uniform.Type.VEC2, 0, 1, 2, 3); 
 * 
 * @author tombr
 *
 */
public class Uniform implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** Shaders that owns this uniform. Will be notified when uniform change. */
	ArrayList<Shader> owners = new ArrayList<Shader>();

	/** The uniform types */
	public enum Type {FLOAT, VEC2, VEC3, VEC4, INT, IVEC2, IVEC3, IVEC4, MAT2
		, MAT3, MAT4, MAT2x3, MAT3x2, MAT2x4, MAT4x2, MAT3x4, MAT4x3}
	
	/** The name of the uniform */
	private String name;

	/** The name as a 0 terminated string in a ByteBuffer */
	private ByteBuffer nameBuffer;
	
	/** This uniforms type */
	private Type type = Type.FLOAT;

	/** The data. FloatBuffer if type is FLOAT or MATRIX, otherwise IntBuffer */
	private Buffer data;
	
	/** Wether or not to transpose the matrix  */
	private boolean transpose = false;

	/**
	 * Constructs an Uniform from a list of floats. 
	 * @param name the uniform name
	 * @param type the uniform type. Must match data type.
	 * @param floats the data
	 */
	public Uniform(String name, Type type, float... floats) {
		this.name = name;
		this.nameBuffer = SGUtil.nameToByteBuffer(name);
		this.type = type;
		FloatBuffer floatData = BufferUtils.createFloatBuffer(floats.length);
		floatData.put(floats).rewind();
		this.data = floatData;
	}

	/**
	 * Constructs an Uniform from a list of floats. 
	 * @param name the uniform name
	 * @param type the uniform type. Must match data type.
	 * @param floats the data
	 */
	public Uniform(String name, Type type, boolean transpose, float... floats) {
		this.name = name;
		this.nameBuffer = SGUtil.nameToByteBuffer(name);
		this.type = type;
		this.transpose = transpose;
		FloatBuffer floatData = BufferUtils.createFloatBuffer(floats.length);
		floatData.put(floats).rewind();
		this.data = floatData;
	}
	
	/**
	 * Constructs an Uniform from a list of ints. 
	 * @param name the uniform name
	 * @param type the uniform type. Must match data type.
	 * @param ints the data
	 */
	public Uniform(String name, Type type, int... ints) {
		this.nameBuffer = SGUtil.nameToByteBuffer(name);
		this.type = type;
		IntBuffer intData = BufferUtils.createIntBuffer(ints.length);
		intData.put(ints).rewind();
		this.data = intData;
	}
	
	/**
	 * Constructs an Uniform from a list of matrices. 
	 * @param name the uniform name
	 * @param transpose Wheter to transpose the matrix values. True indicates 
	 *        that the matrix values are specified in row major order, false is
	 *        column major order
	 * @param matrix the data
	 */
//	public Uniform(String name, boolean transpose, Matrix2f... matrix) {
//		this.name = name;
//		this.nameBuffer = SGUtil.nameToByteBuffer(name);
//		this.type = Type.MAT2;
//		this.transpose = transpose;
//		FloatBuffer floatData = BufferUtils.createFloatBuffer(matrix.length*4);
//		for (Matrix2f m : matrix) { 
//			floatData.put(m.m00).put(m.m01).put(m.m10).put(m.m11).rewind();
//		}
//	}
	
	/**
	 * Constructs an Uniform from a list of matrices. 
	 * @param name the uniform name
	 * @param transpose Wheter to transpose the matrix values. True indicates 
	 *        that the matrix values are specified in row major order, false is
	 *        column major order
	 * @param matrix the data
	 */
	public Uniform(String name, boolean transpose, Matrix3f... matrix) {
		this.name = name;
		this.nameBuffer = SGUtil.nameToByteBuffer(name);
		this.type = Type.MAT3;
		this.transpose = transpose;
		FloatBuffer floatData = BufferUtils.createFloatBuffer(matrix.length*9);
		for (Matrix3f m : matrix) { 
			floatData.put(m.m00).put(m.m01).put(m.m02);
			floatData.put(m.m10).put(m.m11).put(m.m12);
			floatData.put(m.m20).put(m.m21).put(m.m22);
			floatData.rewind();
		}
	}
	
	/**
	 * Constructs an Uniform from a list of matrices. 
	 * @param name the uniform name
	 * @param transpose Wheter to transpose the matrix values. True indicates 
	 *        that the matrix values are specified in row major order, false is
	 *        column major order
	 * @param matrix the data
	 */
	public Uniform(String name, boolean transpose, Matrix4f... matrix) {
		this.name = name;
		this.nameBuffer = SGUtil.nameToByteBuffer(name);
		this.type = Type.MAT4;
		this.transpose = transpose;
		FloatBuffer floatData = BufferUtils.createFloatBuffer(matrix.length*16);
		for (Matrix4f m : matrix) { 
			floatData.put(m.m00).put(m.m01).put(m.m02).put(m.m03);
			floatData.put(m.m10).put(m.m11).put(m.m12).put(m.m13);
			floatData.put(m.m20).put(m.m21).put(m.m22).put(m.m23);
			floatData.put(m.m30).put(m.m31).put(m.m32).put(m.m33);
			floatData.rewind();
		}
	}
	
	/**
	 * Gets the name as a String.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets name as a 0 terminated ByteBuffer that can be sent to OpengGL.
	 * @return the name
	 */
	public ByteBuffer getNameBuffer() {
		return nameBuffer;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Buffer data) {
		this.data = data;
		notifyOwners();
	}

	/**
	 * @return the data
	 */
	public Buffer getData() {
		return data;
	}

    public void setFloats(float... floats) {
        FloatBuffer floatData = (FloatBuffer) data;
        floatData.put(floats).rewind();
        notifyOwners();
    }

	/**
	 * Sets the transpose flag.
	 * @param transpose the transpose to set
	 */
	public void setTranspose(boolean transpose) {
		if (this.transpose != transpose) {
			this.transpose = transpose;
			notifyOwners();
		}
	}

	/**
	 * Checks wether or not to transpose the matrix.
	 * @return the transpose flag
	 */
	public boolean getTranspose() {
		return transpose;
	}

	/**
	 * Calls uniformedChanged(this) on all the owners.
	 */
	private void notifyOwners() {
		for (int i=0; i<owners.size(); i++) {
			owners.get(i).uniformChanged(this);
		}
	}
}
