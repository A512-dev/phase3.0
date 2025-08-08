package com.mygame.model.packet.confidentialPacket;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

public class ConfidentialPacket extends Packet {
    protected ConfidentialPacket(Vector2D spawn, int health) {
        super(spawn, health);
    }
    @Override public Shape shape() { return Shape.CONFIDENTIAL_A; }

    /* Randomised routing each hop → override before node dispatch */
    @Override public Packet copy(){ return new ConfidentialPacket(pos.copy(), health); }
    @Override public int getCoinValue()  { return 5; }         // example value
}
