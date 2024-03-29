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
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import trb.jsg.util.Mat4;


/**
 * The view is a frustum containing the visible are of the world. The frustum
 * is used to do view frustum culling. The view defines the near and far 
 * clipping plane as well as the aspect ratio.
 *  
 * @author tombr
 *
 */
public class View implements Serializable {

	private static final long serialVersionUID = 0L;

    public static boolean useFrustumCulling = true;

    private float near = 0;
    private float far = 0;
	
	// defines the viewing volume
	private Mat4 projectionMatrix = new Mat4();
	
	// the location of the view
	private Mat4 cameraMatrix = new Mat4();
	private Mat4 cameraMatrixInverted = new Mat4();
	
	/** 
	 * The view frustum planes in local (view) space. Is updated in
	 * frustum(...). Can be used to do custom view frustum culling.
	 */
	private Plane[] planes = new Plane[6];
	
	// The planes in world space.
	private Plane[] worldPlanes = new Plane[6];
	
	// temp vars
	private Point3f lower = new Point3f();
	private Point3f upper = new Point3f();
	
	/**
	 * Default constructor.
	 */
	public View() {
		for (int i=0; i<worldPlanes.length; i++) {
			worldPlanes[i] = new Plane();
		}
	}

    public View(View view) {
        projectionMatrix.set(view.projectionMatrix);
        cameraMatrix.set(view.cameraMatrix);
        cameraMatrixInverted.set(view.cameraMatrixInverted);
        near = view.near;
        far = view.far;
        for (int i=0; i<6; i++) {
            planes[i] = new Plane(view.planes[i].P, view.planes[i].N);
            worldPlanes[i] = new Plane(view.worldPlanes[i].P, view.worldPlanes[i].N);
        }
    }

	/**
	 * Sets the view frustum to perspective projection.
	 * @param fovYRad the field of view in radians
	 * @param aspect the aspect ratio
	 * @param near the near plane
	 * @param far the far plane
	 */
    public void perspective(float fovYRad, float aspect, float near, float far) {
        this.near = near;
        this.far = far;
        float h = (float) Math.tan(fovYRad * 0.5f) * near;
        float w = h * aspect;
        frustum(-w, w, h, -h, near, far);
    }
	
	
	/**
	 * Sets the view frustum to perspective projection.
	 * @param l left
	 * @param r right
	 * @param t top
	 * @param b bottom
	 * @param n near
	 * @param f far
	 */
	public void frustum(float l, float r, float t, float b, float n, float f) {
		if (f<n || n<=0) {
			System.out.println("far plane should be larger than near, and both should be positive");
		}
        this.near = n;
        this.far = f;
		
		Matrix4f p = new Matrix4f();
		p.setIdentity();
		p.m00  = (2*n)/(r-l);
		p.m02  = (r+l)/(r-l);
		p.m11  = (2*n)/(t-b);
		p.m12  = (t+b)/(t-b);
		p.m22 = -(f+n)/(f-n);
		p.m23 = -(2*f*n)/(f-n);
		p.m32 = -1;
		p.m33 = 0;
		projectionMatrix = new Mat4(p);
		
		// create the planes of the camera
		Tuple3f origo = new Point3f();
		Tuple3f ul = new Point3f(l, t, n);
		Tuple3f ur = new Point3f(r, t, n);
		Tuple3f ll = new Point3f(l, b, n);
		Tuple3f lr = new Point3f(r, b, n);
		planes[0] = new Plane(new Point3f(0,0,-n), new Point3f(0,0,-1));
		planes[1] = new Plane(origo, ll, ul);
		planes[2] = new Plane(origo, ur, lr);
		planes[3] = new Plane(origo, ul, ur);
		planes[4] = new Plane(origo, lr, ll);
		planes[5] = new Plane(new Point3f(0,0,-f),  new Point3f(0,0,1));
		
		if (t > b) {
			for (int i=1; i<5; i++) {
				planes[i].N.scale(-1);
			}
		}
	}
	
	/**
	 * Sets the view frustum to ortho projection.
	 * @param l left
	 * @param r right
	 * @param t top
	 * @param b bottom
	 * @param n near
	 * @param f far
	 */
	public void ortho(float l, float r, float t, float b, float n, float f) {
		if (f<n) {
			System.out.println("far plane should be larger than near");
		}
        this.near = n;
        this.far = f;
		
		Matrix4f p = new Matrix4f();
		p.setIdentity();
		p.m00  = 2/(r-l);
		p.m03  = -(r+l)/(r-l);
		p.m11  = 2/(t-b);
		p.m13  = -(t+b)/(t-b);
		p.m22 = -2/(f-n);
		p.m23 = (f+n)/(f-n);
		p.m32 = 0;
		p.m33 = 1;
		projectionMatrix = new Mat4(p);
		
		// create the planes of the camera
		planes[0] = new Plane(new Point3f(0,0,-n), new Point3f(0,0,-1));
		planes[1] = new Plane(new Point3f(l, 0, 0), new Point3f( 1, 0, 0));
		planes[2] = new Plane(new Point3f(r, 0, 0), new Point3f(-1, 0, 0));
		planes[3] = new Plane(new Point3f(0, t, 0), new Point3f( 0,  1, 0));
		planes[4] = new Plane(new Point3f(0, b, 0), new Point3f( 0, -1, 0));
		planes[5] = new Plane(new Point3f(0,0,-f),  new Point3f(0,0,1));
		
		if (t > b) {
			planes[3].N.scale(-1);
			planes[4].N.scale(-1);
		}
		if (l > r) {
			planes[1].N.scale(-1);
			planes[2].N.scale(-1);
		}
	}
	
	/**
	 * with culling: 160-163
	 * no transform: 179
	 * return true: 263
	 * Checks if the BoundingSphere is inside the view frustum.
	 * @param modelViewMatrix the local to world matrix of the bounding sphere
	 * @param boundingSphere the bounding sphere in local space
	 * @param center3f where the view space center of bounding box is stored
	 * @return true if BoundingSphere is inside frustum
	 */
	public boolean isInsideFrustum(Mat4 modelViewMatrix, BoundingSphere boundingSphere, Point3f center3f) {
        if (useFrustumCulling) {
            center3f.set(boundingSphere.getCenter());
            modelViewMatrix.transform(center3f);
            float radius = boundingSphere.getRadius();

            for (int planeIdx = 0; planeIdx < planes.length; planeIdx++) {
                Plane plane = planes[planeIdx];
                float distance = plane.getDistance(center3f);
                if (distance < -radius) {
                    return false;
                }
            }
        }
		
		return true;
	}

	/**
	 * Checks if the specified world space bounding sphere is inside the view 
	 * frustum.
	 * @param center3f center of bounding sphere in world space
	 * @param radius the radius
	 * @return true if BoundingSphere is inside frustum
	 */
	public boolean isInsideFrustum(Point3f center3f, float radius) {
        if (useFrustumCulling) {
            for (int planeIdx = 0; planeIdx < worldPlanes.length; planeIdx++) {
                Plane plane = worldPlanes[planeIdx];
                float distance = plane.getDistance(center3f);
                if (distance < -radius) {
                    return false;
                }
            }
        }

		return true;
	}
	
	/**
	 * Checks if the specified bounding box is inside the view frustum.
	 * @param bbox
	 * @return
	 */
	public boolean isInsideFrustum(BoundingBox bbox) {
		bbox.getLower(lower);
		bbox.getUpper(upper);
		Point3f l = new Point3f(lower);
		Point3f u = new Point3f(upper);
		Point3f a1 = new Point3f(l.x, l.y, l.z);
		Point3f a2 = new Point3f(u.x, l.y, l.z);
		Point3f a3 = new Point3f(l.x, u.y, l.z);
		Point3f a4 = new Point3f(u.x, u.y, l.z);
		Point3f a5 = new Point3f(l.x, l.y, u.z);
		Point3f a6 = new Point3f(u.x, l.y, u.z);
		Point3f a7 = new Point3f(l.x, u.y, u.z);
		Point3f a8 = new Point3f(u.x, u.y, u.z);
		
		for (int planeIdx=0; planeIdx<worldPlanes.length; planeIdx++) {
			Plane plane = worldPlanes[planeIdx];
			if (plane.isInfront(a1)) {
				continue;
			}
			if (plane.isInfront(a2)) {
				continue;
			}
			if (plane.isInfront(a3)) {
				continue;
			}
			if (plane.isInfront(a4)) {
				continue;
			}
			if (plane.isInfront(a5)) {
				continue;
			}
			if (plane.isInfront(a6)) {
				continue;
			}
			if (plane.isInfront(a7)) {
				continue;
			}
			if (plane.isInfront(a8)) {
				continue;
			}
			return false;
		}
		return true;
		
//		// Go through all of the corners of the box and check then again each plane
//		// in the frustum.  If all of them are behind one of the planes, then it most
//		// like is not in the frustum.
//		for (int i = 0; i < 6; i++) {
//			float row[] = frustum[i];
//			float fA = row[A];
//			float fB = row[B];
//			float fC = row[C];
//			float fD = row[D];
//			if (fA * x  + fB * y  + fC * z  + fD > 0)  continue;
//			if (fA * x2 + fB * y  + fC * z  + fD > 0)  continue;
//			if (fA * x  + fB * y2 + fC * z  + fD > 0)  continue;
//			if (fA * x2 + fB * y2 + fC * z  + fD > 0)  continue;
//			if (fA * x  + fB * y  + fC * z2 + fD > 0)  continue;
//			if (fA * x2 + fB * y  + fC * z2 + fD > 0)  continue;
//			if (fA * x  + fB * y2 + fC * z2 + fD > 0)  continue;
//			if (fA * x2 + fB * y2 + fC * z2 + fD > 0)  continue;
//
//			// If we get here, it isn't in the frustum
//			return false;
//		}
//
//		// Return a true for the box being inside of the frustum
//		return true;
		
	}
	
	/**
	 * Transforms the view frustum planes into world space.
	 * @return a reference to the transformed planes
	 */
	public Plane[] updateWorldPlanes() {
		cameraMatrixInverted.invert(getCameraMatrix());
		for (int i=0; i<planes.length; i++) {
			cameraMatrixInverted.transform(planes[i].P, worldPlanes[i].P);
			cameraMatrixInverted.transform(planes[i].N, worldPlanes[i].N);
		}
		
		return worldPlanes;
	}
	
	/**
	 * Gets the local space view frustum planes.
	 * @return the planes
	 */
	public Plane[] getFrustumPlanesLocal() {
		return planes;
	}
	
	/**
	 * Gets the view frustum planes in world space. Use updateWorldPlanes() to
	 * first update the planes. 
	 * @return the planes
	 */
	public Plane[] getFrustumPlanesWorld() {
		return planes;
	}

	/**
	 * Gets the projection matrix.
	 * @return the projectionMatrix
	 */
	public Mat4 getProjectionMatrix() {
		return projectionMatrix;
	}

	/**
	 * Sets the camera matrix.
	 * @param cameraMatrix the cameraMatrix to set
	 */
	public void setCameraMatrix(Mat4 cameraMatrix) {
		this.cameraMatrix = cameraMatrix;
	}

	/**
	 * Gets the camera matrix.
	 * @return the cameraMatrix
	 */
	public Mat4 getCameraMatrix() {
		return cameraMatrix;
	}

    /**
     * Gets the near clip distance.
     */
    public float getNear() {
        return near;
    }

    /**
     * Gets the far clip distance.
     */
    public float getFar() {
        return far;
    }

    /**
     * Creates a view with ortho projection.
     */
    public static View createOrtho(float l, float r, float t, float b, float n, float f) {
        View view = new View();
        view.ortho(l, r, t, b, n, f);
        return view;
    }

    public static View createPerspective(float fovYRad, float aspect, float near, float far) {
        View view = new View();
        view.perspective(fovYRad, aspect, near, far);
        return view;        
    }
}
