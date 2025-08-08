// MovementStrategy.java
package com.mygame.engine.physics;
import com.mygame.model.Connection;
import com.mygame.model.packet.Packet;

import java.util.List;

public interface MovementStrategy {
    /** Mutate the packet’s position / velocity in-place. */
//    void update(Packet p, double dt);

    void update(double dt, List<Connection> wires);
}