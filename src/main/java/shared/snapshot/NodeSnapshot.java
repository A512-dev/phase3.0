/* ───────────────── NodeSnapshot.java ───────────────────────── */
package shared.snapshot;

import shared.Vector2D;

import shared.model.NodeType;

import java.util.List;

/** Immutable, render-only picture of a single node (and all its ports). */
public record NodeSnapshot(
        Vector2D            position,
        int                 width,
        int                 height,
        List<PortSnapshot>  ports,
        NodeType            nodeType,      // “BasicNode”, “SpyNode”, …
        boolean             isAllConnected,
        boolean isbaseleft,
        List<PacketSnapshot> queue         // ✅ immutable snapshots, not live Packets
) {

}
