package com.mygame.model.packet.bulkPacket;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

import java.util.UUID;

public class BulkPacket extends Packet {
    public BulkPacket(Vector2D spawn, int size) {
        super(spawn, 3);
        this.sizeUnits = size;
        this.heavyId   = UUID.randomUUID().hashCode();
    }
    @Override public int getCoinValue()  { return Math.max(sizeUnits - 1, 1); }
    @Override public Shape shape() { return Shape.HEXAGON; }

    @Override
    public Packet copy() {
        return new BulkPacket(pos.copy(), sizeUnits);
    }
}
