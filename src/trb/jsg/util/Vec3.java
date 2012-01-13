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

import java.awt.Color;
import javax.vecmath.*;

/**
 *
 * @author tomrbryn
 */
public class Vec3 extends Vector3f {

    private static final ThreadLocal<Mat4> tempMat4 = new ThreadLocal();
    static {
        tempMat4.set(new Mat4());
    }

    public Vec3() {
        super();
    }

    public Vec3(float x, float y, float z) {
        super(x, y, z);
    }

    public Vec3(double x, double y, double z) {
        super((float) x, (float) y, (float) z);
    }

    public Vec3(Tuple3f tuple) {
        super(tuple);
    }

    public Vec3(Tuple3d tuple) {
        super(tuple);
    }

    /** w is ignored */
    public Vec3(Tuple4f tuple) {
        super(tuple.x, tuple.y, tuple.z);
    }

    public Vec3(Color c) {
        super(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
    }

    public Vec3(float[] floats) {
        super(floats);
    }

    public Vec3(float[] floats, int offset) {
        this(floats[offset], floats[offset+1], floats[offset+2]);
    }

    public double[] toDoubles() {
        return new double[]{x, y, z};
    }

    public float[] toFloats() {
        return new float[]{x, y, z};
    }

    public Vec3 absolute_() {
        super.absolute();
        return this;
    }

    public Vec3 add_(Tuple3f t) {
        super.add(t);
        return this;
    }

    public Vec3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3 add(double x, double y, double z) {
        this.x += (float) x;
        this.y += (float) y;
        this.z += (float) z;
        return this;
    }

    public Vec3 sub_(Tuple3f t) {
        super.sub(t);
        return this;
    }

    public Vec3 sub(float x, float y, float z) {
    	this.x -= x;
    	this.y -= y;
    	this.z -= z;
        return this;
    }

    public Vec3 sub(Tuple3d tuple3d) {
        return sub((float) tuple3d.x, (float) tuple3d.y, (float) tuple3d.z);
    }

    public float angle(Tuple3f t1) {
        return angle(this, t1);
    }

    public static float angle(Tuple3f t1, Tuple3f t2) {
        double vDot = dot(t1, t2) / (length(t1) * length(t2));
        if (vDot < -1.0) {
            vDot = -1.0;
        }
        if (vDot > 1.0) {
            vDot = 1.0;
        }
        return ((float) (Math.acos(vDot)));
    }

    /**
     * Gets the angle between t1 and t2 and in the range [-PI, PI]. The rotation
     * is negative if the cross product between t1 and t2 don't point in the
     * same direction as the normal.
     */
    public static float angle2PI(Tuple3f t1, Tuple3f t2, Tuple3f normal) {
        if (lengthSquared(t1) == 0 || lengthSquared(t2) == 0) {
            return 0;
        }

        double vDot = dot(t1, t2) / (length(t1) * length(t2));
        if (vDot < -1.0) {
            vDot = -1.0;
        }
        if (vDot > 1.0) {
            vDot = 1.0;
        }
        double angle = Math.acos(vDot);
        Vec3 cross = new Vec3().cross_(t1, t2);
        if (cross.dot(normal) < 0) {
            angle *= -1;
        }

        return (float) angle;
    }

    public Vec3 clampMin_(float min) {
        super.clampMin(min);
        return this;
    }

    public Vec3 clampMin(Tuple3f min) {
        return clampMin(min.x, min.y, min.z);
    }

    public Vec3 clampMin(float minx, float miny, float minz) {
        x = Math.max(x, minx);
        y = Math.max(y, miny);
        z = Math.max(z, minz);
        return this;
    }

    public Vec3 clampMax(Tuple3f max) {
        return clampMax(max.x, max.y, max.z);
    }

    public Vec3 clampMax(float maxx, float maxy, float maxz) {
        x = Math.min(x, maxx);
        y = Math.min(y, maxy);
        z = Math.min(z, maxz);
        return this;
    }

    public Vec3 clampMax_(float max) {
        super.clampMax(max);
        return this;
    }

    public Vec3 clamp_(float min, float max) {
        super.clamp(min, max);
        return this;
    }

    public Vec3 clampLengthMax(float max) {
        float length = length();
        if (length > max) {
            scale(max / length);
        }
        return this;
    }

    public float max() {
        return Math.max(x, Math.max(y, z));
    }

    public Vec3 cross_(Tuple3f v1, Tuple3f v2) {
        float x = v1.y * v2.z - v1.z * v2.y;
        float y = v2.x * v1.z - v2.z * v1.x;
        this.z = v1.x * v2.y - v1.y * v2.x;
        this.x = x;
        this.y = y;
        return this;
    }

    public float distance(Tuple3f t) {
        return distance(this, t);
    }

    public static float distance(Tuple3f a, Tuple3f b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        float dz = b.z - a.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distance(Tuple3d t) {
        return distance(this, t);
    }

    public static float distance(Tuple3f a, Tuple3d b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float distanceL1(Tuple3f t) {
        return distanceL1(this, t);
    }

    public static float distanceL1(Tuple3f t1, Tuple3f t2) {
        return (Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y) + Math.abs(t1.z - t2.z));
    }

    public float distanceLinf(Tuple3f t) {
        return distanceLinf(this, t);
    }

    public static float distanceLinf(Tuple3f t1, Tuple3f t2) {
        float tmp = Math.max(Math.abs(t1.x-t2.x), Math.abs(t1.y-t2.y));
        return (Math.max(tmp ,Math.abs(t1.z-t2.z)));
    }

    public float distanceSquared(Tuple3f t1) {
        return distanceSquared(this, t1);
    }

    public static float distanceSquared(Tuple3f t1, Tuple3f t2) {
        float dx = t1.x - t2.x;
        float dy = t1.y - t2.y;
        float dz = t1.z - t2.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float dot(Tuple3f t1) {
        return dot(this, t1);
    }

    public static float dot(Tuple3f t1, Tuple3f t2) {
        return (t1.x * t2.x + t1.y * t2.y + t1.z * t2.z);
    }

    public Color getColor() {
        int r = Math.round(x * 255.0f);
        int g = Math.round(y * 255.0f);
        int b = Math.round(z * 255.0f);

        return new Color(r, g, b);
    }

    public Vec3 interpolate_(Tuple3f t1, float alpha) {
        super.interpolate(t1, alpha);
        return this;
    }

    public Vec3 interpolate_(Tuple3f t1, Tuple3f t2, float alpha) {
        super.interpolate(t1, t2, alpha);
        return this;
    }

    public static float length(Tuple3f t1) {
        return (float) Math.sqrt(t1.x*t1.x + t1.y*t1.y + t1.z*t1.z);
    }

    public static float lengthSquared(Tuple3f t1) {
        return (t1.x * t1.x + t1.y * t1.y + t1.z * t1.z);
    }

    public Vec3 negate_() {
        super.negate();
        return this;
    }

    /**
     * Converts elements from radians to degrees.
     * @return this
     */
    public Vec3 toDegrees() {
        x = (float) Math.toDegrees(x);
        y = (float) Math.toDegrees(y);
        z = (float) Math.toDegrees(z);
        return this;
    }

    /**
     * Converts elements from degrees to radians.
     * @return this
     */
    public Vec3 toRadians() {
        x = (float) Math.toRadians(x);
        y = (float) Math.toRadians(y);
        z = (float) Math.toRadians(z);
        return this;
    }

    public Vec3 normalize_() {
        super.normalize();
        return this;
    }

    public Vec3 project(Tuple4f p1) {
        float oneOw = 1 / p1.w;
        x = p1.x * oneOw;
        y = p1.y * oneOw;
        z = p1.z * oneOw;
        return this;
    }

    public Vec3 scale(Tuple3f scale) {
        x *= scale.x;
        y *= scale.y;
        z *= scale.z;
        return this;
    }

    public Vec3 scale(float scalex, float scaley, float scalez) {
        x *= scalex;
        y *= scaley;
        z *= scalez;
        return this;
    }

    public Vec3 scale(double scalex, double scaley, double scalez) {
        x *= scalex;
        y *= scaley;
        z *= scalez;
        return this;
    }

    public Vec3 scale(Tuple3d scale) {
        x *= scale.x;
        y *= scale.y;
        z *= scale.z;
        return this;
    }

    public Vec3 scale_(float scale) {
        super.scale((float) scale);
        return this;
    }

    public Vec3 scale(double scale) {
        super.scale((float) scale);
        return this;
    }

    public Vec3 scaleAdd_(float s, Tuple3f t1) {
        super.scaleAdd(s, t1);
        return this;
    }

    public Vec3 scaleAdd_(float s, Tuple3f t1, Tuple3f t2) {
        super.scaleAdd(s, t1, t2);
        return this;
    }

    public float get(int index) {
        switch (index) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
        }
        return Float.NaN;
    }

    public Vec3 set(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        return this;
    }

    public Vec3 set_(float[] t) {
        super.set(t);
        return this;
    }

    public Vec3 set_(float x, float y, float z) {
        super.set(x, y, z);
        return this;
    }

    public Vec3 set_(Tuple3f t1) {
        super.set(t1);
        return this;
    }

    public Vec3 set(int index, float value) {
        switch (index) {
            case 0:
                x = value;
                break;
            case 1:
                y = value;
                break;
            case 2:
                z = value;
                break;
        }
        return this;
    }

    public Vec3 setX_(float value) {
        super.setX(value);
        return this;
    }

    public Vec3 setY_(float value) {
        super.setY(value);
        return this;
    }

    public Vec3 setZ_(float value) {
        super.setZ(value);
        return this;
    }

    public Vec3 setColor(Color color) {
        x = (float) color.getRed() / 255.0f;
        y = (float) color.getGreen() / 255.0f;
        z = (float) color.getBlue() / 255.0f;
        return this;
    }

    /**
     * Sets this to the euler equivalent of the specified quaternion.
     */
    public Vec3 set(Quat4f q1) {
        set(tempMat4.get().set_(q1).getEuler());
        return this;
    }

    /**
     * Sets this to the euler equivalent of the specified axis-angle.
     */
    public Vec3 set(AxisAngle4f a1) {
        set(tempMat4.get().set_(a1).getEuler());
        return this;
    }

    /** Returns true if x, y or z is NaN */
    public boolean containsNaN() {
        return Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z);
    }

    public static Vec3 parse(String[] strings) {
        if (strings.length < 3) {
            throw new IllegalArgumentException("strings.length < 3");
        }
        Vec3 vec = new Vec3();
        vec.x = Float.parseFloat(strings[0]);
        vec.y = Float.parseFloat(strings[1]);
        vec.z = Float.parseFloat(strings[2]);
        return vec;
    }

    /**
     * set length on vector
     *
     */
    public Vec3 setLength(float diff) {

        float length = length();

        if ( length > 0 ) {
            scale(diff/length);
        }

        return this;
    }
}
