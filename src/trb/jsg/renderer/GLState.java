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

package trb.jsg.renderer;

import java.nio.FloatBuffer;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Util;

import trb.jsg.LightState;
import trb.jsg.Shader;
import trb.jsg.Shape;
import trb.jsg.State;
import trb.jsg.State.StencilFuncParams;
import trb.jsg.State.StencilOpParams;
import trb.jsg.Uniform;
import trb.jsg.Unit;
import trb.jsg.enums.*;
import trb.jsg.util.GLUtils;
import trb.jsg.util.Mat4;

/**
 * Wraps the OpenGL state. A diff can be used to only apply the state that has 
 * changed.
 * 
 * @author tombr
 *
 */
class GLState {

	public static Shape prevShape = null;
	public static int currentProgram = 0;
	public static int activeTexture = GL_TEXTURE0;
	
	// culling state
	public static boolean cullEnabled = false;
	public static Face cullFace = Face.BACK;
	public static FrontFace frontFace = FrontFace.CCW;
	public static boolean depthTestEnabled = true;
	public static DepthFunc depthFunc = DepthFunc.LESS;
	public static boolean depthWriteEnabled = true;
	
	public static boolean blendEnabled = false;
	public static BlendSrcFunc blendSrcFunc = BlendSrcFunc.ONE;
	public static BlendDstFunc blendDstFunc = BlendDstFunc.ZERO;
	
	public static long clientState = 0;
	public static long textureUnitEnable = 0;
	
	public static Mat4 modelWorldMatrix = new Mat4();
	
	public static TextureUnitState[] glUnits;
	
	private static boolean stencilTestEnabled = false;
    private static StencilFuncParams stencilFuncFront = State.DEFAULT_STENCIL_FUNC;
    private static StencilOpParams stencilOpFront = State.DEFAULT_STENCIL_OP;
    private static int stencilMaskFront = 0xff;

    private static StencilFuncParams stencilFuncBack = State.DEFAULT_STENCIL_FUNC;
    private static StencilOpParams stencilOpBack = State.DEFAULT_STENCIL_OP;
    private static int stencilMaskBack = 0xff;
	
	private static boolean alphaTestEnabled = false;
	private static AlphaTestFunc alphaTestFunc = AlphaTestFunc.ALWAYS;
	private static float alphaTestRef = 0;
	
	private static boolean polygonOffsetFillEnabled = false;
	private static float polygonOffsetFactor = 0;
	private static float polygonOffsetUnits = 0;

    private static PolygonMode polygonMode = PolygonMode.FILL;
    private static float lineWidth = 1f;
    private static boolean lineSmooth = false;
	
	private static final State.Material DEFAULT_MATERIAL = new State.Material();
	private static boolean isMaterialSet = false;
	private static State.Material material = new State.Material();
	private static FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(4);

	private static boolean[] lightEnabled;
	
	
	public static void init() {
		if (glUnits == null) {
			modelWorldMatrix.setIdentity();

			// GL_MAX_TEXTURE_IMAGE_UNITS params returns one value, the maximum
			// supported texture image units that can be used to access texture
			// maps from the fragment shader.
			// GL_MAX_TEXTURE_UNITS
			int unitCount = GLUtils.getInteger(GL_MAX_TEXTURE_UNITS);
			glUnits = new TextureUnitState[unitCount];
			for (int i=0; i<glUnits.length; i++) {
				glUnits[i] = new TextureUnitState();
			}
			
			apply();
			
			int lightCount = GLUtils.getInteger(GL_MAX_LIGHTS);
			lightEnabled = new boolean[lightCount];
		}
	}

	/**
	 * Static class.
	 */
	private GLState() {
	}
	
	/**
	 * Apply the light state.
	 * @param lightState the light state to apply
	 */
	public static void applyLights(LightState lightState) {
		for (int i=0; i<lightEnabled.length; i++) {
			if (lightEnabled[i] != (lightState.lights.get(i) != null)) {
				lightEnabled[i] = (lightState.lights.get(i) != null);
				setEnable(GL_LIGHT0+i, lightEnabled[i]);
			}
			if (lightEnabled[i]) {
				LightState.Light light = lightState.lights.get(i);
				setLightProperty(i, GL_AMBIENT, light.ambient.x, light.ambient.y, light.ambient.z, 1);
				setLightProperty(i, GL_DIFFUSE, light.diffuse.x, light.diffuse.y, light.diffuse.z, 1);
				setLightProperty(i, GL_SPECULAR, light.specular.x, light.specular.y, light.specular.z, 1);
				setLightProperty(i, GL_POSITION, light.position.x, light.position.y, light.position.z, light.position.w);
				setLightProperty(i, GL_SPOT_DIRECTION, light.spotDirection.x, light.spotDirection.y, light.spotDirection.z, 1);
				setLightProperty(i, GL_SPOT_EXPONENT, light.spotExponent, 0, 0, 0);
				setLightProperty(i, GL_SPOT_CUTOFF, light.spotCutoff, 0, 0, 0);
				setLightProperty(i, GL_CONSTANT_ATTENUATION, light.constantAttenuation, 0, 0, 0);
				setLightProperty(i, GL_LINEAR_ATTENUATION, light.linearAttenuation, 0, 0, 0);
				setLightProperty(i, GL_QUADRATIC_ATTENUATION, light.quadratiqAttenuation, 0, 0, 0);
			}
		}
	}

	/**
	 * Sets a light property.
	 * @param lightIdx zero based index into lights
	 * @param pname name of property
	 * @param x the first parameter
	 * @param y the second parameter
	 * @param z the third parameter
	 * @param w the fourth parameter
	 */
	private static void setLightProperty(int lightIdx, int pname, float x, float y, float z, float w) {
		colorBuffer.put(0, x);
		colorBuffer.put(1, y);
		colorBuffer.put(2, z);
		colorBuffer.put(3, w);
		glLight(GL_LIGHT0+lightIdx, pname, colorBuffer);
	}
	

	/**
	 * Checks if the shape has the same state as the previous applied shape.
	 * @param shape the shape to test
	 * @return true if shape is equal to the previously applied shape
	 */
	public static boolean isEqual(Shape shape) {
		if (!RetainedSceneGraph.SAFE_MODE && (prevShape != null) 
				&& (prevShape.getState().getHash() == shape.getState().getHash())) {
			return true;
		}
		
		return false;
	}

	/**
	 * Applies the shapes state. All state are set even if the do not change.
	 * @param shape contains the state to set
	 */
	public static void apply(Shape shape) {
		State state = shape.getState();
		currentProgram = (state.getShader() == null) ? 0 : ((RetainedShader) state.getShader().getShaderProgram().nativePeer).programId;
		blendEnabled = state.isBlendEnabled();
		blendSrcFunc = state.getBlendSrcFunc();
		blendDstFunc = state.getBlendDstFunc();
		depthTestEnabled = state.isDepthTestEnabled();
		depthFunc = state.getDepthFunc();
		depthWriteEnabled = state.isDepthWriteEnabled();
		cullEnabled = state.isCullEnabled();
		cullFace = state.getCullFace();
		frontFace = state.getFrontFace();
		
		stencilTestEnabled = state.isStencilTestEnabled();
        stencilFuncFront = state.getStencilFuncFront();
        stencilOpFront = state.getStencilOpFront();
        stencilMaskFront = state.getStencilMaskFront();
        stencilFuncBack = state.getStencilFuncBack();
        stencilOpBack = state.getStencilOpBack();
        stencilMaskBack = state.getStencilMaskBack();

		alphaTestEnabled = state.isAlphaTestEnabled();
		alphaTestFunc = state.getAlphaTestFunc();
		alphaTestRef = state.getAlphaTestRef();
		
		polygonOffsetFillEnabled = state.polygonOffsetFillEnabled;
		polygonOffsetFactor = state.polygonOffsetFactor;
		polygonOffsetUnits = state.polygonOffsetUnits;

        glPolygonMode(GL_FRONT_AND_BACK, state.getPolygonMode().get());
        glLineWidth(state.getLineWidth());
        setEnable(GL_LINE_SMOOTH, state.getLineSmooth());

		State.Material m = ((state.getMaterial() != null) ? state.getMaterial() : DEFAULT_MATERIAL);
		material.setAmbientColor(m.getAmbientColor());
		material.setDiffuseColor(m.getDiffuseColor());
		material.setEmissionColor(m.getEmissionColor());
		material.setSpecularColor(m.getSpecularColor());
		material.setShininess(m.getShininess());
		
		for (int unitIdx=0; unitIdx<glUnits.length; unitIdx++) {
			Unit shapeUnit = state.getUnit(unitIdx);
			if (shapeUnit == null) {
				shapeUnit = Unit.disabledUnit;
			}
			TextureUnitState glUnit = glUnits[unitIdx];
			glUnit.enabled = shapeUnit.isEnabled();
            if (shapeUnit.getTexture() != null) {
                glUnit.enabledType = shapeUnit.getTexture().getType().get();
                glUnit.bindId = ((RetainedTexture) shapeUnit.getTexture().nativePeer).getTextureId();
            } else {
                glUnit.enabledType = GL_TEXTURE_2D;
                glUnit.bindId = 0;
            }
			glUnit.envMode = shapeUnit.getTextureEnvMode().get();
			glUnit.combineFuncRGB = shapeUnit.getCombineFuncRGB().get();
			glUnit.combineFuncAlpha = shapeUnit.getCombineFuncAlpha().get();
			glUnit.combineSrc0RGB = shapeUnit.getCombineSrc0RGB().get();
			glUnit.combineSrc1RGB = shapeUnit.getCombineSrc1RGB().get();
			glUnit.combineSrc2RGB = shapeUnit.getCombineSrc2RGB().get();
			glUnit.combineSrc0Alpha = shapeUnit.getCombineSrc0Alpha().get();
			glUnit.combineSrc1Alpha = shapeUnit.getCombineSrc1Alpha().get();
			glUnit.combineSrc2Alpha = shapeUnit.getCombineSrc2Alpha().get();
			glUnit.combineOperand0RGB = shapeUnit.getCombineOperand0RGB().get();
			glUnit.combineOperand1RGB = shapeUnit.getCombineOperand1RGB().get();
			glUnit.combineOperand2RGB = shapeUnit.getCombineOperand2RGB().get();
			glUnit.combineOperand0Alpha = shapeUnit.getCombineOperand0Alpha().get();
			glUnit.combineOperand1Alpha = shapeUnit.getCombineOperand1Alpha().get();
			glUnit.combineOperand2Alpha = shapeUnit.getCombineOperand2Alpha().get();
			glUnit.combineScaleRGB = shapeUnit.getCombineScaleRGB().get();
			glUnit.combineScaleAlpha = shapeUnit.getCombineScaleAlpha().get();
			// texture generation
			for (int i=0; i<4; i++) {
				glUnit.textureGen[i] = shapeUnit.isTexGenEnabled(TextureCoordinate.COORDS[i]);
				glUnit.textureGenMode[i] = shapeUnit.getTexGenMode(TextureCoordinate.COORDS[i]).get();
			}
		}
		
		apply();
	}

	/**
	 * Sets the all the opengl states to match this class.
	 */
	private static void apply() {
		Util.checkGLError();
		glUseProgram(currentProgram);
		
		for (int unitIdx=0; unitIdx<glUnits.length; unitIdx++) {
			glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
			TextureUnitState glUnit = glUnits[unitIdx];
			setEnable(glUnit.enabledType, glUnit.enabled);
			glBindTexture(glUnit.enabledType, glUnit.bindId);
			glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, glUnit.envMode);
			glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, glUnit.combineFuncRGB);
			glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, glUnit.combineFuncAlpha);
			glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB, glUnit.combineSrc0RGB);
			glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_RGB, glUnit.combineSrc1RGB);
			glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_RGB, glUnit.combineSrc2RGB);
			glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA, glUnit.combineSrc0Alpha);
			glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_ALPHA, glUnit.combineSrc1Alpha);
			glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_ALPHA, glUnit.combineSrc2Alpha);
			glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, glUnit.combineOperand0RGB);
			glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, glUnit.combineOperand1RGB);
			glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_RGB, glUnit.combineOperand2RGB);
			glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, glUnit.combineOperand0Alpha);
			glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_ALPHA, glUnit.combineOperand1Alpha);
			glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_ALPHA, glUnit.combineOperand2Alpha);
			glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, glUnit.combineScaleRGB);
			glTexEnvf(GL_TEXTURE_ENV, GL_ALPHA_SCALE, glUnit.combineScaleAlpha);
			for (int i=0; i<4; i++) {
				setEnable(GL_TEXTURE_GEN_S+i, glUnit.textureGen[i]);
				glTexGeni(GL_S+i, GL_TEXTURE_GEN_MODE, glUnit.textureGenMode[i]);
			}
		}
		
		// set depth test
		setEnable(GL_DEPTH_TEST, depthTestEnabled);
		glDepthFunc(depthFunc.get());

		// set depth write
		glDepthMask(depthWriteEnabled);			
		
		// setup blend state
		setEnable(GL_BLEND, blendEnabled);
		glBlendFunc(blendSrcFunc.get(), blendDstFunc.get());
		
		// setup cull state
		setEnable(GL_CULL_FACE, cullEnabled);
		glCullFace(cullFace.get());
		glFrontFace(frontFace.get());

		// stencil test
		setEnable(GL_STENCIL_TEST, stencilTestEnabled);
        stencilFuncFront.apply(Face.FRONT);
        stencilFuncBack.apply(Face.BACK);
        stencilOpFront.apply(Face.FRONT);
        stencilOpBack.apply(Face.BACK);
        glStencilMaskSeparate(GL_FRONT, stencilMaskFront);
        glStencilMaskSeparate(GL_BACK, stencilMaskBack);
		
		// alpha test
		setEnable(GL_ALPHA_TEST, alphaTestEnabled);
		glAlphaFunc(alphaTestFunc.get(), alphaTestRef);

		setEnable(GL_POLYGON_OFFSET_FILL, polygonOffsetFillEnabled);
		glPolygonOffset(polygonOffsetFactor, polygonOffsetUnits);

        glPolygonMode(GL_FRONT_AND_BACK, polygonMode.get());
        glLineWidth(lineWidth);
        setEnable(GL_LINE_SMOOTH, lineSmooth);
		
		setMaterialColor(GL_DIFFUSE, material.getDiffuseColor());
		setMaterialColor(GL_AMBIENT, material.getAmbientColor());
		setMaterialColor(GL_EMISSION, material.getEmissionColor());
		setMaterialColor(GL_SPECULAR, material.getSpecularColor());
		glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, material.getShininess());
		
		Util.checkGLError();
	}
	
	/**
	 * Sets the front and back material 3 component color.
	 * @param pname name of the material property
	 * @param color the color to set
	 */
	private static void setMaterialColor(int pname, Color3f color) {
		colorBuffer.put(0, color.x);
		colorBuffer.put(1, color.y);
		colorBuffer.put(2, color.z);
		colorBuffer.put(3, 1f);
		glMaterial(GL_FRONT_AND_BACK, pname, colorBuffer);
	}
	
	/**
	 * Sets the front and back material 4 component color.
	 * @param pname name of the material property
	 * @param color the color to set
	 */
	private static void setMaterialColor(int pname, Color4f color) {
		colorBuffer.put(0, color.x);
		colorBuffer.put(1, color.y);
		colorBuffer.put(2, color.z);
		colorBuffer.put(3, color.w);
		glMaterial(GL_FRONT_AND_BACK, pname, colorBuffer);
	}
	
	/**
	 * Enables or disables the specified state.
	 * @param flag the OpenGL id of the state (Example: GL_DEPTH_TEST, GL_ALPHA_TEST) 
	 * @param value true to enable, false to disable
	 */
	private static void setEnable(int flag, boolean value) {
		if (value) {
			glEnable(flag);
		} else {
			glDisable(flag);
		}
	}

	/**
	 * Applies the shapes state by only setting the state that has changed. 
	 * @param shape contains the shape to set
	 */
	public static void applyDif(Shape shape) {
		if (RetainedSceneGraph.SAFE_MODE) {
			prevShape = shape;
			apply(shape);
			return;
		}
		prevShape = shape;
		State state = shape.getState();
		
		// shader program
        int shapeShaderId = 0;
        if (state.getShader() != null) {
            shapeShaderId = ((RetainedShader) state.getShader().getShaderProgram().nativePeer).programId;
        }
        if (currentProgram != shapeShaderId) {
            currentProgram = shapeShaderId;
            glUseProgram(currentProgram);
        }
        Util.checkGLError();
		
		// blend
		if (blendEnabled != state.isBlendEnabled()) {
			blendEnabled = state.isBlendEnabled();
			setEnable(GL_BLEND, blendEnabled);
		}
		if (blendEnabled && (state.getBlendSrcFunc() != blendSrcFunc || state.getBlendDstFunc() != blendDstFunc)) {
			blendSrcFunc = state.getBlendSrcFunc();
			blendDstFunc = state.getBlendDstFunc();
			glBlendFunc(blendSrcFunc.get(), blendDstFunc.get());
		}

		// depth test
		if (depthTestEnabled != state.isDepthTestEnabled()) {
			depthTestEnabled = state.isDepthTestEnabled();
			setEnable(GL_DEPTH_TEST, depthTestEnabled);
		}
		if (depthTestEnabled && depthFunc != state.getDepthFunc()) {
			depthFunc = state.getDepthFunc();
			glDepthFunc(depthFunc.get());
		}		
		
		// depth mask
        applyDepthMaskDif(state.isDepthWriteEnabled());

		// cull and front face
		if (cullEnabled != state.isCullEnabled()) {
			cullEnabled = state.isCullEnabled();
			setEnable(GL_CULL_FACE, cullEnabled);
		}
		if (cullEnabled) {
			if (cullFace != state.getCullFace()) {
				cullFace = state.getCullFace();
				glCullFace(cullFace.get());
			}
			if (frontFace != state.getFrontFace()) {
				frontFace = state.getFrontFace();
				glFrontFace(frontFace.get());
			}
		}
		
		// stencil test
		if (stencilTestEnabled != state.isStencilTestEnabled()) {
			stencilTestEnabled = state.isStencilTestEnabled();
			setEnable(GL_STENCIL_TEST, stencilTestEnabled);
		}
		if (stencilTestEnabled) {
            // front
            if (!stencilFuncFront.equals(state.getStencilFuncFront())) {
                stencilFuncFront = state.getStencilFuncFront();
                stencilFuncFront.apply(Face.FRONT);
            }
            if (!stencilOpFront.equals(state.getStencilOpFront())) {
                stencilOpFront = state.getStencilOpFront();
                stencilOpFront.apply(Face.FRONT);
            }
            if (stencilMaskFront != state.getStencilMaskFront()) {
                stencilMaskFront = state.getStencilMaskFront();
                glStencilMaskSeparate(GL_FRONT, stencilMaskFront);
            }
			
			// back
            if (!stencilFuncBack.equals(state.getStencilFuncBack())) {
                stencilFuncBack = state.getStencilFuncBack();
                stencilFuncBack.apply(Face.BACK);
            }
            if (!stencilOpBack.equals(state.getStencilOpBack())) {
                stencilOpBack = state.getStencilOpBack();
                stencilOpBack.apply(Face.BACK);
            }
            if (stencilMaskBack != state.getStencilMaskBack()) {
                stencilMaskBack = state.getStencilMaskBack();
                glStencilMaskSeparate(GL_BACK, stencilMaskBack);
            }
		}
		
		// alpha test
		if (alphaTestEnabled != state.isAlphaTestEnabled()) {
			alphaTestEnabled = state.isAlphaTestEnabled();
			setEnable(GL_ALPHA_TEST, alphaTestEnabled);
		}
		if (alphaTestEnabled 
				&& (alphaTestFunc != state.getAlphaTestFunc() 
					|| alphaTestRef != state.getAlphaTestRef())) {
			alphaTestFunc = state.getAlphaTestFunc();
			alphaTestRef = state.getAlphaTestRef();
			glAlphaFunc(alphaTestFunc.get(), alphaTestRef);
		}

		// polygon offset
		if (polygonOffsetFillEnabled != state.polygonOffsetFillEnabled) {
			polygonOffsetFillEnabled = state.polygonOffsetFillEnabled;
			setEnable(GL_POLYGON_OFFSET_FILL, polygonOffsetFillEnabled);
		}
		if (polygonOffsetFillEnabled 
				&& (polygonOffsetFactor != state.polygonOffsetFactor
					|| polygonOffsetUnits != state.polygonOffsetUnits)) {
			polygonOffsetFactor = state.polygonOffsetFactor;
			polygonOffsetUnits = state.polygonOffsetUnits;
			glPolygonOffset(polygonOffsetFactor, polygonOffsetUnits);
		}

        if (polygonMode != state.getPolygonMode()) {
            polygonMode = state.getPolygonMode();
            glPolygonMode(GL_FRONT_AND_BACK, polygonMode.get());
        }

        if (lineWidth != state.getLineWidth()) {
            lineWidth = state.getLineWidth();
            glLineWidth(lineWidth);
        }

        if (lineSmooth != state.getLineSmooth()) {
            lineSmooth = state.getLineSmooth();
            setEnable(GL_LINE_SMOOTH, lineSmooth);
        }

		// enable lighting if material is set
		if (isMaterialSet != (state.getMaterial() != null)) {
			isMaterialSet = (state.getMaterial() != null);
			setEnable(GL_LIGHTING, isMaterialSet);
		}
		
		// material
		if (isMaterialSet) {
			State.Material m = state.getMaterial();
			if (!material.getAmbientColor().equals(m.getAmbientColor())) {
				material.setAmbientColor(m.getAmbientColor());
				setMaterialColor(GL_AMBIENT, material.getAmbientColor());
			}
			if (!material.getDiffuseColor().equals(m.getDiffuseColor())) {
				material.setDiffuseColor(m.getDiffuseColor());
				setMaterialColor(GL_DIFFUSE, material.getDiffuseColor());
			}
			if (!material.getEmissionColor().equals(m.getEmissionColor())) {
				material.setEmissionColor(m.getEmissionColor());
				setMaterialColor(GL_EMISSION, material.getEmissionColor());
			}
			if (!material.getSpecularColor().equals(m.getSpecularColor())) {
				material.setSpecularColor(m.getSpecularColor());
				setMaterialColor(GL_SPECULAR, material.getSpecularColor());
			}
			if (material.getShininess() != m.getShininess()) {
				material.setShininess(m.getShininess());
				glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, material.getShininess());
			}
		}
		
		// texture units
		for (int unitIdx=0; unitIdx<glUnits.length; unitIdx++) {
			TextureUnitState glUnit = glUnits[unitIdx];
			Unit shapeUnit = state.getUnit(unitIdx);
			if (shapeUnit == null) {
				shapeUnit = Unit.disabledUnit;
			}
			
			applyTextureUnitDif(glUnit, shapeUnit, unitIdx);
		}
		Util.checkGLError();
	}

    public static void applyDepthMaskDif(boolean b) {
        if (depthWriteEnabled != b) {
            depthWriteEnabled = b;
            glDepthMask(depthWriteEnabled);
        }
    }

	/**
	 * Applies the shapes state by only setting the state that has changed. 
	 * @param shape contains the shape to set
	 */
	
	/**
	 * Applies the specified texture unit state.  
	 * @param glUnit the current unit state
	 * @param shapeUnit the shape unit state to set
	 * @param unitIdx the index of the unit
	 */
	private static void applyTextureUnitDif(TextureUnitState glUnit, Unit shapeUnit, int unitIdx) {
		if (shapeUnit.isEnabled()) {
			if (!glUnit.enabled) {
				glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
				glEnable(shapeUnit.getTexture().getType().get());					
				glUnit.enabled = true;
				glUnit.enabledType = shapeUnit.getTexture().getType().get();
			} else if (shapeUnit.getTexture().getType().get() != glUnit.enabledType) {
				glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
				glDisable(glUnit.enabledType);					
				glEnable(shapeUnit.getTexture().getType().get());
				glUnit.enabledType = shapeUnit.getTexture().getType().get();
			}
			
			if (shapeUnit.getTexture().nativePeer == null) {
				System.err.println("texture peer is null "+unitIdx+" "+shapeUnit.getTexture());
				Thread.dumpStack();
				System.exit(0);
			}
			
			int textureId = ((RetainedTexture) shapeUnit.getTexture().nativePeer).getTextureId(); 
			if (textureId != glUnit.bindId) {
				glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
				glBindTexture(shapeUnit.getTexture().getType().get(), textureId);
				glUnit.bindId = textureId;
			}
			if (shapeUnit.getTextureEnvMode().get() != glUnit.envMode) {
				glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
				glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, shapeUnit.getTextureEnvMode().get());
				glUnit.envMode = shapeUnit.getTextureEnvMode().get();
			}

			if (shapeUnit.getTextureEnvMode() == TextureEnvMode.COMBINE) {
				glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
				if (shapeUnit.getCombineFuncRGB().get() != glUnit.combineFuncRGB) {
					glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, shapeUnit.getCombineFuncRGB().get());
					glUnit.combineFuncRGB = shapeUnit.getCombineFuncRGB().get(); 
				}
				if (shapeUnit.getCombineFuncAlpha().get() != glUnit.combineFuncAlpha) {
					glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, shapeUnit.getCombineFuncAlpha().get());
					glUnit.combineFuncAlpha = shapeUnit.getCombineFuncAlpha().get(); 
				}
				if (shapeUnit.getCombineSrc0RGB().get() != glUnit.combineSrc0RGB) {
					glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_RGB, shapeUnit.getCombineSrc0RGB().get());
					glUnit.combineSrc0RGB = shapeUnit.getCombineSrc0RGB().get(); 
				}
				if (shapeUnit.getCombineSrc1RGB().get() != glUnit.combineSrc1RGB) {
					glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_RGB, shapeUnit.getCombineSrc1RGB().get());
					glUnit.combineSrc1RGB = shapeUnit.getCombineSrc1RGB().get(); 
				}
				if (shapeUnit.getCombineSrc2RGB().get() != glUnit.combineSrc2RGB) {
					glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_RGB, shapeUnit.getCombineSrc2RGB().get());
					glUnit.combineSrc2RGB = shapeUnit.getCombineSrc2RGB().get(); 
				}
				if (shapeUnit.getCombineSrc0Alpha().get() != glUnit.combineSrc0Alpha) {
					glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE0_ALPHA, shapeUnit.getCombineSrc0Alpha().get());
					glUnit.combineSrc0Alpha = shapeUnit.getCombineSrc0Alpha().get(); 
				}
				if (shapeUnit.getCombineSrc1Alpha().get() != glUnit.combineSrc1Alpha) {
					glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE1_ALPHA, shapeUnit.getCombineSrc1Alpha().get());
					glUnit.combineSrc1Alpha = shapeUnit.getCombineSrc1Alpha().get(); 
				}
				if (shapeUnit.getCombineSrc2Alpha().get() != glUnit.combineSrc2Alpha) {
					glTexEnvi(GL_TEXTURE_ENV, GL_SOURCE2_ALPHA, shapeUnit.getCombineSrc2Alpha().get());
					glUnit.combineSrc2Alpha = shapeUnit.getCombineSrc2Alpha().get(); 
				}
				if (shapeUnit.getCombineOperand0RGB().get() != glUnit.combineOperand0RGB) {
					glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, shapeUnit.getCombineOperand0RGB().get());
					glUnit.combineOperand0RGB = shapeUnit.getCombineOperand0RGB().get(); 
				}
				if (shapeUnit.getCombineOperand1RGB().get() != glUnit.combineOperand1RGB) {
					glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, shapeUnit.getCombineOperand1RGB().get());
					glUnit.combineOperand1RGB = shapeUnit.getCombineOperand1RGB().get(); 
				}
				if (shapeUnit.getCombineOperand2RGB().get() != glUnit.combineOperand2RGB) {
					glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_RGB, shapeUnit.getCombineOperand2RGB().get());
					glUnit.combineOperand2RGB = shapeUnit.getCombineOperand2RGB().get(); 
				}
				if (shapeUnit.getCombineOperand0Alpha().get() != glUnit.combineOperand0Alpha) {
					glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, shapeUnit.getCombineOperand0Alpha().get());
					glUnit.combineOperand0Alpha = shapeUnit.getCombineOperand0Alpha().get(); 
				}
				if (shapeUnit.getCombineOperand1Alpha().get() != glUnit.combineOperand1Alpha) {
					glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_ALPHA, shapeUnit.getCombineOperand2Alpha().get());
					glUnit.combineOperand1Alpha = shapeUnit.getCombineOperand1Alpha().get(); 
				}
				if (shapeUnit.getCombineOperand2Alpha().get() != glUnit.combineOperand2Alpha) {
					glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_ALPHA, shapeUnit.getCombineOperand2Alpha().get());
					glUnit.combineOperand2Alpha = shapeUnit.getCombineOperand2Alpha().get(); 
				}
				if (shapeUnit.getCombineScaleRGB().get() != glUnit.combineScaleRGB) {
					glTexEnvf(GL_TEXTURE_ENV, GL_RGB_SCALE, shapeUnit.getCombineScaleRGB().get());
					glUnit.combineScaleRGB = shapeUnit.getCombineScaleRGB().get(); 
				}
				if (shapeUnit.getCombineScaleAlpha().get() != glUnit.combineScaleAlpha) {
					glTexEnvf(GL_TEXTURE_ENV, GL_ALPHA_SCALE, shapeUnit.getCombineScaleAlpha().get());
					glUnit.combineScaleAlpha = shapeUnit.getCombineScaleAlpha().get(); 
				}
			}
			
			// texture generation
			for (int i=0; i<4; i++) {
				TextureCoordinate coord = TextureCoordinate.COORDS[i];
				if (shapeUnit.isTexGenEnabled(coord)) {
					if (!glUnit.textureGen[i]) {
						glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
						glEnable(GL_TEXTURE_GEN_S+i);
						glUnit.textureGen[i] = true;
					}
					int texGenMode = shapeUnit.getTexGenMode(coord).get();
					if (texGenMode != glUnit.textureGenMode[i]) {
						glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
						glTexGeni(GL_S+i, GL_TEXTURE_GEN_MODE, texGenMode);
						glUnit.textureGenMode[i] = texGenMode;
					}
				} else {
					if (glUnit.textureGen[i]) {
						glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
						glDisable(GL_TEXTURE_GEN_S+i);
						glUnit.textureGen[i] = false;
					}
				}
			}
		} else {
			if (glUnit.enabled) {
				glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
				glDisable(glUnit.enabledType);
				glUnit.enabled = false;
			}
		}
	}
//	RetainedShader simpleShaderPeer = (RetainedShader) shape.getState().getShader().getShaderProgram().nativePeer;
//	GLState.applyUniforms(simpleShaderPeer, shape.getState());
	
	/**
	 * Applies the shapes shader uniforms.
	 * @param shape the shape containing the uniforms to set
	 */
	public static void applyUniforms(Shape shape) {
		Shader shader = shape.getState().getShader();
		RetainedShader simpleShaderProgramPeer = (RetainedShader) shader.getShaderProgram().nativePeer;
		if (currentProgram != 0 && shader != null && 
			(simpleShaderProgramPeer.currentUniformSet != shader
			|| shader.changeFrameIdx > simpleShaderProgramPeer.currentUniformSetFrameIdx)) {
			// upload uniforms
			simpleShaderProgramPeer.currentUniformSet = shader;
			simpleShaderProgramPeer.currentUniformSetFrameIdx = Renderer.frameIdx;
			Uniform[] uniforms = shader.getAllUniforms();
			for (Uniform uniform : uniforms) {
				simpleShaderProgramPeer.setUniform(uniform);
			}
		}
	}

	/**
	 * Lazy sets glActiveTexture to the texture unit with the specified id.
	 * @param id the id of the texture unit to set
	 */
	public static void glActiveTextureWrapper(int id) {
		if (id != activeTexture) {
			activeTexture = id;
			glActiveTexture(id);
		}
	}
	
	/**
	 * Lazy binds a texture on the active texture unit.
	 * @param target the target (Example: GL_TEXTURE_2D)
	 * @param id the texture id to bind
	 */
	public static void glBindTextureWrapper(int target, int id) {
		int unitIdx = activeTexture - GL_TEXTURE0;
		TextureUnitState unit = glUnits[unitIdx];
		if (unit.enabled && unit.enabledType != target) {
			glDisable(unit.enabledType);
			glEnable(target);
		}
		
		glBindTexture(target, id);
		unit.enabledType = target;
		unit.bindId = id;
	}

	/**
	 * Checks if the cached state matches the opengl state.
	 */
	public static void validateState() {
		// TODO: check lights and material
		Util.checkGLError(); 
		checkInt(GL_CURRENT_PROGRAM, currentProgram, "GL_CURRENT_PROGRAM");
		checkEnabled(GL_CULL_FACE, cullEnabled, "GL_CULL_FACE");
		checkInt(GL_CULL_FACE_MODE, cullFace.get(), "GL_CULL_FACE_MODE");
		checkInt(GL_FRONT_FACE, frontFace.get(), "GL_FRONT_FACE");
		checkEnabled(GL_DEPTH_TEST, depthTestEnabled, "GL_DEPTH_TEST");
		checkInt(GL_DEPTH_FUNC, depthFunc.get(), "GL_DEPTH_FUNC");
		checkInt(GL_DEPTH_WRITEMASK, depthWriteEnabled ? GL_TRUE : GL_FALSE, "GL_DEPTH_WRITEMASK");
		checkEnabled(GL_BLEND, blendEnabled, "GL_BLEND");
		checkInt(GL_BLEND_SRC, blendSrcFunc.get(), "GL_BLEND_SRC");
		checkInt(GL_BLEND_DST, blendDstFunc.get(), "GL_BLEND_DST");
		checkInt(GL_ACTIVE_TEXTURE, activeTexture, "GL_ACTIVE_TEXTURE");
		checkEnabled(GL_STENCIL_TEST, stencilTestEnabled, "GL_STENCIL_TEST");

        checkInt(GL_STENCIL_FUNC, stencilFuncFront.func.get(), "GL_STENCIL_FUNC");
		checkInt(GL_STENCIL_REF, stencilFuncFront.ref, "GL_STENCIL_REF");
		checkInt(GL_STENCIL_VALUE_MASK, stencilFuncFront.mask, "GL_STENCIL_VALUE_MASK");
		checkInt(GL_STENCIL_WRITEMASK, stencilMaskFront, "GL_STENCIL_WRITEMASK");
		checkInt(GL_STENCIL_FAIL, stencilOpFront.sfail.get(), "GL_STENCIL_FAIL");
		checkInt(GL_STENCIL_PASS_DEPTH_FAIL, stencilOpFront.dpfail.get(), "GL_STENCIL_PASS_DEPTH_FAIL");
		checkInt(GL_STENCIL_PASS_DEPTH_PASS, stencilOpFront.dppass.get(), "GL_STENCIL_PASS_DEPTH_PASS");

        checkInt(GL_STENCIL_BACK_FUNC, stencilFuncBack.func.get(), "GL_STENCIL_BACK_FUNC");
        checkInt(GL_STENCIL_BACK_REF, stencilFuncBack.ref, "GL_STENCIL_BACK_REF");
        checkInt(GL_STENCIL_BACK_VALUE_MASK, stencilFuncBack.mask, "GL_STENCIL_BACK_VALUE_MASK");
        checkInt(GL_STENCIL_BACK_WRITEMASK, stencilMaskBack, "GL_STENCIL_BACK_WRITEMASK");
        checkInt(GL_STENCIL_BACK_FAIL, stencilOpBack.sfail.get(), "GL_STENCIL_BACK_FAIL");
        checkInt(GL_STENCIL_BACK_PASS_DEPTH_FAIL, stencilOpBack.dpfail.get(), "GL_STENCIL_BACK_PASS_DEPTH_FAIL");
        checkInt(GL_STENCIL_BACK_PASS_DEPTH_PASS, stencilOpBack.dppass.get(), "GL_STENCIL_BACK_PASS_DEPTH_PASS");

        checkEnabled(GL_ALPHA_TEST, alphaTestEnabled, "GL_ALPHA_TEST");
		checkInt(GL_ALPHA_TEST_FUNC, alphaTestFunc.get(), "GL_ALPHA_TEST_FUNC");
		checkFloat(GL_ALPHA_TEST_REF, alphaTestRef, "GL_ALPHA_TEST_REF");
		checkEnabled(GL_POLYGON_OFFSET_FILL, polygonOffsetFillEnabled, "GL_POLYGON_OFFSET_FILL");
		checkFloat(GL_POLYGON_OFFSET_FACTOR, polygonOffsetFactor, "GL_POLYGON_OFFSET_FACTOR");
		checkFloat(GL_POLYGON_OFFSET_UNITS, polygonOffsetUnits, "GL_POLYGON_OFFSET_UNITS");
		
		Util.checkGLError(); 
		
		for (int unitIdx=0; unitIdx<glUnits.length; unitIdx++) {
			TextureUnitState unit = glUnits[unitIdx];
			glActiveTextureWrapper(GL_TEXTURE0 + unitIdx);
			// TODO: check enable and bind id
			checkEnabled(unit.enabledType, unit.enabled, "GL_TEXTURE_XX "+unitIdx);
			if (unit.enabled) {
				if (unit.enabledType == GL_TEXTURE_1D) {
					checkInt(GL_TEXTURE_BINDING_1D, unit.bindId, "GL_TEXTURE_BINDING_1D");
				} else if (unit.enabledType == GL_TEXTURE_2D) {
					checkInt(GL_TEXTURE_BINDING_2D, unit.bindId, "GL_TEXTURE_BINDING_2D");
//				} else if (unit.enabledType == GL_TEXTURE_3D) {
//					checkInt(GL_TEXTURE_BINDING_3D, unit.bindId, "GL_TEXTURE_BINDING_3D");
				} else if (unit.enabledType == GL_TEXTURE_CUBE_MAP) {
					checkInt(GL_TEXTURE_BINDING_CUBE_MAP, unit.bindId, "GL_TEXTURE_BINDING_CUBE_MAP");
				}
			}
			
			checkTexEnv(GL_TEXTURE_ENV_MODE, unit.envMode, "GL_TEXTURE_ENV_MODE");
			checkTexEnv(GL_COMBINE_RGB, unit.combineFuncRGB, "GL_COMBINE_RGB");
			checkTexEnv(GL_COMBINE_ALPHA, unit.combineFuncAlpha, "GL_COMBINE_ALPHA");
			checkTexEnv(GL_SOURCE0_RGB, unit.combineSrc0RGB, "GL_SOURCE0_RGB");
			checkTexEnv(GL_SOURCE1_RGB, unit.combineSrc1RGB, "GL_SOURCE1_RGB");
			checkTexEnv(GL_SOURCE2_RGB, unit.combineSrc2RGB, "GL_SOURCE2_RGB");
			checkTexEnv(GL_SOURCE0_ALPHA, unit.combineSrc0Alpha, "GL_SOURCE0_ALPHA");
			checkTexEnv(GL_SOURCE1_ALPHA, unit.combineSrc1Alpha, "GL_SOURCE1_ALPHA");
			checkTexEnv(GL_SOURCE2_ALPHA, unit.combineSrc2Alpha, "GL_SOURCE2_ALPHA");
			checkTexEnv(GL_OPERAND0_RGB, unit.combineOperand0RGB, "GL_OPERAND0_RGB");
			checkTexEnv(GL_OPERAND1_RGB, unit.combineOperand1RGB, "GL_OPERAND1_RGB");
			checkTexEnv(GL_OPERAND2_RGB, unit.combineOperand2RGB, "GL_OPERAND2_RGB");
			checkTexEnv(GL_OPERAND0_ALPHA, unit.combineOperand0Alpha, "GL_OPERAND0_ALPHA");
			checkTexEnv(GL_OPERAND1_ALPHA, unit.combineOperand1Alpha, "GL_OPERAND1_ALPHA");
			checkTexEnv(GL_OPERAND2_ALPHA, unit.combineOperand2Alpha, "GL_OPERAND2_ALPHA");
			checkTexEnv(GL_RGB_SCALE, unit.combineScaleRGB, "GL_RGB_SCALE");
			checkTexEnv(GL_ALPHA_SCALE, unit.combineScaleAlpha, "GL_ALPHA_SCALE");
			
			for (int i=0; i<4; i++) {
				checkEnabled(GL_TEXTURE_GEN_S + i, unit.textureGen[i], "GL_TEXTURE_GEN_S+"+i);
				int value = unit.textureGenMode[i];
				int actual = GLUtils.getTexGen(GL_S + i);
				if (value != actual) {
					System.out.println("GLState incorrectly cached GL_TEXTURE_GEN_S+"+i+" cached "+value+" correct "+actual);
				}
			}
			
			Util.checkGLError(); 
		}
		Util.checkGLError(); 
	}
	
	/**
	 * Checks if the cached flag matches OpenGL.
	 * @param cap Specifies a symbolic constant indicating a GL capability.
	 * @param value the cached state
	 * @param name name of capability. Is printed if state don't match.
	 */
	private static void checkEnabled(int cap, boolean value, String name) {
		if (glIsEnabled(cap) != value) {
			System.out.println("GLState incorrectly cached "+name+" cached "+value+" correct "+!value);
		}		
	}

	/**
	 * Checks if the cached int matches OpenGL.
	 * @param cap Specifies a symbolic constant indicating a GL capability.
	 * @param value the cached state
	 * @param name name of capability. Is printed if state don't match.
	 */
	private static int checkInt(int cap, int value, String name) {
		int actual = GLUtils.getInteger(cap);
		if (value != actual) {
			System.out.println("GLState incorrectly cached "+name+" cached "+value+" correct "+actual);
			value = actual;
		}
		return value;
	}

	/**
	 * Checks if the cached float matches OpenGL.
	 * @param cap Specifies a symbolic constant indicating a GL capability.
	 * @param value the cached state
	 * @param name name of capability. Is printed if state don't match.
	 */
	private static float checkFloat(int cap, float value, String name) {
		float actual = GLUtils.getFloat(cap);
		if (value != actual) {
			System.out.println("GLState incorrectly cached "+name+" cached "+value+" correct "+actual);
			value = actual;
		}
		return value;
	}

	/**
	 * Checks if the cached texEnv matches OpenGL.
	 * @param cap Specifies a symbolic constant indicating a GL capability.
	 * @param value the cached state
	 * @param name name of capability. Is printed if state don't match.
	 */
	private static int checkTexEnv(int cap, int value, String name) {
		int actual = GLUtils.getTexEnv(cap);
		if (value != actual) {
			System.out.println("GLState incorrectly cached "+name+" cached "+value+" correct "+actual);
			value = actual;
		}
		return value;
	}
//
//	
//	private static int checkTexParameter(int id, int val, String name) {
//		int actual = GLTools.getTexParameteri(GL11.GL_TEXTURE_2D, id);
//		if (val != actual) {
//			//System.out.println(name+" incorrectly cached actual:"+actual+" was:"+val);
//			System.out.println(name+" incorrectly cached actual:"+getGLName(actual)+" was:"+getGLName(val));
//			val = actual;
//			Thread.dumpStack();
//		}
//		return val;
//	}
//
//	
//	private static float checkTexParameter(int id, float val, String name) {
//		float actual = GLTools.getTexParameterf(GL11.GL_TEXTURE_2D, id);
//		if (val != actual) {
//			System.out.println(name+" incorrectly cached actual:"+actual+" was:"+val);
//			val = actual;
//		}
//		return val;
//	}
	/**
	 * From the red book:
	 * Multitexturing introduces multiple texture untis, wich are additional
	 * texture application passes. Each texture unit has identical capabilities and
	 * houses its own texturing state, including the following:
	 * - Texture image
	 * - Filtering parameters
	 * - Environment application
	 * - Texture matrix stack
	 * - Automatic texture-coordinate generation
	 * - Vertex-array specification (if needed)
	 */
	public static class TextureUnitState {
		public int 	   bindId              = 0;
		public boolean enabled             = false;
		/** One of GL_TEXTURE_1D, GL_TEXTURE_2D, GL_TEXTURE_3D or GL_TEXTURE_CUBE_MAP */
		public int     enabledType = GL_TEXTURE_2D;
		public int     envMode             = GL_MODULATE;
		public int 	   combineFuncRGB = GL_MODULATE;
		public int     combineFuncAlpha = GL_MODULATE;
		public int     combineSrc0RGB = GL_TEXTURE;
		public int     combineSrc1RGB = GL_PREVIOUS;
		public int     combineSrc2RGB = GL_CONSTANT;
		public int     combineSrc0Alpha = GL_TEXTURE;
		public int     combineSrc1Alpha = GL_PREVIOUS;
		public int     combineSrc2Alpha = GL_CONSTANT;
		public int     combineOperand0RGB = GL_SRC_COLOR;
		public int     combineOperand1RGB = GL_SRC_COLOR;
		public int     combineOperand2RGB = GL_SRC_ALPHA;
		public int     combineOperand0Alpha = GL_SRC_ALPHA;
		public int     combineOperand1Alpha = GL_SRC_ALPHA;
		public int     combineOperand2Alpha = GL_SRC_ALPHA;
		public int     combineScaleRGB = 1;
		public int     combineScaleAlpha = 1;
		public boolean[] textureGen = new boolean[4];	// S, T, R, Q
		public int[]   textureGenMode = new int[] {GL_EYE_LINEAR, GL_EYE_LINEAR, GL_EYE_LINEAR, GL_EYE_LINEAR};
		public boolean texCoordArrayEnabled= false;
		public int     texCoordPointerVbo  = 0;
		public FloatBuffer texCoordPointer = null;
	}
}
