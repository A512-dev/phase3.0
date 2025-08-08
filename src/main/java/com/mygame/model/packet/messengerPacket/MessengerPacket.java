package com.mygame.model.packet.messengerPacket;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

public class MessengerPacket extends Packet {
    private final Shape packetShape;
    public MessengerPacket(Vector2D spawn, Shape packetShape) {
        super(spawn, 1);
        this.packetShape = packetShape;
    }

    @Override
    public int getCoinValue() {
        return 0;
    }


    @Override
    public Shape shape() {
        return packetShape;
    }
    @Override public MessengerPacket copy(){
        MessengerPacket clone = new MessengerPacket(pos.copy(), packetShape);
        clone.setVelocity(this.getVelocity().copy());
        clone.setAcceleration(this.getAcceleration().copy());
        clone.setMobile(this.isMobile());
        clone.setAlive(this.isAlive());
        clone.setOpacity(this.getOpacity());
        return clone;    }
}
