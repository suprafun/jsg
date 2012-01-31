/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.jsg.examples;

import java.awt.image.BufferedImage;
import trb.jsg.util.Vec3;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shader;
import trb.jsg.ShaderProgram;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.VertexData.AttributeData;
import trb.jsg.View;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Mat4;
import trb.jsg.util.ShaderUtils;
import trb.jsg.util.geometry.VertexDataUtils;

/**
 *
 * @author tomrbryn
 */
public class NormalMap {

    private static String vertexShader =
              "attribute vec4 tangentIn;"
            + "varying vec3 tangent;"
            + "varying vec3 bitangent;"
            + "varying vec3 normal;"
            + "void main(void) {"
            + "    tangent = gl_NormalMatrix * tangentIn.xyz;"
            + "    normal = (gl_NormalMatrix * gl_Normal);"
            + "    bitangent = cross(normal, tangent) * tangentIn.w;"
            + "    gl_TexCoord[0] = gl_MultiTexCoord0;"
            + "    gl_Position = ftransform();"
            + "}";

    private static String fragmentShader =
            "uniform sampler2D source;"
            + "\nvarying vec3 tangent;"
            + "\nvarying vec3 bitangent;"
            + "\nvarying vec3 normal;"
            + "\nvoid main(void) {"
            + "\n    vec3 n = normal;"
            + "\n    vec3 t = tangent;"
            + "\n    vec3 b = bitangent;"
            + "\n    mat3 base = mat3(t.x, b.x, n.x,  t.y, b.y, n.y,  t.z, b.z, n.z);"
            + "\n    n = texture2D(source, gl_TexCoord[0].xy) * 2.0 - 1.0;"
            + "\n    vec3 nVS = base * n;"
            + "\n    float c = dot(nVS, vec3(0, 0, -1));"
            + "\n    gl_FragColor = vec4(tangent, 1);"
            + "\n}";


    public static void main(String[] args) throws Exception {
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(600, 600);
//        frame.add(new JLabel(new ImageIcon(createNormalMap())));
//        frame.setVisible(true);

        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create();

        View view = View.createPerspective((float) Math.PI / 4f, 640f / 480f, 0.1f, 1000);
        view.setCameraMatrix(new Mat4().translate(0, 0, -10));

        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setView(view);

        int size = 256;
        Texture texture = createNormalMapTexture(createNormalMap(size));

        VertexData vertexData = VertexDataUtils.createQuad(-1, -1, 2, 2, 0);
        Shader shader = new Shader(new ShaderProgram(vertexShader, fragmentShader, "tangentIn"));
        Shape shape = new Shape(vertexData);
        shape.getState().setUnit(0, new Unit(texture));
        shape.getState().setShader(shader);
        vertexData.attributes.set(new AttributeData(ShaderUtils.calculateTangent(vertexData, 0), 4), 0);

        renderPass.getRootNode().addShape(shape);
        Renderer renderer = new Renderer(new SceneGraph(renderPass));

        float angle = 0f;
        while (!Display.isCloseRequested()) {
            shape.setModelMatrix(new Mat4().rotateEulerDeg(0, angle++, 0));
            renderer.render();
            Display.update();
            Display.sync(60);
        }

        Display.destroy();
    }

    public static Texture createNormalMapTexture(BufferedImage normalMap) {
        int w = normalMap.getWidth();
        int h = normalMap.getHeight();
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(w * h * 4);
        // TODO: pass in specular map as image.
        int[] rgb = normalMap.getRGB(0, 0, w, h, null, 0, w);
        for (int i=0; i<rgb.length; i++) {
            rgb[i] &= 0xffffff;
            rgb[i] |= ((int)(Math.random() * 255)) << 24;
        }
        byteBuffer.asIntBuffer().put(rgb).rewind();
        ByteBuffer[][] pixels = {{byteBuffer}};
        return new Texture(TextureType.TEXTURE_2D, 4, w, h, 0, Format.BGRA, pixels, true, false);
    }

    public static BufferedImage createNormalMap(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, getNormal(x, y, 0x3f));
            }
        }
        return image;
    }

    static int getNormal(int ix, int iy, int mask) {
        float x = (ix & mask) / (float) mask;
        float y = (iy & mask) / (float) mask;
        Vec3 v = new Vec3(x*2-1, y*2-1, 1).absolute_().normalize_();
        return v.getColor().getRGB();
    }
}
