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

package trb.jsg.tests;

import java.nio.ByteBuffer;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;
import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.TreeNode;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.renderer.Renderer;

/**
 *
 * @author tomrbryn
 */
public class TexturedQuad {
    public static void main(String[] args) throws Exception {
        // Use LWJGL to create a frame
        int windowWidth = 640;
        int windowHeight = 480;
        Display.setLocation((Display.getDisplayMode().getWidth() - windowWidth) / 2,
                (Display.getDisplayMode().getHeight() - windowHeight) / 2);
        Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
        Display.create();

        // camera at origo without rotation
        Matrix4f cameraMatrix = new Matrix4f();
        cameraMatrix.setIdentity();

        // ortho mode with a 1:1 mapping to the screen
        View view = new View();
        view.setCameraMatrix(cameraMatrix);
        view.ortho(0, windowWidth, 0, windowHeight, -1000, 1000);

        // create a renderpass that renders to the screen
        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setClearColor(new Color4f(0, 0, 0.4f, 0));
        renderPass.setView(view);

        // generate the texture pixels
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(256 * 256 * 4);
        for (int y=0; y<256; y++) {
            for (int x = 0; x < 256; x++) {
                byte b = (byte) (x ^ y);
                byteBuffer.put(b).put(b).put(b).put(b);
            }
        }

        // create the texture using the pixels
        ByteBuffer[][] pixels = {{byteBuffer}};
        Texture texture = new Texture();
        texture.setTextureData(TextureType.TEXTURE_2D, 4, 256, 256, 0, Format.BGRA, pixels, false);

        // create texture unit containing the texture
        Unit unit = new Unit();
        unit.setEnabled(true);
        unit.setTexture(texture);

        // a simple triangle
        VertexData vertexData = new VertexData();
        vertexData.setCoordinates(
                new float[]{100, 100, 0, 100, 400, 0, 400, 400, 0, 400, 100, 0} // coordinates
                , null, null, 2
                , new float[][]{{0, 0,  0, 1,   1, 1,   1, 0}}
                , new int[]{0, 1, 2,  2, 3, 0} // indices
                );

        // shape has vertex data, state and matrix
        Shape shape = new Shape();
        shape.setVertexData(vertexData);

        // set texture unit to the shapes state
        shape.getState().setUnit(0, unit);

        // add shape to the renderpass tree
        TreeNode root = renderPass.getRootNode();
        root.addShape(shape);

        // add renderpass to scene graph
        SceneGraph sceneGraph = new SceneGraph();
        sceneGraph.insertRenderPass(renderPass, 0);

        // create a renderer that renders the scenegraph
        Renderer renderer = new Renderer(sceneGraph);

        // main game loop
        while (!Display.isCloseRequested()) {
            // render the scene graph
            renderer.render();

            // flip backbuffer
            Display.update();
        }

        // destroy frame when we're done
        Display.destroy();
    }
}
