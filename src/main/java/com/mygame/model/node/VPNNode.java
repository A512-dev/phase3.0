package com.mygame.model.node;

import com.mygame.core.GameConfig;
import com.mygame.model.Port;
import com.mygame.model.packet.*;
import com.mygame.model.packet.messengerPacket.types.SquarePacket;
import com.mygame.model.packet.ProtectedPacket;

import java.util.Collection;
import java.util.List;

/** Converts Protected packets *back* to their original type. */
public final class VPNNode extends Node {

    public VPNNode(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        if (p instanceof ProtectedPacket prot) {
            // TODO: retrieve original type from prot (store field), replace.
            Packet restored = new SquarePacket(p.getPosition().copy(), GameConfig.squareLife);
            restored.setRoute(prot.getFromPort(), prot.getToPort());
            enqueuePacket(restored);
            prot.setAlive(false);               // consume wrapper
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
        return null;
    }

    @Override
    public void onDelivered(Packet p, Port port) {

    }

    @Override
    public Node copy() {
        return null;
    }
}
