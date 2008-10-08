/*
 * Copyright (c) 2008 Java Scene Graph
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
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
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

package trb.jsg.tests;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import trb.jsg.RenderPass;
import trb.jsg.SceneGraph;
import trb.jsg.Shape;
import trb.jsg.TreeNode;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.renderer.Renderer;

/**
 * Renders a single triangle in ortho mode.
 * 
 * @author tombr
 *
 */
public class HelloWorld {
	
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

		// a simple triangle
		VertexData vertexData = new VertexData();
		vertexData.setCoordinates(
			  new float[] {100, 100, 0,   100, 400, 0,  400, 400, 0} // coordinates
			, null, null, 0, null
			, new int[] {0, 1, 2} // indices
		);

		// shape has vertex data, state and matrix
		Shape shape = new Shape();
		shape.setVertexData(vertexData);
		
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
