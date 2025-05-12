package com.mygame.model;

import com.mygame.util.Database;
import com.mygame.util.Vector2D;
import java.awt.Graphics2D;

public abstract class Packet {
    protected Vector2D position;
    protected int life;
    protected int coinValue;
    protected double size;
    protected Vector2D velocity;
    protected Vector2D impactImpulse = new Vector2D();  // gets added temporarily
    protected double impulseDecay = 3.0;  // units per second
    protected Vector2D pathStart;
    protected Vector2D pathEnd;
    protected boolean mobile = false;



    public Packet(Vector2D pos, Vector2D vel, int life, int coinValue, double size) {
        this.position = new Vector2D(pos);
        this.velocity = new Vector2D(vel);
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


    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public void setPath(Vector2D start, Vector2D end) {
        this.pathStart = start;
        this.pathEnd = end;
        this.velocity = new Vector2D(end.x - start.x, end.y - start.y).normalized().multiplied(Database.speedOfPackets);
    }



    public void update(double dt) {
        // Total velocity = base + impulse
        Vector2D totalVelocity = velocity.added(impactImpulse);
        position.add(totalVelocity.multiplied(dt));

        // Decay the impulse gradually back to 0
        impactImpulse.multiply(Math.max(0, 1 - impulseDecay * dt));

        onUpdate(dt);
    }

    public void setImpactImpulse(Vector2D impactImpulse) {
        this.impactImpulse = impactImpulse;
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

    public double getSize() { return size; }
    public int getLife() { return life; }
    public int getCoinValue() { return coinValue; }

    public Vector2D getPathStart() {
        return pathStart;
    }

    public void setPathStart(Vector2D pathStart) {
        this.pathStart = pathStart;
    }

    public Vector2D getPathEnd() {
        return pathEnd;
    }

    public void setPathEnd(Vector2D pathEnd) {
        this.pathEnd = pathEnd;
    }

    public boolean isOffTrackLine(double maxDistance) {
        Vector2D p = getPosition();
        Vector2D a = getPathStart();
        Vector2D b = getPathEnd();

        // Distance from point p to line segment ab
        double dist = distanceToSegment(p, a, b);
        return dist > maxDistance;
    }
    public boolean isOffTrackLine(int maxDistanceToBeOfTheLine, Vector2D predictedPos) {
        Vector2D p = predictedPos;
        Vector2D a = getPathStart();
        Vector2D b = getPathEnd();

        // Distance from point p to line segment ab
        double dist = distanceToSegment(p, a, b);
        return dist > maxDistanceToBeOfTheLine;
    }
    /** Returns true if this packet has reached (or passed) its pathEnd. */
    public boolean hasArrived() {
        return position.distanceTo(pathEnd) <= Database.THRESHOLD_FOR_REACHING_PORT;
    }


    // Helper function
    public double distanceToSegment(Vector2D p, Vector2D a, Vector2D b) {
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
