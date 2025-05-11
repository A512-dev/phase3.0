package com.mygame.model;

import com.mygame.engine.CollisionManager;
import com.mygame.engine.TimeController;
import com.mygame.util.Database;
import com.mygame.util.Vector2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.mygame.model.Port.PortType;

import static com.mygame.util.Database.PORT_SIZE;

public class World {
    private boolean viewOnlyMode = false;
    public boolean isViewOnlyMode() { return viewOnlyMode; }
    public void setViewOnlyMode(boolean mode) { viewOnlyMode = mode; }


    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    private boolean gameOver = false;
    private final List<Packet> packets = new ArrayList<>();
    private final List<SystemNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final HUDState hud = new HUDState();

    private final CollisionManager collisionManager = new CollisionManager();
    private final TimeController timeController = new TimeController();
    private WorldSnapshot initialState;
    private final double PORT_CLICK_RADIUS = Database.PORT_CLICK_RADIUS; // Radius of clickable area

    public WorldSnapshot getInitialState() {
        return initialState;
    }

    //debugg
    public World() {


//        // Center-to-center collisions (head-on)
//        for (int i = 0; i < 4; i++) {
//            Vector2D a = new Vector2D(100 + i * 30, 200);
//            Vector2D b = new Vector2D(700 - i * 30, 200);
//
//            SquarePacket p1 = new SquarePacket(a, new Vector2D(30, 0));
//            p1.setPath(a, new Vector2D(700, 200));
//            packets.add(p1);
//            hud.incrementTotalPackets();
//
//            SquarePacket p2 = new SquarePacket(b, new Vector2D(-30, 0));
//            p2.setPath(b, new Vector2D(100, 200));
//            packets.add(p2);
//            hud.incrementTotalPackets();
//
//        }
//
//        // Vertical triangle packets crossing diagonally
//        for (int i = 0; i < 3; i++) {
//            Vector2D start = new Vector2D(300, 100 + i * 30);
//            Vector2D end = new Vector2D(300, 500);
//            TrianglePacket tp = new TrianglePacket(start, new Vector2D(0, 40));
//            tp.setPath(start, end);
//            packets.add(tp);
//            hud.incrementTotalPackets();
//
//        }
//
//        // Diagonal movement — square from bottom left, triangle from top right
//        for (int i = 0; i < 3; i++) {
//            Vector2D left = new Vector2D(150 + i * 20, 500);
//            Vector2D right = new Vector2D(650 - i * 20, 100);
//
//            SquarePacket sp = new SquarePacket(left, new Vector2D(25, -25));
//            sp.setPath(left, new Vector2D(650, 100));
//            packets.add(sp);
//            hud.incrementTotalPackets();
//
//
//            TrianglePacket tp = new TrianglePacket(right, new Vector2D(-25, 25));
//            tp.setPath(right, new Vector2D(150, 500));
//            packets.add(tp);
//            hud.incrementTotalPackets();
//
//        }
//
//        // Some slow packets to test off-track without collisions
//        for (int i = 0; i < 3; i++) {
//            Vector2D s = new Vector2D(350 + i * 20, 300);
//            SquarePacket slow = new SquarePacket(s, new Vector2D(10, 0));
//            slow.setPath(s, new Vector2D(600, 300));
//            packets.add(slow);
//            hud.incrementTotalPackets();
//        }
        createTestLevel1();
        captureDynamicInitial();
        this.initialState = takeSnapshot();
    }
    // somewhere in your World.java (or as a top-level class)


    public WorldSnapshot takeSnapshot() {
        // next record *which* port-pairs were connected in the *current* world:

        return new WorldSnapshot(
                nodes.stream().map(SystemNode::copy).collect(Collectors.toList()));
    }

    public void resetToSnapshot(WorldSnapshot snapshot) {
        List<ConnectionRecord> connRecs = new ArrayList<>();
        for (Connection c : connections) {
            SystemNode fromNode = c.getFrom().getOwner();
            SystemNode toNode   = c.getTo()  .getOwner();
            hud.setLostPackets(0);

            int fn = nodes.indexOf(fromNode);
            int tn = nodes.indexOf(toNode);
            int fp = fromNode.getPorts().indexOf(c.getFrom());
            int tp = toNode  .getPorts().indexOf(c.getTo());

            connRecs.add(new ConnectionRecord(fn,fp, tn,tp));
        }
        packets.clear();
//        packets.addAll(snapshot.packets.stream().map(Packet::copy).collect(Collectors.toList()));
//
        nodes.clear();
        nodes.addAll(snapshot.nodes.stream().map(SystemNode::copy).collect(Collectors.toList()));
        // 2) now clear & rebuild your connection objects
        this.connections.clear();
        for (ConnectionRecord r : connRecs) {
            SystemNode fn = this.nodes.get(r.fromNodeIndex);
            SystemNode tn = this.nodes.get(r.toNodeIndex);

            Port fromPort = fn.getPorts().get(r.fromPortIndex);
            Port   toPort = tn.getPorts().get(r.toPortIndex);

            // re-link the two Ports...
            fromPort.setConnectedPort(toPort);
            toPort  .setConnectedPort(fromPort);

            // ...and recreate the Connection for rendering/logic
            this.connections.add(new Connection(fromPort, toPort));
        }
        //System.out.println(Arrays.toString(initialState.nodes.get(1).getPorts().toArray()));// capture t = 0


//        connections.clear();
//        connections.addAll(snapshot.connections.stream().map(Connection::copy).collect(Collectors.toList()));
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
//        if (!packets.isEmpty()) {
//            Packet test = packets.get(0);
//            Vector2D a = test.getPathStart();
//            Vector2D b = test.getPathEnd();
//            // pick a point 20px “above” the line:
//            Vector2D pOff = test.getPosition().added(
//                    b.subtracted(a).perpendicular().normalized().multiplied(20)
//            );
//            double d = test.distanceToSegment(pOff, a, b);
//            System.out.println("DEBUG: off-line test distance = " + d);
//        }
        long start = System.nanoTime();
        int maxDistance = Database.maxDistanceToBeOfTheLine;
        // 1) Move everything
        for (Packet p : packets) {
            p.update(dt);
        }
        // 2) Handle collisions & apply impulses
        collisionManager.checkCollisions(packets);

        // 3) NOW cull off-track & dead packets immediately
        List<Packet> stillAlive = new ArrayList<>();
        //System.out.println(packets.size());
        for (Packet p : packets) {
            if (p.hasArrived()) {
                // find the node whose input port is at this pathEnd
                for (SystemNode node : nodes) {
                    for (Port in : node.getInputs()) {

                        if (in.getCenter().distanceTo(p.getPathEnd()) < 1e-6) {
                            if (node == nodes.get(0)) {
                                hud.incrementSuccessful();
                            } else {
                                p.setMobile(false);
                                node.enqueuePacket(p);
                            }

                        }
                    }
                }
                continue;  // do not add to stillAlive, do NOT lost++
            }
//            System.out.println(i+"distanceToLine"+p.distanceToSegment(p.getPosition(), p.getPathStart(), p.getPathEnd()));
//            System.out.println("Position"+p.getPosition());
//            System.out.println("pathEnd="+p.getPathEnd());
//            System.out.println("pathStart="+p.getPathStart());
            if (!p.isAlive() || p.isOffTrackLine(Database.maxDistanceToBeOfTheLine)) {
                System.out.printf("LOST @ t=%.3f  Δt overshoot=%.3f  dist=%.2f  threshold=%d%n",
                        hud.getGameTime(), dt, p.distanceToSegment(p.getPosition(), p.getPathStart(), p.getPathEnd()), Database.maxDistanceToBeOfTheLine);
                System.out.println();
                hud.incrementLostPackets();
                System.out.println(hud.getLostPackets());
            } else {
                stillAlive.add(p);
            }
        }
        //System.out.println("Hud="+hud.getLostPackets());

        packets.clear();
        packets.addAll(stillAlive);


        if (hud.getPacketLossRatio() > 0.5 && !viewOnlyMode)
            gameOver = true;
        for (SystemNode node : nodes) {
            node.update(dt, packets);
        }
        if (timeController.getTargetTime() >= 0 &&
                hud.getGameTime() >= timeController.getTargetTime()) {
            System.out.println("Reached Target");
            System.out.println("packets:"+getPackets().toString());
            System.out.println(getNodes().get(0).getQueuedPackets().toString());
            hud.setGameTime(timeController.getTargetTime());
            timeController.stopJump();
            timeController.setTimeMultiplier(1.0);
            Database.timeMultiplier = 1;
            timeController.waitToStart();
        }
        hud.setGameTime(hud.getGameTime() + dt);
        long end = System.nanoTime();
        //System.out.println("World.updateAll took " + (end - start) / 1_000_000.0 + " ms");
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
    public void createTestLevel1() {
        packets.clear();
        nodes.clear();
        connections.clear();
        hud.reset();
        setGameOver(false);

        // Base Left Node (emitter)
        SystemNode baseLeft = new SystemNode(100, 250);
        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE/2,
                baseLeft.getHeight()/3));
        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - PORT_SIZE/2,
                2*baseLeft.getHeight()/3));
        // **NEW**: add the “loopback” input on baseLeft
        baseLeft.addInputPort(
                PortType.SQUARE,
                new Vector2D(-Database.PORT_SIZE/2, baseLeft.getHeight()*0.5)
        );
        nodes.add(baseLeft);

        // Intermediate Node 1
        SystemNode mid1 = new SystemNode(250, 150);
        mid1.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE/2,
                baseLeft.getHeight()/2));
        mid1.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE/2,
                baseLeft.getHeight()/3));
        mid1.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - PORT_SIZE/2,
                2*baseLeft.getHeight()/3));
        nodes.add(mid1);

        // Intermediate Node 2
        SystemNode mid2 = new SystemNode(250, 350);
        mid2.addInputPort(PortType.TRIANGLE, new Vector2D(-PORT_SIZE/2,
                baseLeft.getHeight()/2));
        mid2.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE/2,
                baseLeft.getHeight()/2));
        nodes.add(mid2);

        // Intermediate Node 3
        SystemNode mid3 = new SystemNode(450, 250);
        mid3.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE/2,
                baseLeft.getHeight()/3));
        mid3.addInputPort(PortType.TRIANGLE, new Vector2D(-PORT_SIZE/2,
                2*baseLeft.getHeight()/3));
        mid3.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE/2,
                baseLeft.getHeight()/2));
        nodes.add(mid3);

        // Base Right Node (sink)
        SystemNode baseRight = new SystemNode(600, 250);
        baseRight.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE/2,
                baseLeft.getHeight()/4));
        baseRight.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE/2,
                2*baseLeft.getHeight()/4));
        baseRight.addInputPort(PortType.TRIANGLE, new Vector2D(-PORT_SIZE/2,
                3*baseLeft.getHeight()/4));
        baseRight.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE/2,
                baseLeft.getHeight()/2)); // loop back
        nodes.add(baseRight);
        for (SystemNode node: nodes)
            node.getPortsPrinted();

        // Globally Balanced:
        // Total Inputs: 6
        // Total Outputs: 6 ✅

        // Packets start flowing from left node periodically
        for (int i = 0; i < 5; i++) {
            Vector2D pos = new Vector2D(baseLeft.getPosition().x+baseLeft.getWidth()/2,
                    baseLeft.getPosition().y+baseLeft.getHeight()/2);
            Packet p;
            if (i % 2 == 0 && i%2==1)
                p = new SquarePacket(pos, new Vector2D(0, 0));
            else
                p = new TrianglePacket(pos, new Vector2D(0, 0));
            p.setMobile(false);
            baseLeft.enqueuePacket(p);
            hud.incrementTotalPackets();
            System.out.println("packet "+i+" created");
        }

        initialState = takeSnapshot();
    }
    public Port findPortAtPosition(Vector2D pos) {
        for (SystemNode node : getNodes()) {
            for (Port port : node.getPorts()) {
                if (port.getCenter().distanceTo(pos) <= PORT_CLICK_RADIUS) {
                    if (port.getConnectedPort()!=null) {
                        port.getConnectedPort().setConnectedPort(null);
                        port.setConnectedPort(null);
                    }
                    return port;
                }
            }
        }
        return null;
    }
    private DynamicSnapshot dynamicInitial;  // only packets & gameTime

    public void captureDynamicInitial() {
        dynamicInitial = new DynamicSnapshot(
                packets.stream().map(Packet::copy).collect(Collectors.toList()),
                hud.getGameTime()
        );
    }

    public void resetDynamic() {
        // restore packets
        packets.clear();
        packets.addAll(dynamicInitial.packets.stream().map(Packet::copy).collect(Collectors.toList()));
        // restore time
        hud.setGameTime(0);
    }



//    public Port findNearestFreePort(Vector2D pos) {
//        double nearestDist = 20; // Threshold for clicking on a port
//        Port nearestPort = null;
//
//        for (SystemNode node : getNodes()) {
//            for (Port port : node.getPorts()) {
//                if (port.getConnectedPort() == null) {
//                    double dist = port.getPosition().distanceTo(pos);
//                    if (dist < nearestDist) {
//                        nearestDist = dist;
//                        nearestPort = port;
//                    }
//                }
//            }
//        }
//        return nearestPort;
//    }
//    public Port findNearestCompatibleFreePort(Vector2D pos, Port fromPort) {
//        double nearestDist = 20;
//        Port nearestPort = null;
//
//        for (SystemNode node : getNodes()) {
//            for (Port port : node.getPorts()) {
//                if (port.getConnectedPort() == null &&
//                        port.getType() == fromPort.getType() &&
//                        port.getPosition() != fromPort.getPosition()) {
//
//                    double dist = port.getPosition().distanceTo(pos);
//                    if (dist < nearestDist) {
//                        nearestDist = dist;
//                        nearestPort = port;
//                    }
//                }
//            }
//        }
//        return nearestPort;
//    }
}
