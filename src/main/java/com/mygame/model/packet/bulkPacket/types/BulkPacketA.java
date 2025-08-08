// BulkPacketA.java  (size 8)
package com.mygame.model.packet.bulkPacket.types;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.physics.ConstantSpeed;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.bulkPacket.BulkPacket;

public final class BulkPacketA extends BulkPacket {
    public BulkPacketA(Vector2D spawn){
        super(spawn, 8);
    }
    @Override public int getCoinValue(){ return 8; }
    @Override public Shape shape(){ return Shape.BULK_A; }

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