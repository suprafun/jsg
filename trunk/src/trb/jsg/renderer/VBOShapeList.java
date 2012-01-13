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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.vecmath.Point3f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.Util;

import trb.jsg.VertexData;
import trb.jsg.util.ObjectArray;

class VBOShapeList {
	
	static final boolean DEBUG = false;

	// a sorted list of chunks
	private LinkedList<Handle> handles = new LinkedList<Handle>();
	
	// the number of elements in the memory
	private int numElements;
	
	// the number of elements used
	private int elementsUsed = 0;
	
	// the vbo id
	private int vboVertexId = 0;
	private long clientState = 1;

	// index buffer used to stream indices
	private IntBuffer indexBuffer = BufferUtils.createIntBuffer(1024*64*3);
	
	// temp buffer used to upload data
	private FloatBuffer uploadBuffer = BufferUtils.createFloatBuffer(1024*3);
	
	// temp variable used to transform a vertex
	private Point3f tempP3f = new Point3f();
	
	
	/**
	 * Initializes the vbo. Uses opengl calls.
	 */
	public VBOShapeList(int initialSize) {
		numElements = initialSize;
		IntBuffer bufferIds = BufferUtils.createIntBuffer(1);
		ARBVertexBufferObject.glGenBuffersARB(bufferIds);
		vboVertexId = bufferIds.get(0);
		resizeArrays();
	}	

	/**
	 * Adds the specified shape to the list. Uses opengl calls an dust be
	 * called from the scenegraph render method.
	 * @param shapePeer the shape peer to add
	 */
	public void addShape(RetainedShape shapePeer) {
		if (shapePeer.vboHandle != null) {
			throw new IllegalArgumentException("vboHandle != null");
		}
		
		int[] minMax = ((RetainedVertexData)shapePeer.shape.getVertexData().nativePeer).getMinMaxIndex();
		int chunkSize = minMax[1] - minMax[0] + 1;
		
		ListIterator<Handle> iter = handles.listIterator();

		// look for empty space in existing array 
		int startIdx = 0;
		while (iter.hasNext()) {
			Handle next = iter.next();
			int available = next.startIdx - startIdx;
			if (available >= chunkSize) {
				System.out.println("found place at offset "+startIdx);
				// move back iterator so the new handle is inserted in sorted order
				if (iter.hasPrevious()) {
					iter.previous();
				}
				break;
			}

			startIdx = next.startIdx + next.count;
		}		

		// check if array need to grow
		int sizeNeeded = startIdx+chunkSize; 
		if (sizeNeeded > numElements) {
			// grow array if we need to use more than 50% of array
			if (elementsUsed > sizeNeeded / 2) {
				numElements = sizeNeeded * 2;
				resizeArrays();
			}
			compact();
			
			// put new handle at end of compacted memory
			startIdx = elementsUsed;
		}
		
		// insert handle at current iterator position so it will be sorted
		Handle handle = new Handle();
		handle.shapePeer = shapePeer;
		handle.startIdx = startIdx;
		handle.count = chunkSize;
		handle.upload();
		shapePeer.vboHandle = handle;
		iter.add(handle);
		
		elementsUsed += chunkSize;
		
		VertexData vertexData = shapePeer.shape.getVertexData();
		if (vertexData.colors != null) {
			clientState |= (1 << 2);
		}
	}
	
	/**
	 * Removes the specified shape from the list. Uses opengl calls an dust be
	 * called from the scenegraph render method.
	 * @param shapePeer the shape to remove
	 */
	public void removeShape(RetainedShape shapePeer) {
		shapePeer.vboHandle.delete();
	}
	
	/**
	 * Begin rendering using vbo.
	 */
	public void begin(GLState currentState) {
		if (DEBUG) {
			System.out.println("begin");
		}
//		currentState.clientState = SimpleVertexDataPeer.applyClientState(currentState.clientState, clientState);
		
		Util.checkGLError();
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboVertexId);

		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
//		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
//		GL11.glColorPointer(3, GL11.GL_FLOAT, 0, numElements*3*4);

		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, numElements*6*4);

//		int offset = (numElements*(6+unit*2))+(startIdx*3);
		
		GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, numElements*8*4);
//		Util.checkGLError();
		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
	}
	
	/**
	 * End rendering using vbo.
	 */
	public void end() {
		if (DEBUG) {
			System.out.println("end");
		}
		flush();
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
		
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

		GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GLState.clientState = 0;
//		Util.checkGLError();
	}
	
	/**
	 * Queues the specified shape for rendering.
	 */
	public void queue(RetainedShape shapePeer) {
		Handle handle = shapePeer.vboHandle;
//		if (indexBuffer.position()+handle.indices.length >= indexBuffer.capacity()) {
//			flush();
//		}
		indexBuffer.put(handle.indices);
	}
	
	/**
	 * Flushes the queued shapes.
	 */
	public void flush() {
		indexBuffer.flip();
		if (DEBUG) {
			System.out.println("flush "+indexBuffer.position()+" "+indexBuffer.limit());
		}
		if (indexBuffer.limit() > 0) {
			GL11.glDrawElements(GL11.GL_TRIANGLES, indexBuffer);
		}
		indexBuffer.limit(indexBuffer.capacity());
	}
	
	/**
	 * Resizes the server side arrays to the size of numElements.
	 */
	private void resizeArrays() {
//		Thread.dumpStack();
		
		// coordinates, colors, 2 texunits
		int size = numElements * (3 + 3 + 2 + 2) * 4;
		System.out.println("resizeArrays "+size+" "+numElements);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboVertexId);
		ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, size, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
		
		uploadBuffer.clear();
		for (int i=0; i<uploadBuffer.capacity(); i++) {
			uploadBuffer.put(i, 1);
		}
		
		// clear the data
		int pos = 0;
		while (pos < size) {
			if (size - pos < uploadBuffer.capacity() * 4) {
				uploadBuffer.limit((size - pos) / 4);
			}
			ARBVertexBufferObject.glBufferSubDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, pos, uploadBuffer);
			Util.checkGLError();
			pos += uploadBuffer.capacity() * 4;
		}
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
	}
	
	/**
	 * Compacts the memory.
	 */
	private void compact() {
		int nextIdx = 0;
		for (Handle handle : handles) {
			handle.startIdx = nextIdx;
			nextIdx += handle.count;
			handle.upload();
		}		
	}
	
	/**
	 * 
	 * @author tombr
	 *
	 */
	public class Handle {
		// set to true when handle has been deleted
		boolean isDeleted = false;
		
		// the index of the first element in the global array
		int startIdx;
		
		// number of elements used by shape
		int count;
		
		// the minimum index in the original index array
		int minIndex;
		
		// a reference to the shape peer
		RetainedShape shapePeer;
		
		int[] indices;
		
		/**
		 * Removes the shape from 
		 */
		public void delete() {
			if (!isDeleted && handles.remove(this)) {
				elementsUsed -= count;
				isDeleted = true;
				if (elementsUsed < numElements / 4) {
					compact();
					numElements = Math.max(10, elementsUsed * 2);
				}
				shapePeer.vboHandle = null;
			}
		}
		
		/**
		 * Uploads the data from the shape to the vbo server memory. Must be
		 * called every time the position moves.
		 */
		public void upload() {
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, vboVertexId);
			FloatBuffer coordinates = shapePeer.shape.getVertexData().coordinates;
			int tempIdx=0;
			for (int i=0; i<count; i++) {
				tempP3f.x = coordinates.get((minIndex+i)*3+0);
				tempP3f.y = coordinates.get((minIndex+i)*3+1);
				tempP3f.z = coordinates.get((minIndex+i)*3+2);
				shapePeer.shape.getModelMatrix().transform(tempP3f);
				uploadBuffer.put(tempIdx*3+0, tempP3f.x);
				uploadBuffer.put(tempIdx*3+1, tempP3f.y);
				uploadBuffer.put(tempIdx*3+2, tempP3f.z);
				if (tempIdx*3 >= uploadBuffer.capacity() || i==count-1) {
					int offset = (startIdx+i-tempIdx)*3;
					uploadBuffer.position(0).limit((tempIdx+1)*3);
//					System.out.println("upload "+offset+" "+uploadBuffer.limit()+" "+startIdx);
					ARBVertexBufferObject.glBufferSubDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, offset*4, uploadBuffer);
					uploadBuffer.position(0).limit(uploadBuffer.capacity());
					tempIdx=0;
				} else {
					tempIdx++;
				}
			}
			
			// upload colors
			FloatBuffer colors = shapePeer.shape.getVertexData().colors;
			if (colors != null) {
				colors.position(minIndex).limit(minIndex+count*3);
				int offset = (numElements*3)+(startIdx*3);
				ARBVertexBufferObject.glBufferSubDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, offset*4, colors);
			} else {
				
			}
			
			// upload texture coordinates
			ObjectArray<VertexData.TexCoordData> texCoords = shapePeer.shape.getVertexData().texCoords;
			if (texCoords != null) {
				for (int unit=0; unit<texCoords.length(); unit++) {
					VertexData.TexCoordData texCoord = texCoords.get(unit);
					if (texCoord != null) {
						GL13.glClientActiveTexture(GL13.GL_TEXTURE0 + unit);
						texCoord.data.position(minIndex*2).limit(minIndex*2+count*2);
//						int offset = (numElements*(6+unit*2))+(startIdx*2);
						int offset = (numElements*(6+unit*2))+startIdx+startIdx;
						ARBVertexBufferObject.glBufferSubDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, offset*4, texCoord.data);
					}
				}
			}
			GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
			
			ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);

			VertexData vertexData = shapePeer.shape.getVertexData();
			IntBuffer indices = vertexData.indices;
			indices.position(0).limit(indices.capacity());
			this.indices = new int[indices.capacity()];
			indices.get(this.indices);
			for (int i=0; i<this.indices.length; i++) {
				this.indices[i] += startIdx-minIndex;
			}
			Util.checkGLError();
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		int windowWidth = 640;
//		int windowHeight = 480;
//		Display.setLocation((Display.getDisplayMode().getWidth() - windowWidth) / 2,
//							(Display.getDisplayMode().getHeight() - windowHeight) / 2);
//		Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
//		Display.setTitle("Transparency sort test");
//		Display.create();
//		
//		Matrix4f cameraMatrix = new Matrix4f();
//		cameraMatrix.set(new Vector3f(0, 0, -50));
//		float h = (float) windowHeight / (float) windowWidth;
//
//		View view = new View();
//		view.setCameraMatrix(cameraMatrix);
//		view.frustum(-1, 1, h, -h, 1, 1000);
//		
//		RenderPass renderPass = new RenderPass();
//		renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
//		renderPass.setClearColor(new Color4f(0, 0, 0.2f, 0));
//		renderPass.setView(view);
//		
//		final boolean STATIC = true;
//		
//		// 3375 boxes
//		float boxSize = 0.1f;
//		int dim = 15;
//        int xCnt = dim;
//        int yCnt = dim;
//        int zCnt = dim;
//        for (int x=0; x<xCnt; x++) {
//            for (int y=0; y<yCnt; y++) {
//                for (int z=0; z<zCnt; z++) {
//                	Shape b = null;
//                	if (STATIC) {
//                    	Vector3f min = new Vector3f(x-xCnt/2f, y-yCnt/2f, z-zCnt/2f);
//                    	Vector3f max = new Vector3f(min.x+boxSize*2, min.y+boxSize*2, min.z+boxSize*2);
//                		b = new Box(min, max);
//                	} else {
//                    	Vector3f min = new Vector3f(-boxSize, -boxSize, -boxSize);
//                    	Vector3f max = new Vector3f( boxSize,  boxSize,  boxSize);
//                		b = new Triangle(min, max);
//                		b.getModelMatrix().set(new Vector3f(x-xCnt/2f, y-yCnt/2f, z-zCnt/2f));
//                	}
//
//            		b.getState().setBlendEnabled(true);
//            		b.getState().setBlendSrcFunc(BlendSrcFunc.ONE);
//            		b.getState().setBlendDstFunc(BlendDstFunc.ONE);
//            		b.getState().setDepthWriteEnabled(false);
//            		Sphere.setColor(b, new Color3f((x+1)/(float)xCnt, (y+1)/(float)yCnt, (z+1)/(float)zCnt));
//            		renderPass.addShape(b);
//                }            	
//            }        	
//        }
//		
//
//		SceneGraph sceneGraph = new SceneGraph();
//		sceneGraph.insertRenderPass(renderPass, 0);
//
//		RetainedSceneGraph renderer = new RetainedSceneGraph(sceneGraph);
//
//		long startTime = System.currentTimeMillis() + 5000;
//		long fps = 0;
//		
//		while (!Display.isCloseRequested()) {
//			while (Keyboard.next()) {
//				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
//					return;
//				}
//			}
//
//			Point3d cameraPos = new Point3d();
//			cameraPos.x = Math.sin(RetainedSceneGraph.frameIdx * 0.001) * 20;
//			cameraPos.z = Math.cos(RetainedSceneGraph.frameIdx * 0.001) * 20;
//			
//			Transform3D lookAtT3D = new Transform3D();
//			lookAtT3D.lookAt(cameraPos, new Point3d(), new Vector3d(0, 1, 0));
//			lookAtT3D.get(cameraMatrix);
//			
//			renderer.render();
//
//			Display.update();
//			if (startTime > System.currentTimeMillis()) {
//				fps++;
//			} else {
//				long timeUsed = 5000 + (startTime - System.currentTimeMillis());
//				startTime = System.currentTimeMillis() + 5000;
//				System.out.println(fps + " frames in " + (float) (timeUsed / 1000f) + " seconds = "
//						+ (fps / (timeUsed / 1000f)));
//				fps = 0;
//			}
//		}
//		
//		Display.destroy();
//	}
//	
//	public static class Triangle extends Shape {
//		public Triangle(Vector3f min, Vector3f max) {
//			
//			Vector3f verts[] = new Vector3f[3];
//			verts[0] = new Vector3f(min.x, min.y, min.z);
//			verts[1] = new Vector3f(max.x, min.y, min.z);
//			verts[2] = new Vector3f(max.x, max.y, min.z);
//	
//			float[] coords = new float[] {
//					min.x, min.y, min.z
//					, max.x, min.y, min.z
//					, max.x, max.y, min.z
//			};
//			int[] indices = {0, 1, 2};
//			
//			setVertexData(new VertexData());
//			getVertexData().setCoordinates(coords, null, null, 0, null, indices);
//	
//			getState().setCullEnabled(false);
//		}
//	}	
}
