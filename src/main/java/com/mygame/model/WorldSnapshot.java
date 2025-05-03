package com.mygame.model;

import java.util.List;

public class WorldSnapshot {
    public final List<Packet> packets;
    public final List<SystemNode> nodes;
    public final List<Connection> connections;

    public WorldSnapshot(List<Packet> packets, List<SystemNode> nodes, List<Connection> connections) {
        this.packets = packets;
        this.nodes = nodes;
        this.connections = connections;
    }
}
