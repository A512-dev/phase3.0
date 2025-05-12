package com.mygame.model;

import com.mygame.util.Database;
import com.mygame.util.Vector2D;

public class TrianglePacket extends Packet {

    private double accel = Database.TRIANGLE_BASE_IMPULSE;
    public TrianglePacket(Vector2D pos, Vector2D vel) {
        super(pos, vel, 3, 2, 12);
    }

    @Override
    protected void onUpdate(double dt) {
//
//        Vector2D accelVector = velocity.normalized().multiplied(accel);
//        velocity.add(accelVector.multiplied(dt));
//        //System.out.println();
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
        TrianglePacket clone = new TrianglePacket(position.copy(), velocity.copy());
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
