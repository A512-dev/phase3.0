package com.mygame.model.packet.messengerPacket.types;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.messengerPacket.MessengerPacket;

public final class SquarePacket extends MessengerPacket {
    public SquarePacket(Vector2D spawn, int health) {
        super(spawn, Shape.SQUARE);  // 2 = health (aka size/strength)
        this.health = health;
    }

    @Override public int getCoinValue() { return 2; }

    @Override public Shape shape() { return Shape.SQUARE; }

    @Override public SquarePacket copy() {
        SquarePacket copy = new SquarePacket(pos.copy(), getHealth());
        copy.setVelocity(vel.copy());
        return copy;
    }

    @Override public Vector2D getPosition()     { return pos; }
    @Override public Vector2D getVelocity()     { return vel; }
    @Override public double   getRadius()       { return radius; }
    @Override public double   getInvMass()      { return invMass; }
    @Override public double   getRestitution()  { return restitution; }
    @Override public double   getFriction()     { return friction; }
    @Override public void     setPosition(Vector2D p) { pos.set(p); }
    @Override public void     setVelocity(Vector2D v) { vel.set(v); }
}
