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

import org.lwjgl.opengl.*;

import trb.jsg.*;
import trb.jsg.renderer.Renderer;

/**
 * Tests rendering various VertexData modes.
 */
public class VertexDataModes {
	
	public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create();

        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));
        renderPass.addShape(createQuads());
        renderPass.addShape(createTriangleStrip());
        renderPass.addShape(createLines());
        Renderer renderer = new Renderer(new SceneGraph(renderPass));

        while (!Display.isCloseRequested()) {
            renderer.render();
            Display.update();
        }

        Display.destroy();
	}

    private static Shape createQuads() {
        float[] coordinates = {100, 100, 0, 200, 100, 0, 200, 200, 0, 100, 200, 0};
        VertexData vertexData = new VertexData(coordinates, null, null, 2, null, null);
        vertexData.mode = VertexData.Mode.QUADS;
        return new Shape(vertexData);
    }

    private static Shape createTriangleStrip() {
        float[] coordinates = {300, 100, 0,  400, 100, 0,  300, 200, 0,  400, 200, 0};
        VertexData vertexData = new VertexData(coordinates, null, null, 2, null, null);
        vertexData.mode = VertexData.Mode.TRIANGLE_STRIP;
        return new Shape(vertexData);
    }

    private static Shape createLines() {
        float[] coordinates = {100, 50, 0, 300, 50, 0};
        int[] indices = {0, 1};
        VertexData vertexData = new VertexData(coordinates, null, null, 2, null, indices);
        vertexData.mode = VertexData.Mode.LINES;
        return new Shape(vertexData);
    }
}
