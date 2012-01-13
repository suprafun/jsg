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
import java.util.List;

import javax.vecmath.Matrix4f;
import trb.jsg.util.Mat4;


/**
 * A node in a transform hierarchy
 * 
 * @author tombr
 *
 */
public class TreeNode implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** The parent node */
	protected TreeNode parent = null;
	
	/** The nodes transform */
	protected Mat4 transform = new Mat4();

	/** The local to world matrix of the node */
	protected Mat4 localToWorld = new Mat4();
	
	/** Optimization is done if transform is identity matrix */
	protected boolean isIdentity = true;
	
	/** List of children */
	protected ArrayList<TreeNode> children = new ArrayList<TreeNode>();
	
	/** All transforms in this node and its descendants need to be updated */
	protected boolean isSubtreeDirty = true;
	
	/** Node is part of a path to a dirty subtree */
	protected boolean isPathDirty = true;
	
	/** List of shapes */
	protected ArrayList<Shape> shapes = new ArrayList<Shape>();
	
	/**
	 * Constructs a node with no children or shapes and an identity matrix.
	 */
	public TreeNode() {
	}
	
	/**
	 * Sets the nodes transform matrix by copy.
	 * @param newTransform the new transform
	 */
	public void setTransform(Mat4 newTransform) {
		transform.set(newTransform);
		isIdentity = false;		
		
		flagAsDirty();
	}

	/**
	 * Set flag isSubtreeDirty and marks the path to this node as dirty.
	 */
	protected void flagAsDirty() {
		// flags all shapes in subtree as dirty
		isSubtreeDirty = true;
		
		// marks path to this node as dirty
		isPathDirty = true;
		TreeNode p = parent;
		while (p != null && !p.isPathDirty) {
			p.isPathDirty = true;
			p = p.parent;
		}
	}
	
	/**
	 * Gets a reference to the transform matrix.
	 * @return the transform matrix
	 */
	public Mat4 getTransform() {
		return transform;
	}
	
	/**
	 * Adds the specified node to the list of children.
	 * @param child the node to add
	 */
	public void addChild(TreeNode child) {
		if (child.parent == null) {
			children.add(child);
			child.parent = this;
			
			// flag path as dirty so the shape transforms is updated on the next update
			child.flagAsDirty();
			
			// attach descendant Shapes
			RootNode root = getRoot();
			if (root != null) {
				ArrayList<Shape> allShapes = child.getAllShapesInTree();
				for (int i=0; i<allShapes.size(); i++) {
					Shape shape = allShapes.get(i);
					root.renderPass.addShape(shape);
				}
			}
		} else {
			System.err.println(getClass().getSimpleName()+" addChild Error: child already has a parent");
		}
	}
	
	/** 
	 * Removes the specified node from the list of children
	 * @param child the node to remove
	 */
	public void removeChild(TreeNode child) {
		if (child.parent == this) {
			children.remove(child);
			child.parent = null;

			// detach descendant Shapes
			RootNode root = getRoot();
			if (root != null) {
				ArrayList<Shape> allShapes = child.getAllShapesInTree();
				for (int i=0; i<allShapes.size(); i++) {
					Shape shape = allShapes.get(i);
					root.renderPass.removeShape(shape);
				}
			}
		} else {
			System.err.println(getClass().getSimpleName()+" removeChild Error: child parent != this");
		}
	}
	
	/**
	 * Gets the number of children.
	 * @return the number of children
	 */
	public int numChildren() {
		return children.size();
	}
	
	/**
	 * Gets the child at the specified index.
	 * @param index the index
	 * @return the child
	 */
	public TreeNode getChild(int index) {
		return children.get(index);
	}
	
	/**
	 * Gets a list of all the children.
	 * @return a list of all the chilren
	 */
	public List<TreeNode> getChildren() {
		return new ArrayList(children);
	}
	
	/**
	 * Adds the specified shape.
	 * @param shape the shape to add
	 */
	public void addShape(Shape shape) {
		shapes.add(shape);
		RootNode root = getRoot();
		if (root != null) {
			root.renderPass.addShape(shape);
		} else {
//			Thread.dumpStack();
		}
		flagAsDirty();
	}
	
	/** 
	 * Removes the specified shape.
	 * @param shape the shape to remove
	 */
	public void removeShape(Shape shape) {
		shapes.remove(shape);
	}
	
	/**
	 * Gets the number of shapes.
	 * @return the number of shapes
	 */
	public int numShapes() {
		return shapes.size();
	}
	
	/**
	 * Gets the shape at the specified index.
	 * @param index the index
	 * @return the shape
	 */
	public Shape getShape(int index) {
		return shapes.get(index);
	}
	
	/**
	 * Gets a list of all the shapes.
	 * @return a list of all the shapes
	 */
	public List<Shape> getShapes() {
		return new ArrayList(shapes);
	}
	
	/**
	 * Gets a reference to the parent node.
	 * @return the parent
	 */
	public TreeNode getParent() {
		return parent;
	}
	
	/**
	 * Gets the root of the tree, or null if node is not attached to a RootNode. 
	 * @return the RootNode or null if it do not exist
	 */
	public RootNode getRoot() {
		TreeNode p = this;
		while (p.getParent() != null) {
			p = p.parent;
		}
		
		if (p instanceof RootNode) {
			return (RootNode) p;
		}
		
		return null;
	}
	
	/**
	 * Gets a list of all the shapes in this subtree.
	 * @return a list of shapes
	 */
	public ArrayList<Shape> getAllShapesInTree() {
		ArrayList<Shape> allShapes = new ArrayList<Shape>();
		getAllShapesInTreeHelper(allShapes);
		return allShapes;
	}
	
	/**
	 * Recursive helper method.
	 * @param allShapes where list of shapes is stored.
	 */
	private void getAllShapesInTreeHelper(ArrayList<Shape> allShapes) {
		allShapes.addAll(shapes);
		for (int i=0; i<children.size(); i++) {
			children.get(i).getAllShapesInTreeHelper(allShapes);
		}
	}

	/**
	 * Traverses the tree and updates dirty shapes. All nodes is traversed if
	 * updateShapes is true. Otherwise only children with isPathDirty flag set
	 * is visited.
	 * @param node the node to search
	 * @param updateShapes true if shapes should be updated
	 */
	public void updateTree(boolean updateShapes) {
		updateShapes |= isSubtreeDirty;
		
		if (updateShapes) {
			// concatinate matrices
			if (parent != null) {
				localToWorld.set(parent.localToWorld);
				localToWorld.mul(transform);
			} else {
				localToWorld.set(transform);
			}
			
			// update shapes modelMatrix
			for (int i=0; i<shapes.size(); i++) {
				Shape shape = shapes.get(i);
				shape.setModelMatrix(localToWorld);
			}
		}
		
		// traverse dirty children
		for (int i=0; i<children.size(); i++) {
			TreeNode child = children.get(i);
			if (updateShapes || child.isPathDirty) {
				child.updateTree(updateShapes);
			}
		}
		
		isSubtreeDirty = false;
		isPathDirty = false;
	}
}
