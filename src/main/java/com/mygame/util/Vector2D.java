// Vector2D.java
package com.mygame.util;

public class Vector2D {
    public double x;
    public double y;

    /** Creates a zero vector (0,0). */
    public Vector2D() {
        this(0, 0);
    }

    /** Creates a vector with the given components. */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Copy constructor. */
    public Vector2D(Vector2D v) {
        this(v.x, v.y);
    }

    /** Adds v to this vector (in place). */
    public Vector2D add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    /** Returns a new vector = this + v. */
    public Vector2D added(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    /** Subtracts v from this vector (in place). */
    public Vector2D subtract(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    /** Returns a new vector = this – v. */
    public Vector2D subtracted(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    /** Multiplies this vector by scalar s (in place). */
    public Vector2D multiply(double s) {
        this.x *= s;
        this.y *= s;
        return this;
    }

    /** Returns a new vector = this × s. */
    public Vector2D multiplied(double s) {
        return new Vector2D(this.x * s, this.y * s);
    }

    /** Returns the dot product of this and v. */
    public double dot(Vector2D v) {
        return this.x * v.x + this.y * v.y;
    }

    /** Returns the squared length (magnitude²). */
    public double lengthSq() {
        return x * x + y * y;
    }

    /** Returns the length (magnitude). */
    public double length() {
        return Math.sqrt(lengthSq());
    }

    /** Normalizes this vector to unit length (in place). */
    public Vector2D normalize() {
        double len = length();
        if (len != 0) {
            this.x /= len;
            this.y /= len;
        }
        return this;
    }

    /** Returns a new unit-length vector pointing in the same direction. */
    public Vector2D normalized() {
        double len = length();
        return (len != 0)
                ? new Vector2D(x / len, y / len)
                : new Vector2D(0, 0);
    }

    /** Returns the distance to vector v. */
    public double distanceTo(Vector2D v) {
        double dx = this.x - v.x;
        double dy = this.y - v.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.3f, %.3f)", x, y);
    }
    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }

}
