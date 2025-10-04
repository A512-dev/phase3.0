package server.sim.model.node;

import client.audio.AudioManager;
import server.sim.engine.physics.Vector2D;
import server.sim.model.Port;
import server.sim.model.packet.*;
import server.sim.model.packet.bulkPacket.types.BulkPacketA;
import server.sim.model.packet.messengerPacket.types.InfinityPacket;

import java.util.*;
import java.util.List;

/** Collects Bit-packets and recreates a Bulk packet. */
public final class MergerNode extends Node {

    private final List<InfinityPacket> buffer = new ArrayList<>();
    private static final int   NEEDED_BITS = 6;
    private static final double RADIUS     = 60;

    public MergerNode(double x, double y, double w, double h) { super(x, y, w, h); setNodeType(Type.MERGER);}

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        if (p instanceof InfinityPacket mi) {
            buffer.add(mi);
            mi.setAlive(false);                 // absorbed
        } else {
            enqueuePacket(p);                   // forward others
        }
    }

    @Override
    public void onCollision(Packet a, Packet b) {

    }

    @Override
    public void update(double dt, List<Packet> worldPackets) {
        super.update(dt, worldPackets);
        int count = (int) getQueuedPackets().stream().filter(q -> q instanceof InfinityPacket).count();
        if (count >= 8) {  // threshold
            // remove 8 bits
            int removed = 0;
            Iterator<Packet> it = getQueuedPackets().iterator();
            while (it.hasNext() && removed < 8) {
                if (it.next() instanceof InfinityPacket) { it.remove(); removed++; }
            }

            Packet bulk = new BulkPacketA(getCenter().copy(), removed, getHeight()); /*** fix! it's very shityu now* */
            // push bulk to output
            bulk.setMobile(false);
            enqueuePacket(bulk);
            AudioManager.get().playFx("merge_whoosh");
        }
        // Combine when enough bits collected
        if (buffer.size() >= NEEDED_BITS) {
            Vector2D pos = getCenter().copy();
            Packet bulk = new BulkPacketA(getCenter().copy(), buffer.size(), getHeight());   // or B depending on design
            enqueuePacket(bulk);
            buffer.clear();
        }
        emitQueued(worldPackets);
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return queue;
    }

    @Override
    public void onDelivered(Packet p, Port port) {
        super.onDelivered(p, port);
        onDelivered(p);
    }

    @Override
    public Node copy() {
        return new MergerNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
