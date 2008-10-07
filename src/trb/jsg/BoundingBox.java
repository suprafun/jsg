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

import javax.vecmath.*;

/**
 * This class defines an axis aligned bounding box which is used for bounding
 * regions.
 * 
 */

public class BoundingBox {

	/**
	 * The corner of the bounding box with the numerically smallest values.
	 */
	Point3d lower;

	/**
	 * The corner of the bounding box with the numerically largest values.
	 */
	Point3d upper;

	/**
	 * Constructs and initializes a BoundingBox given min,max in x,y,z.
	 * 
	 * @param lower the "small" corner
	 * @param upper the "large" corner
	 */
	public BoundingBox(Point3d lower, Point3d upper) {
		this.lower = new Point3d(lower);
		this.upper = new Point3d(upper);
	}

	/**
	 * Constructs and initializes a 2X bounding box about the origin. The lower
	 * corner is initialized to (-1.0d, -1.0d, -1.0d) and the opper corner is
	 * initialized to (1.0d, 1.0d, 1.0d).
	 */
	public BoundingBox() {
		lower = new Point3d(-1.0d, -1.0d, -1.0d);
		upper = new Point3d(1.0d, 1.0d, 1.0d);
	}

	/**
	 * Gets the lower corner of this bounding box.
	 * 
	 * @param p1
	 *            a Point to receive the lower corner of the bounding box
	 */
	public void getLower(Point3d p1) {
		p1.x = lower.x;
		p1.y = lower.y;
		p1.z = lower.z;
	}

	/**
	 * Sets the lower corner of this bounding box.
	 * 
	 * @param xmin minimum x value of boundining box
	 * @param ymin minimum y value of boundining box
	 * @param zmin minimum z value of boundining box
	 */
	public void setLower(double xmin, double ymin, double zmin) {
		lower.x = xmin;
		lower.y = ymin;
		lower.z = zmin;
	}

	/**
	 * Sets the lower corner of this bounding box.
	 * @param p1 a Point defining the new lower corner of the bounding box
	 */
	public void setLower(Point3d p1) {

		lower.x = p1.x;
		lower.y = p1.y;
		lower.z = p1.z;
	}

	/**
	 * Gets the upper corner of this bounding box.
	 * @param p1 a Point to receive the upper corner of the bounding box
	 */
	public void getUpper(Point3d p1) {
		p1.x = upper.x;
		p1.y = upper.y;
		p1.z = upper.z;
	}

	/**
	 * Sets the upper corner of this bounding box.
	 * @param xmax max x value of boundining box
	 * @param ymax max y value of boundining box
	 * @param zmax max z value of boundining box
	 */
	public void setUpper(double xmax, double ymax, double zmax) {
		upper.x = xmax;
		upper.y = ymax;
		upper.z = zmax;
	}

	/**
	 * Sets the upper corner of this bounding box.
	 * 
	 * @param p1 a Point defining the new upper corner of the bounding box
	 */
	public void setUpper(Point3d p1) {
		upper.x = p1.x;
		upper.y = p1.y;
		upper.z = p1.z;
	}

	/**
	 * Returns a string representation of this class.
	 */
	public String toString() {
		return new String("Bounding box: Lower=" + lower.x + " " + lower.y
				+ " " + lower.z + " Upper=" + upper.x + " " + upper.y + " "
				+ upper.z);
	}

}
