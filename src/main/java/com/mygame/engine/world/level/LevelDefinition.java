// com.mygame.level.LevelDefinition.java
package com.mygame.engine.world.level;

import com.mygame.engine.world.World;

/** Minimal contract for loading a level into a fresh World instance. */
public interface LevelDefinition {
    String name();
    void apply(World world);   // place nodes, set HUD flags, etc.
    default boolean failStateEnabled() { return false; } // sandbox => false
}
