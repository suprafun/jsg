package trb.jsg.util.geometry;

import java.util.Arrays;
import java.util.EnumSet;
import javax.vecmath.Vector3f;
import trb.jsg.VertexData;

public class VertexDataUtils {

    public enum Type {COLOR, TEX_COORD, NORMAL};

    public static VertexData createQuad(int x, int y, int w, int h, float d) {
        return createQuad(x, y, w, h, d, EnumSet.allOf(Type.class));
    }

    public static VertexData createQuad(int x, int y, int w, int h, float d, EnumSet<Type> set) {
        float[] coords = {x, y, d, x, y + h, d, x + w, y + h, d, x + w, y, d};
        int[] indices = {0, 1, 2, 2, 3, 0};
        float[] colors = null;
        float[] normals = null;
        float[][] texCoords = null;
        if (set.contains(Type.TEX_COORD)) {
            texCoords = new float[][]{{0, 0, 0, 1, 1, 1, 1, 0}};
        }
        if (set.contains(Type.COLOR)) {
            colors = new float[coords.length];
            Arrays.fill(colors, 1f);
        }
        if (set.contains(Type.NORMAL)) {
            normals = new float[] {0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1};
        }

        return new VertexData(coords, normals, colors, 2, texCoords, indices);
    }
    
    public static VertexData createBox(Vector3f min, Vector3f max) {
        Vector3f verts[] = new Vector3f[8];
        verts[0] = new Vector3f(min.x, min.y, min.z);
        verts[1] = new Vector3f(max.x, min.y, min.z);
        verts[2] = new Vector3f(max.x, max.y, min.z);
        verts[3] = new Vector3f(min.x, max.y, min.z);

        verts[4] = new Vector3f(max.x, min.y, max.z);
        verts[5] = new Vector3f(min.x, min.y, max.z);
        verts[6] = new Vector3f(max.x, max.y, max.z);
        verts[7] = new Vector3f(min.x, max.y, max.z);

        int[] sides = new int[]{
            0, 1, 2, 3 // back
            , 1, 4, 6, 2 // right
            , 4, 5, 7, 6 // front
            , 5, 0, 3, 7 // left
            , 2, 6, 7, 3 // top
            , 0, 5, 4, 1 // bottom
        };

        float[] coords = new float[6 * 4 * 3];
        for (int i = 0; i < sides.length; i++) {
            coords[i * 3 + 0] = verts[sides[i]].x;
            coords[i * 3 + 1] = verts[sides[i]].y;
            coords[i * 3 + 2] = verts[sides[i]].z;
        }

        int[] indices = {2, 1, 0, 3, 2, 0, 6, 5, 4, 7, 6, 4, 10, 9, 8, 11, 10,
            8, 14, 13, 12, 15, 14, 12, 18, 17, 16, 19, 18, 16, 22, 21, 20,
            23, 22, 20};

        return new VertexData(coords, null, null, 2, null, indices);
    }
}
