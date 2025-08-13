package com.mygame.model.packet.confidentialPacket;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

public abstract class ConfidentialPacket extends Packet {

    protected ConfidentialPacket(Vector2D spawn, int health, int sizeUnits) {
        super(spawn, health, sizeUnits);
    }

    @Override
    public boolean isConfidentialPacket() {
        return true;
    }
    @Override
    public Shape shape() {
        return Shape.HEXAGON;
    }

    @Override
    public abstract Packet copy(); // force subclasses to copy fully
}
