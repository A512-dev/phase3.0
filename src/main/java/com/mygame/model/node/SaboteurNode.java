package com.mygame.model.node;

import com.mygame.model.Port;
import com.mygame.model.packet.*;

import java.util.Collection;
import java.util.List;

/** Has a chance to “infect” packets that enter. */
public final class SaboteurNode extends Node {

    private static final double INFECT_CHANCE = 0.3;   // 30 %

    public SaboteurNode(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        if (Math.random() < INFECT_CHANCE && !(p instanceof ProtectedPacket)) {
            // TODO: mark packet as Trojan / change sprite / reduce health
            p.setOpacity(0.4f);                 // visual hint (placeholder)
        }
        enqueuePacket(p);
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
        return null;
    }

    @Override
    public void onDelivered(Packet p, Port port) {

    }

    @Override
    public Node copy() {
        return new SaboteurNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
