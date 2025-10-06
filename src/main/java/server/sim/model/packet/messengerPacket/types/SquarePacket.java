package server.sim.model.packet.messengerPacket.types;

import shared.Vector2D;
import server.sim.model.packet.messengerPacket.MessengerPacket;
import shared.model.PacketShape;

public final class SquarePacket extends MessengerPacket {
    public SquarePacket(Vector2D spawn, double health, double radius) {
        super(spawn, PacketShape.SQUARE, radius);  // 2 = health (aka size/strength)
        this.health = health;
    }

    @Override public int getCoinValue() { return 2; }

    @Override public PacketShape shape() { return PacketShape.SQUARE; }

    @Override public SquarePacket copy() {
        SquarePacket copy = new SquarePacket(pos.copy(), getHealth(), getRadius());
        copy.setVelocity(vel.copy());
        return copy;
    }

    @Override public Vector2D getPosition()     { return pos; }
    @Override public Vector2D getVelocity()     { return vel; }
    @Override public double   getRadius()       { return radius; }
    @Override public double   getInvMass()      { return invMass; }
    @Override public double   getRestitution()  { return restitution; }
    @Override public double   getFriction()     { return friction; }
    @Override public void     setPosition(Vector2D p) { pos.set(p); }
    @Override public void     setVelocity(Vector2D v) { vel.set(v); }
}
