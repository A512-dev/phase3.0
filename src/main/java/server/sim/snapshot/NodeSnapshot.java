/* ───────────────── NodeSnapshot.java ───────────────────────── */
package server.sim.snapshot;

import server.sim.engine.physics.Vector2D;
import server.sim.model.node.Node;
import server.sim.model.packet.Packet;

import java.util.List;

/** Immutable, render-only picture of a single node (and all its ports). */
public record NodeSnapshot(
        Vector2D                 position,
        int                      width,
        int                      height,
        List<PortSnapshot>       ports,
        Node.Type type,     // “BasicNode”, “SpyNode”, …
        boolean isAllConnected,
        List<Packet> queue

) {

    /** Factory – called by the World when it builds the frame snapshot */
    public static NodeSnapshot of(Node n) {
        return new NodeSnapshot(
                n.getPosition().copy(),
                (int) n.getWidth(),
                (int) n.getHeight(),
                n.getPorts().stream().map(PortSnapshot::of).toList(),
                n.getNodeType(),
                n.isAllConnected(),
                n.getQueuedPackets().stream().toList()
        );
    }
}