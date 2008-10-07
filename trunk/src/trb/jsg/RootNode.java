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

package trb.jsg;

import java.io.Serializable;


/**
 * The root of a transform hierarchy. Shapes added to the tree are added to a
 * RenderPass. The update() method will recalculate all the model matrices of
 * the shapes in the tree. Should be called every frame after changes has been
 * made to the tree and before rendering scenegraph. Change include setting a
 * transform or adding a new subgraph.
 * 
 * @author tombr
 *
 */
class RootNode extends TreeNode implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** Where the shapes are added */
	protected RenderPass renderPass;
	
	/**
	 * Constructs a RootNode that adds it shapes to the specified RenderPass.
	 * @param renderPass the RenderPass
	 */
	public RootNode(RenderPass renderPass) {
		this.renderPass = renderPass;
	}
	
	/**
	 * Updates the model matrix of the dirty shapes in the tree.
	 */
//	public void update() {
//		if (isSubtreeDirty || isPathDirty) {
//			updateTree(isSubtreeDirty);
//		}
//	}
}
