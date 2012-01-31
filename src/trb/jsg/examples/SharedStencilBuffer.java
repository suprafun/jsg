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
package trb.jsg.examples;

import java.nio.ByteBuffer;
import javax.vecmath.Color4f;
import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import trb.jsg.DepthBuffer;

import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.Texture;
import trb.jsg.Unit;
import trb.jsg.View;
import trb.jsg.enums.Format;
import trb.jsg.enums.StencilAction;
import trb.jsg.enums.StencilFunc;
import trb.jsg.enums.TextureType;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.VertexDataUtils;

/**
 * Shows how the same depth buffer can be used by different render targets.
 */
public class SharedStencilBuffer {
    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create(new PixelFormat().withStencilBits(8));

        View view = View.createOrtho(0, 640, 0, 480, -1000, 1000);

        DepthBuffer depthBuffer = new DepthBuffer(GL30.GL_DEPTH24_STENCIL8);

        ByteBuffer[][] pixels = {{BufferUtils.createByteBuffer(256 * 256 * 4)}};

        // render a box to depth buffer
        Texture texture1 = new Texture(TextureType.TEXTURE_2D, 4, 256, 256, 0, Format.BGRA, pixels, false, false);
        RenderTarget target1 = new RenderTarget(256, 256, depthBuffer, false, texture1);

        RenderPass renderPass1 = new RenderPass();
        renderPass1.setClearStencil(1);
        renderPass1.setClearMask(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        renderPass1.setView(view);
        renderPass1.setRenderTarget(target1);
        Shape shape1 = new Shape(VertexDataUtils.createQuad(300, 200, 100, 100, 0).fillColor(new Vec3(1, 0, 0)));
        shape1.getState().setStencilTestEnabled(true);
        shape1.getState().setStencilFunc(new StencilFuncParams(StencilFunc.ALWAYS, 3, 3));
        shape1.getState().setStencilOp(new StencilOpParams(StencilAction.KEEP, StencilAction.KEEP, StencilAction.REPLACE));
        renderPass1.getRootNode().addShape(shape1);

        RenderPass renderPass2 = new RenderPass();
        renderPass2.setClearMask(0);
        renderPass2.setView(view);
        renderPass2.setRenderTarget(new RenderTarget(256, 256, depthBuffer, false, texture1));
        Shape shape2 = new Shape(VertexDataUtils.createQuad(250, 200, 100, 100, 1).fillColor(new Vec3(0, 2, 0)));
        shape2.getState().setStencilTestEnabled(true);
        shape2.getState().setStencilFunc(new StencilFuncParams(StencilFunc.EQUAL, 3, 3));
        renderPass2.getRootNode().addShape(shape2);

        // copy render pass 2 to screen
        RenderPass renderPass3 = new RenderPass();
        renderPass3.setClearMask(GL11.GL_COLOR_BUFFER_BIT  | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass3.setClearColor(new Color4f(0.4f, 0, 0, 0));
        renderPass3.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));
        Shape shape3 = new Shape(VertexDataUtils.createQuad(0, 480, 640, -480, 0));
        shape3.getState().setUnit(0, new Unit(texture1));
        renderPass3.getRootNode().addShape(shape3);

        SceneGraph sceneGraph = new SceneGraph();
        sceneGraph.addRenderPass(renderPass1);
        sceneGraph.addRenderPass(renderPass2);
        sceneGraph.addRenderPass(renderPass3);
        Renderer renderer = new Renderer(sceneGraph);

        long startTime = System.currentTimeMillis();
        while (!Display.isCloseRequested()) {
            float time = (System.currentTimeMillis() - startTime) / 1000f;

            // animate object back and forth
//            shape2.setModelMatrix(new Mat4().translate(Math.sin(time) * 10, 0, 0));

            renderer.render();
            Display.update();
        }

        Display.destroy();
    }
}
