package server.sim.model.packet.bulkPacket.types;

import server.sim.engine.physics.Vector2D;
import server.sim.model.packet.bulkPacket.BulkPacket;

public final class BulkPacketB extends BulkPacket {

    public BulkPacketB(Vector2D spawn,int payload,  double health) { super(spawn, payload, health); }

    @Override
    public int getCoinValue() { return 10; }       // طبق سند

    @Override
    public Shape shape() { return Shape.BULK_B; }

    @Override
    public BulkPacketB copy() {
        BulkPacketB cp = new BulkPacketB(pos.copy(), payloadSize, getHealth());
        cp.heavyId = this.heavyId;
        cp.setVelocity(vel.copy());
        cp.setAcceleration(getAcceleration().copy());
        cp.setProtectedPacket(isProtectedPacket());
        return cp;
    }
}
