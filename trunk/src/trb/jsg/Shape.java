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
import java.util.Comparator;

import javax.vecmath.Matrix4f;

import trb.jsg.enums.SortOrder;
import trb.jsg.peers.ShapePeer;


/**
 * The Shape contains all the information needed to render some geometry. It
 * contains a local to world matrix that defines it transform. The vertex data
 * contains the geometry. The state contains the opengl state to use with this
 * shape.
 * <p>
 * The visible flag can be used to hide the shape without removing it from the
 * graph. The sort order can be used to order the shapes in a render pass back
 * to front, front to back or in any order. Back to front is useful for 
 * transparent objects. Front to back can be used in an initial pass to fill 
 * the depth buffer so expensive pixel operations can be rejected later. Any 
 * order is the default and will be order on state to increase performance.
 *  
 * @author tombr
 *
 */
public class Shape implements Serializable {

	private static final long serialVersionUID = 0L;
	
	// A ShapeComparator object.
	public static StateComparator stateComparator = new StateComparator();

	/** Shape is not rendered if false */
	private boolean visible = true;
	
	/** In what order to render this shape */
	private SortOrder sortOrder = SortOrder.ANY;
	
	/** The shapes local to world matrix */
	private Matrix4f modelMatrix = new Matrix4f();
	
	/** The vertex data */
	private VertexData vertexData;
	
	/** The opengl state to use with this shape */
	protected State state = new State();

	/** The RenderPass the shape is attached to */ 
	public RenderPass parent;
	
	/** The native peer */
	transient public ShapePeer nativePeer;

	/**
	 * Constructs a new Shape.
	 */
	public Shape() {
        this(null);
	}

    public Shape(VertexData vertexData) {
        modelMatrix.setIdentity();
        state.owners.add(this);
        setVertexData(vertexData);
    }
	
	/**
	 * Sets the vertex data.
	 * @param newVertexData the new VertexData
	 */
	public void setVertexData(VertexData newVertexData) {
		VertexData oldVertexData = vertexData;
		vertexData = newVertexData;
		if (nativePeer != null) {
			nativePeer.vertexDataChanged(oldVertexData, vertexData);
		}
	}

	/**
	 * Gets the vertex data.
	 * @return the vertexData
	 */
	public VertexData getVertexData() {
		return vertexData;
	}

	/**
	 * Sets the state.
	 * @param state the state
	 */
	public void setState(State state) {
		if (this.state != state) {
			State oldState = this.state;
			this.state = state;
			
			oldState.owners.remove(this);
			state.owners.add(this);
			
			if (nativePeer != null) {
				if (oldState != null) {
					nativePeer.shaderChanged(oldState.getShader(), null);
					int[] activeUnits = oldState.getActiveUnits();
					for (int i=0; i<activeUnits.length; i++) {
						Unit unit = oldState.getUnit(activeUnits[i]);
						if (unit.getTexture() != null) {
							nativePeer.textureChanged(unit.getTexture(), null);
						}
					}
				}
				if (state != null) {
					nativePeer.shaderChanged(null, state.getShader());
					int[] activeUnits = state.getActiveUnits();
					for (int i=0; i<activeUnits.length; i++) {
						Unit unit = state.getUnit(activeUnits[i]);
						if (unit.getTexture() != null) {
							nativePeer.textureChanged(null, unit.getTexture());
						}
					}
				}
				nativePeer.stateChanged();
			}
		}
	}
	
	/**
	 * Gets the state.
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Sets the shapes visible flag. Shape is not rendered if false.
	 * @param visible true to show, false, to hide
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Checks if shape is visible
	 * @return true if visible, otherwise false
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the shapes local to world matrix.
	 * @param modelMatrix the local to world matrix
	 */
	public void setModelMatrix(Matrix4f modelMatrix) {
		this.modelMatrix = modelMatrix;
		if (nativePeer != null) {
			nativePeer.matrixChanged();
		}
	}

	/**
	 * Gets the shapes local to world matrix.
	 * @return the local to world matrix
	 */
	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}

	/**
	 * Sets how to sort this shape.
	 * @param sortOrder the sort order
	 */
	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * Gets the sort order.
	 * @return the sortOrder
	 */
	public SortOrder getSortOrder() {
		return sortOrder;
	}

	/**
	 * Compares shape states
	 */
	public static class StateComparator implements Comparator<State> {
		
		public int compareCnt = 0;
		
		public int compare(State a, State b) {
			compareCnt++;
			if (false) {
				return a.getHash() - b.getHash();
			}
			int aShaderId = a.getShader() != null && a.getShader().getShaderProgram() != null ? a.getShader().getShaderProgram().getStateId() : 0;
			int bShaderId = b.getShader() != null && b.getShader().getShaderProgram() != null ? b.getShader().getShaderProgram().getStateId() : 0;
			if (aShaderId != bShaderId) {
				return aShaderId - bShaderId;
			}
			
			int[] activeUnitsA = a.getActiveUnits();
			int[] activeUnitsB = b.getActiveUnits();
			int activeUnitsLengtDif = activeUnitsA.length - activeUnitsB.length;
			if (activeUnitsLengtDif != 0) {
				return activeUnitsLengtDif;
			}
			for (int i=0; i<activeUnitsA.length; i++) {
				if (activeUnitsA[i] != activeUnitsB[i]) {
					return activeUnitsA[i] - activeUnitsB[i];
				}
				
				int dif = Unit.unitComparator.compare(a.getUnit(activeUnitsA[i]), b.getUnit(activeUnitsA[i]));
				if (dif != 0) {
					return dif;
				}
			}
			
			// blend state
			if (a.isBlendEnabled() ^ b.isBlendEnabled()) {
				return a.isCullEnabled() ? -1 : 1;
			}
			
			if (a.isBlendEnabled()) {
				if (a.getBlendSrcFunc() != b.getBlendSrcFunc()) {
					return a.getBlendSrcFunc().get() - b.getBlendSrcFunc().get();
				}
				if (a.getBlendDstFunc() != b.getBlendDstFunc()) {
					return a.getBlendDstFunc().get() - b.getBlendDstFunc().get();
				}
			}
			
			// cull state
			if (a.isCullEnabled() ^ b.isCullEnabled()) {
				return a.isCullEnabled() ? -1 : 1;
			}
			
			if (a.isCullEnabled()) {
				if (a.getCullFace() != b.getCullFace()) {
					return a.getCullFace().get() - b.getCullFace().get();
				}
				
				if (a.getFrontFace() != b.getFrontFace()) {
					return a.getFrontFace().get() - b.getFrontFace().get();
				}
			}
			
			if (a.isDepthTestEnabled() ^ b.isDepthTestEnabled()) {
				return a.isDepthTestEnabled() ? -1 : 1;
			}
			
			if (a.getDepthFunc() != b.getDepthFunc()) {
				return a.getDepthFunc().get() - b.getDepthFunc().get();
			}
			
			if (a.isDepthWriteEnabled() ^ b.isDepthWriteEnabled()) {
				return a.isDepthWriteEnabled() ? -1 : 1;
			}
			
			return 0;
		}
	}
}
