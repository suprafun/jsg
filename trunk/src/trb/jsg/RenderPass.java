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

package  trb.jsg;

import java.io.Serializable;
import java.util.ArrayList;

import javax.vecmath.Color4f;

import trb.jsg.peers.RenderPassPeer;


/**
 * RenderPass contains a list of shapes that is rendered to the render target.
 * The shapes are rendered to screen if no render target is specified.
 * <p>
 * The view defines the view frustum.
 * <p>
 * The clearMask can be set to clear the color and depth before the shapes are 
 * rendered.
 * 
 * @author tombr
 *
 */
public class RenderPass implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** What buffers to clear. Example: GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT */
	private int clearMask = 0;
	
	/** The clear value for the color buffer */
	private Color4f clearColor = new Color4f();
	
	/** The clear value for the depth buffer */
	private float clearDepth = 1;
	
	/** The clear value for the stencil buffer*/
	private int clearStencil = 0;
	
	/** The view with the projection matrix and view transform */
	private View view = new View();
	
	/** The light state that is applied to all shapes */
	private LightState lightState = new LightState();
	
	/** Shapes to be rendered */
	private ArrayList<Shape> shapes = new ArrayList<Shape>();
	
	/** Root of transform tree */
	private RootNode rootNode = new RootNode(this);
	
	/** Where to render */
	private RenderTarget renderTarget;
	
	/** The SceneGraph the RenderPass is attached to */
	SceneGraph parent;
	
	/** The native peer. Don't touch. */
	transient public RenderPassPeer nativePeer;
	
	/**
	 * Adds a shape to the end of the RenderPass list of shapes.
	 * @param shape the shape to add
	 */
	void addShape(Shape shape) {
		if (shape.parent != null) {
			throw new RuntimeException("Shape already has a parent");
		}
		shape.parent = this;
		shapes.add(shape);

		if (nativePeer != null) {
			nativePeer.shapeAdded(shape);
		}
	}	
	
	/**
	 * Removes the shape from the RenderPass list of shapes.
	 * @param shape the shape to remove
	 */
	void removeShape(Shape shape) {
		if (shapes.remove(shape)) {
			
			if (nativePeer != null) {
				nativePeer.shapeRemoved(shape);
			}

			shape.parent = null;
		}
	}
	
	/**
	 * Gets the number of shapes in the RenderPass.
	 * @return the number of shapes
	 */
	public int getShapeCount() {
		return shapes.size();
	}
	
	/**
	 * Gets the shape at the specified index.
	 * @param index the index of the shape
	 * @return the shape
	 */
	public Shape getShape(int index) {
		return shapes.get(index);
	}
	
	/**
	 * Gets the root of the tree.
	 * @return the root of the tree
	 */
	public TreeNode getRootNode() {
		return rootNode;
	}
	
	/**
	 * Sets the render target to use, or null to render to screen.
	 * @param newTarget the new render target
	 */
	public void setRenderTarget(RenderTarget newTarget) {
		RenderTarget oldTarget = this.renderTarget;
		this.renderTarget = newTarget;
		if (nativePeer != null) {
			nativePeer.renderTargetChanged(oldTarget, newTarget);
		}
	}
	
	/**
	 * Gets a reference to the RenderTarget.
	 * @return the RenderTarget
	 */
	public RenderTarget getRenderTarget() {
		return renderTarget;
	}
	
	/**
	 * Sets the view.
	 * @param view the view
	 */
	public void setView(View view) {
		this.view = view;
	}
	
	/**
	 * Gets the view.
	 * @return the view
	 */
	public View getView() {
		return view;
	}

	/**
	 * Sets the clear mask . 
	 * @param clearMask the clearMask to set. One of the following: 
	 *        GL_COLOR_BUFFER_BIT, GL_DEPTH_BUFFER_BIT, GL_ACCUM_BUFFER_BIT,
	 *        GL_STENCIL_BUFFER_BIT
	 */
	public void setClearMask(int clearMask) {
		this.clearMask = clearMask;
	}

	/**
	 * Gets the clear mask.
	 * @return the clearMask
	 */
	public int getClearMask() {
		return clearMask;
	}

	/**
	 * Sets the clear color.
	 * @param clearColor the clearColor to set
	 */
	public void setClearColor(Color4f clearColor) {
		this.clearColor.set(clearColor);
	}

	/**
	 * Gets the clear color.
	 * @return the clearColor
	 */
	public Color4f getClearColor() {
		return clearColor;
	}

	/**
	 * Sets the clear value for the depth buffer.
	 * @param the depth buffer clear value
	 */
	public void setClearDepth(float clearDepth) {
		this.clearDepth = clearDepth;
	}

	/**
	 * Gets the clear value for the depth buffer.
	 * @return the clear value for the depth buffer
	 */
	public float getClearDepth() {
		return clearDepth;
	}

	/**
	 * Sets the clear value for the stencil buffer.
	 * @param clearStencil the stencil clear value
	 */
	public void setClearStencil(int clearStencil) {
		this.clearStencil = clearStencil;
	}

	/**
	 * Gets the clear value for the stencil buffer.
	 * @return the clear value for the stencil buffer
	 */
	public int getClearStencil() {
		return clearStencil;
	}

	/**
	 * Sets the light state.
	 * @param lightState the light state to state
	 */
	public void setLightState(LightState lightState) {
		this.lightState = lightState;
	}

	/**
	 * Gets the light state.
	 * @return the light state
	 */
	public LightState getLightState() {
		return lightState;
	}
}
