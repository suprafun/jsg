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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.Util;

import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.Uniform;
import trb.jsg.peers.*;
import trb.jsg.util.GLUtils;
import trb.jsg.util.SGUtil;

/**
 * The SimpleRenderer shaders native peer.
 * 
 * When state changes:
 * Shader notifies SimpleShaderPeer of change
 * SimpleShaderPeer notifes its parent shapes peer
 * if shape is in static bucket, remove it and put it dynamic bucket
 * if shape is in dynamic bucket, remove shape from list for resorting
 * 
 * 
 * @author tombr
 *
 */
class RetainedShader implements ShaderPeer, NativeResource {

	/** List of shapes that references this Shader */
	public ArrayList<Shape> parents = new ArrayList<Shape>();
	
	/** This peers shader */
	public Shader shader;
	
	/** Set to true when shader progam changed and cleared when program is compiled */
	public boolean isShaderProgramDirty = true;
	
	/** The vertex shader id */
	private int vertexId = 0; 

	/** The fragment shader id */
	private int fragmentId = 0;
	
	/** The shader program id */
	public int programId = 0;
	
	/**
	 * TODO: to lazy set uniforms we need the to keep track of the current 
	 * ShaderUniformSet here.
	 */
	public Shader currentUniformSet = null;
	public int currentUniformSetFrameIdx = -10;
	
	
	/** List of uniforms that has changed */
//	public ArrayList<Uniform> dirtyUniforms = new ArrayList<Uniform>();

	/**
	 * Constructs a SimpleShaderPeer that backs the specified shader.
	 * @param shader the shader to back
	 */
	public RetainedShader(Shader shader) {
		this.shader = shader;
	}

	/**
	 * Implements ShaderPeer.
	 */
//	public void programChanged() {
//		isShaderProgramDirty = true;
//		for (Shape parent : parents) {
//			SimpleShapePeer shapePeer = (SimpleShapePeer) parent.nativePeer;
//			shapePeer.shaderProgramChanged();
//		}
//
//		SimpleSceneGraphPeer renderer = ((SimpleRenderPassPeer) parents.get(0).parent.nativePeer).sceneGraphPeer;
//		renderer.updateList.add(this);
//	}

	/**
	 * Implements ShaderPeer.
	 */
//	public void uniformChanged(Uniform uniform) {
//		if (!dirtyUniforms.contains(uniform)) {
//			dirtyUniforms.add(uniform);
//		}
//
//		SimpleSceneGraphPeer renderer = ((SimpleRenderPassPeer) parents.get(0).parent.nativePeer).sceneGraphPeer;
//		renderer.updateList.add(this);
//	}

	/**
	 * Implements SimpleNativeResource. Deletes the vertex, fragment and program.
	 */
	public void destroyNativeResource() {
		Util.checkGLError(); 
		if (programId != 0) {
			System.err.println("Delete program "+programId);
			GL20.glDeleteProgram(programId);
			programId = 0;
		}
		
		if (fragmentId != 0) {
			GL20.glDeleteShader(fragmentId);
			fragmentId = 0;
		}

		if (vertexId != 0) {
			GL20.glDeleteShader(vertexId);
			vertexId = 0;
		}
		Util.checkGLError(); 
	}

	/**
	 * Implements SimpleNativeResource. Recompiles the shader.
	 */
	public void updateNativeResource() {
		ShaderProgram program = shader.getShaderProgram();
		if (isShaderProgramDirty) {
			Util.checkGLError(); 
			isShaderProgramDirty = false;
			if (program.getVertexShader() != null) {
				if (vertexId == 0) {
					vertexId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
				}
				GL20.glShaderSource(vertexId, program.getVertexShader());
				GL20.glCompileShader(vertexId);
				if (!GLUtils.isCompileOk(vertexId)) {
					System.out.println("Failed to compile vertex shader:");
					System.out.println(program.getVertexShader());
					GLUtils.printLogInfo(vertexId);
				}
			}

			if (program.getFragmentShader() != null) {
				if (fragmentId == 0) {
					fragmentId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
				}
				GL20.glShaderSource(fragmentId, program.getFragmentShader());
				GL20.glCompileShader(fragmentId);
				if (!GLUtils.isCompileOk(fragmentId)) {
					System.out.println("Failed to compile fragment shader:");
					System.out.println(program.getFragmentShader());
					GLUtils.printLogInfo(fragmentId);
				}
			}

			if (vertexId != 0 || fragmentId != 0) {
				if (programId == 0) {
					programId = GL20.glCreateProgram();
				}
				
				for (int nameIdx=0; nameIdx<program.getAttributeNames().length; nameIdx++) {
					ByteBuffer name = SGUtil.nameToByteBuffer(program.getAttributeNames()[nameIdx]);
					GL20.glBindAttribLocation(programId, nameIdx+1, name);
				}

				if (vertexId > 0) {
					GL20.glAttachShader(programId, vertexId);
				}
				if (fragmentId > 0) {
					GL20.glAttachShader(programId, fragmentId);
				}
				GL20.glLinkProgram(programId);
				if (!GLUtils.isLinkOk(programId)) {
					System.out.println("Failed to link program:");
					GLUtils.printLogInfo(programId);
				}
				GL20.glUseProgram(programId);
				
//				for (Uniform uniform : shader.getAllUniforms()) {
//					setUniform(uniform);
//				}
//				dirtyUniforms.clear();
			}
			Util.checkGLError(); 
		}
		
//		if (dirtyUniforms.size() > 0) {
//			GL20.glUseProgram(programId);
//			for (Uniform uniform : dirtyUniforms) {
//				setUniform(uniform);
//			}
//		}
	}

	/**
	 * Sets the specified uniform
	 * @param uniform the uniform to set
	 */
	protected void setUniform(Uniform uniform) {
		Util.checkGLError(); 
		int loc = GL20.glGetUniformLocation(programId, uniform.getName());
//		System.out.println("setUniform "+uniform.getName()+" "+loc);
		if (loc == -1) {
			System.err.println(getClass().getSimpleName()+" shader do not contain uniform named: "+uniform.getName());
			return;
		}
		switch (uniform.getType()) {
		case FLOAT:
			GL20.glUniform1(loc, (FloatBuffer) uniform.getData());
			break;
		case VEC2:
			GL20.glUniform2(loc, (FloatBuffer) uniform.getData());
			break;
		case VEC3:
			GL20.glUniform3(loc, (FloatBuffer) uniform.getData());
			break;
		case VEC4:
			GL20.glUniform4(loc, (FloatBuffer) uniform.getData());
			break;
		case INT:
			GL20.glUniform1(loc, (IntBuffer) uniform.getData());
			break;
		case IVEC2:
			GL20.glUniform2(loc, (IntBuffer) uniform.getData());
			break;
		case IVEC3:
			GL20.glUniform3(loc, (IntBuffer) uniform.getData());
			break;
		case IVEC4:
			GL20.glUniform4(loc, (IntBuffer) uniform.getData());
			break;
		case MAT2:
			GL20.glUniformMatrix2(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT3:
			GL20.glUniformMatrix3(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT4:
			GL20.glUniformMatrix4(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT2x3:
			GL21.glUniformMatrix2x3(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT3x2:
			GL21.glUniformMatrix3x2(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT2x4:
			GL21.glUniformMatrix2x4(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT4x2:
			GL21.glUniformMatrix4x2(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT3x4:
			GL21.glUniformMatrix3x4(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		case MAT4x3:
			GL21.glUniformMatrix4x3(loc, uniform.getTranspose(), (FloatBuffer) uniform.getData());
			break;
		default:
			System.err.println("Failed to set transform "+uniform.getName());
			break;
		}
		Util.checkGLError(); 
	}
}
