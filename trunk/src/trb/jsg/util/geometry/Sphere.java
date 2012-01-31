package trb.jsg.util.geometry;

import javax.vecmath.Color3f;

import trb.jsg.Shape;
import trb.jsg.VertexData;
import trb.jsg.enums.*;
import trb.jsg.util.Vec3;

/**
 * Creates a textured sphere.
 * @return the sphere Shape
 */
public class Sphere extends Shape {

	private static final long serialVersionUID = 0L;
	
	public Vec3 position = new Vec3();
	
	public Sphere() {
		this(9, 5);
	}
	
	public Sphere(int width, int height) {
		float[] coords = new float[width*height*3];
		float[] colors = new float[width*height*3];
		float[] normals = new float[width*height*3];
		float[][] texCoords = new float[1][width*height*2];
		int dstCoordIdx = 0;
		int dstTexIdx = 0;
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float u = x / (width-1f);
				float v = y / (height-1f);
				
				float coordxz = (float) Math.sin(v*Math.PI);
				float coordy = (float) Math.cos(v*Math.PI);
				float coordx = (float) (Math.cos(u*2*Math.PI) * coordxz);
				float coordz = (float) (Math.sin(u*2*Math.PI) * coordxz);
				
				colors[dstCoordIdx] = 1;//u;
				normals[dstCoordIdx] = coordx;
				coords[dstCoordIdx++] = coordx;
				
				colors[dstCoordIdx] = 1;//v;
				normals[dstCoordIdx] = coordy;
				coords[dstCoordIdx++] = coordy;
				
				colors[dstCoordIdx] = 1;
				normals[dstCoordIdx] = coordz;
				coords[dstCoordIdx++] = coordz;
				
				texCoords[0][dstTexIdx++] = u;
				texCoords[0][dstTexIdx++] = v;
			}
		}
		int[] indices = new int[(width-1) * (height-1) * 2 * 3];
		int indiceIdx = 0;
		for (int y=0; y<height-1; y++) {
			for (int x=0; x<width-1; x++) {
				int idx1 = (y+0) * width + x;
				int idx2 = (y+1) * width + x;
				indices[indiceIdx++] = idx1;
				indices[indiceIdx++] = idx1 + 1;
				indices[indiceIdx++] = idx2 + 1;
				
				indices[indiceIdx++] = idx2 + 1;
				indices[indiceIdx++] = idx2;
				indices[indiceIdx++] = idx1;
			}
		}
			
		VertexData vertexData = new VertexData();
		vertexData.setCoordinates(coords, normals, colors, 2, texCoords, indices);

		this.setVertexData(vertexData);
		
		// set state 
		state.setCullEnabled(true);
		state.setCullFace(Face.BACK);
		state.setDepthTestEnabled(true);
	}
	
	public static void setColor(Shape shape, Color3f color) {
		VertexData vertexData = shape.getVertexData();
		if (vertexData.colors != null) {
			for (int i=0; i<shape.getVertexData().colors.capacity(); i+=3) {
				shape.getVertexData().colors.put(i+0, color.x);
				shape.getVertexData().colors.put(i+1, color.y);
				shape.getVertexData().colors.put(i+2, color.z);
			}
		}
	}
}
