// com.mygame.model.node.VPNNode
package com.mygame.model.node;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Port;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.ProtectedPacket;
import com.mygame.model.packet.messengerPacket.MessengerPacket;

import java.util.*;

public final class VPNNode extends Node {

    /** track only the protected packets this VPN created (for revert-on-fail) */
    private final Set<ProtectedPacket> issued =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public VPNNode(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override public void onDelivered(Packet p) {
        // already protected? just pass it through unchanged
        if (p instanceof ProtectedPacket prot) {
            enqueuePacket(prot);
            return;
        }

        // only messenger packets are protectable per spec
        if (p instanceof MessengerPacket msg) {
            ProtectedPacket wrapped = new ProtectedPacket(msg);

            // keep routing & kinematics consistent
            wrapped.setRoute(p.getFromPort(), p.getToPort());
            wrapped.setVelocity(p.getVelocity().copy());
            wrapped.setAcceleration(p.getAcceleration().copy());
            wrapped.setMobile(p.isMobile());
            wrapped.setOpacity(p.getOpacity());

            issued.add(wrapped);     // remember: created by THIS VPN
            enqueuePacket(wrapped);  // emit protected
            return;
        }

        // other packet types: forward unchanged
        enqueuePacket(p);
    }

    @Override public void onDelivered(Packet p, Port at) { onDelivered(p); }
    @Override public void onLost(Packet p) { /* no-op */ }
    @Override public void onCollision(Packet a, Packet b) { /* no-op */ }

    @Override public void update(double dt, List<Packet> worldPackets) {
        emitQueued(worldPackets);
    }

    @Override public Collection<Packet> getQueuedPackets() { return queue; }

    /** Call this when the VPN “fails”: revert all issued packets back to originals. */
    public void shutdown(List<Packet> worldPackets) {
        // turn every still-alive ProtectedPacket we issued back into its original
        for (ProtectedPacket prot : new ArrayList<>(issued)) {
            MessengerPacket original = prot.revert();
            original.setProtectedPacket(false);   // they become vulnerable again
            prot.setAlive(false);           // consume the wrapper
            worldPackets.add(original);     // put the original back into the world
        }
        issued.clear();
    }

    @Override public Node copy() {
        return new VPNNode(position.x(), position.y(), width, height);
    }
}
