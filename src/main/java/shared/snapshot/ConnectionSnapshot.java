
// ────────────────────────── com/mygame/engine/world/ConnectionSnapshot.java
package shared.snapshot;


import shared.Vector2D;
import shared.model.PortType;


import java.util.List;

/** Immutable DTO for rendering a single wire between two ports. */
public record ConnectionSnapshot(
        Vector2D fromPos,
        Vector2D toPos,
        PortType fromType,
        PortType toType,
        List<Vector2D> bends     // ✅ new field
) {}
