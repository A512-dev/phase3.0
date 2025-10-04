// MessengerTrianglePacket.java
package server.sim.model.packet.messengerPacket.types;
import server.sim.engine.physics.Vector2D;
import server.sim.model.packet.messengerPacket.MessengerPacket;

public final class TrianglePacket extends MessengerPacket {
    public TrianglePacket(Vector2D spawn, double health, double radius){
        super(spawn, Shape.TRIANGLE, radius);
        this.health = health;
    }
    @Override public int getCoinValue(){ return 3; }
    @Override public Shape shape(){ return Shape.TRIANGLE; }

    @Override
    public TrianglePacket copy() {
        TrianglePacket newT = new TrianglePacket(pos, getHealth(), getRadius());
        newT.setVelocity(vel);
        return newT;
    }

    @Override
    public Vector2D getPosition() {
        return pos;
    }

    @Override
    public Vector2D getVelocity() {
        return vel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public double getInvMass() {
        return invMass;
    }

    @Override
    public double getRestitution() {
        return restitution;
    }

    @Override
    public double getFriction() {
        return friction;
    }

    @Override
    public void setPosition(Vector2D p) {
        pos.set(p);
    }

    @Override
    public void setVelocity(Vector2D v) {
        vel.set(v);
    }
}