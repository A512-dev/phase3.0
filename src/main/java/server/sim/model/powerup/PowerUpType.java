package server.sim.model.powerup;

/**
 *  Simple enum: cost (coins)   |  durationSec (0 = instant)
 */
public enum PowerUpType {
    O_ATAR     (3, 10),   // Disable impact waves for 10 s
    O_AIRYAMAN (4,  5),   // Disable ALL collisions for 5 s
    O_ANAHITA  (5,  0);   // Full-heal every living packet (instant)

    public final int    cost;
    public final double durationSec;

    PowerUpType(int cost, double duration) {
        this.cost = cost;
        this.durationSec = duration;
    }
}
