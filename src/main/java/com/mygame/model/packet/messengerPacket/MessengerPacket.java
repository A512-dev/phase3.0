package com.mygame.model.packet.messengerPacket;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

public class MessengerPacket extends Packet {
    private final Shape packetShape;
    public MessengerPacket(Vector2D spawn, Shape packetShape, double radius) {
        super(spawn, 1, radius);
        this.packetShape = packetShape;
    }

    @Override
    public int getCoinValue() {
        return 1;
    }


    @Override
    public Shape shape() {
        return packetShape;
    }
    @Override public MessengerPacket copy(){
        MessengerPacket clone = new MessengerPacket(pos.copy(), packetShape, radius);
        clone.setVelocity(this.getVelocity().copy());
        clone.setAcceleration(this.getAcceleration().copy());
        clone.setMobile(this.isMobile());
        clone.setAlive(this.isAlive());
        clone.setOpacity(this.getOpacity());
        return clone;    }
}
