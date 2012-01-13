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

import java.util.Comparator;

import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;

import trb.jsg.BoundingBox;
import trb.jsg.BoundingSphere;
import trb.jsg.Shader;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.VertexData;
import trb.jsg.peers.*;
import trb.jsg.util.Mat4;

class RetainedShape implements ShapePeer {
	
	/** Identity matrix returned by getModelMatrix if renderType is DISPLAY_LIST_WORLD */
	private static Mat4 IDENTITY_MATRIX = new Mat4();
	
	public static SimpleShapePeerComparator simpleShapePeerComparator = new SimpleShapePeerComparator();

	/** The peers shape */
	public Shape shape;
	
	/** What renderer this shape is currently assigned to */
	public StateSortRenderer renderer;
	
	/** What queue the shape is in */
	public Object renderQueue;

	/** Hash of the texture unit states that can be used in sorting */
	public long textureStateHash = -1;

	/** Info about how often the state has changed */
	public ChangeInfo stateChangeInfo = new ChangeInfo();

	/** Info about how often the matrix has changed */
	public ChangeInfo matrixChangeInfo = new ChangeInfo();

	/** Info about how often the VertexData has changed */
	public ChangeInfo vertexDataChangeInfo = new ChangeInfo();
	
	/** Handle to the vbo chunk */
	public VBOShapeList.Handle vboHandle;
	
	/**
	 * IMMEDIATE - immediate mode
	 * VERTEX_ARRAY - vertex array
	 * DISPLAY_LIST - display list
	 * DISPLAY_LIST_WORLD - display list where coordinates is stored in world space relative to this shape.
	 * SHARED_VBO - vertex data from all shapes in one big vbo 
	 * @author tombr
	 *
	 */
	public enum RenderType {IMMEDIATE, VERTEX_ARRAY, DISPLAY_LIST, DISPLAY_LIST_WORLD, SHARED_VBO};
	
	/** How the vertices are rendered */
	public RenderType renderType = RenderType.VERTEX_ARRAY;

	/** The id of the world coordinates display list */
	private int worldDisplayListId = 0;
	
	/** The center of the bounding sphere in world space */
	public Point3f worldBoundsCenter = new Point3f();
	
	/** The radius of the bounding sphere in world space. This includes scale. */
	public float worldBoundsRadius = 0;
	
	public BoundingBox worldBBox = null;
	

	/**
	 * Constructs a SimpleShapePeer that is the peer of the specified shape.
	 * @param shape the shape
	 */
	public RetainedShape(Shape shape) {
		this.shape = shape;
		updateWorldBoundsCenter();
	}	
	
	/**
	 * Updates the worldBoundsCenter member.
	 */
	public void updateWorldBoundsCenter() {
		BoundingSphere sphere = shape.getVertexData().getBoundingSphere();
		worldBoundsCenter.set(sphere.getCenter());
		shape.getModelMatrix().transform(worldBoundsCenter);
		worldBoundsRadius = sphere.getRadius();
		worldBoundsRadius *= shape.getModelMatrix().getScale();
	}
	
	/**
	 * Calculates the textureStateHash
	 */
//	public long getTextureStateHash() {
//		if (textureStateHash == -1) {
//			textureStateHash = 0;
//			for (int i=0; i<shape.getState().units.length; i++) {
//				int texId = 0;
//				if (shape.getState().units[i] != null) {
//					texId = ((SimpleTexturePeer) shape.getState().units[i].texture.nativePeer).getTextureId();
//				}
//				textureStateHash |= texId << (i*8); 
//			}
//		}
//		
//		return textureStateHash;
//	}
	
	/**
	 * Called when shader program changes.
	 */
	public void shaderProgramChanged() {
		/**
		 * Need to mark shape as dirty so it is compiled next time it is rendered
		 */
		System.out.println(getClass().getSimpleName()+".shaderProgramChanged()");
	}
	
	/**
	 * Called when shader state changes.
	 */
	public void shaderStateChanged() {
		/**
		 * Prepare the shape to be resorted the next time it is rendered
		 */
		System.out.println(getClass().getSimpleName()+".shaderStateChanged()");
	}

	/**
	 * Called when texture data is changed. Adds the texture to the renderers updateList.
	 * @param texturePeer the texture peer that chenged
	 */
	public void textureDataChanged(RetainedTexture texturePeer) {
//		System.out.println(getClass().getSimpleName()+".textureDataChanged()");
		RetainedSceneGraph renderer = ((RetainedRenderPass)shape.parent.nativePeer).sceneGraphPeer;
		renderer.updateList.add((NativeResource) texturePeer);
	}

	/**
	 * Called when texture state is changed. Adds the texture to the renderers updateList.
	 * @param texturePeer the texture peer that chenged
	 */
	public void textureStateChanged(RetainedTexture texturePeer) {
//		System.out.println(getClass().getSimpleName()+".textureStateChanged()");
		RetainedSceneGraph renderer = ((RetainedRenderPass)shape.parent.nativePeer).sceneGraphPeer;
		renderer.updateList.add((NativeResource) texturePeer);
	}
	
	/**
	 * Implements TexturePeer. Called when a shader is changed.
	 * @param oldShader the old shader
	 * @param newShader the new shader
	 */
	public void shaderChanged(Shader oldShader, Shader newShader) {
		RetainedSceneGraph renderPassPeer = ((RetainedRenderPass)shape.parent.nativePeer).sceneGraphPeer;
		if (oldShader != null) {
			RetainedShader simpleShaderPeer = (RetainedShader) oldShader.getShaderProgram().nativePeer;
			simpleShaderPeer.parents.remove(shape);
			if (simpleShaderPeer.parents.isEmpty()) {
				// destroy shader in render
				renderPassPeer.destroyList.add(simpleShaderPeer);
				oldShader.getShaderProgram().nativePeer = null;
			}			
		}
		
		if (newShader != null) {
			if (newShader.getShaderProgram().nativePeer == null) {
				// first time shader is added
				newShader.getShaderProgram().nativePeer = new RetainedShader(newShader);
				
				// compile shader in render
				renderPassPeer.updateList.add((NativeResource) newShader.getShaderProgram().nativePeer);
			}
			RetainedShader simpleShaderPeer = (RetainedShader) newShader.getShaderProgram().nativePeer;
			simpleShaderPeer.parents.add(shape);
		}
		
		stateChanged();
	}
	
	/**
	 * Called when a shader uniform set is changed.
	 * @param oldUniforms the old uniform set
	 * @param newUniforms the new uniform set
	 */
//	public void shaderUniformsChanged(ShaderUniformSet oldUniforms, ShaderUniformSet newUniforms) {
//		stateChanged();
//	}

	/**
	 * Implements TexturePeer. Called when a texture is changed.
	 * @param oldTexture the old texture
	 * @param newTexture the new texture
	 */
	public void textureChanged(Texture oldTexture, Texture newTexture) {
		System.out.println("textureChanged "+oldTexture+" "+newTexture);
		RetainedSceneGraph renderer = ((RetainedRenderPass)shape.parent.nativePeer).sceneGraphPeer;
		if (oldTexture != null && oldTexture.nativePeer != null) {
			RetainedTexture simpleTexturePeer = (RetainedTexture) oldTexture.nativePeer;
			if (!simpleTexturePeer.parents.remove(shape)) {
				System.err.println("SimpleShapePeer.textureChanged FAILED to remove parent!!");
			}
			if (simpleTexturePeer.parents.isEmpty()) {
				// destroy texture in render
				renderer.destroyList.add(simpleTexturePeer);
				oldTexture.nativePeer = null;
			}
		}
		if (newTexture != null) {
			if (newTexture.nativePeer == null) {
				// first time texture is added
				newTexture.nativePeer = new RetainedTexture(newTexture);

				// compile texture in render
				renderer.updateList.add((NativeResource) newTexture.nativePeer);
			}
			RetainedTexture tPeer = (RetainedTexture) newTexture.nativePeer;
			tPeer.parents.add(shape);
		}
		
		stateChanged();
	}
	
	/**
	 * Implements ShapePeer. Called when the matrix is changed.
	 */
	public void matrixChanged() {
		if (renderType == RenderType.DISPLAY_LIST_WORLD) {
			renderType = RenderType.VERTEX_ARRAY;
		}
		ChangeInfo changeInfo = matrixChangeInfo;
		if (changeInfo.lastChange < renderer.renderPassPeer.frameIdx) {
			changeInfo.lastChange = renderer.renderPassPeer.frameIdx;
			changeInfo.changeCnt++;
		}
		updateWorldBoundsCenter();
	}
	
	
	/**
	 * Implements ShapePeer. Called when the vertex data instance in shape is changed.
	 * @param oldVertexData the old VertexData
	 * @param newVertexData the new VertexData
	 */
	public void vertexDataChanged(VertexData oldVertexData, VertexData newVertexData) {
		if (oldVertexData != null) {
			RetainedVertexData peer = (RetainedVertexData) oldVertexData.nativePeer;
			peer.removeListUser(this);
			peer.parents.remove(shape);
			if (peer.parents.isEmpty()) {
				oldVertexData.nativePeer = null;
			}
		}
		
		if (newVertexData != null) {
			if (newVertexData.nativePeer == null) {
				// first time vertex data is added
				newVertexData.nativePeer = new RetainedVertexData(newVertexData);
			}
			RetainedVertexData simpleVertexDataPeer = (RetainedVertexData) newVertexData.nativePeer;
			simpleVertexDataPeer.parents.add(shape);
			renderType = RenderType.VERTEX_ARRAY;
		}
		
		updateWorldBoundsCenter();
	}
	
	/**
	 * Invoked when the content of the shapes vertex data is changed.
	 * @param vertexDataPeer the VertexDataPeer that changed.
	 */
	public void vertexDataChanged(RetainedVertexData vertexDataPeer) {
		switch (renderType) {
		case DISPLAY_LIST:
			vertexDataPeer.removeListUser(this);
			renderType = RenderType.VERTEX_ARRAY;
			break;
		case DISPLAY_LIST_WORLD:
			renderType = RenderType.VERTEX_ARRAY;
			// display list will be deleted in the next draw()
			// TODO: what if draw() never is invoked (shape is deleted)
			break;
		case SHARED_VBO:
			renderType = RenderType.VERTEX_ARRAY;
			break;
		}
		
		ChangeInfo changeInfo = vertexDataChangeInfo;
		if (changeInfo.lastChange < renderer.renderPassPeer.frameIdx) {
			changeInfo.lastChange = renderer.renderPassPeer.frameIdx;
			changeInfo.changeCnt++;
		}
		
		updateWorldBoundsCenter();
	}

	/**
	 * Implements ShapePeer. Called when the any other state is changed.
	 */
	public void stateChanged() {
		if (renderer != null) {
			renderer.shapeStateChanged(this);
		}
	}
	
	/**
	 * Draws the geometry.
	 */
	public void draw() {
		if (shape.getState().getShader() != null) {
			GLState.applyUniforms(shape);
		}
		
		switch (renderType) {
		case IMMEDIATE:
			((RetainedVertexData) shape.getVertexData().nativePeer).drawImmediate(null);
			if (worldDisplayListId > 0) {
				worldBBox = null;
				GL11.glDeleteLists(worldDisplayListId, 1);
				worldDisplayListId = 0;
			}
			break;
		case VERTEX_ARRAY:
			GLState.clientState = ((RetainedVertexData) shape.getVertexData().nativePeer).drawVertexArray(GLState.clientState);
			if (worldDisplayListId > 0) {
				worldBBox = null;
				GL11.glDeleteLists(worldDisplayListId, 1);
				worldDisplayListId = 0;
			}
			break;
		case DISPLAY_LIST:
			((RetainedVertexData) shape.getVertexData().nativePeer).drawList();
			if (worldDisplayListId > 0) {
				worldBBox = null;
				GL11.glDeleteLists(worldDisplayListId, 1);
				worldDisplayListId = 0;
			}
			break;
		case DISPLAY_LIST_WORLD:
			if (worldDisplayListId <= 0) {
				worldDisplayListId = GL11.glGenLists(1);
				GL11.glNewList(worldDisplayListId, GL11.GL_COMPILE);
				RetainedVertexData vertexDataPeer = ((RetainedVertexData) shape.getVertexData().nativePeer); 
				vertexDataPeer.drawImmediate(shape.getModelMatrix());
				worldBBox = vertexDataPeer.calculateBoundingBox(shape.getModelMatrix()); 
				GL11.glEndList();
			}
			// TODO: we can no longer batch lists because shader uniforms can change between shapes
			GL11.glCallList(worldDisplayListId);
			//DisplayListBatch.queueList(worldDisplayListId);
			break;
		case SHARED_VBO:
			if (worldDisplayListId > 0) {
				worldBBox = null;
				GL11.glDeleteLists(worldDisplayListId, 1);
				worldDisplayListId = 0;
			}
			break;
		}
	}
	
	/**
	 * Changes the render type.
	 * @param newRenderType
	 */
	public void changeRenderType(RenderType newRenderType) {
		if (newRenderType != renderType) {
			if (renderType == RenderType.DISPLAY_LIST) {
				RetainedVertexData vertexDataPeer = (RetainedVertexData) shape.getVertexData().nativePeer;
				vertexDataPeer.removeListUser(this);
			}
			if (newRenderType == RenderType.DISPLAY_LIST) {
				RetainedVertexData vertexDataPeer = (RetainedVertexData) shape.getVertexData().nativePeer;
				vertexDataPeer.addListUser(this);
			}
			
			renderType = newRenderType;
		}
	}
	
	/**
	 * Gets the model matrix used to draw. Returns always identity if
	 * renderType is DISPLAY_LIST_WORLD.
	 * @return the transform matrix
	 */
	public Mat4 getModelMatrix() {
		if (renderType == RenderType.DISPLAY_LIST_WORLD) {
			return IDENTITY_MATRIX;
		}
		return shape.getModelMatrix();
	}

	/**
	 * Compares SimpleShapePeers on state.
	 */
	public static class SimpleShapePeerComparator implements Comparator<RetainedShape> {
		public int compare(RetainedShape a, RetainedShape b) {
			return Shape.stateComparator.compare(a.shape.getState(), b.shape.getState());
		}		
	}
}
