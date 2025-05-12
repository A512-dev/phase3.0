package com.mygame.model;

public interface PacketEventListener {
    void onLost(Packet p);
    void onDelivered(Packet p);
    void onCollision(Packet a, Packet b);  // optional
}
