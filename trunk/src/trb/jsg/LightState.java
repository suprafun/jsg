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

import javax.vecmath.Color3f;
import javax.vecmath.Point4f;
import javax.vecmath.Tuple3f;

import trb.jsg.util.ObjectArray;
import trb.jsg.util.Vec3;

public class LightState implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** The state for each of the lights used by this shape. The light is
	 * disabled if its index contains a null value
	 */
	public ObjectArray<Light> lights = new ObjectArray<Light>();
	
	/**
	 * Sets the light to use at the specified index.
	 * @param index the light index
	 * @param light the light, can be null
	 */
//	public void setLight(int index, Light light) {
//		lights.set(light, index);
//	}
	
	/**
	 * Gets the light at the specified index.
	 * @param index the light index
	 * @return the light at the specified index
	 */
//	public Light getLight(int index) {
//		return lights.get(index);
//	}
	
	/**
	 * Gets the index of the last non null element in the lights array.
	 * @return the index of the last non null element
	 */
//	public int getSize() {
//		return lights.length();
//	}

	/**
	 * 
	 * @author tombr
	 *
	 */
	public static class Light implements Serializable {
		
		private static final long serialVersionUID = 0L;
		
		public Color3f ambient = new Color3f(0, 0, 0);
		public Color3f diffuse = new Color3f(1, 1, 1);
		public Color3f specular = new Color3f(0, 0, 0);
		public Point4f position = new Point4f(0, 0, 1, 0);
		public Vec3 spotDirection = new Vec3(0, 0, -1);
		/** [0, 180] */
		public float spotExponent = 0;
		/** [0, 90] and 180 */
		public float spotCutoff = 180;
		public float constantAttenuation = 1;
		public float linearAttenuation = 0;
		public float quadratiqAttenuation = 0;
		
		public void setAmbientLight(Color3f color) {
			ambient.set(color);
		}
		
		public void setDirectionalLight(Tuple3f direction) {
			position.set(direction.x, direction.y, direction.z, 0);
		}
		
		public void setPointLight(Tuple3f position, float constantAttenuation
				, float linearAttenuation, float quadratiqAttenuation) {
			this.position.set(position.x, position.y, position.z, 1);
			this.constantAttenuation = constantAttenuation;
			this.linearAttenuation = linearAttenuation;
			this.quadratiqAttenuation = quadratiqAttenuation;
		}
		
		public void setSpotLight(Tuple3f position, Tuple3f spotDirection
				, float spotExponent, float spotCutoff, float constantAttenuation
				, float linearAttenuation, float quadratiqAttenuation) {
			this.position.set(position.x, position.y, position.z, 1);
			this.spotDirection.set(spotDirection);
			this.spotExponent = spotExponent;
			this.spotCutoff = spotCutoff;
			this.constantAttenuation = constantAttenuation;
			this.linearAttenuation = linearAttenuation;
			this.quadratiqAttenuation = quadratiqAttenuation;
		}
	}
}
