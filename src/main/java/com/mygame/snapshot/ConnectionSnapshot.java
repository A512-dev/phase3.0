
// ────────────────────────── com/mygame/engine/world/ConnectionSnapshot.java
package com.mygame.snapshot;


import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Port.PortType;

import java.util.List;

/** Immutable DTO for rendering a single wire between two ports. */
public record ConnectionSnapshot(
        Vector2D fromPos,
        Vector2D toPos,
        PortType fromType,
        PortType toType,
        List<Vector2D> bends     // ✅ new field
) {}
