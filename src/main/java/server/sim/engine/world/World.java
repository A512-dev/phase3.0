package server.sim.engine.world;

import client.audio.AudioManager;
import server.sim.core.GameConfig;
import server.sim.engine.debug.LossDebug;
import server.sim.engine.physics.CollisionSystem;
import server.sim.engine.physics.ImpactWaveSystem;
import server.sim.engine.world.level.Level;


import server.sim.model.Connection;
import server.sim.engine.loop.TimeController;
import shared.Vector2D;
import server.sim.model.PacketEventListener;
import server.sim.model.Port;
import server.sim.model.node.Node;
import server.sim.model.packet.Packet;
import server.sim.model.packet.TrojanPacket;
import server.sim.model.packet.confidentialPacket.ConfidentialPacket;
import server.sim.model.packet.confidentialPacket.types.ConfidentialLargePacket;
import server.sim.model.packet.confidentialPacket.types.ConfidentialSmallPacket;
import server.sim.model.packet.messengerPacket.types.InfinityPacket;
import server.sim.model.packet.messengerPacket.types.SquarePacket;
import server.sim.model.packet.messengerPacket.types.TrianglePacket;
import server.sim.model.powerup.ActivePowerUp;
import server.sim.model.powerup.PowerUpType;
import server.sim.model.state.HUDState;
import server.sim.service.CoinService;
import shared.snapshot.*;
import shared.model.PortDirection;
import shared.model.PortType;


import java.util.*;


public class World {
    // 1) A field to collect repeated loss counts:
    private List<Integer> lossPacketRepeat = new ArrayList<>();

    // 2) A setter for your gameOver flag:
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    private final GameConfig cfg = GameConfig.defaultConfig();

    private Runnable onReachedTargetCallback;

    public void setOnReachedTarget(Runnable cb) {
        this.onReachedTargetCallback = cb;
    }

    private boolean viewOnlyMode       = false;
    private boolean gameOver           = false;
    private double  simTimeAccumulator = 0;

    private final List<Packet>    packets      = new ArrayList<>();
    private final List<Node> nodes       = new ArrayList<>();
    private final List<Connection> connections  = new ArrayList<>();
    private final HUDState        hud          = new HUDState(cfg);

    /* NEW: single source of truth for all coin operations */
    private final CoinService  coinService  = new CoinService(hud);


    private final ImpactWaveSystem waveSystem = new ImpactWaveSystem();
    private final CollisionSystem collisionSystem = new CollisionSystem(mid ->
            waveSystem.emit(mid, /*radius*/GameConfig.RADIUS_OF_WAVE, /*strength*/GameConfig.STRENGTH_OF_WAVE));


    private final TimeController timeController = new TimeController();
    private WorldSnapshot initialState;

    /* ---------------- power-up support ---------------- */
    private final List<ActivePowerUp> active = new ArrayList<>();
    private PacketEventListener eventListener;


    public void setPacketEventListener(PacketEventListener listener) {
        this.eventListener = listener;
    }



    public WorldSnapshot getInitialState() {
        return initialState;

    }
    /** Called by levels after building to lock in the baseline snapshot.
     * @return*/
    public WorldSnapshot captureInitialSnapshot() {
        this.initialState = snapshot();
        return initialState;
    }
    /** Utility used by levels before build. */
    public void clearAll() {
        packets.clear();
        nodes.clear();
        connections.clear();
        hud.reset();
        setGameOver(false);
        viewOnlyMode = false;
        simTimeAccumulator = 0;
    }
    public World(Level level) {
        try {
            level.build(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build level: " + level.id(), e);
        }
//        if (GameState.currentLevel == 1) {
//            createTestLevel1();
//        } else if (GameState.currentLevel == 2) {
//            createTestLevel2();
//        }
        this.initialState = snapshot();
    }


    public TimeController getTimeController() {
        return timeController;
    }
    public WorldSnapshot snapshot() {
        // ⬇ convert every live Connection → ConnectionSnapshot
        List<ConnectionSnapshot> connSnaps = connections.stream()
                .map(c -> new ConnectionSnapshot(
                        c.getFrom().getCenter(),
                        c.getTo().getCenter(),
                        c.getFrom().getType(),
                        c.getTo().getType(), c.getBends()))
                .toList();

        List<PacketSnapshot> pktSnaps = packets.stream()
                .filter(Packet::isMobile)            // immobile ones are queued in nodes
                .map(Packet::of)
                .toList();



        return new WorldSnapshot(
                nodes.stream().map(Node::of).toList(),
                connSnaps,
                pktSnaps,
                hud.readOnlyCopy(),
                gameOver,                             // flag for overlays
                viewOnlyMode                          // flag for review mode
        );
    }


    public void update(double dt) {
        /* ───────── time control guard ───────── */
        if ( timeController.isWaitingToStart()
                || timeController.isFrozen()
                || timeController.isPaused() )
            return;

        if (simTimeAccumulator==0)
            System.out.println("Timer Started");
        // NEW — clamp dt when jumping to avoid overshoot
        if (timeController.getTargetTime() >= 0) {
            double left = timeController.getTargetTime() - hud.getGameTime();
            if (left <= 0) {
                hud.setGameTime(timeController.getTargetTime());
                timeController.stopJump();
                timeController.setTimeMultiplier(1.0);
                if (!timeController.isFrozen()) timeController.toggleFrozen(); // hard freeze
                timeController.waitToStart();                                   // gate future ticks
                return;
            }
            dt = Math.min(dt, left);
        }
        simTimeAccumulator += dt;

        //System.out.println("WorldPackets="+ packets.size());
//        // 1) Move everything
//        for (Packet p : packets) {
//            System.out.println(1);
//            p.update(dt);
//        }


        // 2) physics resolution
        collisionSystem.step(packets, dt);
        waveSystem.update(dt, connections, false);


        // 4) per-packet behavior ticks
        applyPacketRules(dt);

        // 5) passive node auras
        applyNodeAreaEffects(dt);


        // 3) Cull off-track & dead; handle arrivals
        List<Packet> stillAlive = new ArrayList<>();
        for (Packet p : packets) {
            if (p.hasArrived()) {
                //eventListener.onDelivered(p);
                AudioManager.get().playFx("Picked_Coin_Echo");
                // Find which node’s INPUT port this packet reached
                Node arrivedAt = null;
                Port arrivedPort = null;
                for (Node node : nodes) {
                    for (Port in : node.getInputs()) {
                        if (in.getCenter().distanceTo(p.getPathEnd()) < 1e-6) {
                            if (node == nodes.get(0)) {
                                if (eventListener != null) eventListener.onDelivered(p);
                                hud.incrementSuccessful();   // whichever counter you use
                                p.setAlive(false);           // consumed, not lost
                                continue;
                            }
                            else {
                                p.setMobile(false);
                                node.enqueuePacket(p);
                                System.out.println();
                            }

                            arrivedAt = node;
                            arrivedPort = in;

                            //break;
                        }
                    }
                    //if (arrivedAt != null) break;
                }
//
//                if (arrivedAt != null) {
//
//                    // ① Coins for ANY node arrival
//                    int coinValue = p.getCoinValue();
//                    coinService.addCoins(coinValue);           // <- use the single source of truth
//                    AudioManager.get().playFx("Picked_Coin_Echo");
//
//                    // ② Successful “reached” ONLY if it returned to the emitter/BaseLeft
//                    boolean isEmitter = (arrivedAt == nodes.get(0));         // fallback: first node is base-left
//
//                    if (isEmitter) {
//                        if (eventListener != null) eventListener.onDelivered(p); // “reached” + stats
//                        // consumed at the emitter: DO NOT enqueue back into the node
//                        // (optional) despawn or mark not mobile
//                        p.setAlive(false);
//                    }
//                    else {
//                        // entering a regular node: stop traveling & enqueue
//                        p.setMobile(false);
//                        arrivedAt.onDelivered(p, arrivedPort);
//                        // FALLBACK: if node didn't enqueue the same packet, enqueue it now
//                        if (!arrivedAt.getQueuedPackets().contains(p) && p.isAlive()) {
//                            arrivedAt.enqueuePacket(p);
//                        }
//                    }
//                }

                continue;   // handled this packet’s arrival
            }

            if (!p.isAlive()) {
                // Try to use a reason someone set earlier (e.g., node overflow), otherwise default
                String reason = LossDebug.consume(p).orElse("HEALTH_ZERO");
                reportLoss(p, reason);
            }
            else if (p.isOffTrackLine(cfg.maxDistanceOffTrack, p.getPosition())) {
                p.setAlive(false);
                LossDebug.mark(p, "OFF_TRACK > " + cfg.maxDistanceOffTrack + "px");
                reportLoss(p, "OFF_TRACK");
            }
            else {
                stillAlive.add(p);
            }
        }

        packets.clear();
        packets.addAll(stillAlive);

        if (hud.getPacketLossRatio() > 2 && !viewOnlyMode) {
            setGameOver(true);
            AudioManager.get().playFx("losegamemusic");
        }

        //System.out.println("nodes update");
        for (Node node : nodes) {
            //System.out.println(Arrays.toString(node.getQueuedPackets().toArray()));
            node.update(dt, packets);
        }
        //System.out.println("WorldPackets="+ packets.size());


        boolean allSettled = packets.isEmpty()
                && nodes.stream().allMatch(n -> n.getQueuedPackets().isEmpty());

        if (allSettled && !gameOver && !viewOnlyMode) {
            double successRatio = (double) hud.getSuccessful() / hud.getTotalPackets();
            if (successRatio >= 0.5) {
                AudioManager.get().playFx("happy_ending");
                setGameOver(true);
                if (onReachedTargetCallback != null) onReachedTargetCallback.run();
            }
        }

        if (timeController.getTargetTime() > 0
                && simTimeAccumulator >= timeController.getTargetTime()) {

            hud.setGameTime(timeController.getTargetTime());
            timeController.stopJump();
            timeController.setTimeMultiplier(1.0);
            timeController.waitToStart();
            if (!timeController.isFrozen()) timeController.toggleFrozen();

            lossPacketRepeat.add(hud.getLostPackets());
        } else {
            hud.setGameTime(simTimeAccumulator);
        }

        updatePowerUps(dt);
    }

    public List<Packet> getPackets()            { return packets; }
    public List<Node> getNodes()          { return nodes; }
    public List<Connection> getConnections()     { return connections; }
    public HUDState getHudState()               { return hud; }

    public void addPacket(Packet p)             { packets.add(p); }
    public void addNode(Node n)           { nodes.add(n); }
    public void addConnection(Connection c)      {
        System.out.println("[WORLD] addConnection " + c +
                " bends=" + c.getBends().size());


        // Enforce exclusivity on both ends
        disconnectPort(c.getFrom());
        disconnectPort(c.getTo());

        // Link ports
        c.getFrom().setConnectedPort(c.getTo());
        c.getTo().setConnectedPort(c.getFrom());

        //set their wire
        c.getTo().setWire(c);
        c.getFrom().setWire(c);


        if (hud.getWireLengthRemaining()>= c.getLength()) {
            connections.add(c);
            hud.setWireLengthRemaining(hud.getWireLengthRemaining() - c.getLength());
        }

    }
    /** Removes the existing wire (if any) from this port, and clears both ends. */
    public void disconnectPort(Port port) {
        Port connectedPort = port.getConnectedPort();
        if (connectedPort == null) return;

        // find the specific connection first (so we can refund)
        Connection toRemove = null;
        for (Connection c : connections) {
            if ((c.getFrom() == port && c.getTo() == connectedPort) ||
                    (c.getFrom() == connectedPort && c.getTo() == port)) {
                toRemove = c;
                break;
            }
        }
        if (toRemove != null) {
            // refund full wire length (includes bends)
            hud.setWireLengthRemaining(hud.getWireLengthRemaining() + toRemove.getLength());
            connections.remove(toRemove);
        }

        // Clear back-references
        port.setConnectedPort(null);
        connectedPort.setConnectedPort(null);

        // If you keep a Wire object on Port, also clear it:
        if (port.getWire() != null) port.setWire(null);
        if (connectedPort.getWire() != null &&
                (connectedPort.getWire().getFrom().equals(port) || connectedPort.getWire().getTo().equals(port)))
            connectedPort.setWire(null);

    }
    public boolean isGameOver()                 { return gameOver; }
    public void setViewOnlyMode(boolean mode) {
        viewOnlyMode = mode;
    }
    public boolean isViewOnlyMode() {
        return viewOnlyMode;
    }


    public Port findPortAtPosition(Vector2D pos) {
        for (Node node : getNodes()) {
            for (Port port : node.getPorts()) {
                double PORT_CLICK_RADIUS = cfg.portClickRadius;
                if (port.getCenter().distanceTo(pos) <= PORT_CLICK_RADIUS) {

                    return port;
                }
            }
        }
        return null;
    }

    public boolean isPowerUpActive(PowerUpType t) {
        return active.stream()
                .anyMatch(p -> p.type() == t && p.remainingSec() > 0);
    }

    public void healAllPackets() {
        packets.stream()
                .filter(Packet::isAlive)
                .forEach(Packet::resetHealth);
    }

    public boolean tryActivate(PowerUpType t, HUDState hudState) {
        if (hudState.getCoins() < t.cost) return false;
        hudState.setCoins(hudState.getCoins() - t.cost);
        AudioManager.get().playFx("coinSpent");
        if (t.durationSec == 0) {
            healAllPackets();
            AudioManager.get().playFx("coinSpent");
        } else {
            active.add(new ActivePowerUp(t, t.durationSec));
        }
        return true;
    }

    public void updatePowerUps(double dt) {
        ListIterator<ActivePowerUp> it = active.listIterator();
        while (it.hasNext()) {
            ActivePowerUp p = it.next();
            double left = p.remainingSec() - dt;
            if (left <= 0) it.remove();
            else it.set(new ActivePowerUp(p.type(), left));
        }
    }

    public void removeConnectionBetween(Port a, Port b) {
        // find the specific connection first (so we can refund)
        Connection toRemove = null;
        for (Connection c : connections) {
            if ((c.getFrom() == a && c.getTo() == b) ||
                    (c.getFrom() == b && c.getTo() == a)) {
                toRemove = c;
                break;
            }
        }
        if (toRemove != null) {
            // refund full wire length (includes bends)
            hud.setWireLengthRemaining(hud.getWireLengthRemaining() + toRemove.getLength());
            connections.remove(toRemove);
        }

    }
    // inside server.sim.engine.world.World

    // World.java

    private Node createNodeFromSnapshot(NodeSnapshot ns) {
        double x = ns.position().x();
        double y = ns.position().y();
        double w = ns.width();
        double h = ns.height();

        Node n;
        switch (ns.nodeType()) { // ← make sure NodeSnapshot.nodeType() exists & is set in NodeSnapshot.of(...)
            case BASIC       -> n = new server.sim.model.node.BasicNode(x, y, w, h);
            case SPY         -> n = new server.sim.model.node.SpyNode(x, y, w, h);
            case VPN         -> n = new server.sim.model.node.VPNNode(x, y, w, h);
            case SABOTEUR    -> n = new server.sim.model.node.SaboteurNode(x, y, w, h);
            case ANTITROJAN  -> n = new server.sim.model.node.AntiTrojanNode(x, y, w, h);
            case DISTRIBUTOR -> n = new server.sim.model.node.DistributorNode(x, y, w, h);
            case MERGER      -> n = new server.sim.model.node.MergerNode(x, y, w, h);
            default          -> n = new server.sim.model.node.BasicNode(x, y, w, h);
        }
        // if your constructors don’t set it, keep this line:
        n.setNodeType(ns.nodeType());
        return n;
    }



    /** Re-initialises runtime lists from an immutable snapshot. */
    public void resetToSnapshot(WorldSnapshot snap) {
        // 1. basic counters
        hud.reset();
        simTimeAccumulator = 0;

        // 2. replace domain collections (lists are final, so clear+add)
        packets.clear();                  // packets list stays empty; base nodes will re-emit
        nodes.clear();
        for (NodeSnapshot ns : snap.nodes()) {
            Node n = createNodeFromSnapshot(ns);
            // assume all your level-1/2 nodes are BasicNode; if you have other types
            // switch on ns.kind() to pick the right subclass
            // if you stored flags in kind or elsewhere, restore them here:
//            if (Node.NodeType.BASIC.equals(ns.nodeType()) ) {
//                n.setBaseLeft(true);
//            }
            // Convert port world pos → local offset
            for (PortSnapshot ps : ns.ports()) {
                Vector2D local = ps.position().copy().subtracted(n.getPosition());
                if (ps.direction() == PortDirection.INPUT) {
                    n.addInputPort(ps.type(), local);
                } else {
                    n.addOutputPort(ps.type(), local);
                }
            }
            // restore queued packets
            for (PacketSnapshot qs : ns.queue()) {
                Packet p = Packet.fromSnapshot(qs);
                p.setMobile(false);
                n.enqueuePacket(p);
            }
            nodes.add(n);
        }

        connections.clear();
        for (ConnectionSnapshot cs : snap.connections()) {
            Port from = resolvePort(cs.fromPos(), cs.fromType(), nodes);
            Port to   = resolvePort(cs.toPos(),   cs.toType(),   nodes);


            if (from == null || to == null) {
                System.out.println("⚠ couldn't resolve ports for connection: " + cs);
                continue;
            }
            // Build the wire and hook everything up
            Connection wire = new Connection(from, to, new ArrayList<>(cs.bends()));
            from.setConnectedPort(to);
            to.setConnectedPort(from);
            from.setWire(wire);            // ← critical
            to.setWire(wire);              // ← critical

            connections.add(wire);
        }

        gameOver     = snap.gameOver();
        viewOnlyMode = snap.viewOnlyMode();
    }
    private static final double EPS = 1e-3; // or a pixel, e.g. 0.5–1.0

    private Port resolvePort(Vector2D target,
                             PortType type,
                             List<Node> nodes) {

        Port best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (Node n : nodes) {
            for (Port p : n.getPorts()) {
                if (p.getType() != type)     continue;

                double d = p.getCenter().distanceTo(target);
                if (d < bestDist) {
                    best = p;
                    bestDist = d;
                }
            }
        }
        return (best != null && bestDist <= EPS) ? best : null;
    }



//    public void createTestLevel1() {
//        packets.clear();
//        nodes.clear();
//        connections.clear();
//        hud.reset();
//        setGameOver(false);
//
//        // Base Left Node (emitter)
//        BasicNode baseLeft = new BasicNode(100, 250, cfg.nodeWidth, cfg.nodeHeight);
//        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
//                baseLeft.getHeight() / 3));
//        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
//                2 * baseLeft.getHeight() / 3));
//        baseLeft.addInputPort(
//                PortType.SQUARE,
//                new Vector2D(-cfg.portSize / 2, baseLeft.getHeight() * 0.5   -7));
//        baseLeft.setBaseLeft(true);
//        nodes.add(baseLeft);
//        baseLeft.getPortsPrinted();
//        baseLeft.dumpPorts();
//
//        // Intermediate Node 1
//        VPNNode mid1 = new VPNNode(400, 350, cfg.nodeWidth, cfg.nodeHeight);
//        mid1.addInputPort(PortType.SQUARE, new Vector2D(-cfg.portSize / 2,
//                baseLeft.getHeight() / 3));
//        mid1.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2,
//                2*baseLeft.getHeight() / 3));
//        mid1.getPortsPrinted();
//
//
//
////        mid1.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
////                baseLeft.getHeight() / 3));
//        mid1.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
//                2 * baseLeft.getHeight() / 3));
//        nodes.add(mid1);
//
//        // Intermediate Node 2
//        BasicNode mid2 = new BasicNode(250, 250, cfg.nodeWidth, cfg.nodeHeight);
//        mid2.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2,
//                baseLeft.getHeight() / 2));
//        mid2.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
//                baseLeft.getHeight() / 2));
//        nodes.add(mid2);
//
//        BasicNode baseRight = new BasicNode(600, 150, cfg.nodeWidth, cfg.nodeHeight);
//
//        baseRight.addInputPort(PortType.SQUARE, new Vector2D(-cfg.portSize / 2,
//                2 * baseLeft.getHeight() / 4));
//
//        baseRight.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
//                baseLeft.getHeight() / 2 )); // loop back
//        nodes.add(baseRight);
//        for (Node node : nodes)
//            node.getPortsPrinted();
//
//
//
//        // Packets start flowing from left node periodically
//        for (int i = 0; i < 1; i++) {
//            Vector2D pos = new Vector2D(baseLeft.getPosition().x() + baseLeft.getWidth() / 2,
//                    baseLeft.getPosition().y() + baseLeft.getHeight() / 2);
//            Packet[] p = new Packet[(int) cfg.numberOfPacketsLevel1];
//            for (int j=0; j<cfg.numberOfPacketsLevel1; j++) {
//                if (j%2==0)
//                    p[j] = new SquarePacket(pos, GameConfig.squareLife, GameConfig.squareSize);
//                else
//                    p[j] = new TrianglePacket(pos, GameConfig.triangleLife, GameConfig.triangleSize);
//                p[j].setMobile(false);
//                baseLeft.enqueuePacket(p[j]);
//                hud.incrementTotalPackets();
//                System.out.println("packet " + j + " created");
//                System.out.println(Arrays.toString(baseLeft.getQueuedPackets().toArray()));
//            }
//        }
//        for (Node n : nodes) {
//            n.setPacketEventListener(hud);   // HUDState already implements PacketEventListener
//        }
//        coinService.addCoins(10);
//        initialState = snapshot();
//    }
//
//    public void createTestLevel2() {
//        packets.clear();
//        nodes.clear();
//        connections.clear();
//        hud.reset();
//        gameOver = false;
//
//        BasicNode baseLeft = new BasicNode(50, 200, cfg.nodeWidth, cfg.nodeHeight);
//        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
//                baseLeft.getHeight() / 3));
//        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
//                2 * baseLeft.getHeight() / 3));
//        baseLeft.addInputPort(Port.PortType.SQUARE, new Vector2D(-cfg.portSize / 2, baseLeft.getHeight()/2));
//        baseLeft.setBaseLeft(true);
//        nodes.add(baseLeft);
//
//        BasicNode mid1 = new BasicNode(250, 50, cfg.nodeWidth, cfg.nodeHeight);
//        mid1.addInputPort(Port.PortType.SQUARE, new Vector2D(-cfg.portSize / 2, baseLeft.getHeight()/2));
//        mid1.addOutputPort(Port.PortType.SQUARE, new Vector2D(baseLeft.getWidth() -cfg.portSize / 2, baseLeft.getHeight()/2));
//        nodes.add(mid1);
//
////        SystemNode mid2 = new SystemNode(200, 350);
////        mid2.addInputPort(Port.PortType.TRIANGLE, new Vector2D(-PORT_SIZE / 2, baseLeft.getHeight()/2));
////        mid2.addOutputPort(Port.PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() -PORT_SIZE / 2, baseLeft.getHeight()/2));
////        nodes.add(mid2);
//
//        BasicNode mid3 = new BasicNode(450, 300, cfg.nodeWidth, cfg.nodeHeight);
//        mid3.addInputPort(Port.PortType.SQUARE, new Vector2D(-cfg.portSize / 2, 1*baseLeft.getHeight()/3));
//        mid3.addInputPort(Port.PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2, 2*baseLeft.getHeight()/3));
//        mid3.addOutputPort(Port.PortType.SQUARE, new Vector2D(baseLeft.getWidth() -cfg.portSize / 2, baseLeft.getHeight()/2));
//        nodes.add(mid3);
//
//        BasicNode baseRight = new BasicNode(650, 400, cfg.nodeWidth, cfg.nodeHeight);
//        baseRight.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2, baseLeft.getHeight()/2));
//        baseRight.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() -cfg.portSize / 2, baseLeft.getHeight()/2));
//        nodes.add(baseRight);
//
//        for (int i = 0; i < cfg.numberOfPacketsLevel2; i++) {
//            Vector2D pos = new Vector2D(
//                    baseLeft.getPosition().x() + baseLeft.getWidth()/2,
//                    baseLeft.getPosition().y() + baseLeft.getHeight()/2);
//            Packet p = (Math.random() > 0.5)
//                    ? new SquarePacket(pos, GameConfig.squareLife, GameConfig.squareSize)
//                    : new TrianglePacket(pos, GameConfig.triangleLife, GameConfig.triangleSize);
//            p.setMobile(false);
//            baseLeft.enqueuePacket(p);
//            hud.incrementTotalPackets();
//        }
//        nodes.forEach(n -> n.setPacketEventListener(hud));
//        initialState = snapshot();
//    }
    /** Returns all live, mobile packets within radius r of point c. */
    private List<Packet> packetsNear(Vector2D c, double r) {
        double r2 = r * r;
        List<Packet> out = new ArrayList<>();
        for (Packet p : packets) {
            if (!p.isAlive() || !p.isMobile()) continue;
            if (p.getPosition().subtracted(c).lengthSq() <= r2)
                out.add(p);
        }
        return out;
    }
    private void applyPacketRules(double dt) {
        for (Packet p : packets) {
            if (!p.isAlive()) continue;

            // TODO: 8/12/2025 : packet rules:

            // TODO: 8/12/2025 squareRules


            // Example: TrianglePacket keeps higher forward speed on wires
            // TODO: 8/12/2025 triangleRules

            // I think: trojan decays health
            if (p.isTrojanPacket()) {
                p.damage(0.1 * dt);
                if (!p.isAlive()) {
                    if (eventListener != null) eventListener.onLost(p);
                }
            }
        }
    }
    private void applyNodeAreaEffects(double dt) {
        for (Node n : nodes) {
            // AntiTrojanNode: cleans infected packets in a small radius
            if (n instanceof server.sim.model.node.AntiTrojanNode at) {
                double R = GameConfig.distanceOfAntiTrojanNodeToWork;                          // or read from node
                for (Packet p : packetsNear(n.getCenter(), R)) {
                    if (p.isTrojanPacket() && p instanceof TrojanPacket tp) {
                        AudioManager.get().playFx("heal_ping");
                        Packet original = tp.revert();   // copies position/velocity/etc. into original
                        replacePacketEverywhere(tp, original);
                        System.out.println("reverted alive=" + original.isAlive() +
                                " mobile=" + original.isMobile() +
                                " onWire=" + (original.getWire()!=null));



                        AudioManager.get().playFx("heal_ping");
                    }
                }
            }
//
//            // SpeedPadNode: boosts speed of passing packets
//            if (n instanceof server.sim.model.node.SpeedPadNode sp) {
//                double R = sp.getPadRadius();           // node API
//                double boost = sp.getBoost();           // e.g., Δv magnitude
//                for (Packet p : packetsNear(n.getCenter(), R)) {
//                    Vector2D dir = p.getVelocity().normalized();
//                    if (!Double.isNaN(dir.x()) && !Double.isNaN(dir.y()))
//                        p.addImpulse(dir.multiplied(boost * dt));
//                }
//            }

//            // SlowFieldNode: dampens velocity (sticky region)
//            if (n instanceof server.sim.model.node.SlowFieldNode sf) {
//                double R = sf.getFieldRadius();
//                double damp = sf.getDampingPerSec();    // e.g., 0.3 → 30%/s
//                for (Packet p : packetsNear(n.getCenter(), R)) {
//                    p.setVelocity(p.getVelocity().multiplied(Math.max(0.0, 1.0 - damp * dt)));
//                }
//            }
        }
    }




    /* expose it if UI / other classes need direct access */
    public CoinService getCoinService() { return coinService; }

    public void connectASimpleWire(Node node, int i, Node sinkOK, int j) {
        Port portFrom = node.getOutputs().get(i);
        Port portTo = sinkOK.getInputs().get(j);
        addConnection(new Connection(portFrom, portTo, new ArrayList<>()));
    }


    // World.java (add at top-level)
    private static final class Scheduled implements Comparable<Scheduled> {
        final double at; final Runnable r;
        Scheduled(double at, Runnable r){ this.at=at; this.r=r; }
        public int compareTo(Scheduled o){ return Double.compare(this.at, o.at); }
    }
    private final java.util.PriorityQueue<Scheduled> q = new java.util.PriorityQueue<>();
    private double time = 0.0;

    public double getTime(){ return time; }

    // call this **every frame** from WorldController.tick(dt)
    public void advanceTime(double dt){
        time += dt;
        while(!q.isEmpty() && q.peek().at <= time){
            q.poll().r.run();
        }
    }

    // schedule
    public void postAt(double atSecondsFromStart, Runnable r){
        q.add(new Scheduled(atSecondsFromStart, r));
    }

    // World.java
    private final PacketFactory packetFactory = new PacketFactory();

    public PacketFactory getPacketFactory(){ return packetFactory; }



// import your confidential types when you have them

    public class PacketFactory {
        public ConfidentialPacket confidentialSmall(Vector2D pos){
            // TODO replace with your real ConfidentialPacketA
            ConfidentialPacket p = new ConfidentialSmallPacket(pos, GameConfig.CONFIDENTIAL_SMALL_PACKET_LIFE);
            return p;
        }
        public ConfidentialPacket confidentialLarge(Vector2D pos){
            // TODO real ConfidentialPacketB
            ConfidentialPacket p = new ConfidentialLargePacket(pos, GameConfig.CONFIDENTIAL_LARGE_PACKET_LIFE);
            p.addTag("CONF_B");
            return p;
        }
        public Packet messengerSquare(Vector2D pos){
            return new SquarePacket(pos, GameConfig.squareLife, GameConfig.squareSize);
        }
        public Packet messengerTriangle(Vector2D pos){
            return new TrianglePacket(pos, GameConfig.triangleLife, GameConfig.triangleSize);
        }
        public Packet messengerInfinity(Vector2D pos){
            return new InfinityPacket(pos, GameConfig.infinityLife, GameConfig.infinitySize);
        }

        // add trojan(), bulkA(), etc. as you implement them
    }
    // World.java
    public void flashBanner(String msg){
        hud.flashBanner(msg);
    }


    // in server.sim.engine.world.World
    private void replacePacketEverywhere(Packet oldPkt, Packet newPkt) {
        // swap in world.packets

        int i = packets.indexOf(oldPkt);
        packets.set(i, newPkt);
        System.out.println("new Packet:::::::::::::::::::::::"+newPkt.isAlive());
        // swap on any wire that’s currently carrying it
        for (Connection c : connections) {
            c.replaceInTransit(oldPkt, newPkt);
        }
    }


    private void reportLoss(Packet p, String reason) {
        // keep whatever state changes you already do
        if (eventListener != null) {
            // if you later add a reasoned onLost(p, reason), call that here
            eventListener.onLost(p);
        }
        System.out.println(LossDebug.formatLine(reason, p, hud.getGameTime()));
    }




}
