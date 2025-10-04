package server.sim.model.node;

import client.audio.AudioManager;
import server.sim.core.GameConfig;
import server.sim.model.Port;
import server.sim.model.packet.*;
import server.sim.model.packet.bulkPacket.types.BulkPacketA;
import server.sim.model.packet.bulkPacket.types.BulkPacketB;
import server.sim.model.packet.messengerPacket.types.InfinityPacket;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/** Splits a Bulk packet into many Bit-packets. */
public final class DistributorNode extends Node {
    private final Deque<Packet> outbox = new ArrayDeque<>();

    public DistributorNode(double x, double y, double w, double h) { super(x, y, w, h); setNodeType(Type.DISTRIBUTOR);}

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        if (p instanceof BulkPacketA || p instanceof BulkPacketB) {
            int bits = Math.max(2, (int)(p.getHealth() / 2));   // tune
            for (int i = 0; i < bits; i++) {
                Packet bit = new InfinityPacket(getCenter().copy(), GameConfig.squareLife, GameConfig.squareSize); // or port pos
                bit.setMobile(false);
                enqueuePacket(bit);   // node’s queue; emitted in node.update(...)
            }
            AudioManager.get().playFx("split_pop");
        } else {
            enqueuePacket(p);
        }
    }

    @Override
    public void onCollision(Packet a, Packet b) {

    }

    @Override
    public void update(double dt, List<Packet> worldPackets) {
        for (Port port: getPorts()) {
            if (port.isEmitting()) {
                port.tickCooldown(0.6*dt);
                //System.out.println("port CoolDown subtracted dt");
            }
        }

        emitQueued(worldPackets);
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return queue;
    }

    @Override
    public void onDelivered(Packet p, Port port) {
        super.onDelivered(p, port);
        onDelivered(p);
    }

    @Override
    public Node copy() {
        return new DistributorNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
