package server.sim.engine.physics;

import java.util.Objects;

public class Vector2D {
    private double x;
    private double y;

    // Constructors
    public Vector2D() {
        this(0, 0);
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D other) {
        this(other.x, other.y);
    }

    // Getters and setters
    public double x() { return x; }
    public double y() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    // Basic arithmetic (in-place)
    public Vector2D add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vector2D subtract(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }



    public Vector2D divide(double scalar) {
        if (scalar != 0) {
            this.x /= scalar;
            this.y /= scalar;
        }
        return this;
    }

    // Functional (non-mutating)
    public Vector2D added(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D subtracted(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }


    public Vector2D divided(double scalar) {
        return scalar != 0 ? new Vector2D(this.x / scalar, this.y / scalar) : new Vector2D(0, 0);
    }

    // Vector operations
    public double dot(Vector2D v) {
        return this.x * v.x + this.y * v.y;
    }

    public double lengthSq() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public Vector2D normalize() {
        double len = length();
        if (len != 0) {
            this.x /= len;
            this.y /= len;
        }
        return this;
    }

    public Vector2D normalized() {
        double len = length();
        return len != 0 ? new Vector2D(x / len, y / len) : new Vector2D();
    }

    public double distanceTo(Vector2D v) {
        double dx = this.x - v.x;
        double dy = this.y - v.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Vector2D perpendicular() {
        return new Vector2D(-y, x);
    }

    public Vector2D copy() {
        return new Vector2D(this.x, this.y);
    }

    // Standard overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2D)) return false;
        Vector2D v = (Vector2D) o;
        return Double.compare(v.x, x) == 0 &&
                Double.compare(v.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("Vector2D(%.3f, %.3f)", x, y);
    }


    /** In-place addition: this += (dx, dy) */
    public Vector2D add(double dx, double dy) {
        this.x += dx;
        this.y += dy;
        return this;
    }

    // In Vector2D.java
    public Vector2D set(Vector2D other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public Vector2D set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }
    // --- add once, near the bottom of Vector2D ---
    public Vector2D multiply(double s)       { this.x*=s; this.y*=s; return this; }
    public Vector2D multiplied(double s)     { return new Vector2D(x*s, y*s); }



    /** Distance from point {@code p} to the line-segment AB. */
    public static double distPointToSegment(Vector2D p,
                                            Vector2D a,
                                            Vector2D b)
    {
        Vector2D ap = p.subtracted(a);
        Vector2D ab = b.subtracted(a);

        double abLenSq = ab.lengthSq();
        if (abLenSq < 1e-9)   // A and B coincide
            return p.distanceTo(a);

        double t = Math.max(0, Math.min(1, ap.dot(ab) / abLenSq)); // clamp 0‒1
        Vector2D proj = a.added(ab.multiplied(t));                 // projection
        return proj.distanceTo(p);
    }

}
