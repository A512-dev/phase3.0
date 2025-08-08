package com.mygame.engine.modelUpdates;

import com.mygame.engine.world.World;
import com.mygame.model.Port;
import com.mygame.model.node.Node;
import com.mygame.model.packet.Packet;

public final class NodeSystem {
    public void update(double dt, World w) {
        for (Node n : w.getNodes()) n.update(10*dt, w.getPackets());
    }
    public void deliver(Packet p, Port port, World w) {
        port.getOwner().onDelivered(p, port);
    }
}
