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

package trb.jsg.enums;

import static org.lwjgl.opengl.GL11.*;


public enum BlendSrcFunc {
	ZERO(GL_ZERO), 
	ONE(GL_ONE), 
	SRC_COLOR(GL_SRC_COLOR), 
	ONE_MINUS_SRC_COLOR(GL_ONE_MINUS_SRC_COLOR), 
	DST_COLOR(GL_DST_COLOR),
	ONE_MINUS_DST_COLOR(GL_ONE_MINUS_DST_COLOR), 
	SRC_ALPHA(GL_SRC_ALPHA), 
	ONE_MINUS_SRC_ALPHA(GL_ONE_MINUS_SRC_ALPHA), 
	DST_ALPHA(GL_DST_ALPHA), 
	ONE_MINUS_DST_ALPHA(GL_ONE_MINUS_DST_ALPHA), 
	CONSTANT_COLOR(GL_CONSTANT_COLOR), 
	ONE_MINUS_CONSTANT_COLOR(GL_ONE_MINUS_CONSTANT_COLOR), 
	CONSTANT_ALPHA(GL_CONSTANT_ALPHA), 
	ONE_MINUS_CONSTANT_ALPHA(GL_ONE_MINUS_CONSTANT_ALPHA), 
	SRC_ALPHA_SATURATE(GL_SRC_ALPHA_SATURATE);
	
	private int val;
	
	private BlendSrcFunc(int val) {
		this.val = val;
	}
	
	public int get() {
		return val;
	}
}
