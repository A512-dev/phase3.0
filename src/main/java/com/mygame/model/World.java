package com.mygame.model;

import com.mygame.audio.AudioManager;
import com.mygame.engine.CollisionManager;
import com.mygame.engine.TimeController;
import com.mygame.model.powerups.ActivePowerUp;
import com.mygame.model.powerups.PowerUpType;
import com.mygame.ui.GamePanel;
import com.mygame.util.Database;
import com.mygame.util.Vector2D;

import java.util.*;
import java.util.stream.Collectors;
import com.mygame.model.Port.PortType;

import javax.xml.crypto.Data;

import static com.mygame.util.Database.PORT_SIZE;

public class World {
    private Runnable onReachedTargetCallback;
    private PacketEventListener eventListener;
    public void setPacketEventListener(PacketEventListener listener) {
        this.eventListener = listener;
    }
    public void setOnReachedTarget(Runnable cb) {
        this.onReachedTargetCallback = cb;
    }

    private boolean viewOnlyMode = false;
    private ArrayList<Integer> lossPacketRepeat = new ArrayList<>();

    public boolean isViewOnlyMode() {
        return viewOnlyMode;
    }

    public void setViewOnlyMode(boolean mode) {
        viewOnlyMode = mode;
    }


    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    private boolean gameOver = false;
    private final List<Packet> packets = new ArrayList<>();
    private final List<SystemNode> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final HUDState hud = new HUDState();

    private final CollisionManager collisionManager = new CollisionManager(this);
    private final TimeController timeController = new TimeController();
    private WorldSnapshot initialState;
    private final double PORT_CLICK_RADIUS = Database.PORT_CLICK_RADIUS; // Radius of clickable area

    /* ---------------- power-up support ---------------- */
    private final List<ActivePowerUp> active = new ArrayList<>();


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
            SystemNode toNode = c.getTo().getOwner();
            hud.setLostPackets(0);
            hud.resetGameTime();
            simTimeAccumulator = 0;           // reset the sim-time clock

            int fn = nodes.indexOf(fromNode);
            int tn = nodes.indexOf(toNode);
            int fp = fromNode.getPorts().indexOf(c.getFrom());
            int tp = toNode.getPorts().indexOf(c.getTo());
            // ✅ RECORD IT!
            connRecs.add(new ConnectionRecord(fn, fp, tn, tp));
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
            Port toPort = tn.getPorts().get(r.toPortIndex);

            // re-link the two Ports...
            fromPort.setConnectedPort(toPort);
            toPort.setConnectedPort(fromPort);

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

    private double simTimeAccumulator = 0;



    public void updateAll(double dt) {

        // 1) advance OUR internal clock by the full dt:
        simTimeAccumulator += dt;

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
//                System.out.println("p"+p.position);
//                System.out.println("port"+p.getPathEnd());
//                System.out.println("distance=="+p.getPosition().distanceTo(p.getPathEnd()));
//                System.out.println(Database.THRESHOLD_FOR_REACHING_PORT);
//                System.out.println(nodes.get(0).getInputs().get(0).getCenter());

            if (p.hasArrived()) {
                AudioManager.get().playFx("Picked_Coin_Echo");
                // find the node whose input port is at this pathEnd
                for (SystemNode node : nodes) {
                    for (Port in : node.getInputs()) {

                        if (in.getCenter().distanceTo(p.getPathEnd()) < 1e-6) {
                            if (node == nodes.get(0)) {
                                //hud.incrementSuccessful();
                                if (eventListener != null) eventListener.onDelivered(p);
                            } else {

                                p.setMobile(false);
                                node.enqueuePacket(p);
                            }
                            // ✅ Coin logic here:
                            if (eventListener instanceof HUDState) {
                                int coinValue = p.getCoinValue();  // 1 for square, 2 for triangle
                                HUDState hud = (HUDState) eventListener;
                                hud.setCoins(hud.getCoins() + coinValue);
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
                System.out.println("Triangle="+(p instanceof TrianglePacket));
                //hud.incrementLostPackets();
                if (eventListener != null) eventListener.onLost(p);
                System.out.println(hud.getLostPackets());
            } else {
                stillAlive.add(p);
            }
        }
        //System.out.println("Hud="+hud.getLostPackets());

        packets.clear();
        packets.addAll(stillAlive);


        if (hud.getPacketLossRatio() > 0.5 && !viewOnlyMode) {
            setGameOver(true);
            AudioManager.get().playFx("losegamemusic");
        }

        for (SystemNode node : nodes) {
            node.update(dt, packets);
        }
        boolean allPacketsSettled = packets.isEmpty() &&
                nodes.stream().allMatch(n -> n.getQueuedPackets().isEmpty());

        if (allPacketsSettled && !gameOver) {
            double successRatio = (double) hud.getSuccessful() / hud.getTotalPackets();
            if (successRatio >= 0.5) {
                AudioManager.get().playFx("happy_ending");
                setGameOver(true);  // mark game as finished
                System.out.println("✅ Mission Passed!");
                if (onReachedTargetCallback != null) onReachedTargetCallback.run();  // or create a dedicated callback
            }
        }


        if (timeController.getTargetTime() > 0 &&
                simTimeAccumulator >= timeController.getTargetTime()) {
            System.out.println("Reached Target");
            System.out.println("packets:" + getPackets().toString());
            System.out.println(getNodes().get(0).getQueuedPackets().toString());
            hud.setGameTime(timeController.getTargetTime());
            timeController.stopJump();
            timeController.setTimeMultiplier(1.0);
            Database.timeMultiplier = 1;
            timeController.waitToStart();

            lossPacketRepeat.add(hud.getLostPackets());
            System.out.println("lossPackets" + lossPacketRepeat.toString());
            // ✅ Run another simulation round via callback
            if (hud.getNumOfGoToTarget() < Database.NUMBER_OF_RUNS && onReachedTargetCallback != null) {
                onReachedTargetCallback.run();
            }
            int x = 0;
            int y = 0;
            if (hud.getNumOfGoToTarget() == Database.NUMBER_OF_RUNS) {
                y++;
                for (int i=0; i<Database.NUMBER_OF_RUNS; i++) {
                    x += lossPacketRepeat.get(i);
                    Database.timeMultiplier = Database.timeMultiplier +i;
                }

                x = (int) Math.round(((double) x) / (double) Database.NUMBER_OF_RUNS);
            }

            if (y == 1) {
                hud.setLostPackets(x);
                System.out.println("minimum set");
            }
            if (lossPacketRepeat.size() == Database.NUMBER_OF_RUNS+1) {
                lossPacketRepeat = new ArrayList<>();
                Database.timeMultiplier = 10;
            }
//            if (Database.NUMBER_OF_RUNS==0)
//                hud.setLostPackets(lossPacketRepeat.get(i));



        } else {
            // if we haven’t hit the slider target yet, just display simTimeAccumulator
            hud.setGameTime(simTimeAccumulator);
        }
        //hud.setGameTime(hud.getGameTime() + dt);

        long end = System.nanoTime();

        updatePowerUps(dt);
        //System.out.println("World.updateAll took " + (end - start) / 1_000_000.0 + " ms");
        //System.out.printf("dt = %.3f | GameTime = %.3f%n", dt, hud.getGameTime());
    }


    public List<Packet> getPackets() {
        return packets;
    }

    public List<SystemNode> getNodes() {
        return nodes;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public HUDState getHudState() {
        return hud;
    }

    public void addPacket(Packet p) {
        packets.add(p);
    }

    public void addNode(SystemNode n) {
        nodes.add(n);
    }

    public void addConnection(Connection c) {
        connections.add(c);
    }

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
        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
                baseLeft.getHeight() / 3));
        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
                2 * baseLeft.getHeight() / 3));
        // **NEW**: add the “loopback” input on baseLeft
        baseLeft.addInputPort(
                PortType.SQUARE,
                new Vector2D(-Database.PORT_SIZE / 2, baseLeft.getHeight() * 0.5)
        );
        baseLeft.setBaseLeft(true);
        nodes.add(baseLeft);

        // Intermediate Node 1
        SystemNode mid1 = new SystemNode(250, 150);
        mid1.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE / 2,
                baseLeft.getHeight() / 2));
        mid1.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
                baseLeft.getHeight() / 3));
        mid1.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
                2 * baseLeft.getHeight() / 3));
        nodes.add(mid1);

        // Intermediate Node 2
        SystemNode mid2 = new SystemNode(250, 350);
        mid2.addInputPort(PortType.TRIANGLE, new Vector2D(-PORT_SIZE / 2,
                baseLeft.getHeight() / 2));
        mid2.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
                baseLeft.getHeight() / 2));
        nodes.add(mid2);

        // Intermediate Node 3
        SystemNode mid3 = new SystemNode(450, 250);
        mid3.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE / 2,
                baseLeft.getHeight() / 3));
        mid3.addInputPort(PortType.TRIANGLE, new Vector2D(-PORT_SIZE / 2,
                2 * baseLeft.getHeight() / 4));
        mid3.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE / 2,
                3 * baseLeft.getHeight() / 4));
        mid3.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
                baseLeft.getHeight() / 2));
        nodes.add(mid3);

        // Base Right Node (sink)
        SystemNode baseRight = new SystemNode(600, 100);
//        baseRight.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE / 2,
//                baseLeft.getHeight() / 4));
        baseRight.addInputPort(PortType.SQUARE, new Vector2D(-PORT_SIZE / 2,
                2 * baseLeft.getHeight() / 4));
//        baseRight.addInputPort(PortType.TRIANGLE, new Vector2D(-PORT_SIZE / 2,
//                3 * baseLeft.getHeight() / 4));
        baseRight.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
                baseLeft.getHeight() / 2)); // loop back
        nodes.add(baseRight);
        for (SystemNode node : nodes)
            node.getPortsPrinted();



        // Packets start flowing from left node periodically
        for (int i = 0; i < 5; i++) {
            Vector2D pos = new Vector2D(baseLeft.getPosition().x + baseLeft.getWidth() / 2,
                    baseLeft.getPosition().y + baseLeft.getHeight() / 2);
            Packet[] p = new Packet[(int) Database.NUMBER_OF_PACKETS_LEVEL1];
            for (int j=0; j<Database.NUMBER_OF_PACKETS_LEVEL1; j++) {
                double x = Math.random();
                if (x>0.5)
                    p[j] = new SquarePacket(pos, new Vector2D());
                else
                    p[j] = new TrianglePacket(pos, new Vector2D());
                p[j].setMobile(false);
                baseLeft.enqueuePacket(p[j]);
                hud.incrementTotalPackets();
                System.out.println("packet " + i + " created");
            }
//            if (i % 2 == 0 && i % 2 == 1)
//                p = new SquarePacket(pos, new Vector2D(0, 0));
//            else
//                p = new TrianglePacket(pos, new Vector2D(0, 0));
//            p.setMobile(false);
//            baseLeft.enqueuePacket(p);
//            hud.incrementTotalPackets();

        }
        for (SystemNode n : nodes) {
            n.setPacketEventListener(hud);   // HUDState already implements PacketEventListener
        }


        initialState = takeSnapshot();
    }

    public Port findPortAtPosition(Vector2D pos) {
        for (SystemNode node : getNodes()) {
            for (Port port : node.getPorts()) {
                if (port.getCenter().distanceTo(pos) <= PORT_CLICK_RADIUS) {
                    if (port.getConnectedPort() != null) {
                        port.getConnectedPort().setConnectedPort(null);
                        port.setConnectedPort(null);
                    }
                    return port;
                }
            }
        }
        return null;
    }

    //* -- query helpers -- */
    public boolean isPowerUpActive(PowerUpType t) {
        return active.stream()
                .anyMatch(p -> p.type() == t && p.remainingSec() > 0);
    }

    /* -- one-shot heal for O' Anahita -- */
    public void healAllPackets() {
        packets.stream()
                .filter(Packet::isAlive)
                .forEach(Packet::resetHealth);
    }
    /* -- try to buy/activate from ShopPanel -- */
    public boolean tryActivate(PowerUpType t, HUDState hud) {
        if (hud.getCoins() < t.cost) return false;
        else {
            hud.setCoins(hud.getCoins()-t.cost);
            AudioManager.get().playFx("coinSpent");
        }

        if (t.durationSec == 0) {           // O' Anahita
            healAllPackets();
            AudioManager.get().playFx("coinSpent");
        } else {
            active.add(new ActivePowerUp(t, t.durationSec));
        }
        return true;
    }

    /* -- call once per logic tick (add near the end of updateAll) -- */
    public void updatePowerUps(double dt) {
        ListIterator<ActivePowerUp> it = active.listIterator();
        while (it.hasNext()) {
            ActivePowerUp p = it.next();
            double left = p.remainingSec() - dt;
            if (left <= 0) it.remove();
            else it.set(new ActivePowerUp(p.type(), left));  // replace
        }
    }

}