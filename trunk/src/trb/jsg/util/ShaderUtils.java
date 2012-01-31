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

package trb.jsg.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.FloatBuffer;
import javax.vecmath.Vector2f;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.VertexData;
import trb.jsg.VertexData.TexCoordData;

public class ShaderUtils {

    public static Shader loadFromResource(String path1, String path2) {
        return load(ShaderUtils.class.getResourceAsStream(path1),
                ShaderUtils.class.getResourceAsStream(path2));

    }

    public static Shader load(InputStream vertexIn, InputStream fragmentIn) {
        // create the shader
        CharSequence vertexShader = toCharSequence(vertexIn);
        CharSequence fragmentShader = toCharSequence(fragmentIn);
        ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        return new Shader(shaderProgram);
    }

    private static CharSequence toCharSequence(InputStream in) {
        try {
            String code = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                code = code + line + "\n";
            }
            return code;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static float[] calculateTangent(VertexData vertexData, int texCoordIdx) {
        FloatBuffer normalBuffer = vertexData.normals;
        TexCoordData texCoordBuffer = vertexData.texCoords.get(texCoordIdx);
        if (normalBuffer == null || texCoordBuffer == null || texCoordBuffer.size != 2) {
            return null;
        }
        Vec3[] coords = toVec3Array(vertexData.coordinates);
        Vec3[] normals = toVec3Array(normalBuffer);
        float[] texCoords = toArray(texCoordBuffer.data);
        Vec3[] tan1 = createArray(Vec3.class, coords.length);
        Vec3[] tan2 = createArray(Vec3.class, coords.length);
        for (int idx = 0; idx < vertexData.indices.limit(); idx+=3) {
            int i1 = vertexData.indices.get(idx);
            int i2 = vertexData.indices.get(idx + 1);
            int i3 = vertexData.indices.get(idx + 2);

            Vec3 v1 = coords[i1];
            Vec3 v2 = coords[i2];
            Vec3 v3 = coords[i3];

            float x1 = v2.x - v1.x;
            float y1 = v2.y - v1.y;
            float z1 = v2.z - v1.z;
            float x2 = v3.x - v1.x;
            float y2 = v3.y - v1.y;
            float z2 = v3.z - v1.z;

            Vector2f w1 = new Vector2f(texCoords[i1 * 2], texCoords[i1 * 2 + 1]);
            Vector2f w2 = new Vector2f(texCoords[i2 * 2], texCoords[i2 * 2 + 1]);
            Vector2f w3 = new Vector2f(texCoords[i3 * 2], texCoords[i3 * 2 + 1]);
            float s1 = w2.x - w1.x;
            float s2 = w3.x - w1.x;
            float t1 = w2.y - w1.y;
            float t2 = w3.y - w1.y;

            float r = 1.0F / (s1 * t2 - s2 * t1);
            Vec3 sdir = new Vec3(
                    (t2 * x1 - t1 * x2) * r,
                    (t2 * y1 - t1 * y2) * r,
                    (t2 * z1 - t1 * z2) * r);
            Vec3 tdir = new Vec3(
                    (s1 * x2 - s2 * x1) * r,
                    (s1 * y2 - s2 * y1) * r,
                    (s1 * z2 - s2 * z1) * r);

            tan1[i1].add_(sdir);
            tan1[i2].add_(sdir);
            tan1[i3].add_(sdir);

            tan2[i1].add_(tdir);
            tan2[i2].add_(tdir);
            tan2[i3].add_(tdir);
        }

        float[] tangent = new float[coords.length * 4];
        for (int a = 0; a < coords.length; a++) {
            Vec3 n = normals[a];
            Vec3 t = tan1[a];

            // Gram-Schmidt orthogonalize
            //tangent[a] = (t - n * dot(n, t)).Normalize();
            Vec3 tan = new Vec3(n).scale_(n.dot(t)).negate_().add_(t).normalize_();

            // Calculate handedness
            //tangent[a].w = (Dot(Cross(n, t), tan2[a]) < 0.0F) ? -1.0F : 1.0F;
            float w = new Vec3().cross_(n, t).dot(tan2[a]) < 0f ? -1f : 1f;

            tangent[a*4] = tan.x;
            tangent[a*4+1] = tan.y;
            tangent[a*4+2] = tan.z;
            tangent[a*4+3] = w;
        }

        return tangent;
    }

    static <T> T[] createArray(Class<T> type, int length) {
        try {
            T[] array = (T[]) Array.newInstance(type, length);
            for (int i = 0; i < length; i++) {
                array[i] = type.getConstructor().newInstance();
            }
            return array;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static float[] toArray(FloatBuffer buf) {
        buf.rewind();
        float[] array = new float[buf.limit()];
        buf.get(array).rewind();
        return array;
    }

    static Vec3[] toVec3Array(FloatBuffer buf) {
        Vec3[] array = new Vec3[buf.limit()/3];
        for (int i=0; i<array.length; i++) {
            array[i] = new Vec3(buf.get(i*3), buf.get(i*3+1), buf.get(i*3+2));
        }
        return array;
    }
}
