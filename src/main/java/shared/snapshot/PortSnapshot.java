/* ───────────────── PortSnapshot.java ───────────────────────── */
package shared.snapshot;

import shared.Vector2D;

import shared.model.PortDirection;
import shared.model.PortType;

/** Immutable DTO for a single port. */
public record PortSnapshot(
        Vector2D                  position,
        PortType type,
        PortDirection direction
) {
}
