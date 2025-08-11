package com.mygame.model.node;

import com.mygame.model.Port;
import com.mygame.model.packet.*;
import com.mygame.model.packet.bulkPacket.types.BulkPacketA;
import com.mygame.model.packet.bulkPacket.types.BulkPacketB;
import com.mygame.model.packet.messengerPacket.types.InfinityPacket;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/** Splits a Bulk packet into many Bit-packets. */
public final class DistributorNode extends Node {
    private final Deque<Packet> outbox = new ArrayDeque<>();

    public DistributorNode(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        if (p instanceof BulkPacketA || p instanceof BulkPacketB) {
            // TODO: decide n = size/bitSize; create n MessengerInfinityPacket bits
            int bits = Math.max(2, (int) (p.getHealth() / 2));
            for (int i = 0; i < bits; i++) {
                Packet bit = new InfinityPacket(p.getPosition().copy(), 2);
                bit.setRoute(p.getFromPort(), p.getToPort());
                enqueuePacket(bit);
            }
            p.setAlive(false);                  // original bulk consumed
        } else {
            enqueuePacket(p);
        }
    }

    @Override
    public void onCollision(Packet a, Packet b) {

    }

    @Override
    public void update(double dt, List<Packet> worldPackets) {
        emitQueued(worldPackets);
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return outbox;
    }

    @Override
    public void onDelivered(Packet p, Port port) {
        onDelivered(p);
    }

    @Override
    public Node copy() {
        return new DistributorNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
