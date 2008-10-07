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

import static org.lwjgl.opengl.GL13.*;

public enum CombineSrc {
	TEXTURE(GL_TEXTURE), 
	TEXTURE0(GL_TEXTURE0), 
	TEXTURE1(GL_TEXTURE1), 
	TEXTURE2(GL_TEXTURE2), 
	TEXTURE3(GL_TEXTURE3), 
	TEXTURE4(GL_TEXTURE4), 
	TEXTURE5(GL_TEXTURE5), 
	TEXTURE6(GL_TEXTURE6), 
	TEXTURE7(GL_TEXTURE7), 
	TEXTURE8(GL_TEXTURE8), 
	TEXTURE9(GL_TEXTURE9), 
	TEXTURE10(GL_TEXTURE10), 
	TEXTURE11(GL_TEXTURE11), 
	TEXTURE12(GL_TEXTURE12), 
	TEXTURE13(GL_TEXTURE13), 
	TEXTURE14(GL_TEXTURE14), 
	TEXTURE15(GL_TEXTURE15), 
	TEXTURE16(GL_TEXTURE16), 
	TEXTURE17(GL_TEXTURE17), 
	TEXTURE18(GL_TEXTURE18), 
	TEXTURE19(GL_TEXTURE19), 
	TEXTURE20(GL_TEXTURE20), 
	TEXTURE21(GL_TEXTURE21), 
	TEXTURE22(GL_TEXTURE22), 
	TEXTURE23(GL_TEXTURE23), 
	TEXTURE24(GL_TEXTURE24), 
	TEXTURE25(GL_TEXTURE25), 
	TEXTURE26(GL_TEXTURE26), 
	TEXTURE27(GL_TEXTURE27), 
	TEXTURE28(GL_TEXTURE28), 
	TEXTURE29(GL_TEXTURE29), 
	TEXTURE30(GL_TEXTURE30), 
	TEXTURE31(GL_TEXTURE31), 
	CONSTANT(GL_CONSTANT), 
	PRIMARY_COLOR(GL_PRIMARY_COLOR), 
	PREVIOUS(GL_PREVIOUS);
	
	private int val;
	
	private CombineSrc(int val) {
		this.val = val;
	}
	
	public int get() {
		return val;
	}
}
