// com/mygame/model/node/SpyNode.java
package com.mygame.model.node;

import com.mygame.model.Port;
import com.mygame.model.packet.Packet;

import java.util.*;
import java.util.stream.Collectors;

/** Spy systems:
 *  - Confidential packets are destroyed on entry.
 *  - VPN–protected packets are unaffected → forwarded locally.
 *  - Other packets teleport: enter any spy, exit from a random other spy.
 */
public final class SpyNode extends Node {

    /* global registry of all spies so we can teleport between them */
    private static final List<SpyNode> REGISTRY = new ArrayList<>();
    private static final Random RNG = new Random();

    public SpyNode(double x, double y, double width, double height) {
        super(x, y, width, height);
        REGISTRY.add(this);
    }

    /* convenience: connected output ports on this node */
    private List<Port> connectedOutputs() {
        return outputs.stream()
                .filter(o -> o.getConnectedPort() != null && o.getWire() != null)
                .collect(Collectors.toList());
    }

    /* choose another spy (≠ this) that has at least one connected output */
    private SpyNode pickExitSpy() {
        List<SpyNode> candidates = REGISTRY.stream()
                .filter(s -> s != this && !s.connectedOutputs().isEmpty())
                .collect(Collectors.toList());
        if (candidates.isEmpty()) return null;
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    /* ---------- Node hooks ---------- */

    /** some callers use this form; delegate to the port-aware handler */
    @Override public void onDelivered(Packet p) { onDelivered(p, null); }

    /** main entry: decide what to do with the packet */
    @Override
    public void onDelivered(Packet p, Port at) {
        // 1) confidential → removed
        if (p.isConfidentialPacket()) {
            p.setAlive(false);
            lose(p);                 // notify global listener as "lost"
            return;
        }

        // 2) protected by VPN → unaffected: forward locally (no teleport)
        if (p.isProtectedPacket()) {
            forwardLocally(p);
            return;
        }

        // 3) normal packet → teleport to a random other spy (if possible)
        SpyNode exit = pickExitSpy();
        if (exit != null) {
            exit.forwardFromHere(p);  // transmit from exit spy
        } else {
            // fallback: no exit spies or no connected outputs → forward locally
            forwardLocally(p);
        }
    }

    /** forward from THIS spy through one of its connected outputs */
    private void forwardLocally(Packet p) {
        forwardFromNode(this, p);
    }

    /** forward from an arbitrary spy node (used by teleport exit) */
    private void forwardFromHere(Packet p) {
        forwardFromNode(this, p);
    }

    private static void forwardFromNode(SpyNode node, Packet p) {
        List<Port> outs = node.connectedOutputs();
        if (outs.isEmpty()) {
            // nothing to do: queue it so it doesn't disappear silently
            node.enqueuePacket(p);
            return;
        }
        Port out = outs.get(RNG.nextInt(outs.size()));
        // use the wire already connected to this output to transmit
        out.getWire().transmit(p);
    }

    /* spies don’t emit on their own each tick */
    @Override public void update(double dt, List<Packet> worldPackets) { /* no-op */ }

    /* PacketEventListener passthroughs (no special behavior here) */
    @Override public void onLost(Packet p) { /* optional: log */ }
    @Override public void onCollision(Packet a, Packet b) { /* not used here */ }

    /* queue is unused for spies */
    @Override public Collection<Packet> getQueuedPackets() { return List.of(); }

    @Override public Node copy() {
        return new SpyNode(position.x(), position.y(), width, height);
    }
}
