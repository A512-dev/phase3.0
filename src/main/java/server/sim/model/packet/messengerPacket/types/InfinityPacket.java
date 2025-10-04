

package server.sim.model.packet.messengerPacket.types;

        import server.sim.engine.physics.Vector2D;
        import server.sim.model.packet.messengerPacket.MessengerPacket;

public final class InfinityPacket extends MessengerPacket {

    public InfinityPacket(Vector2D spawn, double health, double radius) {
        super(spawn, Shape.INFINITY, radius); // Health/size = 1 unit
        this.health = health;
    }

    @Override
    public int getCoinValue() {
        return 1;
    }

    @Override
    public Shape shape() {
        return Shape.INFINITY;
    }

    @Override
    public InfinityPacket copy() {
        InfinityPacket copy = new InfinityPacket(pos.copy(), getHealth(), getRadius());
        copy.setVelocity(vel.copy());
        return copy;
    }

    @Override public Vector2D getPosition()    { return pos; }
    @Override public Vector2D getVelocity()    { return vel; }
    @Override public double   getRadius()      { return radius; }
    @Override public double   getInvMass()     { return invMass; }
    @Override public double   getRestitution() { return restitution; }
    @Override public double   getFriction()    { return friction; }
    @Override public void     setPosition(Vector2D p) { pos.set(p); }
    @Override public void     setVelocity(Vector2D v) { vel.set(v); }
}
