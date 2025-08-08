// MessengerInfinityPacket.java
package com.mygame.model.packet.messengerPacket.types;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.physics.Accelerated;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.messengerPacket.MessengerPacket;

import java.security.MessageDigest;

public final class InfinityPacket extends MessengerPacket {
    public InfinityPacket(Vector2D spawn, int health){
        super(spawn, Shape.INFINITY);
    }
    @Override public int getCoinValue(){ return 1; }
    @Override public Shape shape(){ return Shape.INFINITY; }

    @Override
    public InfinityPacket copy() {
        return new InfinityPacket(getPosition(), getHealth());
    }


}