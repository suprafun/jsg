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
import java.util.ArrayList;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;

import trb.jsg.enums.AlphaTestFunc;
import trb.jsg.enums.BlendDstFunc;
import trb.jsg.enums.BlendSrcFunc;
import trb.jsg.enums.CullFace;
import trb.jsg.enums.DepthFunc;
import trb.jsg.enums.FrontFace;
import trb.jsg.enums.StencilFunc;
import trb.jsg.enums.StencilOp;
import trb.jsg.util.Hash;
import trb.jsg.util.ObjectArray;

/**
 * The opengl state.
 * 
 * @author tombr
 *
 */
public class State implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** Shapes that references this object */
	ArrayList<Shape> owners = new ArrayList<Shape>();
	
	// hash value of the current state, -1 if it is unknown
	private Hash hash = new Hash();
	
	/** The shader that defines the appearance of the shape */
	private Shader shader;
	
	/** The state for each of the texture units used by this shape. The unit is
	 * disabled if its index contains a null value
	 */
	private ObjectArray<Unit> units = new ObjectArray<Unit>(); 
	
	/** Sorted list of the indexes of the active units */
	private int[] activeUnits = new int[0];
	
	// culling state
	private boolean cullEnabled = false;
	private CullFace cullFace = CullFace.BACK;
	private FrontFace frontFace = FrontFace.CCW;
	
	// transparency state
	private boolean blendEnabled = false;
	
	private BlendSrcFunc blendSrcFunc = BlendSrcFunc.ONE;

	private BlendDstFunc blendDstFunc = BlendDstFunc.ZERO;
	
	/** True to enable depth testing */
	private boolean depthTestEnabled = true;
	
	private DepthFunc depthFunc = DepthFunc.LESS;

	/** True to enable depth write */
	private boolean depthWriteEnabled = true;
	
	private boolean stencilTestEnabled = false;
	private StencilFunc stencilFuncFront = StencilFunc.ALWAYS;
	private int stencilRefFront = 0;
	private int stencilMaskFront = 0xff;
	private int stencilWriteMaskFront = 0xff;
	private StencilOp stencilFailFront = StencilOp.KEEP;
	private StencilOp stencilDepthFailFront = StencilOp.KEEP;
	private StencilOp stencilDepthPassFront = StencilOp.KEEP;
	private StencilFunc stencilFuncBack = StencilFunc.ALWAYS;
	private int stencilRefBack = 0;
	private int stencilMaskBack = 0xff;
	private int stencilWriteMaskBack = 0xff;
	private StencilOp stencilFailBack = StencilOp.KEEP;
	private StencilOp stencilDepthFailBack = StencilOp.KEEP;
	private StencilOp stencilDepthPassBack = StencilOp.KEEP;
	
	private boolean alphaTestEnabled = false;
	private AlphaTestFunc alphaTestFunc = AlphaTestFunc.ALWAYS;
	private float alphaTestRef = 0;
	
	public boolean polygonOffsetFillEnabled = false;
	public float polygonOffsetFactor = 0;
	public float polygonOffsetUnits = 0;
	
	// The material state.
	private Material material;

	/**
	 * Constructs a State with the default state.
	 */
	public State() {
	}

	/**
	 * Gets a has of the state.
	 * @return the hash
	 */
	public int getHash() {
		if (hash.hash == -1) {
			hash.setSeed(shader != null && shader.getShaderProgram() != null ? shader.getShaderProgram().getStateId() : 0);
			hash.addInt((shader == null) ? 0 : shader.uniformSet.hashCode());
			hash.addBoolean(cullEnabled);
			if (cullEnabled) {
				hash.addInt(cullFace.get());
				hash.addInt(frontFace.get());
			}

			hash.addBoolean(depthTestEnabled);
			if (depthTestEnabled) {
				hash.addInt(depthFunc.get());
			}
			hash.addBoolean(depthWriteEnabled);
			
			hash.addBoolean(blendEnabled);
			if (blendEnabled) {
				hash.addInt(blendSrcFunc.get());
				hash.addInt(blendDstFunc.get());
			}
			
			int unitsEnabledFlags = 0;
			for (int i=0; i<units.length(); i++) {
				if (units.get(i).isEnabled()) {
					unitsEnabledFlags |= (1 << i);
				}
				
				units.get(i).updateHash();
				hash.addInt(units.get(i).hash.hash);
			}
			
			hash.addInt(unitsEnabledFlags);
			
			hash.addBoolean(stencilTestEnabled);
			if (stencilTestEnabled) {
				hash.addInt(stencilFuncFront.get());
				hash.addInt(stencilRefFront);
				hash.addInt(stencilMaskFront);
				hash.addInt(stencilWriteMaskFront);
				hash.addInt(stencilFailFront.get());
				hash.addInt(stencilDepthFailFront.get());
				hash.addInt(stencilDepthPassFront.get());
				hash.addInt(stencilFuncBack.get());
				hash.addInt(stencilRefBack);
				hash.addInt(stencilMaskBack);
				hash.addInt(stencilWriteMaskBack);
				hash.addInt(stencilFailBack.get());
				hash.addInt(stencilDepthFailBack.get());
				hash.addInt(stencilDepthPassBack.get());
			}
			hash.addBoolean(alphaTestEnabled);
			if (alphaTestEnabled) {
				hash.addInt(alphaTestFunc.get());
				hash.addFloat(alphaTestRef);
			}
			hash.addBoolean(polygonOffsetFillEnabled);
			if (polygonOffsetFillEnabled) {
				hash.addFloat(polygonOffsetFactor);
				hash.addFloat(polygonOffsetUnits);
			}
			hash.addBoolean(material != null);
			if (material != null) {
				hash.addFloat(material.ambientColor.x);
				hash.addFloat(material.ambientColor.y);
				hash.addFloat(material.ambientColor.z);
				hash.addFloat(material.diffuseColor.x);
				hash.addFloat(material.diffuseColor.y);
				hash.addFloat(material.diffuseColor.z);
				hash.addFloat(material.emissionColor.x);
				hash.addFloat(material.emissionColor.y);
				hash.addFloat(material.emissionColor.z);
				hash.addFloat(material.specularColor.x);
				hash.addFloat(material.specularColor.y);
				hash.addFloat(material.specularColor.z);
				hash.addFloat(material.shininess);
			}
		}
		
		return hash.hash;
	}
	
	/**
	 * Notifies the owners the the state has changed. Also dirtys the hash.
	 */
	public void stateChanged() {
		hash.hash = -1;
		for (Shape owner : owners) {
			if (owner.nativePeer != null) {
				owner.nativePeer.stateChanged();
			}
		}
	}
	
	/**
	 * Sets the unit to use at the specified index.
	 * @param index the unit index
	 * @param unit the unit, can be null
	 */
	public void setUnit(int index, Unit unit) {
		Unit oldUnit = units.get(index);
		if (oldUnit != unit) {
			units.set(unit, index);
			Texture oldTexture = null;
			Texture newTexture = null;
			if (oldUnit != null) {
				oldUnit.owners.remove(this);
				oldTexture = oldUnit.getTexture();
			}
			if (unit != null) {
				unit.owners.add(this);
				newTexture = unit.getTexture();
			}
			
			for (Shape owner : owners) {
				if (owner.nativePeer != null) {
					owner.nativePeer.textureChanged(oldTexture, newTexture);
				}
			}

			// update activeUnits
			int activeUnitCnt = 0;
			for (int i=0; i<units.length(); i++) {
				if (units.get(i) != null) {
					activeUnitCnt++;
				}
			}
			activeUnits = new int[activeUnitCnt];
			int off = 0;
			for (int i=0; i<units.length(); i++) {
				if (units.get(i) != null) {
					activeUnits[off++] = i;
				}
			}

			stateChanged();
		}
	}
	
	/**
	 * Gets the unit at the specified index. The default value for all units is 
	 * null.
	 * @param index the unit index
	 * @return the unit at the specified index
	 */
	public Unit getUnit(int index) {
		return units.get(index);
	}
	
	/**
	 * Gets a list of the units that is in use (not null).
	 * @return a list of active units
	 */
	public int[] getActiveUnits() {
		return activeUnits;
	}

	/**
	 * Sets this shape to use the specified Shader.
	 * @param newShader the new shader
	 */
	public void setShader(Shader newShader) {
//		if (newShader == null) {
//			newShader = Shader.disabledShader;
//		}
		Shader oldShader = shader;
		shader = newShader;
		for (Shape owner : owners) {
			if (owner.nativePeer != null) {
				owner.nativePeer.shaderChanged(oldShader, newShader);
			}
		}
		stateChanged();
	}
	
	/**
	 * Gets the shader.
	 * @return the shader
	 */
	public Shader getShader() {
		return shader;
	}

	/**
	 * Enables or disables culling.
	 * @param cullEnabled true to enable, false to disable
	 */
	public void setCullEnabled(boolean cullEnabled) {
		if (this.cullEnabled != cullEnabled) {
			this.cullEnabled = cullEnabled;
			stateChanged();
		}
	}

	/**
	 * Checks if culling is enabled
	 * @return true if enabled, otherwise false
	 */
	public boolean isCullEnabled() {
		return cullEnabled;
	}

	/**
	 * Sets the face culling.
	 * @param cullFace the face to be culled
	 */
	public void setCullFace(CullFace cullFace) {
		if (this.cullFace != cullFace) {
			this.cullFace = cullFace;
			stateChanged();
		}
	}

	/**
	 * Gets the face culling.
	 * @return the face culling
	 */
	public CullFace getCullFace() {
		return cullFace;
	}

	/**
	 * Sets the front face.
	 * @param frontFace the front face to set
	 */
	public void setFrontFace(FrontFace frontFace) {
		if (this.frontFace != frontFace) {
			this.frontFace = frontFace;
			stateChanged();
		}
	}

	/**
	 * Gets the front face.
	 * @return the front face
	 */
	public FrontFace getFrontFace() {
		return frontFace;
	}

	/**
	 * Enables or disables blending.
	 * @param blendEnabled true to enable, false to disable
	 */
	public void setBlendEnabled(boolean blendEnabled) {
		if (this.blendEnabled != blendEnabled) {
			this.blendEnabled = blendEnabled;
			stateChanged();
		}
	}

	/**
	 * Checks if blending is enabled.
	 * @return true if enabled, otherwise false
	 */
	public boolean isBlendEnabled() {
		return blendEnabled;
	}

	/**
	 * Sets the source blend function. The source function specifies the factor
	 * that is multiplied by the source color; this value is added to the 
	 * product of the destination factor and the destination color. 
	 * @param blendSrcFunc the source blend function to set
	 */
	public void setBlendSrcFunc(BlendSrcFunc blendSrcFunc) {
		if (this.blendSrcFunc != blendSrcFunc) {
			this.blendSrcFunc = blendSrcFunc;
			stateChanged();
		}
	}

	/**
	 * Gets the source blend function.
	 * @return the blendSrcFunc
	 */
	public BlendSrcFunc getBlendSrcFunc() {
		return blendSrcFunc;
	}

	/**
	 * Sets the destination blend function used in blended transparency and 
	 * antialiasing operations. The destination function specifies the factor 
	 * that is multiplied by the destination color; this value is added to the 
	 * product of the source factor and the source color.
	 * @param blendDstFunc the destination blend function to set
	 */
	public void setBlendDstFunc(BlendDstFunc blendDstFunc) {
		if (this.blendDstFunc != blendDstFunc) {
			this.blendDstFunc = blendDstFunc;
			stateChanged();
		}
	}

	/**
	 * Gets the destination blend function.
	 * @return the destination blend function
	 */
	public BlendDstFunc getBlendDstFunc() {
		return blendDstFunc;
	}

	/**
	 * Enables or disables the depth test.
	 * @param depthTestEnabled true to enable, false to disable
	 */
	public void setDepthTestEnabled(boolean depthTestEnabled) {
		if (this.depthTestEnabled != depthTestEnabled) {
			this.depthTestEnabled = depthTestEnabled;
			stateChanged();
		}
	}

	/**
	 * Checks if depth test is enabled.
	 * @return true if enabled, otherwise false
	 */
	public boolean isDepthTestEnabled() {
		return depthTestEnabled;
	}

	/**
	 * Set depth test function. This function is used to compare each incoming
	 * (source) per-pixel depth test value with the stored per-pixel depth 
	 * value in the frame buffer. If the test passes, the pixel is written, 
	 * otherwise the pixel is not written. 
	 * @param depthFunc the depth function to set
	 */
	public void setDepthFunc(DepthFunc depthFunc) {
		if (this.depthFunc != depthFunc) {
			this.depthFunc = depthFunc;
			stateChanged();
		}
	}

	/**
	 * Gets the depth function.
	 * @return the depth function
	 */
	public DepthFunc getDepthFunc() {
		return depthFunc;
	}

	/**
	 * Enables or disables writing the depth buffer for this object.
	 * @param depthWriteEnabled true to enable, false to disable
	 */
	public void setDepthWriteEnabled(boolean depthWriteEnabled) {
		if (this.depthWriteEnabled != depthWriteEnabled) {
			this.depthWriteEnabled = depthWriteEnabled;
			stateChanged();
		}
	}

	/**
	 * Checks if writing the depth buffer is enabled.
	 * @return true if enabled, otherwise false
	 */
	public boolean isDepthWriteEnabled() {
		return depthWriteEnabled;
	}

	/**
	 * @param stencilTestEnabled the stencilTestEnabled to set
	 */
	public void setStencilTestEnabled(boolean stencilTestEnabled) {
		this.stencilTestEnabled = stencilTestEnabled;
		stateChanged();
	}

	/**
	 * @return the stencilTestEnabled
	 */
	public boolean isStencilTestEnabled() {
		return stencilTestEnabled;
	}

	/**
	 * @param stencilFuncFront the stencilFuncFront to set
	 */
	public void setStencilFuncFront(StencilFunc stencilFuncFront) {
		this.stencilFuncFront = stencilFuncFront;
		stateChanged();
	}

	/**
	 * @return the stencilFuncFront
	 */
	public StencilFunc getStencilFuncFront() {
		return stencilFuncFront;
	}

	/**
	 * @param stencilRefFront the stencilRefFront to set
	 */
	public void setStencilRefFront(int stencilRefFront) {
		this.stencilRefFront = stencilRefFront;
		stateChanged();
	}

	/**
	 * @return the stencilRefFront
	 */
	public int getStencilRefFront() {
		return stencilRefFront;
	}

	/**
	 * @param stencilMaskFront the stencilMaskFront to set
	 */
	public void setStencilMaskFront(int stencilMaskFront) {
		this.stencilMaskFront = stencilMaskFront;
		stateChanged();
	}

	/**
	 * @return the stencilMaskFront
	 */
	public int getStencilMaskFront() {
		return stencilMaskFront;
	}

	/**
	 * @param stencilWriteMaskFront the stencilWriteMaskFront to set
	 */
	public void setStencilWriteMaskFront(int stencilWriteMaskFront) {
		this.stencilWriteMaskFront = stencilWriteMaskFront;
		stateChanged();
	}

	/**
	 * @return the stencilWriteMaskFront
	 */
	public int getStencilWriteMaskFront() {
		return stencilWriteMaskFront;
	}

	/**
	 * @param stencilFailFront the stencilFailFront to set
	 */
	public void setStencilFailFront(StencilOp stencilFailFront) {
		this.stencilFailFront = stencilFailFront;
		stateChanged();
	}

	/**
	 * @return the stencilFailFront
	 */
	public StencilOp getStencilFailFront() {
		return stencilFailFront;
	}

	/**
	 * @param stencilDepthFailFront the stencilDepthFailFront to set
	 */
	public void setStencilDepthFailFront(StencilOp stencilDepthFailFront) {
		this.stencilDepthFailFront = stencilDepthFailFront;
		stateChanged();
	}

	/**
	 * @return the stencilDepthFailFront
	 */
	public StencilOp getStencilDepthFailFront() {
		return stencilDepthFailFront;
	}

	/**
	 * @param stencilDepthPassFront the stencilDepthPassFront to set
	 */
	public void setStencilDepthPassFront(StencilOp stencilDepthPassFront) {
		this.stencilDepthPassFront = stencilDepthPassFront;
		stateChanged();
	}

	/**
	 * @return the stencilDepthPassFront
	 */
	public StencilOp getStencilDepthPassFront() {
		return stencilDepthPassFront;
	}

	/**
	 * @param stencilFuncBack the stencilFuncBack to set
	 */
	public void setStencilFuncBack(StencilFunc stencilFuncBack) {
		this.stencilFuncBack = stencilFuncBack;
		stateChanged();
	}

	/**
	 * @return the stencilFuncBack
	 */
	public StencilFunc getStencilFuncBack() {
		return stencilFuncBack;
	}

	/**
	 * @param stencilRefBack the stencilRefBack to set
	 */
	public void setStencilRefBack(int stencilRefBack) {
		this.stencilRefBack = stencilRefBack;
		stateChanged();
	}

	/**
	 * @return the stencilRefBack
	 */
	public int getStencilRefBack() {
		return stencilRefBack;
	}

	/**
	 * @param stencilMaskBack the stencilMaskBack to set
	 */
	public void setStencilMaskBack(int stencilMaskBack) {
		this.stencilMaskBack = stencilMaskBack;
		stateChanged();
	}

	/**
	 * @return the stencilMaskBack
	 */
	public int getStencilMaskBack() {
		return stencilMaskBack;
	}

	/**
	 * @param stencilWriteMaskBack the stencilWriteMaskBack to set
	 */
	public void setStencilWriteMaskBack(int stencilWriteMaskBack) {
		this.stencilWriteMaskBack = stencilWriteMaskBack;
		stateChanged();
	}

	/**
	 * @return the stencilWriteMaskBack
	 */
	public int getStencilWriteMaskBack() {
		return stencilWriteMaskBack;
	}

	/**
	 * @param stencilFailBack the stencilFailBack to set
	 */
	public void setStencilFailBack(StencilOp stencilFailBack) {
		this.stencilFailBack = stencilFailBack;
		stateChanged();
	}

	/**
	 * @return the stencilFailBack
	 */
	public StencilOp getStencilFailBack() {
		return stencilFailBack;
	}

	/**
	 * @param stencilDepthFailBack the stencilDepthFailBack to set
	 */
	public void setStencilDepthFailBack(StencilOp stencilDepthFailBack) {
		this.stencilDepthFailBack = stencilDepthFailBack;
		stateChanged();
	}

	/**
	 * @return the stencilDepthFailBack
	 */
	public StencilOp getStencilDepthFailBack() {
		return stencilDepthFailBack;
	}

	/**
	 * @param stencilDepthPassBack the stencilDepthPassBack to set
	 */
	public void setStencilDepthPassBack(StencilOp stencilDepthPassBack) {
		this.stencilDepthPassBack = stencilDepthPassBack;
		stateChanged();
	}

	/**
	 * @return the stencilDepthPassBack
	 */
	public StencilOp getStencilDepthPassBack() {
		return stencilDepthPassBack;
	}

	/**
	 * @param alphaTestEnabled the alphaTestEnabled to set
	 */
	public void setAlphaTestEnabled(boolean alphaTestEnabled) {
		this.alphaTestEnabled = alphaTestEnabled;
		stateChanged();
	}

	/**
	 * @return the alphaTestEnabled
	 */
	public boolean isAlphaTestEnabled() {
		return alphaTestEnabled;
	}

	/**
	 * @param alphaTestFunc the alphaTestFunc to set
	 */
	public void setAlphaTestFunc(AlphaTestFunc alphaTestFunc) {
		this.alphaTestFunc = alphaTestFunc;
		stateChanged();
	}

	/**
	 * @return the alphaTestFunc
	 */
	public AlphaTestFunc getAlphaTestFunc() {
		return alphaTestFunc;
	}

	/**
	 * @param alphaTestRef the alphaTestRef to set
	 */
	public void setAlphaTestRef(float alphaTestRef) {
		this.alphaTestRef = alphaTestRef;
		stateChanged();
	}

	/**
	 * @return the alphaTestRef
	 */
	public float getAlphaTestRef() {
		return alphaTestRef;
	}
	
	/**
	 * Sets the material object to the specified object. Setting it to null disables lighting. 
	 * @param material object that specifies the desired material properties
	 */
	public void setMaterial(Material material) {
		if (this.material != null) {
			this.material.owners.remove(this);
		}
		if (material != null) {
			material.owners.add(this);
		}
		this.material = material;
		stateChanged();
	}
	
	/**
	 * Gets a reference to the current material object.
	 * @return a reference to the current material object
	 */
	public Material getMaterial() {
		return material;
	}
	
	/**
	 * The material properites.
	 *  
	 * @author tombr
	 *
	 */
	public static class Material implements Serializable {

		private static final long serialVersionUID = 0L;
		
		/** States that references this object */
		ArrayList<State> owners = new ArrayList<State>();
		
		private Color3f ambientColor = new Color3f(.2f, .2f, .2f);
		private Color3f emissionColor = new Color3f(0, 0, 0);
		private Color4f diffuseColor = new Color4f(.8f, .8f, .8f, 1f);
		private Color3f specularColor = new Color3f(0, 0, 0);
		private float shininess = 0;
		
		/**
		 * Sets this material's ambient color.
		 * @param color the material's ambient color
		 */
		public void setAmbientColor(Color3f color) {
			this.ambientColor.set(color);
			for (int ownerIdx=0; ownerIdx<owners.size(); ownerIdx++) {
				owners.get(ownerIdx).stateChanged();
			}
		}
		
		/**
		 * Gets this material's ambient color. 
		 * @return a reference to this material's ambient color
		 */
		public Color3f getAmbientColor() {
			return ambientColor;
		}
		
		/**
		 * Sets this material's diffuse color.
		 * @param color the material's diffuse color
		 */
		public void setDiffuseColor(Color4f color) {
			this.diffuseColor.set(color);
			for (int ownerIdx=0; ownerIdx<owners.size(); ownerIdx++) {
				owners.get(ownerIdx).stateChanged();
			}
		}
		
		/**
		 * Gets this material's diffuse color. 
		 * @return a reference to this material's diffuse color
		 */
		public Color4f getDiffuseColor() {
			return diffuseColor;
		}
		
		/**
		 * Sets this material's specular color.
		 * @param color the material's specular color
		 */
		public void setSpecularColor(Color3f color) {
			this.specularColor.set(color);
			for (int ownerIdx=0; ownerIdx<owners.size(); ownerIdx++) {
				owners.get(ownerIdx).stateChanged();
			}
		}
		
		/**
		 * Gets this material's specular color. 
		 * @return a reference to this material's specular color
		 */
		public Color3f getSpecularColor() {
			return specularColor;
		}
		
		/**
		 * Sets this material's emission color.
		 * @param color the material's emission color
		 */
		public void setEmissionColor(Color3f color) {
			this.emissionColor.set(color);
			for (int ownerIdx=0; ownerIdx<owners.size(); ownerIdx++) {
				owners.get(ownerIdx).stateChanged();
			}
		}
		
		/**
		 * Gets this material's emission color. 
		 * @return a reference to this material's emission color
		 */
		public Color3f getEmissionColor() {
			return emissionColor;
		}
		
		/**
		 * Sets this material's shininess. This specifies a material specular 
		 * scattering exponent, or shininess. It takes a floating point number 
		 * in the range [1.0, 128.0] with 1.0 being not shiny and 128.0 being 
		 * very shiny.
		 * @param shininess the material's shininess 
		 */
		public void setShininess(float shininess) {
			this.shininess = shininess;

			for (int ownerIdx=0; ownerIdx<owners.size(); ownerIdx++) {
				owners.get(ownerIdx).stateChanged();
			}
		}
		
		/**
		 * Gets this material's shininess.  
		 * @return the material's shininess 
		 */
		public float getShininess() {
			return shininess;
		}		
	}
	
}
