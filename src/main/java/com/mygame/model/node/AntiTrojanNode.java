package com.mygame.model.node;

import com.mygame.core.GameConfig;
import com.mygame.model.Port;
import com.mygame.model.packet.Packet;

import java.util.Collection;
import java.util.List;

/** Heals / cleans ‘infected’ packets within a small radius. */
public final class AntiTrojanNode extends Node {

    private static final double CLEAN_RADIUS = 75;

    public AntiTrojanNode(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        enqueuePacket(p);    // just queue; cleaning done in update()
    }

    @Override
    public void onCollision(Packet a, Packet b) {

    }

    @Override
    public void update(double dt, List<Packet> worldPackets) {
        for (Packet p : worldPackets) {
            if (p.getPosition().distanceTo(getCenter()) < CLEAN_RADIUS) {
                // TODO: check infection flag; reset health / opacity.
                p.setOpacity(1f);
            }
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
        return new SpyNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
