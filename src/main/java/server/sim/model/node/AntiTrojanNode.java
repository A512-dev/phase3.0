package server.sim.model.node;

import server.sim.model.Port;
import server.sim.model.packet.Packet;
import server.sim.model.packet.TrojanPacket;
import shared.model.NodeType;

import java.util.Collection;
import java.util.List;

/** Heals / cleans ‘infected’ packets within a small radius. */
public final class AntiTrojanNode extends Node {

    private static final double CLEAN_RADIUS = 75;
    private double coolDown = 2.0;     // seconds between Actons
    private double timeUntilNextAction = 0;  // counts down

    public AntiTrojanNode(double x, double y, double w, double h) {
        super(x, y, w, h);
        setNodeType(NodeType.ANTITROJAN);
    }

    @Override
    public void onLost(Packet p) {

    }

    @Override
    public void onDelivered(Packet p) {
        enqueuePacket(p);    // just queue; cleaning done in update()
    }

    @Override
    public void onCollision(Packet a, Packet b) {

    }

    @Override
    public void update(double dt, List<Packet> worldPackets) {
        if (canDoAction()) {
            for (Packet p : worldPackets) {
                if (p instanceof TrojanPacket && p.getPosition().distanceTo(getCenter()) < CLEAN_RADIUS) {
                    // TODO: check infection flag; reset health / opacity.
                    ((TrojanPacket) p).revert();
                }
            }
        }
        else
            tickCoolDown(dt);
        //emitQueued(worldPackets);
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return queue;
    }

    @Override
    public void onDelivered(Packet p, Port port) {
        onDelivered(p);
    }

    @Override
    public Node copy() {
        return new AntiTrojanNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }


    public boolean canDoAction() {
        return timeUntilNextAction <= 0;
    }
    public boolean isInRest() {
        return timeUntilNextAction > 0;
    }

    public void tickCoolDown(double dt) {
        if (timeUntilNextAction > 0)
            timeUntilNextAction -= dt;
    }

    public void resetCoolDown() {
        timeUntilNextAction = coolDown;
    }
    public void setCoolDown(double seconds) {
        this.coolDown = seconds;
    }
}
