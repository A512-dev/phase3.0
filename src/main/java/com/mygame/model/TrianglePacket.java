package com.mygame.model;

import com.mygame.util.Vector2D;

public class TrianglePacket extends Packet {
    private double accel = 20.0;

    public TrianglePacket(Vector2D pos, Vector2D vel) {
        super(pos, vel, 3, 2, 12);
    }

    @Override
    protected void onUpdate(double dt) {
        Vector2D acceleration = velocity.normalized().multiplied(accel * dt);
        velocity.add(acceleration);
        System.out.println();
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        // Rendering handled by PacketView
    }
    @Override
    public int getMaxLife() {
        return 3;
    }
    @Override
    public Packet copy() {
        TrianglePacket clone = new TrianglePacket(position.copy(), baseVelocity.copy());
        clone.velocity = velocity.copy();
        clone.impactImpulse = impactImpulse.copy();
        clone.life = life;
        clone.coinValue = coinValue;
        clone.size = size;
        clone.pathStart = pathStart.copy();
        clone.pathEnd = pathEnd.copy();
        return clone;
    }

}
