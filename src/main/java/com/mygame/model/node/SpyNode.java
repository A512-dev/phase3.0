// com/mygame/model/node/SpyNode.java
package com.mygame.model.node;

import com.mygame.model.PacketEventListener;
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

    /* optional clustering support: linkSpies(a,b,...) assigns a shared groupId */
    private static int NEXT_GROUP = 1;
    private static final Map<Integer, Set<SpyNode>> GROUPS = new HashMap<>();

    private int groupId = 0; // 0 = ungrouped → use REGISTRY


    public SpyNode(double x, double y, double width, double height) {
        super(x, y, width, height);
        REGISTRY.add(this);
        setNodeType(Type.SPY);
    }

    /** Link a set of spies into one teleportation cluster. */
    public static void linkSpies(SpyNode... spies) {
        if (spies == null || spies.length == 0) return;
        int gid = NEXT_GROUP++;
        Set<SpyNode> set = new HashSet<>(Arrays.asList(spies));
        GROUPS.put(gid, set);
        for (SpyNode s : spies) s.groupId = gid;
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
        for (SpyNode spyNode: candidates) {
            SpyNode candidate = candidates.get(RNG.nextInt(candidates.size()));
            for (Port port: candidate.getOutputs()) {
                if (!port.isEmitting() && port.getConnectedPort()!=null && port.getConnectedPort().getOwner().isActive())
                    return candidate;
            }
        }
        return null;
    }

    /* candidates inside my group (if any), otherwise all spies */
    private List<SpyNode> candidateExits() {
        if (groupId != 0) {
            Set<SpyNode> set = GROUPS.get(groupId);
            if (set != null) {
                return set.stream()
                        .filter(s -> s != this && !s.connectedOutputs().isEmpty())
                        .collect(Collectors.toList());
            }
        }
        // fallback: any other spy with at least one connected output
        return REGISTRY.stream()
                .filter(s -> s != this && !s.connectedOutputs().isEmpty())
                .collect(Collectors.toList());
    }



    /* ---------- Node hooks ---------- */

    /** some callers use this form; delegate to the port-aware handler */
    @Override public void onDelivered(Packet p) { onDelivered(p, null); }

    /** main entry: decide what to do with the packet */
    @Override
    public void onDelivered(Packet p, Port at) {
        super.onDelivered(p, at);
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
        forwardFromNode(this, p, null);
    }

    /** forward from an arbitrary spy node (used by teleport exit) */
    private void forwardFromHere(Packet p) {
        forwardFromNode(this, p, null);
    }

    /** forward from a specific spy node through one of its connected outputs */
    private static void forwardFromNode(SpyNode node, Packet p, String mutationReason) {
        List<Port> outs = node.connectedOutputs();
        if (outs.isEmpty()) {
            // nowhere to go → queue so it doesn't disappear
            node.enqueuePacket(p);
            return;
        }
        // prefer an output whose wire isn't currently emitting if you track that
        Port out = pickBestOut(outs);
        // notify mutation if this was a teleport
        if (mutationReason != null) {
            PacketEventListener lis = node.packetEventListener;
            if (lis != null) lis.onMutation(p, p, mutationReason);
        }
        // safe-guard wire use
        var wire = out.getWire();
        if (wire == null) {
            node.enqueuePacket(p);
            return;
        }
        wire.transmit(p);
    }

    private static Port pickBestOut(List<Port> outs) {
        // prefer a non-busy port if your Port has such a signal; otherwise pick random
        List<Port> idle = outs.stream().filter(o -> !o.isEmitting()).collect(Collectors.toList());
        if (!idle.isEmpty()) return idle.get(RNG.nextInt(idle.size()));
        return outs.get(RNG.nextInt(outs.size()));
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
