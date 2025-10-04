package server.sim.engine.modelUpdates;

import server.sim.engine.world.World;
import server.sim.model.Port;
import server.sim.model.node.Node;
import server.sim.model.packet.Packet;

public final class NodeSystem {
    public void update(double dt, World w) {
        for (Node n : w.getNodes()) n.update(10*dt, w.getPackets());
    }
    public void deliver(Packet p, Port port, World w) {
        port.getOwner().onDelivered(p, port);
    }
}
