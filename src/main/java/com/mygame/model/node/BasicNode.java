package com.mygame.model.node;

import com.mygame.core.GameConfig;
import com.mygame.model.Port;
import com.mygame.model.packet.Packet;

import java.util.Collection;
import java.util.List;

/** Plain pass-through node used in early levels. */
public class BasicNode extends Node {


    private boolean baseLeft = false;


    public BasicNode(double x, double y, double w, double h) {
        super(x, y, w, h);
    }

    public void setBaseLeft(boolean b){ baseLeft = b; }
    public boolean isBaseLeft()       { return baseLeft; }


    /* Simply queue the packet; no side-effects */
    @Override public void onDelivered(Packet p) {
        enqueuePacket(p);
    }

    /* Emit one queued packet per frame (or whatever rule you like) */
    @Override public void update(double dt, List<Packet> worldPackets) {

        super.update(dt, worldPackets);
        if (!queue.isEmpty()) {
            for (Port out : outputs) {
                if (out.getConnectedPort() != null && out.canEmit()) {
                    System.out.println(111);
                    Packet p = queue.poll();
                    if (p == null) return;

                    out.getWire().transmit(p);
                    p.setMobile(true);
                    worldPackets.add(p);
                    out.resetCooldown();         // 🔄 reset the cooldown
                    break;                       // emit one packet per update
                }
            }
        }
    }

    @Override
    public Collection<Packet> getQueuedPackets() {
        return super.queue;
    }

    @Override
    public void onDelivered(Packet p, Port port) {
        super.onDelivered(p, port);
        if (queue.size()>=GameConfig.MAX_QUEUE)
            p.setAlive(false);
        else
            queue.add(p);
    }

    @Override
    public Node copy() {
        BasicNode n = new BasicNode((int)position.x(),(int)position.y(),width,height);
        n.baseLeft = this.baseLeft;
        // ports copied elsewhere when snapshotting
        return n;
    }

    @Override
    public void onLost(Packet p) {
        queue.remove(p);
    }


    @Override
    public void onCollision(Packet a, Packet b) {

    }
}
