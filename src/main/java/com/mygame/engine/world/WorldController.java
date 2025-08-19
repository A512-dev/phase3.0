package com.mygame.engine.world;

import com.mygame.engine.modelUpdates.MovementSystem;
import com.mygame.engine.world.World;
import com.mygame.snapshot.WorldSnapshot;

public final class WorldController {

    private final World world;          // live data-holder
    private final MovementSystem mover = new MovementSystem();

    public WorldController(World w) {
        this.world = w;
    }

    /** fixed-step tick, called from GameLoop */
    public void tick(double dt) {
        mover.update(dt, world.getConnections());
        // *for now* just call the old logic
        world.update(dt);
    }

    /** immutable DTO for rendering */
    public WorldSnapshot snapshot() {
        return world.snapshot();
    }

    /* convenience delegates */
    public void jumpTo(double t)        { world.getTimeController().jumpTo(t); }
    public void setSpeed(double s)      { world.getTimeController().setTimeMultiplier(s); }
    public void pause()                 { world.getTimeController().togglePause(); }
}
