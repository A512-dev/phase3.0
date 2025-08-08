package com.mygame.model;

import com.mygame.model.packet.Packet;

public interface PacketEventListener {
    void onLost(Packet p);
    void onDelivered(Packet p);
    void onCollision(Packet a, Packet b);  // optional
}
