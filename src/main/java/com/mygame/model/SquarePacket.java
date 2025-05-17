package com.mygame.model;

import com.mygame.util.Vector2D;

public class SquarePacket extends Packet {
    public SquarePacket(Vector2D pos, Vector2D vel) {
        super(pos, vel, 4, 1, 10);
    }

    @Override
    protected void onUpdate(double dt) {
        // Constant speed; no acceleration
    }

    @Override
    public void render(java.awt.Graphics2D g) {
        // Rendering handled by PacketView
    }
    @Override
    public int getMaxLife() {
        return 4;
    }
    @Override
    public Packet copy() {
        SquarePacket clone = new SquarePacket(position.copy(), velocity.copy());
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
