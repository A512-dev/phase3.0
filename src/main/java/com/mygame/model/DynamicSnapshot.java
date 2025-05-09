package com.mygame.model;

import java.util.List;

public class DynamicSnapshot {
    public final List<Packet> packets;
    public final double time;
    public DynamicSnapshot(List<Packet> packets, double time) {
        this.packets = packets;
        this.time     = time;
    }
}
