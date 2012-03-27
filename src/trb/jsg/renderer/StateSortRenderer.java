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

import java.util.Arrays;
import java.util.Comparator;

import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;

import trb.jsg.BoundingBox;
import trb.jsg.Shape;
import trb.jsg.View;
import trb.jsg.util.GLUtils;
import trb.jsg.util.Mat4;
import trb.jsg.util.RadixSort;

/**
 * Renderer that renders with state sorting.
 * 
 * @author tombr
 *
 */
class StateSortRenderer {
	
	/** Used to sort int arrays */
	private RadixSort radixSort = new RadixSort();
	
	/** Contains information needed to render the shapes (like the view) */
	public RetainedRenderPass renderPassPeer;
	
	/** Shapes to render back to front */
	private ShapeList backToFrontList = new ShapeList();
	
	/** Shapes to render front to back */
	private ShapeList frontToBackList = new ShapeList();
	
	/** Shapes to render in any order */
	private ShapeList dynamicStateList = new ShapeList();
	
	/** Shapes with static state */
	private ShapeList staticStateList = new ShapeList();
	
	/** Shapes with static state, vertex data and transform */
	private ShapeList staticVboList = new ShapeList();
	
	/** VBO helper class. Represents one large continues vbo array containing vertex data of all shapes */
	private VBOShapeList vboList;
	
	/** True if staticStateList needs to be resorted */
	private boolean staticStateDirty = false;
	
	/** True if staticVboList needs to be resorted */
	private boolean staticVboDirty = false;
	
	/** Temp list used for sorting */
	private RetainedShape[] sortList = new RetainedShape[100];

	/** Number of shapes in the sortList */
	private int sortListLength = 0;
	
	/** The distance to the camera for all the shapes in the sortLIst. Used By
	 * renderBackToFront
	 */
	private int[] radixSortInts = new int[100];
	
	/** Temp variable. The current model to view matrix. */
	private Mat4 modelViewMatrix = new Mat4();
	
	
	public StateSortRenderer(RetainedRenderPass renderPassPeer) {
		this.renderPassPeer = renderPassPeer;
	}
	
	/**
	 * Adds a shape to be rendered using this renderer.
	 * @param shapePeer the Shape
	 */
	public void add(RetainedShape shapePeer) {
		switch (shapePeer.shape.getSortOrder()) {
		case BACK_TO_FRONT:
			backToFrontList.add(shapePeer);
			shapePeer.renderQueue = backToFrontList;
			break;
		case FRONT_TO_BACK:
			frontToBackList.add(shapePeer);
			shapePeer.renderQueue = frontToBackList;
			break;
		default:
			staticStateList.add(shapePeer);
			shapePeer.renderQueue = staticStateList;
			staticStateDirty = true;
			shapePeer.shape.getState().stateChanged();
	//		dynamicStateList.add(shapePeer);
	//		shapePeer.renderQueue = dynamicStateList;
	//		shapePeer.shape.updateHash();
			break;
		}
	}
	
	/**
	 * Removes a shape to be rendered using this renderer.
	 * @param shapePeer the Shape
	 */
	public void remove(RetainedShape shapePeer) {
		ShapeList shapeList = (ShapeList) shapePeer.renderQueue;
		shapeList.remove(shapePeer);
		shapePeer.renderQueue = null;
	}
	
	/**
	 * Renders the shapes using the specified current state
	 * @param currentMatrix the current model matrix state
	 * @param currentTexture the current texture state
	 */
	public void render() {
		if (vboList == null) {
			//vboList = new VBOShapeList(1024*42);
		}

		// init transform
		View view = renderPassPeer.renderPass.getView();
		GLState.modelWorldMatrix = new Mat4();
		
		// lights must be transformed by the view transform
        GLUtils.loadMatrix(view.getCameraMatrix());
		GLState.applyLights(renderPassPeer.renderPass.getLightState());
		
		modelViewMatrix.set(view.getCameraMatrix());
		GLUtils.loadMatrix(modelViewMatrix);		
		
		optimizeLists();
		renderDepthSorted(frontToBackList, false);
		renderDynamicStateList();
		renderStaticStateList();
		//renderStaticVBO(currentState);
		renderDepthSorted(backToFrontList, true);
//		renderBBox();
	}
	
	/**
	 * Analysis the shapes and possible change their list.
	 */
	private void optimizeLists() {
		final int DELAY = 200;

		// don't do this every frame its is expensive
		if ((renderPassPeer.frameIdx % 50) == 0) {
			
			int frameIdx = renderPassPeer.frameIdx;

			if (false) {
				// upgrade dynamic shapes that has not changed to the static vbo list
				for (int i=staticStateList.elementCnt-1; i>=0; i--) {
					RetainedShape peer = staticStateList.list[i];
					int framesSinceStateChange = frameIdx - peer.stateChangeInfo.lastChange;
					int framesSinceVertexChange = frameIdx - peer.vertexDataChangeInfo.lastChange;
					int framesSinceMatrixChange = frameIdx - peer.matrixChangeInfo.lastChange;
					if (framesSinceStateChange > (peer.stateChangeInfo.changeCnt * DELAY)
							&& framesSinceVertexChange > (peer.vertexDataChangeInfo.changeCnt * DELAY)
							&& framesSinceMatrixChange > (peer.matrixChangeInfo.changeCnt * DELAY)
							) {
						// chance to be upgraded decrease with the number of changes
//						System.out.println("Upgrade to vbo "+i);
						staticStateList.removeFast(i);
						staticVboList.add(peer);
						vboList.addShape(peer);
						peer.renderQueue = staticVboList;
						staticVboDirty = true;
						staticStateDirty = true;
						peer.shape.getState().stateChanged();
					}
				}
			} else {
				// upgrade dynamic shapes that has not changed to the static list
				for (int i=dynamicStateList.elementCnt-1; i>=0; i--) {
					RetainedShape peer = dynamicStateList.list[i];
					ChangeInfo info = peer.stateChangeInfo;
					int framesSinceChange = frameIdx - info.lastChange;
					if (framesSinceChange > (info.changeCnt * 50)) {
						// chance to be upgraded decrease with the number of changes
						dynamicStateList.removeFast(i);
						staticStateList.add(peer);
						peer.renderQueue = staticStateList;
						staticStateDirty = true;
						peer.shape.getState().stateChanged();
					}
				}
				
				// upgrade to display list if geometry is static
				for (int shapeIdx=0; shapeIdx<renderPassPeer.renderPass.getShapeCount(); shapeIdx++) {
					Shape shape = renderPassPeer.renderPass.getShape(shapeIdx);
					RetainedShape peer = (RetainedShape) shape.nativePeer;
					if (peer.renderType != RetainedShape.RenderType.DISPLAY_LIST_WORLD) {
						int framesSinceVertexChange = frameIdx - peer.vertexDataChangeInfo.lastChange;
						int framesSinceMatrixChange = frameIdx - peer.matrixChangeInfo.lastChange;
						// 134 / 100
						if (framesSinceVertexChange > (peer.vertexDataChangeInfo.changeCnt * DELAY)
								&& framesSinceMatrixChange > (peer.matrixChangeInfo.changeCnt * DELAY)) {
							peer.changeRenderType(RetainedShape.RenderType.DISPLAY_LIST_WORLD);
							shape.getState().stateChanged();
							//System.out.println("display list in world space");
						} else {
							if (peer.renderType != RetainedShape.RenderType.DISPLAY_LIST
									&& framesSinceVertexChange > (peer.vertexDataChangeInfo.changeCnt * DELAY)) {
								peer.changeRenderType(RetainedShape.RenderType.DISPLAY_LIST);
								//System.out.println("display list");
							}
						}
					}
				}
			}
		}		
	}

	/**
	 * Render sorted on state.
	 * 145
	 * display list buffering: 160 fps
	 * J3D: 121
	 * J3D No sorting: 380fps
	 */
	public void renderDepthSorted(ShapeList shapeList, boolean backToFront) {
		// init sort list
		if (sortList.length < shapeList.elementCnt) {
			sortList = new RetainedShape[shapeList.elementCnt];
			radixSortInts = new int[shapeList.elementCnt];
		}
		sortListLength = 0;
		
		// populate sort list with the visible shapes
		View view = renderPassPeer.renderPass.getView();
		RenderInfo.shapesIterated += shapeList.elementCnt;
		RenderInfo.depthSortedShapes += shapeList.elementCnt;
		for (int shapeIdx=0; shapeIdx<shapeList.elementCnt; shapeIdx++) {
			RetainedShape peer = shapeList.list[shapeIdx];
			Shape shape = peer.shape;
			if (!shape.isVisible()) {
				continue;
			}
			RenderInfo.visibleShapes++;

			if (!view.isInsideFrustum(peer.worldBoundsCenter, peer.worldBoundsRadius)) {
				continue;
			}
			RenderInfo.shapesInFrustum++;
			
			Point3f boundsCenterInViewSpace = peer.worldBoundsCenter;

			sortList[sortListLength] = peer;
			
			float distance = boundsCenterInViewSpace.x * boundsCenterInViewSpace.x 
					+ boundsCenterInViewSpace.y * boundsCenterInViewSpace.y
					+ boundsCenterInViewSpace.z * boundsCenterInViewSpace.z;
			
			radixSortInts[sortListLength] = Float.floatToIntBits(distance);
			sortListLength++;
		}
		
		// sort the visible shapes
		radixSort.resetIndices(sortListLength); // Will get IndexOutOfBoundsException unless we do this. bug???
		radixSort.sort(radixSortInts, sortListLength);
		int[] sortedIndices = radixSort.getIndices();
		
		// no sort: 167
		// radix sort: 128
		// radix sort with reset indices: 122
		// radix sort with reset indices and unique transforms: 91
		
		// iterate sorted shapes
		for (int shapeIdx=0; shapeIdx<sortListLength; shapeIdx++) {
			RetainedShape peer = null;
			if (backToFront) {
				peer = sortList[sortedIndices[shapeIdx]];
			} else {
				peer = sortList[sortedIndices[sortListLength-1-shapeIdx]];
			}
			Shape shape = peer.shape;

			// apply shape transform it has changed
			boolean matrixChanged = false;
			Mat4 modelMatrix = peer.getModelMatrix();
			if (!GLState.modelWorldMatrix.equals(modelMatrix)) {
				GLState.modelWorldMatrix = modelMatrix;
				modelViewMatrix.set(view.getCameraMatrix());
				modelViewMatrix.mul(new Mat4(modelMatrix));
				GLUtils.loadMatrix(modelViewMatrix);
				matrixChanged = true;
			}

			if (!GLState.isEqual(shape)) {
				DisplayListBatch.flush();
				GLState.applyDif(shape);
			} else if (matrixChanged) {
				DisplayListBatch.flush();
			}

			peer.draw();
//			((SimpleVertexDataPeer) shape.vertexData.nativePeer).drawList();
		}
		
		DisplayListBatch.flush();
	}

	/**
	 * Render sorted on state.
	 */
	public void renderDynamicStateList() {
		ShapeList shapeList = dynamicStateList;
		
		// init sort list
		if (sortList.length < shapeList.elementCnt) {
			sortList = new RetainedShape[shapeList.elementCnt];
			radixSortInts = new int[shapeList.elementCnt];
		}
		sortListLength = 0;
		
		// populate sort list with the visible shapes
		View view = renderPassPeer.renderPass.getView();
		RenderInfo.shapesIterated += shapeList.elementCnt;
		RenderInfo.dynamicStateShapes += shapeList.elementCnt;
		for (int shapeIdx=0; shapeIdx<shapeList.elementCnt; shapeIdx++) {
			RetainedShape peer = shapeList.list[shapeIdx];
			Shape shape = peer.shape;
			if (!shape.isVisible()) {
				continue;
			}
			RenderInfo.visibleShapes++;

			// frustum culling
			if (!view.isInsideFrustum(peer.worldBoundsCenter, peer.worldBoundsRadius)) {
				continue;
			}
			RenderInfo.shapesInFrustum++;

			sortList[sortListLength] = peer;
			
			radixSortInts[sortListLength] = shape.getState().getHash();
			sortListLength++;
		}
		
		// sort the visible shapes
		radixSort.resetIndices(sortListLength); // Will get IndexOutOfBoundsException unless we do this. bug???
		radixSort.sort(radixSortInts, sortListLength);
		int[] sortedIndices = radixSort.getIndices();
		
		// iterate sorted shapes
		for (int shapeIdx=0; shapeIdx<sortListLength; shapeIdx++) {
			RetainedShape peer = sortList[sortedIndices[shapeIdx]];
			Shape shape = peer.shape;

			// apply shape transform it has changed
			boolean matrixChanged = false;
			Mat4 modelMatrix = peer.getModelMatrix();
			if (!GLState.modelWorldMatrix.equals(modelMatrix)) {
				GLState.modelWorldMatrix = modelMatrix;
				modelViewMatrix.set(view.getCameraMatrix());
				modelViewMatrix.mul(new Mat4(modelMatrix));
				GLUtils.loadMatrix(modelViewMatrix);
				matrixChanged = true;
			}

			if (!GLState.isEqual(shape)) {
				DisplayListBatch.flush();
				GLState.applyDif(shape);
			} else if (matrixChanged) {
				DisplayListBatch.flush();
			}

			peer.draw();
			// 155 fps (data is in buffers)
//			((SimpleVertexDataPeer) shape.vertexData.nativePeer).drawImmediate();
			// 264 fps
//			clientState = ((SimpleVertexDataPeer) shape.vertexData.nativePeer).drawVertexArray(clientState);
			// 280 (after bounds change)
			// 300 (clearing updateList and destroyList)
//			((SimpleVertexDataPeer) shape.vertexData.nativePeer).drawList();
			// 304 no display list batching because of shader uniforms (19.09.2008)

			// 310 static vbo with color
			// 315 static vbo without color
			// 315 world display lists
			
			// 800 (don't draw)
			// 115 (Java3D, same with or without compile)
			// 227 (JME with lockBranch())
			// 164 (JME without lockBranch())
		}

		DisplayListBatch.flush();
	}

	/**
	 * Render sorted on state.
	 */
	public void renderStaticStateList() {
		ShapeList shapeList = staticStateList;
		
		// Sort list if it is dirty. This is expensive and should hopefully not be done every frame.
		if (staticStateDirty) {
			staticStateDirty = false;
			
			Comparator<RetainedShape> comparator = RetainedShape.simpleShapePeerComparator;
			//System.out.println("sort "+shapeList.elementCnt);
			Arrays.sort(shapeList.list, 0, shapeList.elementCnt, comparator);
		}

		View view = renderPassPeer.renderPass.getView();

		// init transform
		modelViewMatrix.set(view.getCameraMatrix());
		GLUtils.loadMatrix(modelViewMatrix);
		
		// iterate sorted shapes
		RenderInfo.shapesIterated += shapeList.elementCnt;
		RenderInfo.staticStateShapes += shapeList.elementCnt;
		for (int shapeIdx=0; shapeIdx<shapeList.elementCnt; shapeIdx++) {
			RetainedShape peer = shapeList.list[shapeIdx];
			Shape shape = peer.shape;
			if (!shape.isVisible()) {
				continue;
			}
			RenderInfo.visibleShapes++;

			if (!view.isInsideFrustum(peer.worldBoundsCenter, peer.worldBoundsRadius)) {
				continue;
			}
			RenderInfo.shapesInFrustum++;
//			if (peer.worldBBox != null && !view.isInsideFrustum(peer.worldBBox)) {
//				continue;
//			}
			RenderInfo.shapesInFrustum2++;

			// apply shape transform it has changed
			boolean matrixChanged = false;
			Mat4 modelMatrix = peer.getModelMatrix();
			if (!GLState.modelWorldMatrix.equals(modelMatrix)) {
				GLState.modelWorldMatrix = modelMatrix;
				modelViewMatrix.set(view.getCameraMatrix());
				modelViewMatrix.mul(new Mat4(modelMatrix));
				GLUtils.loadMatrix(modelViewMatrix);
				matrixChanged = true;
			}

			if (!GLState.isEqual(shape)) {
				DisplayListBatch.flush();
				GLState.applyDif(shape);
			} else if (matrixChanged) {
				DisplayListBatch.flush();
			}

			peer.draw();
		}

		DisplayListBatch.flush();
	}

	/**
	 * Render static vbo.
	 */
	public void renderStaticVBO() {
		ShapeList shapeList = staticVboList;
		
		// Sort list if it is dirty. This is expensive and should hopefully not be done every frame.
		if (staticVboDirty) {
			staticVboDirty = false;
			
			Comparator<RetainedShape> comparator = RetainedShape.simpleShapePeerComparator;
			Arrays.sort(shapeList.list, 0, shapeList.elementCnt, comparator);
			//System.out.println("sort vbo list "+shapeList.elementCnt);
		}

		View view = renderPassPeer.renderPass.getView();

		// init transform
		modelViewMatrix.set(view.getCameraMatrix());
		GLUtils.loadMatrix(modelViewMatrix);
		
//		vboList.begin(currentState);
		
		// iterate sorted shapes
		RenderInfo.shapesIterated += shapeList.elementCnt;
		RenderInfo.vboShapes += shapeList.elementCnt;
		for (int shapeIdx=0; shapeIdx<shapeList.elementCnt; shapeIdx++) {
			RetainedShape peer = shapeList.list[shapeIdx];
			Shape shape = peer.shape;
			if (!shape.isVisible()) {
				continue;
			}
			RenderInfo.visibleShapes++;

			if (!view.isInsideFrustum(peer.worldBoundsCenter, peer.worldBoundsRadius)) {
				continue;
			}
			RenderInfo.shapesInFrustum++;

			// apply shape transform it has changed
			boolean matrixChanged = false;
			Mat4 modelMatrix = peer.getModelMatrix();
			if (!GLState.modelWorldMatrix.equals(modelMatrix)) {
				GLState.modelWorldMatrix = modelMatrix;
				modelViewMatrix.set(view.getCameraMatrix());
				modelViewMatrix.mul(new Mat4(modelMatrix));
				GLUtils.loadMatrix(modelViewMatrix);
				matrixChanged = true;
			}

			if (!GLState.isEqual(shape)) {
				vboList.flush();
				GLState.applyDif(shape);
			} else if (matrixChanged) {
				vboList.flush();
			}

			vboList.queue(peer);
		}

		vboList.flush();
		vboList.end();
	}
	
	/** Invoked by the SimpleShapePeer when the state changes */
	public void shapeStateChanged(RetainedShape peer) {
		ChangeInfo changeInfo = peer.stateChangeInfo;
		if (changeInfo.lastChange < renderPassPeer.frameIdx) {
			changeInfo.lastChange = renderPassPeer.frameIdx;
			changeInfo.changeCnt++;

			if (peer.renderQueue == staticStateList) {
				if (changeInfo.changeCnt > 5) {
					staticStateList.remove(peer);
					dynamicStateList.add(peer);
					peer.renderQueue = dynamicStateList;
				} else {
					// give it another chance in the statis state list
					staticStateDirty = true;
				}
			}
		}
	}

	/**
	 * 
	 */
	public void renderBBox() {
		View view = renderPassPeer.renderPass.getView();
		modelViewMatrix.set(view.getCameraMatrix());
		GLUtils.loadMatrix(modelViewMatrix);
		
		Point3f lower = new Point3f();
		Point3f upper = new Point3f();
		Point3f l = new Point3f(lower);
		Point3f u = new Point3f(upper);

		// iterate sorted shapes
		GL11.glBegin(GL11.GL_LINES);
		GL11.glColor3f(1, 1, 1);
		ShapeList shapeList = staticStateList;
		for (int shapeIdx=0; shapeIdx<shapeList.elementCnt; shapeIdx++) {
			RetainedShape peer = shapeList.list[shapeIdx];
			if (peer.worldBBox != null) {
				BoundingBox bbox = peer.worldBBox;
				bbox.getLower(lower);
				bbox.getUpper(upper);
				l.set(lower);
				u.set(upper);
				GL11.glVertex3f(l.x, l.y, l.z); GL11.glVertex3f(u.x, l.y, l.z);
				GL11.glVertex3f(u.x, l.y, l.z); GL11.glVertex3f(u.x, u.y, l.z);
				GL11.glVertex3f(u.x, u.y, l.z); GL11.glVertex3f(l.x, u.y, l.z);
				GL11.glVertex3f(l.x, u.y, l.z); GL11.glVertex3f(l.x, l.y, l.z);

				GL11.glVertex3f(l.x, l.y, u.z); GL11.glVertex3f(u.x, l.y, u.z);
				GL11.glVertex3f(u.x, l.y, u.z); GL11.glVertex3f(u.x, u.y, u.z);
				GL11.glVertex3f(u.x, u.y, u.z); GL11.glVertex3f(l.x, u.y, u.z);
				GL11.glVertex3f(l.x, u.y, u.z); GL11.glVertex3f(l.x, l.y, u.z);

				GL11.glVertex3f(l.x, l.y, l.z); GL11.glVertex3f(l.x, l.y, u.z);
				GL11.glVertex3f(u.x, l.y, l.z); GL11.glVertex3f(u.x, l.y, u.z);
				GL11.glVertex3f(u.x, u.y, l.z); GL11.glVertex3f(u.x, u.y, u.z);
				GL11.glVertex3f(l.x, u.y, l.z); GL11.glVertex3f(l.x, u.y, u.z);
			}			
		}		
		GL11.glEnd();
	}

	/**
	 * A list of shapes.
	 */
	class ShapeList {
		RetainedShape[] list = new RetainedShape[100];
		int elementCnt = 0;
		
		/**
		 * Adds the shape.
		 * @param shape the shape
		 */
		void add(RetainedShape shape) {
			if (elementCnt >= list.length) {
				RetainedShape[] newList = new RetainedShape[list.length*2];
				System.arraycopy(list, 0, newList, 0, list.length);
				list = newList;
			}
			
			list[elementCnt++] = shape;
		}
		
		/**
		 * Removes the shape.
		 * @param shape the shape
		 */
		void remove(RetainedShape shape) {
			for (int i=0; i<elementCnt; i++) {
				if (list[i] == shape) {
					System.arraycopy(list, i+1, list, i, elementCnt - i - 1);
					elementCnt--;
					list[elementCnt] = null;
					break;
				}
			}
		}
		
		/**
		 * Removes the shape at the specified index by copying the last element
		 * to its position.
		 * @param index
		 */
		void removeFast(int index) {
			if (index < elementCnt-1) {
				list[index] = list[elementCnt-1];
				list[elementCnt-1] = null;
			}
			
			elementCnt--;
//			System.out.println("removeFast "+elementCnt);
		}
	}
}
