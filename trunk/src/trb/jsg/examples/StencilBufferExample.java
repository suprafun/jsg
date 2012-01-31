package trb.jsg.examples;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.View;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.VertexDataUtils;

/**
 * Shows how the same depth buffer can be used by different render targets.
 */
public class StencilBufferExample {

    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create(new PixelFormat().withStencilBits(8));

        View view = View.createOrtho(0, 640, 0, 480, -1000, 1000);

        // shape fills stencil buffer with 1
        Shape maskShape = new Shape(VertexDataUtils.createQuad(300, 200, 100, 100, 0).fillColor(new Vec3(1, 0, 0)));
        maskShape.getState().setStencilTestEnabled(true);
        maskShape.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 1, 1));
        maskShape.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));

        // render a box to depth buffer
        RenderPass renderPass1 = new RenderPass();
        renderPass1.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        renderPass1.setView(view);
        renderPass1.addShape(maskShape);

        // render a moving box to the color buffer but use the depth buffer of render pass1
        Shape shape2 = new Shape(VertexDataUtils.createQuad(200, 100, 500, 500, 0).fillColor(new Vec3(0, 1, 0)));
        shape2.getState().setStencilTestEnabled(true);
        shape2.getState().setStencilFunc(new StencilFuncParams(StencilFunc.EQUAL, 1, 1));

        RenderPass renderPass2 = new RenderPass();
        renderPass2.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass2.setView(view);
        renderPass2.getRootNode().addShape(shape2);

        SceneGraph sceneGraph = new SceneGraph();
        sceneGraph.addRenderPass(renderPass1);
        sceneGraph.addRenderPass(renderPass2);
        Renderer renderer = new Renderer(sceneGraph);

        long startTime = System.currentTimeMillis();
        while (!Display.isCloseRequested()) {
            float time = (System.currentTimeMillis() - startTime) / 1000f;

            // animate object back and forth
            maskShape.setModelMatrix(new Mat4().translate(Math.sin(time) * 200, 0, 0));

            renderer.render();
            Display.update();
        }

        Display.destroy();
    }
}
