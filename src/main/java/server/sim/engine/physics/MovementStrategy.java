// MovementStrategy.java
package server.sim.engine.physics;
import server.sim.model.Connection;

import java.util.List;

public interface MovementStrategy {
    /** Mutate the packet’s position / velocity in-place. */
//    void update(Packet p, double dt);

    void update(double dt, List<Connection> wires);
}