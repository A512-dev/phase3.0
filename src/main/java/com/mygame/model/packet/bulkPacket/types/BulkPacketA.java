package com.mygame.model.packet.bulkPacket.types;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.bulkPacket.BulkPacket;

public final class BulkPacketA extends BulkPacket {

    public BulkPacketA(Vector2D spawn,int payload,  double health) { super(spawn, payload, health);}

    @Override
    public int getCoinValue() { return 8; }        // طبق سند

    @Override
    public Shape shape() { return Shape.BULK_A; }

    @Override
    public BulkPacketA copy() {
        BulkPacketA cp = new BulkPacketA(pos.copy(), payloadSize, getHealth());
        cp.heavyId = this.heavyId;
        cp.setVelocity(vel.copy());
        cp.setAcceleration(getAcceleration().copy());
        cp.setProtectedPacket(isProtectedPacket());
        return cp;
    }
}
