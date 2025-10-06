package server.sim.model.packet.confidentialPacket;

import shared.Vector2D;
import server.sim.model.packet.Packet;
import shared.model.PacketShape;

public abstract class ConfidentialPacket extends Packet {

    protected ConfidentialPacket(Vector2D spawn, double health, int sizeUnits) {
        super(spawn, health, sizeUnits);
    }

    @Override
    public boolean isConfidentialPacket() {
        return true;
    }
    @Override
    public PacketShape shape() {
        return PacketShape.HEXAGON;
    }

    @Override
    public abstract Packet copy(); // force subclasses to copy fully
}
