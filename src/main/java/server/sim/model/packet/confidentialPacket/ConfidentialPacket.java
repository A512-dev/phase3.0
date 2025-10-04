package server.sim.model.packet.confidentialPacket;

import server.sim.engine.physics.Vector2D;
import server.sim.model.packet.Packet;

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
