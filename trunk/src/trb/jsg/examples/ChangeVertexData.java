package trb.jsg.examples;


import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.View;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.geometry.VertexDataUtils;

public class ChangeVertexData {

    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create();

        Shape shape = new Shape(VertexDataUtils.createQuad(100, 100, 300, 300, 0));

        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));
        renderPass.getRootNode().addShape(shape);
        Renderer renderer = new Renderer(new SceneGraph(renderPass));

        while (!Display.isCloseRequested()) {
            shape.setVertexData(VertexDataUtils.createQuad(100, 100, 300, 300, 0));
            renderer.render();
            Display.update();
        }

        Display.destroy();
    }
}
