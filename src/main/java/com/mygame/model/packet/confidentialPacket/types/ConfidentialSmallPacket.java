package com.mygame.model.packet.confidentialPacket.types;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.confidentialPacket.ConfidentialPacket;

public final class ConfidentialSmallPacket extends ConfidentialPacket {

    public ConfidentialSmallPacket(Vector2D spawn) {
        super(spawn, GameConfig.CONFIDENTIAL_SMALL_PACKET_LIFE, GameConfig.CONFIDENTIAL_SMALL_PACKET_SIZE); // Size = 4 units
    }

    @Override
    public int getCoinValue() {
        return GameConfig.CONFIDENTIAL_SMALL_PACKET_COIN_VALUE;
    }

    @Override
    public Packet copy() {
        ConfidentialSmallPacket copy = new ConfidentialSmallPacket(pos.copy());
        copy.setVelocity(vel.copy());
        copy.setAcceleration(getAcceleration().copy());
        return copy;
    }

    @Override public Vector2D getPosition()    { return pos; }
    @Override public Vector2D getVelocity()    { return vel; }
    @Override public double   getRadius()      { return radius; }
    @Override public double   getInvMass()     { return invMass; }
    @Override public double   getRestitution() { return restitution; }
    @Override public double   getFriction()    { return friction; }
    @Override public void     setPosition(Vector2D p) { pos.set(p); }
    @Override public void     setVelocity(Vector2D v) { vel.set(v); }

    @Override
    public Shape shape() {
        return Shape.CONFIDENTIAL_S;
    }
}
