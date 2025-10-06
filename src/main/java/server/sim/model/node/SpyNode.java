// com/mygame/model/node/SpyNode.java
package server.sim.model.node;

import server.sim.engine.debug.LossDebug;
import server.sim.model.Connection;
import server.sim.model.PacketEventListener;
import server.sim.model.Port;
import server.sim.model.packet.Packet;
import shared.model.NodeType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spy rules:
 *  - Confidential packets are destroyed on entry.
 *  - Protected packets are queued & forwarded LOCALLY (no teleport).
 *  - Normal packets are queued & when possible teleported:
 *      enter any Spy -> exit from a random other Spy with a ready output.
 *    If no exit is ready, keep queued and try again next update.
 *    As a final fallback, forward locally so they don't starve forever.
 */
public final class SpyNode extends Node {

    /* Registry so spies can find each other for teleport exits */
    private static final List<SpyNode> REGISTRY = new ArrayList<>();
    private static final Random RNG = new Random();

    /* Optional grouping (linked clusters) */
    private static int NEXT_GROUP = 1;
    private static final Map<Integer, Set<SpyNode>> GROUPS = new HashMap<>();
    private int groupId = 0; // 0 => ungrouped (use REGISTRY)

    public SpyNode(double x, double y, double width, double height) {
        super(x, y, width, height);
        REGISTRY.add(this);
        setNodeType(NodeType.SPY);
    }

    /** Link a set of spies into one teleportation cluster. */
    public static void linkSpies(SpyNode... spies) {
        if (spies == null || spies.length == 0) return;
        int gid = NEXT_GROUP++;
        Set<SpyNode> set = new HashSet<>(Arrays.asList(spies));
        GROUPS.put(gid, set);
        for (SpyNode s : spies) s.groupId = gid;
    }

    /* -------- helpers -------- */

    /** outputs that are connected + have a wire; optionally must be ready to emit */
    private List<Port> connectedOutputs(boolean requireReady) {
        return outputs.stream()
                .filter(o -> o.getConnectedPort() != null && o.getWire() != null)
                .filter(o -> !requireReady || o.canEmit())
                .collect(Collectors.toList());
    }

    /** pick a random ready output (prefer idle ones if you track emitting) */
    private Port pickReadyOut(List<Port> outs) {
        if (outs.isEmpty()) return null;
        return outs.get(RNG.nextInt(outs.size())); // outs already 'ready' when we call this
    }
    private boolean hasAnyWiredOutput(SpyNode node) {
        return node.outputs.stream().anyMatch(o -> o.getConnectedPort()!=null && o.getWire()!=null);
    }

    /** candidate exit spies in my group if grouped, else any others */
    private List<SpyNode> candidateExits() {
        Collection<SpyNode> pool;
        if (groupId != 0) {
            Set<SpyNode> set = GROUPS.get(groupId);
            pool = (set != null) ? set : List.of();
        } else {
            pool = REGISTRY;
        }
        // other spies (≠ this) that have at least one READY, connected output
        return pool.stream()
                .filter(s -> s != this)
                .filter(s -> !s.connectedOutputs(true).isEmpty())
                .collect(Collectors.toList());
    }

    private SpyNode pickExitSpy() {
        List<SpyNode> candidates = candidateExits();
        if (candidates.isEmpty()) return null;
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    /* -------- Node hooks -------- */

    /** some callers use this form; delegate to port-aware */
    @Override public void onDelivered(Packet p) { onDelivered(p, null); }

    /** enqueue everything except Confidential (destroy those) */
    @Override
    public void onDelivered(Packet p, Port at) {
        // Confidential ⇒ removed
        if (p.isConfidentialPacket()) {
            LossDebug.mark(p, "SPY_BLOCKED");
            p.setAlive(false);
            return;
        }
        // Protected / Normal ⇒ queue; actual emission decided in update()
        if (queue.size()>=4) {
            LossDebug.mark(p, "SPY_BLOCKED_Queue_Full");
            p.setAlive(false);
            return;
        }
        enqueuePacket(p);
    }

    /** emit logic: one packet per tick max */
    @Override
    public void update(double dt, List<Packet> worldPackets) {
        for (Port port : getPorts()) {
            //time of cooldown of port is longer
            if (port.isEmitting()) port.tickCooldown(0.3*dt);
        }

        if (queue.isEmpty()) return;

        Packet p = ((ArrayDeque<Packet>)queue).peekFirst();

        boolean emitted = false;


        // protected => forward locally from THIS spy
        if (p.isProtectedPacket()) {
            if (emitLocal(p, worldPackets)) {
                ((ArrayDeque<Packet>)queue).removeFirst();
            }
            // else: no ready local output yet ⇒ keep queued and try next frame
            return;
        }

        // normal => try teleport exit first; if none ready, try local; else keep queued
        if (emitFromExitSpyOrLocal(p, worldPackets)) {
            ((ArrayDeque<Packet>)queue).removeFirst();
        }
    }

    /** try to emit from THIS spy; return true on success */
    private boolean emitLocal(Packet p, List<Packet> worldPackets) {
        List<Port> outs = connectedOutputs(true); // require canEmit()
        if (outs.isEmpty()) return false;

        Port out = pickReadyOut(outs);
        if (out == null) return false;

        Connection wire = out.getWire();
        if (wire == null) return false;

        wire.transmit(p);
        p.setMobile(true);
        worldPackets.add(p);
        out.resetCooldown();
        return true;
    }

    /** try to emit from a random EXIT spy; if none ready, fallback to local; return true on success */
    private boolean emitFromExitSpyOrLocal(Packet p, List<Packet> worldPackets) {
        SpyNode exit = pickExitSpy();
        if (exit != null) {
            List<Port> outs = exit.connectedOutputs(true); // require canEmit at exit
            Port out = exit.pickReadyOut(outs);
            if (out != null) {
                Connection wire = out.getWire();
                if (wire != null) {
                    wire.transmit(p);
                    p.setMobile(true);
                    worldPackets.add(p);
                    out.resetCooldown();
                    // mutation callback to say "teleported"
                    PacketEventListener lis = exit.packetEventListener;
                    if (lis != null) lis.onMutation(p, p, "SPY_EXIT");
                    return true;
                }
            }
        }
        // fallback: try local
        return emitLocal(p, worldPackets);
    }

    /* PacketEventListener passthroughs (no special behavior here) */
    @Override public void onLost(Packet p) { /* optional: log */ }
    @Override public void onCollision(Packet a, Packet b) { /* not used here */ }

    @Override public Collection<Packet> getQueuedPackets() { return queue; }

    @Override public Node copy() {
        return new SpyNode(position.x(), position.y(), width, height);
    }

    public static void resetRegistry() {
        REGISTRY.clear();
        GROUPS.clear();
        NEXT_GROUP = 1;
    }
}
