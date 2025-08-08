package com.mygame.model.packet.bulkPacket;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

public class BitPacket extends Packet {
    protected BitPacket(Vector2D spawn, int health) {
        super(spawn, health);
    }

    @Override
    public int getCoinValue() {
        return 0;
    }

    @Override
    public Shape shape() {
        return Shape.INFINITY;
    }

    @Override
    public Packet copy() {
        return new BitPacket(pos.copy(), heavyId);
    }
}
