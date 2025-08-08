package com.mygame.model.node;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Port;
import com.mygame.model.packet.*;
import com.mygame.model.packet.bulkPacket.types.BulkPacketA;
import com.mygame.model.packet.messengerPacket.types.InfinityPacket;

import java.util.*;
import java.util.List;

/** Collects Bit-packets and recreates a Bulk packet. */
public final class MergerNode extends Node {

    private final List<InfinityPacket> buffer = new ArrayList<>();
    private static final int   NEEDED_BITS = 6;
    private static final double RADIUS     = 60;

    public MergerNode(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        if (p instanceof InfinityPacket mi) {
            buffer.add(mi);
            mi.setAlive(false);                 // absorbed
        } else {
            enqueuePacket(p);                   // forward others
        }
    }

    @Override
    public void onCollision(Packet a, Packet b) {

    }

    @Override
    public void update(double dt, List<Packet> worldPackets) {
        // Combine when enough bits collected
        if (buffer.size() >= NEEDED_BITS) {
            Vector2D pos = getCenter().copy();
            Packet bulk = new BulkPacketA(pos);   // or B depending on design
            enqueuePacket(bulk);
            buffer.clear();
        }
        emitQueued(worldPackets);
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return null;
    }

    @Override
    public void onDelivered(Packet p, Port port) {

    }

    @Override
    public Node copy() {
        return new DistributorNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
