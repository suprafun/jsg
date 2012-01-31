package trb.jsg.util.geometry;

import javax.vecmath.Vector3f;

import org.lwjgl.BufferUtils;

import trb.jsg.VertexData;
import trb.jsg.util.*;

/**
 * 
 * @author tombr
 *
 */
public class DynamicMesh extends VertexData {

	private static final long serialVersionUID = 0L;
	
	/** x,y,z packed array of coordinates */
	public float[] coords;
	
	/** A triangle array indices into coords */
	protected int[] indices;
	
	/** A list of faces that is connected to the coordinate at the each index */
	protected IntArray[] coordFaces;
	
	protected float[] faceNormals;
	protected float[] vertexNormals;
	
	public DynamicMesh(float[] coords, int[] indices) {
		this.coords = coords;
		this.indices = indices;
		vertexNormals = new float[coords.length];
		faceNormals = new float[indices.length];
		coordFaces = new IntArray[coords.length/3];
		for (int i=0; i<coordFaces.length; i++) {
			coordFaces[i] = new IntArray(10);
		}
		for (int idx=0; idx<indices.length; idx++) {
			IntArray faceList = coordFaces[indices[idx]];
			faceList.set(idx/3, faceList.length()+1);
		}
		
		this.coordinates = BufferUtils.createFloatBuffer(coords.length);
		this.normals = BufferUtils.createFloatBuffer(vertexNormals.length);
		super.indices = BufferUtils.createIntBuffer(indices.length);
		super.indices.put(indices).flip();
		
		updateNormals();
		calculateBounds();
	}

	public void updateNormals() {
		Vector3f a = new Vector3f();
		Vector3f b = new Vector3f();
		Vector3f c = new Vector3f();
		Vector3f ab = new Vector3f();
		Vector3f ac = new Vector3f();
		Vector3f n = new Vector3f();
		
		// calculate face normals
		for (int faceOff=0; faceOff<indices.length; faceOff+=3) {
			int coordIdx1 = indices[faceOff+0];
			int coordIdx2 = indices[faceOff+1];
			int coordIdx3 = indices[faceOff+2];
			a.set(coords[coordIdx1*3+0], coords[coordIdx1*3+1], coords[coordIdx1*3+2]);
			b.set(coords[coordIdx2*3+0], coords[coordIdx2*3+1], coords[coordIdx2*3+2]);
			c.set(coords[coordIdx3*3+0], coords[coordIdx3*3+1], coords[coordIdx3*3+2]);
			ab.sub(a, b);
			ac.sub(a, c);
			n.cross(ab, ac);
			// do not normalize
			n.normalize();
			faceNormals[faceOff+0] = n.x;
			faceNormals[faceOff+1] = n.y;
			faceNormals[faceOff+2] = n.z;
		}
		
		// calculate vertex normals
		for (int coordIdx=0; coordIdx<coordFaces.length; coordIdx++) {
			IntArray faceList = coordFaces[coordIdx];
			n.set(0, 0, 0);
			for (int i=0; i<faceList.length(); i++) {
				int faceIdx = faceList.get(i);
				n.x += faceNormals[faceIdx*3+0];
				n.y += faceNormals[faceIdx*3+1];
				n.z += faceNormals[faceIdx*3+2];
			}
			
			n.normalize();
			vertexNormals[coordIdx*3+0] = n.x;
			vertexNormals[coordIdx*3+1] = n.y;
			vertexNormals[coordIdx*3+2] = n.z;
		}
		
		this.normals.rewind();
		this.normals.put(vertexNormals).flip();
		this.coordinates.rewind();
		this.coordinates.put(coords).flip();
		
		if (nativePeer != null) {
			nativePeer.vertexDataChanged();
		}
	}
}
