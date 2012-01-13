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

package trb.jsg;

import java.io.Serializable;
import java.util.ArrayList;

import trb.jsg.peers.SceneGraphPeer;

/**
 * The SceneGraph is the root of the datastructure that contains all the data
 * that is needed to render on frame. It consist of a list of RenderPasses that
 * are rendered in order. 
 * 
 * @author tombr
 *
 */
public class SceneGraph implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** The list of render passes */
	protected ArrayList<RenderPass> renderPasses = new ArrayList<RenderPass>();
	
	/** The native peer */
	transient public SceneGraphPeer nativePeer;

    public SceneGraph() {

    }

    public SceneGraph(RenderPass renderPass) {
        addRenderPass(renderPass);
    }

    /**
     * Adds the RenderPass at the end.
     */
    public void addRenderPass(RenderPass renderPass) {
        insertRenderPass(renderPass, renderPasses.size());
    }
	
	/**
	 * Inserts the RenderPass at the specified index
	 * @param renderPass what to insert
	 * @param insertIdx where to insert it
	 */
	public void insertRenderPass(RenderPass renderPass, int insertIdx) {
		if (renderPass.parent != null) {
			throw new RuntimeException("RenderPass already has a parent");
		}
		renderPass.parent = this;
		renderPasses.add(insertIdx, renderPass);
		if (nativePeer != null) {
			nativePeer.passAdded(renderPass);
		}
	}
	
	/**
	 * Removes the specified RenderPass from the SceneGraph. 
	 * @param renderPass what to remove
	 */
	public void removeRenderPass(RenderPass renderPass) {
		if (renderPasses.remove(renderPass)) {			
			if (nativePeer != null) {
				nativePeer.passRemoved(renderPass);
			}
			renderPass.parent = null;
		}
	}
	
	/**
	 * Gets the number of render passes in the SceneGraph.
	 * @return the render pass count
	 */
	public int getRenderPassCount() {
		return renderPasses.size();
	}

	/**
	 * Gets the RenderPass at the specified index.
	 * @param index the index
	 * @return the RenderPass
	 */
	public RenderPass getRenderPass(int index) {
		return renderPasses.get(index);
	}
	
	/**
	 * Don't call this directly, it is invoked by the Renderer. Will update all
	 * renderpass tree nodes. 
	 */
	public void updateTrees() {
		for (int i=0; i<renderPasses.size(); i++) {
			RenderPass renderPass = renderPasses.get(i);
			TreeNode rootNode = renderPass.getRootNode();
			if (rootNode.isSubtreeDirty || rootNode.isPathDirty) {
				rootNode.updateTree(rootNode.isSubtreeDirty);
			}
		}
	}
}
