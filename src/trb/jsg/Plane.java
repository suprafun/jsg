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
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

/**
 * The Plane represents a plane in 3d.
 * 
 * @author tombr
 *
 */
public class Plane implements Serializable {

	private static final long serialVersionUID = 0L;
	
	/** A position on the Plane */
	public Point3f P = new Point3f();
	
	/** The Plane normal */
	public Vec3 N = new Vec3();
	
	//The tolerance given when comparing two plane object.
	public final static float equalityTolerance = 0.0001f;
	
	
	/** Creates a plane with the position = (0,0,0) and normal = (0,0,0)
	 */
	public Plane() {
		P.set(0,0,0);
		N.set(0,0,0);
	}
	
	
	/** Creates a plane given by a point on the plane and the normal vector.
	 * @param p the coordinate of a point on the plane.
	 * @param v the coordinate of the normal vector of the plane.
	 * It's normalized befor it's set.
	 */
	public Plane(Tuple3f p, Tuple3f v) {
		setPlane(p,v);
	}
	
	/**
	 * Creates a plane given by three points on the plane.	
	 * @param p1 the coordinate of the first point.
	 * @param p2 the coordinate of the second point.
	 * @param p3 the coordinate of the third point.
	 */
	public Plane(Tuple3f p1, Tuple3f p2, Tuple3f p3) {
		setPlane(p1,p2,p3);
	}
	
	/**
	 * Creates a complete copy of plane. The variables defining plane is copyied.
	 * @param plane the plane to copy.
	 */
	public Plane(Plane plane) {
		P.set(plane.getPosition());
		N.set(plane.getNormal());
	}

	/** Gets a referance to the position tuple of this plane.
	 * Returns a point on this plane.
	 * @return a point on this plane.
	 */
	public Tuple3f getPosition() {
		return P;
	}

	/** Gets a referance to the normal Vector of this plane.
	 * Returns this plane's normal.
	 * @return this plane's normal.
	 */
	public Vec3 getNormal() {
		return N;
	}

	
	/**
	 * Set this plane given by the paramters.<br>
	 * Creates a plane given by a point on the plane and the normal vector.
	 * @param p the coordinate of a point on the plane.
	 * @param v the coordinate of the normal vector of the plane.
	 * The normal i NOT normalized.
	 */
	public void setPlane(float px, float py, float pz, float nx, float ny, float nz) {
		P.x = px;
		P.y = py;
		P.z = pz;
		N.x = nx;
		N.y = ny;
		N.z = nz;
	}
	
	/**
	 * Help function to the constructors that set this plane given by the paramters.<br>
	 * Creates a plane given by a point on the plane and the normal vector.
	 * @param p the coordinate of a point on the plane.
	 * @param v the coordinate of the normal vector of the plane.
	 * It's normalized befor it's set.
	 */
	public void setPlane(Tuple3f p, Tuple3f v) {
		P.x = p.x;
		P.y = p.y;
		P.z = p.z;
		N.x = v.x;
		N.y = v.y;
		N.z = v.z;
		N.normalize();
	}

	/** Creates a plane given by three points on the plane.
	 * @param p1 the coordinate of the first point.
	 * @param p2 the coordinate of the second point.
	 * @param p3 the coordinate of the third point.
	 */
	public void setPlane(Tuple3f p1, Tuple3f p2, Tuple3f p3) {
		Vec3 v1 = new Vec3(p3).sub_(p1);
		Vec3 v2 = new Vec3(p2).sub_(p1);
		P.set(p1);
		N.cross(v1, v2);
		N.normalize();
	}

	/**
	 * Returns true if coordinate is in front of this plane.
	 * @param coordinate the coordinate to check.
	 * @return true if coordinate is infront of this plane.
	 */
	public boolean isInfront(Tuple3f coordinate) {
		return getDistance(coordinate) >= 0.0f;
	}

	/**
	 * Returns true if coordinate p lies on this plane.
	 * @param p the coordinate of the point to check.
	 * @returns true if p lies on this plane.
	 */
	public boolean lieOn(Tuple3f p) {
		return Math.abs(getDistance(p)) < equalityTolerance;
	}

	/**
	 * Returns the closest distance from coordinate p to this plane.
	 * @param p the coordinate of the point to check.
	 * @return the closest distance from p to this plane.
	 */
	public final float getDistance(Tuple3f p) {
		return N.x*(p.x-P.x) + N.y*(p.y-P.y) + N.z*(p.z-P.z);
	}
	
	/**
	 * Returns the parameter t of the intersection between this plane and the line (a,b).
	 * <p>returns the intersection point between the plane and the line (a,b)<br>
	 * referance - 3DICA Programming Tutorial</p>
	 * @param a the coordinate of the starting point.
	 * @param b the coordinate of the end point.
	 * @return the parameter t of the intersection between this plane and the line (a,b).
	 */
	public float getLineIntersectionT(Tuple3f a, Tuple3f b) {
		float div = N.x*(b.x-a.x) + N.y*(b.y-a.y) + N.z*(b.z-a.z);
		if (div == 0.0f) {
            //System.out.println("divide by 0 in Plane.getLineIntersectionT");
            return -1;
        }
		return - ( N.x*(a.x-P.x) + N.y*(a.y-P.y) + N.z*(a.z-P.z) ) / div;
	}
	
	/**
	 * Returns the intersection point between this plane and the line (a,b).
	 * <p>returns the intersection point between the plane and the line (a,b)<br>
	 * referance - 3DICA Programming Tutorial</p>
	 * @param a the coordinate of the starting point.
	 * @param b the coordinate of the end point.
	 * @return the intersection point between this plane and the line (a,b).
	 */
	public Vec3 getLineIntersection(Tuple3f a, Tuple3f b) {
		float t = getLineIntersectionT(a, b);
		if (t < 0 || t > 1) {
			return null;
        }

        return new Vec3().interpolate_(a, b, t);
	}

	/**
	 * returns the intersection point between the plane and the ray (pos, dir)
	 * @param pos the coordinate of the starting point.
	 * @param pos the direction of the ray.
	 * @return the parameter t of the intersection between this plane and the ray (pos,dir).
	 */
	public float getRayIntersectionT(Tuple3f pos, Tuple3f dir) {
		float dominator = N.dot(dir);
		if (dominator == 0)	{
            return Float.MAX_VALUE;
        }
		return - ( N.x*(pos.x-P.x) + N.y*(pos.y-P.y) + N.z*(pos.z-P.z) ) / dominator;
   				
	}
	
	/**
	 * Returns the intersection point between this plane and the ray (pos, dir).
	 * @param pos the coordinate of the starting point.
	 * @param dir the direction of the ray.
	 * @return the intersection point between this plane and the ray (pos, dir),
	 *         or null if no intersection is found
	 */
	public Vec3 getRayIntersection(Tuple3f pos, Tuple3f dir) {
		float t = getRayIntersectionT(pos, dir);
		if (t < 0 || Float.isInfinite(t)) {
			System.out.println("Plane.getRayIntersection("+pos+", " + dir + "). t is infinite or less than zero:" + t);
			return null;
		}

        return new Vec3().scaleAdd_(t, dir, pos);
	}
	
	/** Gets the pos projected down on this plane.
	 * @param pos the position to project.
	 * @return the projected position of pos down on this plane.
	 */
	public Vec3 getProjected(Tuple3f pos) {
		float t = getRayIntersectionT(pos, N);
		return new Vec3().scaleAdd_(t, N, pos);
	}

	/**
	 * Returns true if plane defines the same plane as this.
	 * @param plane the plane to check.
	 * @return true if plane defines the same plane as this.
	 */
	public boolean equals(Plane plane) {
		if (lieOn(plane.getPosition()))	{
			Vec3 v1 = new Vec3(plane.getNormal()).normalize_();
			Vec3 v2 = new Vec3(getNormal());
			v2.negate();
			v2.sub(v1);
			float distance = v2.length();
			if (Math.abs(distance) < equalityTolerance) {
                return true;
            }
		}

		return false;
	}	
	
	
	/** Gets this plane transformed by the spesified matrix
	 */
	public Plane getTransformed(Mat4 m) {
		Vec3 p1 = new Vec3(P);
		Vec3 p2 = new Vec3(P);
		p2.add(N);
		m.transform(p1);
		m.transform(p2);
		p2.sub(p1);
		Plane plane = new Plane(p1, p2);
		return plane;
	}
	
	/**
	 * Gets a string containing the position and normal of the plane.
	 * @return the string
	 */
    @Override
	public String toString() {
		return "Plane [Pos=" + P + " Normal=" + N + "]";
	}
}
