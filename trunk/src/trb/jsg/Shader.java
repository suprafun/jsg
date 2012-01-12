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
import java.util.HashMap;

import trb.jsg.renderer.Renderer;

/**
 * A GLSL shader that consist of a ShaderProgram and a set of uniform. 
 * ShaderPrograms can be shared between shaders.
 * 
 * @author tombr
 *
 */
public class Shader implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** The shader program */
	private ShaderProgram shaderProgram;
	
	/** The uniform variables*/
	HashMap<String, Uniform> uniformSet = new HashMap<String, Uniform>();

	/** When the uniforms was last changed */
	public int changeFrameIdx = -1;
	
	/**
	 * Constructs a Shader with the specified shader program.
	 * @param shaderProgram the shader program to use
	 */
	public Shader(ShaderProgram shaderProgram) {
		this.shaderProgram = shaderProgram;
	}
	
	/**
	 * Gets the shader program.
	 * @return the shader program
	 */
	public ShaderProgram getShaderProgram() {
		return shaderProgram;
	}
	
	/**
	 * Adds the specified uniform the the set of uniforms. The newly specified
	 * uniform replaces an uniform with the same name, if one already exists in
	 * the set.
	 * param uniform the unform to be added to the set
	 */
	public void putUniform(Uniform uniform) {
        uniform.owners.add(this);
		uniformSet.put(uniform.getName(), uniform);
		changeFrameIdx = Renderer.frameIdx;
	}
	
	/**
	 * Removes the uniform with the specified name from the set. If name does 
	 * not exist in the set then nothing happens. 
	 * @param name the name of the uniform to be removed 
	 */
	public void removeUniform(String name) {
		Uniform uniform = uniformSet.remove(name);
        if (uniform != null) {
            uniform.owners.remove(this);
        }
	}
	
	/**
	 * Retrieves the uniform with the specified name from the set. If name does
	 * not exist in the set, null is returned. 
	 * @param name the name of the uniform to be retrieved 
	 * @return the uniform associated with the specified name, or null if the 
	 *         name is not in the set 
	 */
	public Uniform getUniform(String name) {
		return uniformSet.get(name);
	}
	
	/**
	 * Returns a shallow copy of the uniform set. 
	 * @return a shallow copy of the uniform set
	 */
	public Uniform[] getAllUniforms() {
		return uniformSet.values().toArray(new Uniform[uniformSet.size()]);
	}

    void uniformChanged(Uniform uniform) {
        changeFrameIdx = Renderer.frameIdx;
    }
}
