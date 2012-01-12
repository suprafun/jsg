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

import trb.jsg.peers.ShaderPeer;

/**
 * A GLSL shader program. It consist of a vertex and fragment shader. Both are
 * optional. 
 * <p>
 * The attribute names maps attribute names to attribute data in VertexData. 
 * The name of the VertexData attributes is given by this array, using the same
 * index into each array. Matrices takes up one index for each column. Null 
 * values has to be used to pad the name array. Example on how to Pass one mat2
 * and a vec4:<br>
 * String[] attributeNames = new String[] {a_mat2, null, a_vec4};<br>
 * The second null is mandetory. The java vm may crash if it is not provided.
 *  
 * @author tombr
 *
 */
public class ShaderProgram implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** Counter that increased every time a Shader is created */
	private static int nextStateId = 1;
	
	/** Unique id that is not the opengl id */
	private int stateId = nextStateId++;
	
	/** The attribute names */
	private String[] attributeNames = new String[0];

	/** vertex shader text */
	private CharSequence vertexShader;

	/** fragment shader text */
	private CharSequence fragmentShader;

	/** The native peer managed by the renderer. Don't touch. */
	transient public ShaderPeer nativePeer;
	
	/**
	 * Constructs a ShaderProgram with the specified attribute names and shaders. 
	 * @param vertexShader the vertex program. Can be null.
	 * @param fragmentShader the fragment program. Can be null.
	 * @param attributeNames list of attribute names that maps the names to the
	 *                       attributes in the vertex data. 
	 */
	public ShaderProgram(CharSequence vertexShader, CharSequence fragmentShader, String... attributeNames) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		this.attributeNames = attributeNames;
	}

	/**
	 * Gets the attribute names.
	 * @return the attributeNames
	 */
	public String[] getAttributeNames() {
		return attributeNames;
	}

	/**
	 * Gets the vertex shader text.
	 * @return the vertexShader
	 */
	public CharSequence getVertexShader() {
		return vertexShader;
	}

	/**
	 * Gets the fragment shader text.
	 * @return the fragmentShader
	 */
	public CharSequence getFragmentShader() {
		return fragmentShader;
	}

	/**
	 * Gets an unique id for this object. This is not the opengl program id.
	 * @return the stateId
	 */
	public int getStateId() {
		return stateId;
	}
}
