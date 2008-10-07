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

import java.io.IOException;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.lwjgl.BufferUtils;

import trb.jsg.peers.VertexDataPeer;
import trb.jsg.util.ObjectArray;

/**
 * A index triangle array. The number of triangles rendered is specified by the
 * length of indices. Buffer position an limit is used. All non null coordinate
 * are used. They must be atleast as large to contain the maximim index in
 * indices.
 * <p>
 * Both bounding box and sphere are optional. If both are provided, viewfrustum
 * culling is performed on the sphere before the box. If both are null, no 
 * viewfrustum culling is performed.
 * 
 * @author tombr
 *
 */
public class VertexData implements Serializable {
	
	private static final long serialVersionUID = 0L;

	ArrayList<Shape> parents = new ArrayList<Shape>();
	public FloatBuffer coordinates;
	public FloatBuffer normals;
	/** Packed red, green, blue */
	public FloatBuffer colors;
	public ObjectArray<TexCoordData> texCoords = new ObjectArray<TexCoordData>();
	public ObjectArray<AttributeData> attributes = new ObjectArray<AttributeData>();
	public IntBuffer indices;
	
//	transient private BoundingBox boundingBox;
	transient private BoundingSphere boundingSphere;
	
	transient public VertexDataPeer nativePeer;
	
	public void setCoordinates(float[] coordinates, float[] normals, float[] colors
			, int texCoordElementSize, float[][] texCoords, int[] indices) {
		if (this.coordinates == null || this.coordinates.capacity() < coordinates.length) {
			this.coordinates = BufferUtils.createFloatBuffer(coordinates.length);
		}
		this.coordinates.rewind();
		this.coordinates.put(coordinates).flip();

		if (normals == null) {
			this.normals = null;
		} else {
			if (this.normals == null || this.normals.capacity() < normals.length) {
				this.normals = BufferUtils.createFloatBuffer(normals.length);
			}
			this.normals.rewind();
			this.normals.put(normals).flip();
		}

		if (colors == null) {
			this.colors = null;
		} else {
			if (this.colors == null || this.colors.capacity() < colors.length) {
				this.colors = BufferUtils.createFloatBuffer(colors.length);
			}
			this.colors.rewind();
			this.colors.put(colors).flip();
		}

		this.texCoords.clear();
		if (texCoords != null) {
			for (int i=0; i<texCoords.length; i++) {
				if (texCoords[i] != null) {
					if (this.texCoords.get(i) == null) {
						this.texCoords.set(new TexCoordData(), i);
					}
					if (this.texCoords.get(i).data == null || this.texCoords.get(i).data.capacity() < texCoords[i].length) {
						this.texCoords.get(i).data = BufferUtils.createFloatBuffer(texCoords[i].length);
					}
					this.texCoords.get(i).data.rewind();
					this.texCoords.get(i).data.put(texCoords[i]).flip();
					this.texCoords.get(i).size = texCoordElementSize;
				}
			}
		}

		if (this.indices == null || this.indices.capacity() < indices.length) {
			this.indices = BufferUtils.createIntBuffer(indices.length);
		}
		this.indices.rewind();
		this.indices.put(indices).flip();

		if (nativePeer != null) {
			nativePeer.vertexDataChanged();
		}
		calculateBounds();
	}
	
	public void setCoordinates(FloatBuffer coordinates) {
		this.coordinates = coordinates;
		if (nativePeer != null) {
			nativePeer.vertexDataChanged();
		}
		calculateBounds();
	}
	
	/**
	 * Gets the bounding sphere that surounds this object.
	 * @return the bounding sphere
	 */
	public BoundingSphere getBoundingSphere() {
		return boundingSphere;
	}
	
	/**
	 * Calculates a bounding sphere and bounding box from the vertices.
	 */
	public void calculateBounds() {
		Point3f lower = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Point3f upper = new Point3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		for (int vertexOff=0; vertexOff<coordinates.limit(); vertexOff+=3) {
			lower.x = Math.min(lower.x, coordinates.get(vertexOff+0));
			lower.y = Math.min(lower.y, coordinates.get(vertexOff+1));
			lower.z = Math.min(lower.z, coordinates.get(vertexOff+2));
			upper.x = Math.max(upper.x, coordinates.get(vertexOff+0));
			upper.y = Math.max(upper.y, coordinates.get(vertexOff+1));
			upper.z = Math.max(upper.z, coordinates.get(vertexOff+2));
		}
		
//		boundingBox = new BoundingBox(new Point3d(lower), new Point3d(upper));

		Point3f center = new Point3f();
		center.add(lower, upper);
		center.scale(0.5f);
		boundingSphere = new BoundingSphere(new Point3d(center), 0.0000001);
		Point3d currentP3d = new Point3d();
		for (int vertexOff=0; vertexOff<coordinates.limit(); vertexOff+=3) {
			currentP3d.set(coordinates.get(vertexOff+0)
					, coordinates.get(vertexOff+1)
					, coordinates.get(vertexOff+2));
			boundingSphere.combine(currentP3d);
		}
	}
	
	/**
	 * Serializable.
	 */
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(parents);
		out.writeBoolean(coordinates != null);
		if (coordinates != null) {
			out.writeObject(toArray(coordinates));
		}
		out.writeBoolean(normals != null);
		if (normals != null) {
			out.writeObject(toArray(normals));
		}
		out.writeBoolean(colors != null);
		if (colors != null) {
			out.writeObject(toArray(colors));
		}
		out.writeBoolean(indices != null);
		if (indices != null) {
			out.writeObject(toArray(indices));
		}
		out.writeInt(texCoords.length());
		for (int i=0; i<texCoords.length(); i++) {
			TexCoordData data = texCoords.get(i);
			boolean hasData = (data != null && data.data != null);
			out.writeBoolean(hasData);
			if (hasData) {
				out.writeInt(data.size);
				out.writeObject(toArray(data.data));
			}
		}
		out.writeInt(attributes.length());
		for (int i=0; i<attributes.length(); i++) {
			AttributeData data = attributes.get(i);
			boolean hasData = (data != null && data.data != null);
			out.writeBoolean(hasData);
			if (hasData) {
				out.writeInt(data.size);
				out.writeObject(toArray(data.data));
			}
		}
		
		out.writeBoolean(boundingSphere != null);
		if (boundingSphere != null) {
			Point3d center = new Point3d();
			boundingSphere.getCenter(center);
			out.writeObject(center);
			out.writeFloat((float) boundingSphere.getRadius());
		}
	}
	
	public static Object toArray(Buffer buffer) {
		if (buffer instanceof FloatBuffer) {
			FloatBuffer floatBuffer = (FloatBuffer) buffer;
			float[] floats = new float[floatBuffer.limit()-floatBuffer.position()];
			floatBuffer.get(floats);
			return floats;
		} else if (buffer instanceof IntBuffer) {
			IntBuffer intBuffer = (IntBuffer) buffer;
			int[] ints = new int[intBuffer.limit()-intBuffer.position()];
			intBuffer.get(ints);
			return ints;
		} else if (buffer instanceof ByteBuffer) {
			ByteBuffer byteBuffer = (ByteBuffer) buffer;
			byte[] bytes = new byte[byteBuffer.limit()-byteBuffer.position()];
			byteBuffer.get(bytes);
			return bytes;
		}
		
		throw new RuntimeException("Unsupported Buffer type "+buffer.getClass());
	}
	
	/**
	 * Serializable.
	 */
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		parents = (ArrayList<Shape>) in.readObject();
		boolean hasCoords = in.readBoolean();
		if (hasCoords) {
			float[] array = (float[]) in.readObject();
			coordinates = BufferUtils.createFloatBuffer(array.length);
			coordinates.put(array).rewind();
		}
		boolean hasNormals = in.readBoolean();
		if (hasNormals) {
			float[] array = (float[]) in.readObject();
			normals = BufferUtils.createFloatBuffer(array.length);
			normals.put(array).rewind();
		}
		boolean hasColors = in.readBoolean();
		if (hasColors) {
			float[] array = (float[]) in.readObject();
			colors = BufferUtils.createFloatBuffer(array.length);
			colors.put(array).rewind();
		}
		boolean hasIndices = in.readBoolean();
		if (hasIndices) {
			int[] array = (int[]) in.readObject();
			indices = BufferUtils.createIntBuffer(array.length);
			indices.put(array).rewind();
		}
		texCoords = new ObjectArray<TexCoordData>();
		int texCoordCnt = in.readInt();
		for (int i=0; i<texCoordCnt; i++) {
			boolean hasData = in.readBoolean();
			if (hasData) {
				TexCoordData texData = new TexCoordData();
				texData.size = in.readInt();
				float[] array = (float[]) in.readObject();
				texData.data = BufferUtils.createFloatBuffer(array.length);
				texData.data.put(array).rewind();
				texCoords.set(texData, i);
			}
		}		
		attributes = new ObjectArray<AttributeData>();
		int attributeCnt = in.readInt();
		for (int i=0; i<attributeCnt; i++) {
			boolean hasData = in.readBoolean();
			if (hasData) {
				AttributeData attrData = new AttributeData();
				attrData.size = in.readInt();
				float[] array = (float[]) in.readObject();
				attrData.data = BufferUtils.createFloatBuffer(array.length);
				attrData.data.put(array).rewind();
				attributes.set(attrData, i);
			}
		}
		boolean hasBoundingSphere = in.readBoolean();
		if (hasBoundingSphere) {
			Point3d center = (Point3d) in.readObject();
			float radius = in.readFloat();
			boundingSphere = new BoundingSphere(center, radius);
		}
	}
	
	
	/**
	 * An array of vertex attribute values
	 */
	public static class AttributeData {
		
		/** A direct buffer containing the data */
		public FloatBuffer data;
		
		/** Specifies the number of components per generic vertex attribute.
		 * Must be 1, 2, 3, or 4. The initial value is 4. 
		 */
		public int size = 4;
		
		// commented out since we only support float
//		/** Specifies the data type of each component in the array. Symbolic
//		 * constants GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT, 
//		 * GL_INT, GL_UNSIGNED_INT, GL_FLOAT, or GL_DOUBLE are accepted. The
//		 * initial value is GL_FLOAT.
//		 */
//		public int type = GL11.GL_FLOAT;
//		
//		/** Specifies whether fixed-point data values should be normalized 
//		 * (GL_TRUE) or converted directly as fixed-point values (GL_FALSE) 
//		 * when they are accessed. 
//		 */
//		public boolean normalized = false;
//		
//		/** Whether the byte, short or int data is unsigned */
//		public boolean unsigned = false;
	}
	
	/**
	 * 
	 * @author tombr
	 *
	 */
	public static class TexCoordData {

		/** The data packed with stride 0 */
		public FloatBuffer data;
		
		/** Specifies the number of coordinates per array element. Must be 1, 
		 * 2, 3, or 4. The initial value is 4.
		 */
		public int size;
	}
}
