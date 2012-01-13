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

package trb.jsg.peers;

import trb.jsg.Shader;
import trb.jsg.Texture;
import trb.jsg.VertexData;

public interface ShapePeer {
	
	/**
	 * Called when a shader instance is changed.
	 * @param oldShader the old shader
	 * @param newShader the new shader
	 */
	public void shaderChanged(Shader oldShader, Shader newShader);
	
	/**
	 * Called when a shader uniform set is changed.
	 * @param oldUniforms the old uniform set
	 * @param newUniforms the new uniform set
	 */
//	public void shaderUniformsChanged(ShaderUniformSet oldUniforms, ShaderUniformSet newUniforms);

	/**
	 * Called when a texture instance is changed.
	 * @param oldTexture the old texture
	 * @param newTexture the new texture
	 */
	public void textureChanged(Texture oldTexture, Texture newTexture);
	
	/**
	 * Called when the matrix is changed.
	 */
	public void matrixChanged();
	
	/**
	 * Called when the vertex data instance is changed.
	 * @param oldVertexData the old VertexData
	 * @param newVertexData the new VertexData
	 */
	public void vertexDataChanged(VertexData oldVertexData, VertexData newVertexData);

	/**
	 * Called when the any other state is changed.
	 */
	public void stateChanged();
}
