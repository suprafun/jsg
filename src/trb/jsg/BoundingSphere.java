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
import java.lang.Math;

/**
 * This class defines a spherical bounding region which is defined by a center
 * point and a radius.
 */

public class BoundingSphere {

	/**
	 * The center of the bounding sphere.
	 */
	Point3d center;

	/**
	 * The radius of the bounding sphere.
	 */
	double radius;

	/**
	 * Constructs and initializes a BoundingSphere from a center and radius.
	 * 
	 * @param center
	 *            the center of the bounding sphere
	 * @param radius
	 *            the radius of the bounding sphere
	 */
	public BoundingSphere(Point3d center, double radius) {
		this.center = new Point3d(center);
		this.radius = radius;
	}

	/**
	 * Constructs and initializes a BoundingSphere with radius = 1 at 0 0 0.
	 */
	public BoundingSphere() {
		center = new Point3d();
		radius = 1.0;
	}

	/**
	 * Returns the radius of this bounding sphere as a double.
	 * 
	 * @return the radius of the bounding sphere
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of this bounding sphere from a double.
	 * 
	 * @param r
	 *            the new radius for the bounding sphere
	 */
	public void setRadius(double r) {
		radius = r;
	}

	/**
	 * Returns the position of this bounding sphere as a point.
	 * 
	 * @param center
	 *            a Point to receive the center of the bounding sphere
	 * 
	 */
	public void getCenter(Point3d center) {
		center.x = this.center.x;
		center.y = this.center.y;
		center.z = this.center.z;
	}

	/**
	 * Sets the position of this bounding sphere from a point.
	 * 
	 * @param center
	 *            a Point defining the new center of the bounding sphere
	 */
	public void setCenter(Point3d center) {
		this.center.x = center.x;
		this.center.y = center.y;
		this.center.z = center.z;
	}

	/**
	 * Combines this bounding sphere with a point.
	 * 
	 * @param point
	 *            a 3D point in space
	 */
	public void combine(Point3d point) {
		double dis = Math.sqrt((point.x - center.x) * (point.x - center.x)
				+ (point.y - center.y) * (point.y - center.y)
				+ (point.z - center.z) * (point.z - center.z));

		if (dis > radius) {
			radius = (dis + radius) * .5;
			double oldc_to_new_c = dis - radius;
			center.x = (radius * center.x + oldc_to_new_c * point.x) / dis;
			center.y = (radius * center.y + oldc_to_new_c * point.y) / dis;
			center.z = (radius * center.z + oldc_to_new_c * point.z) / dis;
		}
	}

	/**
	 * Combines this bounding sphere with an array of points.
	 * 
	 * @param points
	 *            an array of 3D points in space
	 */
	public void combine(Point3d[] points) {
		int i;
		double dis, dis_sq, rad_sq, oldc_to_new_c;
		for (i = 0; i < points.length; i++) {
			rad_sq = radius * radius;
			dis_sq = (points[i].x - center.x) * (points[i].x - center.x)
					+ (points[i].y - center.y) * (points[i].y - center.y)
					+ (points[i].z - center.z) * (points[i].z - center.z);

			// change sphere so one side passes through the point and
			// other passes through the old sphere
			if (dis_sq > rad_sq) {
				dis = Math.sqrt(dis_sq);
				radius = (radius + dis) * .5;
				oldc_to_new_c = dis - radius;
				center.x = (radius * center.x + oldc_to_new_c * points[i].x)
						/ dis;
				center.y = (radius * center.y + oldc_to_new_c * points[i].y)
						/ dis;
				center.z = (radius * center.z + oldc_to_new_c * points[i].z)
						/ dis;
			}
		}
	}

	Point3d getCenter() {
		return center;
	}
}
