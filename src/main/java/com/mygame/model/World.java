package com.mygame.model;

import com.mygame.engine.CollisionManager;
import com.mygame.engine.TimeController;
import com.mygame.util.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class World {



    private boolean gameOver = false;
    private final List<Packet> packets = new ArrayList<>();
    private final List<SystemNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final HUDState hud = new HUDState();

    private final CollisionManager collisionManager = new CollisionManager();
    private final TimeController timeController = new TimeController();
    private WorldSnapshot initialState;

    public WorldSnapshot getInitialState() {
        return initialState;
    }

    //debugg
    public World() {
        if (!timeController.isPaused())
            timeController.togglePause();

        // Center-to-center collisions (head-on)
        for (int i = 0; i < 4; i++) {
            Vector2D a = new Vector2D(100 + i * 30, 200);
            Vector2D b = new Vector2D(700 - i * 30, 200);

            SquarePacket p1 = new SquarePacket(a, new Vector2D(30, 0));
            p1.setPath(a, new Vector2D(700, 200));
            packets.add(p1);
            hud.incrementTotalPackets();

            SquarePacket p2 = new SquarePacket(b, new Vector2D(-30, 0));
            p2.setPath(b, new Vector2D(100, 200));
            packets.add(p2);
            hud.incrementTotalPackets();

        }

        // Vertical triangle packets crossing diagonally
        for (int i = 0; i < 3; i++) {
            Vector2D start = new Vector2D(300, 100 + i * 30);
            Vector2D end = new Vector2D(300, 500);
            TrianglePacket tp = new TrianglePacket(start, new Vector2D(0, 40));
            tp.setPath(start, end);
            packets.add(tp);
            hud.incrementTotalPackets();

        }

        // Diagonal movement — square from bottom left, triangle from top right
        for (int i = 0; i < 3; i++) {
            Vector2D left = new Vector2D(150 + i * 20, 500);
            Vector2D right = new Vector2D(650 - i * 20, 100);

            SquarePacket sp = new SquarePacket(left, new Vector2D(25, -25));
            sp.setPath(left, new Vector2D(650, 100));
            packets.add(sp);
            hud.incrementTotalPackets();


            TrianglePacket tp = new TrianglePacket(right, new Vector2D(-25, 25));
            tp.setPath(right, new Vector2D(150, 500));
            packets.add(tp);
            hud.incrementTotalPackets();

        }

        // Some slow packets to test off-track without collisions
        for (int i = 0; i < 3; i++) {
            Vector2D s = new Vector2D(350 + i * 20, 300);
            SquarePacket slow = new SquarePacket(s, new Vector2D(10, 0));
            slow.setPath(s, new Vector2D(600, 300));
            packets.add(slow);
            hud.incrementTotalPackets();
        }

        this.initialState = takeSnapshot();  // capture t = 0
    }
    public WorldSnapshot takeSnapshot() {
        return new WorldSnapshot(
                packets.stream().map(Packet::copy).collect(Collectors.toList()),
                nodes.stream().map(SystemNode::copy).collect(Collectors.toList()),
                connections.stream().map(Connection::copy).collect(Collectors.toList())
        );
    }

    public void resetToSnapshot(WorldSnapshot snapshot) {
        packets.clear();
        packets.addAll(snapshot.packets.stream().map(Packet::copy).collect(Collectors.toList()));

        nodes.clear();
        nodes.addAll(snapshot.nodes.stream().map(SystemNode::copy).collect(Collectors.toList()));

        connections.clear();
        connections.addAll(snapshot.connections.stream().map(Connection::copy).collect(Collectors.toList()));
    }

    public TimeController getTimeController() {
        return timeController;
    }

    //
//    public void updateAll(double dt) {
//        for (Packet p : packets) {
//            p.update(dt);
//        }
//
//        collisionManager.checkCollisions(packets);
//
//        // Optional: remove dead ones
//        packets.removeIf(p -> !p.isAlive());
//
//        hud.setGameTime(hud.getGameTime() + dt);
//    }
    public void updateAll(double dt) {
        List<Packet> stillAlive = new ArrayList<>();

        for (Packet p : packets) {
            p.update(dt);

            if (!p.isAlive()) {
                hud.incrementLostPackets();
            } else if (p.isOffTrackLine(20)) {
                hud.incrementLostPackets();
            } else {
                stillAlive.add(p);  // keep it
            }
        }

        packets.clear();
        packets.addAll(stillAlive);

        collisionManager.checkCollisions(packets);
        hud.setGameTime(hud.getGameTime() + dt);
        if (hud.getPacketLossRatio() > 0.5) {
            gameOver = true;
        }

        if (timeController.getTargetTime() >= 0 &&
                hud.getGameTime() >= timeController.getTargetTime()) {
            timeController.stopJump();
            timeController.setTimeMultiplier(1.0);
            timeController.waitToStart();
        }


    }



    public List<Packet> getPackets() { return packets; }
    public List<SystemNode> getNodes() { return nodes; }
    public List<Connection> getConnections() { return connections; }
    public HUDState getHudState() { return hud; }

    public void addPacket(Packet p) { packets.add(p); }
    public void addNode(SystemNode n) { nodes.add(n); }
    public void addConnection(Connection c) { connections.add(c); }
    public boolean isGameOver() {
        return gameOver;
    }

}
