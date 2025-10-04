package server.sim.engine.world;

import server.sim.engine.modelUpdates.MovementSystem;
import server.sim.snapshot.WorldSnapshot;

public final class WorldController {

    private final World world;          // live data-holder
    private final MovementSystem mover = new MovementSystem();

    public WorldController(World w) {
        this.world = w;
    }

    /** fixed-step tick, called from GameLoop */
    public void tick(double dt) {
        double scaledDt = dt;
        if (world.getTimeController().getTargetTime()>0) {

            scaledDt = dt * world.getTimeController().getTimeMultiplier();
            System.out.println(world.getTimeController().getTimeMultiplier());
        }

        if ( world.getTimeController().isWaitingToStart()
                || world.getTimeController().isFrozen()
                || world.getTimeController().isPaused() )
            return;


        mover.update(scaledDt, world.getConnections());
        // *for now* just call the old logic
        world.update(scaledDt);
        world.advanceTime(scaledDt);
    }

    /** immutable DTO for rendering */
    public WorldSnapshot snapshot() {
        return world.snapshot();
    }
}
