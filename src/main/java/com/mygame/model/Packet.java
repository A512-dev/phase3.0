package com.mygame.model;

import com.mygame.util.Vector2D;
import java.awt.Graphics2D;

public abstract class Packet {
    protected Vector2D position;
    protected Vector2D velocity;
    protected int life;
    protected int coinValue;
    protected double size;
    protected Vector2D baseVelocity;
    protected Vector2D impactImpulse = new Vector2D();  // gets added temporarily
    protected double impulseDecay = 3.0;  // units per second
    protected Vector2D pathStart;
    protected Vector2D pathEnd;
    protected boolean mobile = false;



    public Packet(Vector2D pos, Vector2D vel, int life, int coinValue, double size) {
        this.position = new Vector2D(pos);
        this.velocity = new Vector2D(vel);
        this.baseVelocity = new Vector2D(vel);
        this.life = life;
        this.coinValue = coinValue;
        this.size = size;

        // default dummy path (can be replaced after spawn)
        this.pathStart = pos;
        this.pathEnd = pos.added(vel.normalized().multiplied(500));  // assume forward path
    }



    public void setMobile(boolean isMobile) {
        this.mobile = isMobile;
    }

    public boolean isMobile() {
        return mobile;
    }



    public void setPath(Vector2D start, Vector2D end) {
        this.pathStart = start;
        this.pathEnd = end;
        position = start;
        this.baseVelocity = new Vector2D(end.x - start.x, end.y - start.y).normalized().multiplied(30);
    }



    public void update(double dt) {
        // Total velocity = base + impulse
        Vector2D totalVelocity = baseVelocity.added(impactImpulse);
        position.add(totalVelocity.multiplied(dt));

        // Decay the impulse gradually back to 0
        impactImpulse.multiply(Math.max(0, 1 - impulseDecay * dt));

        onUpdate(dt);
    }
    public Vector2D getImpulse() {
        return impactImpulse;
    }
    protected abstract void onUpdate(double dt);
    public abstract void render(Graphics2D g);

    public void onCollision() {
        life = Math.max(0, life - 1);
    }

    public boolean isAlive() {
        return life > 0;
    }
    public void setUnAlive() {
       life = 0;
    }

    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }
    public double getSize() { return size; }
    public int getLife() { return life; }
    public int getCoinValue() { return coinValue; }

    public boolean isOffTrackLine(double maxDistance) {
        Vector2D p = this.position;
        Vector2D a = this.pathStart;
        Vector2D b = this.pathEnd;

        // Distance from point p to line segment ab
        double dist = distanceToSegment(p, a, b);
        return dist > maxDistance;
    }

    // Helper function
    private double distanceToSegment(Vector2D p, Vector2D a, Vector2D b) {
        Vector2D ab = b.subtracted(a);
        Vector2D ap = p.subtracted(a);

        double t = ap.dot(ab) / ab.lengthSq();
        t = Math.max(0, Math.min(1, t));  // clamp to segment
        Vector2D closest = a.added(ab.multiplied(t));
        return p.distanceTo(closest);

    }
    public float getOpacity() {
        return Math.max(0.2f, (float) life / getMaxLife());
    }

    // Override in subclasses to define max life
    public abstract int getMaxLife();


    public abstract Packet copy();
}
