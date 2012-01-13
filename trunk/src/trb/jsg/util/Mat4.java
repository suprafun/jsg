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
package trb.jsg.util;

import javax.vecmath.*;

/**
 *
 * @author tomrbryn
 */
public class Mat4 extends Transform3D {

    public Mat4() {
        super();
    }

    public Mat4(float[] matrix) {
        super(matrix);
    }

    public Mat4(double[] matrix) {
        super(matrix);
    }

    public Mat4(Transform3D t3d) {
        super(t3d);
    }

    public Mat4(Matrix4f m4f) {
        super(m4f);
    }

    public Mat4(Matrix4d m4d) {
        super(m4d);
    }

    public Mat4 set_(Quat4f q1) {
        super.set(q1);
        return this;
    }

    public Mat4 set_(Quat4d q1) {
        super.set(q1);
        return this;
    }

    public Mat4 set_(AxisAngle4f a1) {
        super.set(a1);
        return this;
    }

    public Mat4 set_(AxisAngle4d a1) {
        super.set(a1);
        return this;
    }

    public Mat4 set_(Matrix3f m) {
        super.set(m);
        return this;
    }

    public Mat4 set_(Matrix3d m) {
        super.set(m);
        return this;
    }

    public Mat4 set_(Matrix4f m) {
        super.set(m);
        return this;
    }

    public Mat4 set_(Matrix4d m) {
        super.set(m);
        return this;
    }

    public Mat4 set_(Transform3D t3d) {
        super.set(t3d);
        return this;
    }

    public Quat4f getQuat4f() {
        return getQuat4f(this);
    }

    public Quat4d getQuat4d() {
        return getQuat4d(this);
    }

    public Matrix3f getMatrix3f() {
        return getMatrix3f(this);
    }

    public Matrix3d getMatrix3d() {
        return getMatrix3d(this);
    }

    public Matrix4f getMatrix4f() {
        return getMatrix4f(this);
    }

    public Matrix4d getMatrix4d() {
        return getMatrix4d(this);
    }

    public Vec3 getForward() {
        return getForward(this);
    }

    public Vec3 getRight() {
        return getRight(this);
    }

    public Vec3 getUp() {
        return getUp(this);
    }

    public Vec3 getTranslation() {
        return getTranslation(this);
    }

    public Mat4 setTranslation(Tuple3f translation) {
        setTranslation(this, translation);
        return this;
    }

    public Mat4 setTranslation_(Tuple3f translation) {
        setTranslation(this, translation);
        return this;
    }

    public Mat4 setTranslation(Tuple3d translation) {
        setTranslation(this, translation);
        return this;
    }

    public Vec3 getScale3() {
        return getScale(this);
    }

    public Mat4 setScale(Tuple3f scale) {
        return setScale(this, scale);
    }

    public Mat4 setScale_(Tuple3d scale) {
        return setScale(this, scale);
    }

    public Mat4 setScale_(double scale) {
        setScale(scale);
        return this;
    }

    public Mat4 setScale(double x, double y, double z) {
        setScale(new Vector3d(x, y, z));
        return this;
    }

    public Mat4 scale(double scale) {
        Transform3D scaleT3D = new Transform3D();
        scaleT3D.setScale(scale);
        mul(scaleT3D);
        return this;
    }

    public Mat4 scale(double x, double y, double z) {
        mul(new Mat4().setScale(x, y, z));
        return this;
    }

    public Mat4 scale(Vec3 scale) {
        mul(new Mat4().setScale(scale));
        return this;
    }

    public Mat4 translate(float x, float y, float z) {
        return translate(new Vec3(x, y, z));
    }

    public Mat4 translate(Tuple3f translation) {
        mul(fromTranslation(translation));
        return this;
    }

    public Mat4 translate(double x, double y, double z) {
        return translate(new Vector3d(x, y, z));
    }

    public Mat4 translate(Tuple3d translation) {
        mul(fromTranslation(translation));
        return this;
    }

    public Mat4 rotate(AxisAngle4f aa) {
        mul(fromAxisAngle(aa));
        return this;
    }

    public Mat4 rotate(AxisAngle4d aa) {
        mul(fromAxisAngle(aa));
        return this;
    }

    public Mat4 rotateEulerDeg(Tuple3f eulerDeg) {
        mul(fromEuler(new Vec3(eulerDeg).toRadians()));
        return this;
    }

    public Mat4 rotateEulerDeg(float x, float y, float z) {
        return rotateEulerDeg(new Vec3(x, y, z));
    }

    public Mat4 rotateEuler(float x, float y, float z) {
        return rotateEuler(new Vec3(x, y, z));
    }

    public Mat4 rotateEuler(Tuple3f euler) {
        mul(fromEuler(euler));
        return this;
    }

    public Mat4 setEuler(Tuple3f euler) {
        return setEuler(this, euler);
    }

    public Vec3 getEuler() {
        return getEuler(this);
    }

    public Mat4 setEulerDeg(Tuple3f eulerDeg) {
        return Mat4.setEulerDeg(this, eulerDeg);
    }

    public Vec3 getEulerDeg() {
        return Mat4.getEulerDeg(this);
    }

    public float distance(Mat4 m) {
        return distance(this, m);
    }

    public Mat4 lookAtInv(Tuple3f pos, Tuple3f target, Tuple3f up) {
        Vec3 z = new Vec3(target).sub_(pos);
        final float EPSILON = (0.001f * 0.001f);
        if (z.lengthSquared() < EPSILON) {
            z.set(0, 0, 1);
        }
        z.normalize();

        Vec3 y = new Vec3(up);
        Vec3 x = new Vec3().cross_(y, z);
        if (x.lengthSquared() < EPSILON) {
            if (Math.abs(z.dot(new Vec3(0, 1, 0))) < 0.5f) {
                y.set(0, 1, 0);
            } else {
                y.set(0, 0, -1);
            }

            x.cross_(y, z);
        }
        x.normalize();
        y.cross_(z, x);
        Matrix3f rotationMatrix = new Matrix3f();
        rotationMatrix.setRow(0, x);
        rotationMatrix.setRow(1, y);
        rotationMatrix.setRow(2, z);
        rotationMatrix.transpose();
        setRotation(rotationMatrix);
        setTranslation(pos);
        return this;
    }

    public Mat4 lookAt(Tuple3f eye, Tuple3f target, Tuple3f up) {
        super.lookAt(new Point3d(eye), new Point3d(target), new Vector3d(up));
        return this;
    }

    /**
     * Sets the rotation so that the +z axis is pointing at the specified
     * direction. Up is set to (0, 1, 0)
     */
    public Mat4 lookAtDir(Tuple3f direction) {
        Vec3 eye = getTranslation();
        Point3d target = new Point3d(eye);
        target.add(new Point3d(direction));
        super.lookAt(new Point3d(eye), target, new Vector3d(0, 1, 0));
        return this;
    }

    public Mat4 lookAtDir(Tuple3f eye, Tuple3f direction, Tuple3f up) {
        Point3d target = new Point3d(eye);
        target.add(new Point3d(direction));
        super.lookAt(new Point3d(eye), target, new Vector3d(up));
        return this;
    }

    public Mat4 invert_() {
        invert();
        return this;
    }

    /**
     * Transforms point as a point.
     * @return point
     */
    public <T extends Tuple3f> T transformAsPoint(T point) {
        Point3f p = new Point3f(point);
        transform(p);
        point.set(p);
        return point;
    }

    /**
     * Transforms as a vector.
     * @return point
     */
    public <T extends Tuple3f> T transformAsVector(T vector) {
        Vec3 v = new Vec3(vector);
        transform(v);
        vector.set(v);
        return vector;
    }

    /**
     * Calculates the difference between this and the specified Transform3D and
     * store the result in this. Same as this = invert(t3d) * this;
     * @return this
     */
    public Mat4 diff(Transform3D t3d) {
        Mat4 m = new Mat4(t3d);
        m.invert();
        m.mul(this);
        this.set(m);
        return this;
    }

    public Mat4 mul_(Transform3D t3d) {
        mul(t3d);
        return this;
    }

    public Mat4 mulInverse_(Transform3D t3d) {
        mulInverse(t3d);
        return this;
    }

    public float[] toFloats() {
        return toFloats(this);
    }
    
    public static float[] toFloats(Transform3D t3d) {
        float[] floats = new float[16];
        t3d.get(floats);
        return floats;
    }

    /**
     * Transforms point from source to target space.
     * @return point
     */
    public static <T extends Tuple3f> T transformAsPointFromTo(T point, Transform3D source, Transform3D target) {
        Point3f p = new Point3f(point);
        source.transform(p);
        new Mat4(target).invert_().transform(p);
        point.set(p);
        return point;
    }
    
    public static Vec3 getForward(Transform3D t3d) {
        Vec3 forward = new Vec3(0, 0, -1);
        t3d.transform(forward);
        return forward;
    }

    public static Vec3 getRight(Transform3D t3d) {
        Vec3 right = new Vec3(1, 0, 0);
        t3d.transform(right);
        return right;
    }

    public static Vec3 getUp(Transform3D t3d) {
        Vec3 up = new Vec3(0, 1, 0);
        t3d.transform(up);
        return up;
    }

    /**
     * Gets the translational components of the specified transform.
     * @param t3d the transform
     * @return the translation components
     */
    public static Vec3 getTranslation(Transform3D t3d) {
        Vec3 vec = new Vec3();
        t3d.get(vec);
        return vec;
    }

    /**
     * Replaces the translational components of the specified transform to the
     * values in the Tuple3f argument; the other values of the transform are not
     * modified.
     * @param t3d the transform
     * @param translation the translation
     */
    public static Transform3D setTranslation(Transform3D t3d, Tuple3f translation) {
        if (translation instanceof Vector3f) {
            t3d.setTranslation((Vector3f) translation);
        } else {
            t3d.setTranslation(new Vector3f(translation));
        }

        return t3d;
    }
    
    /**
     * Replaces the translational components of the specified transform to the
     * values in the Tuple3d argument; the other values of the transform are not
     * modified.
     * @param t3d the transform
     * @param translation the translation
     */
    public static Transform3D setTranslation(Transform3D t3d, Tuple3d translation) {
        if (translation instanceof Vector3d) {
            t3d.setTranslation((Vector3d) translation);
        } else {
            t3d.setTranslation(new Vector3d(translation));
        }

        return t3d;
    }

    /**
     * Gets the possibly non-uniform scale components of the specified transform.
     * @param t3d the transform
     * @return the scale components
     */
    public static Vec3 getScale(Transform3D t3d) {
        Vector3d scale = new Vector3d();
        t3d.getScale(scale);
        return new Vec3(scale);
    }

    /**
     * Sets the possibly non-uniform scale component of the current transform;
     * any existing scale is first factored out of the existing transform before
     * the new scale is applied.
     * @param t3d the transform
     * @param scale the scale
     */
    public static <T extends Transform3D> T setScale(T t3d, Tuple3f scale) {
        t3d.setScale(new Vector3d(scale));
        return t3d;
    }

    public static <T extends Transform3D> T setScale(T t3d, Tuple3d scale) {
        t3d.setScale(new Vector3d(scale));
        return t3d;
    }

    /**
     * Gets the euler equivalent of the normalized rotational component of the
     * specified transform.
     * @param t3d the transform
     * @return the euler components in radians
     */
    public static Vec3 getEuler(Transform3D t3d) {
        Matrix3f m = new Matrix3f();
        t3d.get(m);
        Vec3 euler = new Vec3();
        if (m.m10 > 0.998) { // singularity at north pole
            euler.y = (float) Math.atan2(m.m02, m.m22); // heading
            euler.z = (float) Math.PI / 2; // attitude
            euler.x = 0; // bank
            return euler;
        }
        if (m.m10 < -0.998) { // singularity at south pole
            euler.y = (float) Math.atan2(m.m02, m.m22); // heading
            euler.z = (float) -Math.PI / 2; // attitude
            euler.x = 0; // bank
            return euler;
        }
        euler.y = (float) Math.atan2(-m.m20, m.m00); // heading
        euler.x = (float) Math.atan2(-m.m12, m.m11); // bank
        euler.z = (float) Math.asin(m.m10);  // attitude
        return euler;
    }

    /**
     * Sets the rotational component (upper 3x3) of this transform to the
     * rotation matrix converted from the Euler angles provided; any
     * pre-existing scale in the transform is preserved.
     * @param t3d the transform
     * @param euler the rotation in radians
     */
    public static <T extends Transform3D> T setEuler(T t3d, Tuple3f euler) {
        double heading = euler.y;
        double attitude = euler.z;
        double bank = euler.x;

        // Assuming the angles are in radians.
        double ch = Math.cos(heading);
        double sh = Math.sin(heading);
        double ca = Math.cos(attitude);
        double sa = Math.sin(attitude);
        double cb = Math.cos(bank);
        double sb = Math.sin(bank);

        Matrix3d m = new Matrix3d();
        m.m00 = ch * ca;
        m.m01 = sh * sb - ch * sa * cb;
        m.m02 = ch * sa * sb + sh * cb;
        m.m10 = sa;
        m.m11 = ca * cb;
        m.m12 = -ca * sb;
        m.m20 = -sh * ca;
        m.m21 = sh * sa * cb + ch * sb;
        m.m22 = -sh * sa * sb + ch * cb;

        t3d.setRotation(m);
        return t3d;
    }

    public static <T extends Transform3D> T setEulerDeg(T t3d, Tuple3f eulerDeg) {
        Tuple3f eulerRad = new Point3f(eulerDeg);
        eulerRad.scale((2f * (float) Math.PI) / 360f);
        setEuler(t3d, eulerRad);
        return t3d;
    }

    public static Vec3 getEulerDeg(Transform3D t3d) {
        Vec3 eulerDeg = getEuler(t3d);
        eulerDeg.scale(360f / (2f * (float) Math.PI));
        return eulerDeg;
    }

    public static Quat4f getQuat4f(Transform3D t3d) {
        Quat4f q = new Quat4f();
        t3d.get(q);
        return q;
    }

    public static Quat4d getQuat4d(Transform3D t3d) {
        Quat4d q = new Quat4d();
        t3d.get(q);
        return q;
    }

    public static Matrix3f getMatrix3f(Transform3D t3d) {
        Matrix3f m3f = new Matrix3f();
        t3d.get(m3f);
        return m3f;
    }

    public static Matrix3d getMatrix3d(Transform3D t3d) {
        Matrix3d m3d = new Matrix3d();
        t3d.get(m3d);
        return m3d;
    }

    public static Matrix4f getMatrix4f(Transform3D t3d) {
        Matrix4f m4f = new Matrix4f();
        t3d.get(m4f);
        return m4f;
    }

    public static Matrix4d getMatrix4d(Transform3D t3d) {
        Matrix4d m4d = new Matrix4d();
        t3d.get(m4d);
        return m4d;
    }

    /**
     * Calculates the transform that post multiplied with parentT3D equlas worldT3D.
     */
    public static <T extends Transform3D> T calculateLocal(T worldT3D, Transform3D parentT3D) {
        Transform3D t3d = new Transform3D(parentT3D);
        t3d.invert();
        t3d.mul(worldT3D);
        worldT3D.set(t3d);
        return worldT3D;
    }

    /**
     * Calculates x = a - b so  that b.mul(x) = a
     */
    public static Mat4 calculateDifference(Transform3D a, Transform3D b) {
        return new Mat4(b).invert_().mul_(a);
    }

    /**
     *
     */
    public static float distance(Transform3D a, Transform3D b) {
        return Vec3.distance(getTranslation(a), getTranslation(b));
    }

    /**
     * Interpolates a Transform3D by interpolateing the position, rotation and
     * scale seperately.
     * @param t1 the first transform
     * @param t2 the second transform
     * @param alpha the alpha interpolator parameter
     * @return the interpolated transform
     */
    public static Mat4 interpolate(Transform3D t1, Transform3D t2, double alpha) {
        Vector3d position1 = new Vector3d();
        Quat4d rotation1 = new Quat4d();
        Vector3d scale1 = new Vector3d();
        Vector3d position2 = new Vector3d();
        Quat4d rotation2 = new Quat4d();
        Vector3d scale2 = new Vector3d();

        t1.get(position1);
        t1.get(rotation1);
        t1.getScale(scale1);
        t2.get(position2);
        t2.get(rotation2);
        t2.getScale(scale2);

        position1.interpolate(position2, alpha);
        rotation1.interpolate(rotation2, alpha);
        scale1.interpolate(scale2, alpha);

        Mat4 t3d = new Mat4();
        t3d.setTranslation(position1);
        t3d.setRotation(rotation1);
        t3d.setScale(scale1);

        return t3d;
    }

    public static Mat4 fromTranslation(Tuple3f translation) {
        Mat4 mat = new Mat4();
        mat.setTranslation(translation);
        return mat;
    }

    public static Mat4 fromTranslation(Tuple3d translation) {
        Mat4 mat = new Mat4();
        mat.setTranslation(translation);
        return mat;
    }

    public static Mat4 fromQuat4f(Quat4f q) {
        return new Mat4().set_(q);
    }

    public static Mat4 fromQuat4d(Quat4d q) {
        return new Mat4().set_(q);
    }

    public static Mat4 fromAxisAngle(AxisAngle4f aa) {
        Mat4 mat = new Mat4();
        mat.set(aa);
        return mat;
    }

    public static Mat4 fromAxisAngle(AxisAngle4d aa) {
        Mat4 mat = new Mat4();
        mat.set(aa);
        return mat;
    }

    public static Mat4 fromEuler(double x, double y, double z) {
        return fromEuler(new Vec3(x, y, z));
    }

    public static Mat4 fromEuler(Tuple3f euler) {
        Mat4 mat = new Mat4();
        setEuler(mat, euler);
        return mat;
    }

    public static Mat4 fromEulerDeg(double x, double y, double z) {
        return fromEulerDeg(new Vec3(x, y, z));
    }

    public static Mat4 fromEulerDeg(Tuple3f eulerDeg) {
        Mat4 mat = new Mat4();
        setEulerDeg(mat, eulerDeg);
        return mat;
    }

    public static Mat4 fromScale(double scale) {
        Mat4 mat = new Mat4();
        mat.setScale(scale);
        return mat;
    }

    /**
     * Calculates a transform from a position and a normal. The y vector will
     * point in the same direction as normal. The z vector will point downwards
     * if possible. If the normal also points down then the z vector will point
     * towards positive z.
     */
    public static Mat4 fromBottomHeavy(Tuple3f pos, Tuple3f normal) {
        Vector3d x = new Vector3d();
        Vector3d y = new Vector3d(normal);
        y.normalize();
        // try to point z down
        Vector3d z = new Vector3d(0, -1, 0);
        if (Math.abs(y.dot(z)) > 0.9) {
            // if that don't work point it in the z direction
            z.set(0, 0, 1);
        }

        x.cross(y, z);
        x.normalize();
        z.cross(x, y);
        Matrix3d rotationMatrix = new Matrix3d();
        rotationMatrix.setRow(0, x);
        rotationMatrix.setRow(1, y);
        rotationMatrix.setRow(2, z);
        rotationMatrix.invert();
        Mat4 t3d = new Mat4();
        t3d.setRotation(rotationMatrix);
        t3d.setTranslation(pos);
        return t3d;
    }

    /**
     * Same as fromBottomHeavy except the original rotation around its y axis will
     * be preserved.
     */
    public static Mat4 fromBottomHeavyPreserveYRot(Tuple3f pos, Tuple3f normal, Transform3D originalT3D) {
        Mat4 bottomHeavy = fromBottomHeavy(getTranslation(originalT3D), getUp(originalT3D));
        Vec3 euler = new Mat4(originalT3D).diff(bottomHeavy).getEuler();
        Mat4 startYRot = fromEuler(new Vec3(0, euler.y, 0));
        Mat4 t3d = fromBottomHeavy(pos, normal).mul_(startYRot);
        return t3d;
    }

    public static void main(String[] args) {
        Mat4 mat = new Mat4();
        mat.lookAtInv(new Vec3(0, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 1, 0));
        System.out.println(mat);
    }
}
