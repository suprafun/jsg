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
import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import trb.jsg.*;
import trb.jsg.peers.*;
import trb.jsg.util.Mat4;
import trb.jsg.util.ObjectArray;

/**
 * Simple renderer VertexData peer.
 * 
 * @author tombr
 *
 */
class RetainedVertexData implements VertexDataPeer, NativeResource {

	/** List of shapes that references this Shader */
	public ArrayList<Shape> parents = new ArrayList<Shape>();

	/** List of shape peers that uses this vertex data as display lists */
	private ArrayList<RetainedShape> listUsers = new ArrayList<RetainedShape>();
	
	/** The VertexData */
	public VertexData vertexData;
	
	/** The display list id */
	public int listId = -1;
	
	/** True from data is changed until display list is compiled */
	private boolean isListDirty = true;
	
	/** The vertex array client state */
	private long clientState = 0;

	/** Info about how often the transform has changed */
	public ChangeInfo geometryChangeInfo = new ChangeInfo();
	
	/** The client states for each of the flags  */
	private static final int[] CLIENT_STATES = new int[]{
		GL11.GL_VERTEX_ARRAY
		, GL11.GL_NORMAL_ARRAY
		, GL11.GL_COLOR_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
		, GL11.GL_TEXTURE_COORD_ARRAY
	};

	// temp var
	Point3f tempCoord = new Point3f();
	
	/**
	 * 
	 * @param texture
	 */
	public RetainedVertexData(VertexData vertexData) {
		this.vertexData = vertexData;
	}
	
	/**
	 * Adds a shape to list of display list users. Will add itself to the
	 * SimpleRenderPassPeer update list if display list is not generated.
	 * @param shapePeer the shape to add
	 */
	public void addListUser(RetainedShape shapePeer) {
		listUsers.add(shapePeer);
		if (listId <= 0) {
			RetainedSceneGraph renderPassPeer = ((RetainedRenderPass)shapePeer.shape.parent.nativePeer).sceneGraphPeer;
			renderPassPeer.updateList.add(this);
		}		
	}
	
	/**
	 * Removes the shape from the display list users. Will add itself to the
	 * SimpleRenderPassPeer destroy list if there is no users left.
	 * @param shapePeer the shape to remove
	 */
	public void removeListUser(RetainedShape shapePeer) {
		listUsers.remove(shapePeer);
		if (listUsers.size() == 0) {
			RetainedSceneGraph renderPassPeer = ((RetainedRenderPass)shapePeer.shape.parent.nativePeer).sceneGraphPeer;
			renderPassPeer.destroyList.add(this);
		}		
	}

	/**
	 * Implements VertexDataPeer
	 */
	public void vertexDataChanged() {
		isListDirty = true;
		geometryChangeInfo.changeCnt++;
		
		for (int shapeIdx=0; shapeIdx<parents.size(); shapeIdx++) {
			Shape shape = parents.get(shapeIdx);
			((RetainedShape) shape.nativePeer).vertexDataChanged(this);
		}
	}

	/**
	 * Implements SimpleNativeResource.
	 */
	public void destroyNativeResource() {
		if (listId > 0) {
			GL11.glDeleteLists(listId, 1);
			listId = 0;
		}
	}

	/**
	 * Implements SimpleNativeResource.
	 */
	public void updateNativeResource() {
		if (isListDirty && listUsers.size() > 0) {
			isListDirty = false;
			if (listId > 0) {
				GL11.glDeleteLists(listId, 1);
				listId = 0;
			}
	
			listId = GL11.glGenLists(1);
			GL11.glNewList(listId, GL11.GL_COMPILE);
			drawImmediate(null);
			GL11.glEndList();
		}
	}
	
	/**
	 * Draws the display list.
	 */
	public void drawList() {
		if (listId > 0) {
			GL11.glCallList(listId);
		}
	}

	/**
	 * Draws the shape using immediate mode calls.
	 */
	public void drawImmediate(Mat4 localToWorldMatrix) {
		FloatBuffer coords = vertexData.coordinates;
		FloatBuffer colors = vertexData.colors;
		FloatBuffer normals = vertexData.normals;
		ObjectArray<VertexData.TexCoordData> texCoords = vertexData.texCoords;
		ObjectArray<VertexData.AttributeData> attributes = vertexData.attributes;
		IntBuffer indices = vertexData.indices;
		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glColor3f(1, 1, 1);
		for (int index=0; index<indices.limit(); index++) {
			int vertexIndex = indices.get(index);
			int coordOff = vertexIndex * 3;
			if (colors != null) {
				GL11.glColor3f(colors.get(coordOff+0), colors.get(coordOff+1), colors.get(coordOff+2));
			}
			if (normals != null) {
				GL11.glNormal3f(normals.get(coordOff+0), normals.get(coordOff+1), normals.get(coordOff+2));
			}
			for (int unit=0; unit<texCoords.length(); unit++) {
				VertexData.TexCoordData texCoord = texCoords.get(unit);
				if (texCoord != null) {
					FloatBuffer data = texCoord.data;
					int texCoordOff = vertexIndex * texCoord.size;
					switch (texCoord.size) {
					case 1:
						GL13.glMultiTexCoord1f(GL13.GL_TEXTURE0 + unit, data.get(texCoordOff+0));
						break;
					case 2:
						GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0 + unit, data.get(texCoordOff+0), data.get(texCoordOff+1));
						break;
					case 3:
						GL13.glMultiTexCoord3f(GL13.GL_TEXTURE0 + unit, data.get(texCoordOff+0), data.get(texCoordOff+1), data.get(texCoordOff+2));
						break;
					case 4:
						GL13.glMultiTexCoord4f(GL13.GL_TEXTURE0 + unit, data.get(texCoordOff+0), data.get(texCoordOff+1), data.get(texCoordOff+2), data.get(texCoordOff+3));
						break;
					}
				}
			}
			for (int attribIdx=0; attribIdx<attributes.length(); attribIdx++) {
				VertexData.AttributeData attribute = attributes.get(attribIdx);
				if (attribute != null) {
					FloatBuffer floats = attribute.data;
					switch (attribute.size) {
					case 1:
						GL20.glVertexAttrib1f(attribIdx+1, floats.get(vertexIndex));
						break;
					case 2:
						GL20.glVertexAttrib2f(attribIdx+1, floats.get(vertexIndex*2), floats.get(vertexIndex*2+1));
						break;
					case 3:
						GL20.glVertexAttrib3f(attribIdx+1, floats.get(vertexIndex*3), floats.get(vertexIndex*3+1), floats.get(vertexIndex*3+2));
						break;
					case 4:
						GL20.glVertexAttrib4f(attribIdx+1, floats.get(vertexIndex*4), floats.get(vertexIndex*4+1), floats.get(vertexIndex*4+2), floats.get(vertexIndex*4+3));
						break;
					}
				}
			}
			if (localToWorldMatrix != null) {
				tempCoord.set(coords.get(coordOff+0), coords.get(coordOff+1), coords.get(coordOff+2));
				localToWorldMatrix.transform(tempCoord);
				GL11.glVertex3f(tempCoord.x, tempCoord.y, tempCoord.z);				
			} else {
				GL11.glVertex3f(coords.get(coordOff+0), coords.get(coordOff+1), coords.get(coordOff+2));
			}
		}
		GL11.glEnd();
	}
	
	/**
	 * Calculates a bounding box in world space. The bounding box is calculated
	 * using the world space coordinates of this VertexData.
	 * @param localToWorldMatrix the local to world matrix
	 * @return the bounding box in world space
	 */
	public BoundingBox calculateBoundingBox(Mat4 localToWorldMatrix) {
		Point3f lower = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Point3f upper = new Point3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		FloatBuffer coords = vertexData.coordinates;
		IntBuffer indices = vertexData.indices;
		for (int index=0; index<indices.limit(); index++) {
			int vertexIndex = indices.get(index);
			int coordOff = vertexIndex * 3;
			tempCoord.set(coords.get(coordOff+0), coords.get(coordOff+1), coords.get(coordOff+2));
			localToWorldMatrix.transform(tempCoord);
			lower.x = Math.min(lower.x, tempCoord.x);
			lower.y = Math.min(lower.y, tempCoord.y);
			lower.z = Math.min(lower.z, tempCoord.z);
			upper.x = Math.max(upper.x, tempCoord.x);
			upper.y = Math.max(upper.y, tempCoord.y);
			upper.z = Math.max(upper.z, tempCoord.z);
		}
		return new BoundingBox(lower, upper);
	}
	
	/**
	 * Applies the newClientState.
	 * @param currentClientState the current client state
	 * @param newClientState the new client state that will by applyed
	 * @return newClientState
	 */
	public static long applyClientState(long currentClientState, long newClientState) {
		if (RetainedSceneGraph.SAFE_MODE) {
			for (int i=0; i<CLIENT_STATES.length; i++) {
				if (i >= 3) {
					GL13.glClientActiveTexture(GL13.GL_TEXTURE0 + i - 3);
				}
				if ((newClientState & (1 << i)) != 0) {
					GL11.glEnableClientState(CLIENT_STATES[i]);
				} else {
					GL11.glDisableClientState(CLIENT_STATES[i]);
				}
			}
		} else {
			if (newClientState != currentClientState) {
				long dif = newClientState ^ currentClientState;
				for (int i=0; i<CLIENT_STATES.length; i++) {
					if ((dif & (1 << i)) != 0) {
						if (i >= 3) {
							GL13.glClientActiveTexture(GL13.GL_TEXTURE0 + i - 3);
						}
						if ((newClientState & (1 << i)) != 0) {
							GL11.glEnableClientState(CLIENT_STATES[i]);
						} else {
							GL11.glDisableClientState(CLIENT_STATES[i]);
						}
					}
				}
			}
		}		
		
		return newClientState;
	}
	
	/**
	 * Gets the client state of this vertex data.
	 * @return
	 */
	private long getClientState() {
		FloatBuffer colors = vertexData.colors;
		FloatBuffer normals = vertexData.normals;
		ObjectArray<VertexData.TexCoordData> texCoords = vertexData.texCoords;
		long state = 0;
		state |= (1 << 0);
		if (normals != null) {
			state |= (1 << 1);
		}
		if (colors != null) {
			state |= (1 << 2);
		}
		for (int i=0; i<texCoords.length(); i++) {
			if (texCoords.get(i) != null) {
				state |= (1 << (3 + i));
			}
		}
		return state;
	}
	
	/**
	 * @param currentClientState
	 * @return
	 */
	public long drawVertexArray(long currentClientState) {
		FloatBuffer coords = vertexData.coordinates;
		FloatBuffer colors = vertexData.colors;
		FloatBuffer normals = vertexData.normals;
		ObjectArray<VertexData.TexCoordData> texCoords = vertexData.texCoords;
		ObjectArray<VertexData.AttributeData> attributes = vertexData.attributes;
		IntBuffer indices = vertexData.indices;
		
		if (clientState == 0) {
			clientState = getClientState();
		}
		
		applyClientState(currentClientState, clientState);		
		
		if (colors != null) {
			colors.rewind();
			GL11.glColorPointer(3, 0, colors);
		}
		if (normals != null) {
			normals.rewind();
			GL11.glNormalPointer(0, normals);
		}
		if (texCoords != null) {
			for (int unit=0; unit<texCoords.length(); unit++) {
				VertexData.TexCoordData texCoord = texCoords.get(unit);
				if (texCoord != null) {
					FloatBuffer data = texCoords.get(unit).data;
					data.rewind();
					GL13.glClientActiveTexture(GL13.GL_TEXTURE0 + unit);
					GL11.glTexCoordPointer(texCoord.size, 0, data);
				}
			}
		}

		for (int attribIdx=0; attribIdx<attributes.length(); attribIdx++) {
			VertexData.AttributeData attribute = attributes.get(attribIdx);
			if (attribute != null) {				
				int loc = attribIdx+1;
				int stride = 0;
				boolean normalized = false; // attribute.normalized
				GL20.glEnableVertexAttribArray(loc);
				if (attribute.data instanceof FloatBuffer) {
					GL20.glVertexAttribPointer(loc, attribute.size, normalized, stride, (FloatBuffer) attribute.data); 
				} else {
					System.err.println(getClass().getSimpleName()+" vertx attribute type not supported)");
				}
			}
		}
		
		coords.rewind();
		GL11.glVertexPointer(3, 0, coords);

		indices.rewind();
		GL11.glDrawElements(GL11.GL_TRIANGLES, indices);

		for (int attribIdx=0; attribIdx<attributes.length(); attribIdx++) {
			if (attributes.get(attribIdx) != null) {
				GL20.glDisableVertexAttribArray(attribIdx+1);
			}
		}
		
		return clientState;
	}
	
	/**
	 * Disables all client states.
	 */
	public static void disableClientStates() {
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);	
		GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);	
		for (int unit=GL13.GL_TEXTURE0; unit<GL13.GL_TEXTURE8; unit++) {
			GL13.glClientActiveTexture(unit);
			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		}		
	}
	
	/**
	 * Gets the minimum and maximum values in the indices array.
	 * @return array of 2 elements with minimum and maximum values
	 */
	public int[] getMinMaxIndex() {
		IntBuffer indices = vertexData.indices;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i=indices.position(); i<indices.limit(); i++) {
			int index = indices.get(i);
			min = Math.min(min, index);
			max = Math.max(max, index);
		}
		
		return new int[] {min, max};
	}
}
