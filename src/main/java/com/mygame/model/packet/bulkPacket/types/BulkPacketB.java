package com.mygame.model.packet.bulkPacket.types;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.bulkPacket.BulkPacket;

public final class BulkPacketB extends BulkPacket {

    public BulkPacketB(Vector2D spawn) { super(spawn, 10); }

    @Override
    public int getCoinValue() { return 10; }       // طبق سند

    @Override
    public Shape shape() { return Shape.BULK_B; }

    @Override
    public BulkPacketB copy() {
        BulkPacketB cp = new BulkPacketB(pos.copy());
        cp.heavyId = this.heavyId;
        cp.setVelocity(vel.copy());
        cp.setAcceleration(getAcceleration().copy());
        cp.setProtectedPacket(isProtectedPacket());
        return cp;
    }
}
