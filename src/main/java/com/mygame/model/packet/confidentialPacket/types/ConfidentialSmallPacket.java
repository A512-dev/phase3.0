// ConfidentialSmallPacket.java
package com.mygame.model.packet.confidentialPacket.types;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.physics.ConstantSpeed;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.confidentialPacket.ConfidentialPacket;

public final class ConfidentialSmallPacket extends ConfidentialPacket {
    public ConfidentialSmallPacket(Vector2D spawn){
        super(spawn, 4);
    }
    @Override public int getCoinValue(){ return 3; }
    @Override public Shape shape(){ return Shape.HEXAGON; }

    @Override
    public Packet copy() {
        return null;
    }

    @Override
    public Vector2D getPosition() {
        return null;
    }

    @Override
    public Vector2D getVelocity() {
        return null;
    }

    @Override
    public double getRadius() {
        return 0;
    }

    @Override
    public double getInvMass() {
        return 0;
    }

    @Override
    public double getRestitution() {
        return 0;
    }

    @Override
    public double getFriction() {
        return 0;
    }

    @Override
    public void setPosition(Vector2D p) {

    }

    @Override
    public void setVelocity(Vector2D v) {

    }
}
