/* ───────────────── PortSnapshot.java ───────────────────────── */
package com.mygame.snapshot;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Port;

/** Immutable DTO for a single port. */
public record PortSnapshot(
        Vector2D                  position,
        Port.PortType             type,
        Port.PortDirection        direction
) {
    static PortSnapshot of(Port p) {
        return new PortSnapshot(
                p.getPosition().copy(),
                p.getType(),
                p.getDirection()
        );
    }
}
