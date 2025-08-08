package com.mygame.model.node;

import com.mygame.model.Port;
import com.mygame.model.packet.Packet;

import java.util.Collection;
import java.util.List;

public final class SpyNode extends Node {

    protected SpyNode(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void onDelivered(Packet p, Port at) {
        p.setMobile(false);          // eaten by spy
        // maybe log / coins etc.
    }

    @Override
    public void update(double dt, List<Packet> worldPackets) { /* no op */ }

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {

    }

    @Override
    public void onCollision(Packet a, Packet b) {

    }

    @Override public Collection<Packet> getQueuedPackets() { return List.of(); }



    @Override public Node copy() {
        return new SpyNode(position.x(), position.y(), width, height);
    }
}
