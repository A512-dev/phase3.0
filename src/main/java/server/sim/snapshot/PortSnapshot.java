/* ───────────────── PortSnapshot.java ───────────────────────── */
package server.sim.snapshot;

import server.sim.engine.physics.Vector2D;
import server.sim.model.Port;

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
