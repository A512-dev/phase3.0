package server.sim.model.packet.bulkPacket.types;

import shared.Vector2D;
import server.sim.model.packet.bulkPacket.BulkPacket;
import shared.model.PacketShape;

public final class BulkPacketA extends BulkPacket {

    public BulkPacketA(Vector2D spawn,int payload,  double health) { super(spawn, payload, health);}

    @Override
    public int getCoinValue() { return 8; }        // طبق سند

    @Override
    public PacketShape shape() { return PacketShape.BULK_A; }

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
