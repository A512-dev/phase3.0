package com.mygame.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Wire connecting two ports, carrying packets between them.
 */
public class Connection {
    private Port from;
    private Port to;
    private double length;
    private List<Packet> packetsInTransit = new ArrayList<>();

    public Connection(Port from, Port to) {
        this.from = from;
        this.to = to;
        this.length = from.getPosition().distanceTo(to.getPosition());
    }

    public void transmit(Packet p) {
        packetsInTransit.add(p);
    }

    public void update(double dt) {
        // TODO: move each packet along the wire and deliver at end
    }

    public Port getFrom() { return from; }
    public Port getTo() { return to; }
    public double getLength() { return length; }
    public Connection copy() {
        return new Connection(from.copy(null), to.copy(null));  // you'll need to fix port references later
    }

}


