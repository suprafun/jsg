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

package trb.jsg.renderer;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import trb.jsg.Shape;
import trb.jsg.View;
import trb.jsg.util.GLUtils;

/**
 * Renders a list of Shapes in the easiest, slowest and safest way possible.
 * There is no state sorting and vertex data is rendered using immediate mode.
 * 
 * @author tombr
 *
 */
class SlowRenderer {
	public ArrayList<RetainedShape> shapes = new ArrayList<RetainedShape>();
	private RetainedRenderPass renderPassPeer;
	
	public SlowRenderer(RetainedRenderPass renderPassPeer) {
		this.renderPassPeer = renderPassPeer;
	}
	
	public void render(Matrix4f currentMatrix) {
		View view = renderPassPeer.renderPass.getView();

		Matrix4f modelViewMatrix = new Matrix4f();
		modelViewMatrix.set(renderPassPeer.renderPass.getView().getCameraMatrix());
		modelViewMatrix.mul(currentMatrix);
		
		for (int shapeIdx=0; shapeIdx<shapes.size(); shapeIdx++) {
			Shape shape = shapes.get(shapeIdx).shape;
			if (!shape.isVisible()) {
				continue;
			}

			boolean matrixChanged = false;
			if (!currentMatrix.equals(shape.getModelMatrix())) {
				modelViewMatrix.set(renderPassPeer.renderPass.getView().getCameraMatrix());
				modelViewMatrix.mul(shape.getModelMatrix());
				currentMatrix = shape.getModelMatrix();
				matrixChanged = true;
			}

			if (!view.isInsideFrustum(modelViewMatrix, shape.getVertexData().getBoundingSphere(), new Point3f())) {
				continue;
			}
			if (matrixChanged) {
				GLUtils.loadMatrix(modelViewMatrix);
			}
			
			GLState.applyDif(shape);
	
			((RetainedVertexData) shape.getVertexData().nativePeer).drawImmidiate(null);
		}
	}
}
