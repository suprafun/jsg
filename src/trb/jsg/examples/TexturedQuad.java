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
import org.lwjgl.BufferUtils;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Unit;
import trb.jsg.View;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.renderer.Renderer;
import trb.jsg.util.geometry.VertexDataUtils;

/**
 * Example of a textured quad.
 */
public class TexturedQuad {
    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.create();

        RenderPass renderPass = new RenderPass();
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        renderPass.setView(View.createOrtho(0, 640, 0, 480, -1000, 1000));

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(256 * 256 * 4);
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                byte b = (byte) (x ^ y);
                byteBuffer.put(b).put(b).put(b).put(b);
            }
        }
        ByteBuffer[][] pixels = {{byteBuffer}};
        Texture texture = new Texture(TextureType.TEXTURE_2D, 4, 256, 256, 0, Format.BGRA, pixels, false, false);

        Shape shape = new Shape(VertexDataUtils.createQuad(100, 100, 300, 300, 0));
        shape.getState().setUnit(0, new Unit(texture));

        renderPass.getRootNode().addShape(shape);
        Renderer renderer = new Renderer(new SceneGraph(renderPass));

        while (!Display.isCloseRequested()) {
            renderer.render();
            Display.update();
        }

        Display.destroy();
    }
}
