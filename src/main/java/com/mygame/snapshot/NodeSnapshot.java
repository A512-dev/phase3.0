/* ───────────────── NodeSnapshot.java ───────────────────────── */
package com.mygame.snapshot;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.node.Node;
import java.util.List;

/** Immutable, render-only picture of a single node (and all its ports). */
public record NodeSnapshot(
        Vector2D                 position,
        int                      width,
        int                      height,
        List<PortSnapshot>       ports,
        String                   kind     // “BasicNode”, “SpyNode”, …
) {

    /** Factory – called by the World when it builds the frame snapshot */
    public static NodeSnapshot of(Node n) {
        return new NodeSnapshot(
                n.getPosition().copy(),
                (int) n.getWidth(),
                (int) n.getHeight(),
                n.getPorts().stream().map(PortSnapshot::of).toList(),
                n.getClass().getSimpleName()
        );
    }
}