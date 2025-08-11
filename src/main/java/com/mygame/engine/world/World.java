package com.mygame.engine.world;

import com.mygame.audio.AudioManager;
import com.mygame.core.GameConfig;
import com.mygame.engine.physics.CollisionSystem;
import com.mygame.engine.physics.ImpactWaveSystem;
import com.mygame.model.Connection;
import com.mygame.engine.loop.TimeController;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.PacketEventListener;
import com.mygame.model.Port;
import com.mygame.model.Port.*;
import com.mygame.model.node.BasicNode;
import com.mygame.model.node.Node;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.messengerPacket.types.SquarePacket;
import com.mygame.model.packet.messengerPacket.types.TrianglePacket;
import com.mygame.model.powerup.ActivePowerUp;
import com.mygame.model.powerup.PowerUpType;
import com.mygame.model.state.HUDState;
import com.mygame.core.GameState;
import com.mygame.service.CoinService;
import com.mygame.snapshot.*;


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


    private final TimeController timeController    = new TimeController();
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

    public World() {
        if (GameState.currentLevel == 1) {
            createTestLevel1();
        } else if (GameState.currentLevel == 2) {
            createTestLevel2();
        }
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
                .map(PacketSnapshot::of)
                .toList();



        return new WorldSnapshot(
                nodes.stream().map(NodeSnapshot::of).toList(),
                connSnaps,
                pktSnaps,
                hud.readOnlyCopy(),
                coinService,// HudReadOnly instance
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
        simTimeAccumulator += dt;

        System.out.println("WorldPackets update");
        //System.out.println("WorldPackets="+ packets.size());
//        // 1) Move everything
//        for (Packet p : packets) {
//            System.out.println(1);
//            p.update(dt);
//        }
        // 2) physics resolution
        collisionSystem.step(packets, dt);
        waveSystem.update(dt, connections, false);


        // 3) Cull off-track & dead; handle arrivals
        List<Packet> stillAlive = new ArrayList<>();
        for (Packet p : packets) {
            if (p.hasArrived()) {
                eventListener.onDelivered(p);
                AudioManager.get().playFx("Picked_Coin_Echo");
                for (Node node : nodes) {
                    for (Port in : node.getInputs()) {
                        if (in.getCenter().distanceTo(p.getPathEnd()) < 1e-6) {
                            if (node == nodes.get(0)) {
                                if (eventListener != null) eventListener.onDelivered(p);
                            } else {
                                p.setMobile(false);
                                node.enqueuePacket(p);
                            }
                            if (eventListener instanceof HUDState) {
                                int coinValue = p.getCoinValue();
                                HUDState h = (HUDState) eventListener;
                                h.setCoins(h.getCoins() + coinValue);
                            }
                        }
                    }
                }
                continue;
            }

            if (!p.isAlive()) {
                System.out.println("⚰ Packet died: not alive");
                eventListener.onLost(p);
            }
            else if (p.isOffTrackLine(cfg.maxDistanceOffTrack, p.getPosition())) {
                System.out.println("⚠️ Packet died: off track");
                System.out.println("   pos=" + p.getPosition());
                System.out.println("   maxDist=" + cfg.maxDistanceOffTrack);
                eventListener.onLost(p);
            }
            else {
                stillAlive.add(p);
            }
        }
        packets.clear();
        packets.addAll(stillAlive);

//        if (hud.getPacketLossRatio() > 0.5 && !viewOnlyMode) {
//            setGameOver(true);
//            AudioManager.get().playFx("losegamemusic");
//        }

        System.out.println("nodes update");
        for (Node node : nodes) {
            //System.out.println(Arrays.toString(node.getQueuedPackets().toArray()));
            node.update(dt, packets);
        }
        System.out.println("WorldPackets="+ packets.size());


        boolean allSettled = packets.isEmpty()
                && nodes.stream().allMatch(n -> n.getQueuedPackets().isEmpty());

        if (allSettled && !gameOver) {
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

            lossPacketRepeat.add(hud.getLostPackets());
            if (hud.getNumOfGoToTarget() < cfg.numberOfRuns
                    && onReachedTargetCallback != null) {
                onReachedTargetCallback.run();
            }
            if (lossPacketRepeat.size() == cfg.numberOfRuns + 1) {
                lossPacketRepeat = new ArrayList<>();
            }
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
    public void addNode(BasicNode n)           { nodes.add(n); }
    public void addConnection(Connection c)      {
        connections.add(c);
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
        connections.removeIf(conn ->
                (conn.getFrom() == a && conn.getTo() == b) ||
                        (conn.getFrom() == b && conn.getTo() == a)
        );
    }
    // inside com.mygame.engine.world.World

    /** Re-initialises runtime lists from an immutable snapshot. */
    public void resetToSnapshot(WorldSnapshot snap) {
        // 1. basic counters
        hud.reset();
        simTimeAccumulator = 0;

        // 2. replace domain collections (lists are final, so clear+add)
        packets.clear();                  // packets list stays empty; base nodes will re-emit
        nodes.clear();
        for (NodeSnapshot ns : snap.nodes()) {
            // assume all your level-1/2 nodes are BasicNode; if you have other types
            // switch on ns.kind() to pick the right subclass
            BasicNode n = new BasicNode(
                    ns.position().x(),
                    ns.position().y(),
                    ns.width(),
                    ns.height()
            );
            // if you stored flags in kind or elsewhere, restore them here:
            if ("BasicNode".equals(ns.kind()) ) {
                n.setBaseLeft(true);
            }
            // now re-add every port
            for (PortSnapshot ps : ns.ports()) {
                if (ps.direction() == Port.PortDirection.INPUT) {
                    n.addInputPort(ps.type(), ps.position().copy());
                } else {
                    n.addOutputPort(ps.type(), ps.position().copy());
                }
            }
            nodes.add(n);
        }

        connections.clear();
        connections.addAll(
                snap.connections().stream()
                        .map(cs -> {
                            Port from = nodes.stream()
                                    .flatMap(n -> n.getPorts().stream())
                                    .filter(p -> p.getCenter().equals(cs.fromPos()))
                                    .findFirst().orElse(null);
                            Port to   = nodes.stream()
                                    .flatMap(n -> n.getPorts().stream())
                                    .filter(p -> p.getCenter().equals(cs.toPos()))
                                    .findFirst().orElse(null);
                            List<Vector2D> bends = cs.bends();
                            return (from != null && to != null) ? new Connection(from, to, bends) : null;
                        })
                        .filter(Objects::nonNull)
                        .toList()
        );

        gameOver     = snap.isGameOver();
        viewOnlyMode = snap.isViewOnly();
    }


    public void createTestLevel1() {
        packets.clear();
        nodes.clear();
        connections.clear();
        hud.reset();
        setGameOver(false);

        // Base Left Node (emitter)
        BasicNode baseLeft = new BasicNode(100, 250, cfg.nodeWidth, cfg.nodeHeight);
        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 3));
        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                2 * baseLeft.getHeight() / 3));
        baseLeft.addInputPort(
                PortType.SQUARE,
                new Vector2D(-cfg.portSize / 2, baseLeft.getHeight() * 0.5   -7));
        baseLeft.setBaseLeft(true);
        nodes.add(baseLeft);
        baseLeft.getPortsPrinted();
        baseLeft.dumpPorts();

        // Intermediate Node 1
        BasicNode mid1 = new BasicNode(400, 350, cfg.nodeWidth, cfg.nodeHeight);
        mid1.addInputPort(PortType.SQUARE, new Vector2D(-cfg.portSize / 2,
                baseLeft.getHeight() / 3));
        mid1.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2,
                2*baseLeft.getHeight() / 3));
        mid1.getPortsPrinted();



//        mid1.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - PORT_SIZE / 2,
//                baseLeft.getHeight() / 3));
        mid1.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                2 * baseLeft.getHeight() / 3));
        nodes.add(mid1);

        // Intermediate Node 2
        BasicNode mid2 = new BasicNode(250, 250, cfg.nodeWidth, cfg.nodeHeight);
        mid2.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2,
                baseLeft.getHeight() / 2));
        mid2.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 2));
        nodes.add(mid2);

        BasicNode baseRight = new BasicNode(600, 150, cfg.nodeWidth, cfg.nodeHeight);

        baseRight.addInputPort(PortType.SQUARE, new Vector2D(-cfg.portSize / 2,
                2 * baseLeft.getHeight() / 4));

        baseRight.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 2 )); // loop back
        nodes.add(baseRight);
        for (Node node : nodes)
            node.getPortsPrinted();



        // Packets start flowing from left node periodically
        for (int i = 0; i < 1; i++) {
            Vector2D pos = new Vector2D(baseLeft.getPosition().x() + baseLeft.getWidth() / 2,
                    baseLeft.getPosition().y() + baseLeft.getHeight() / 2);
            Packet[] p = new Packet[(int) cfg.numberOfPacketsLevel1];
            for (int j=0; j<cfg.numberOfPacketsLevel1; j++) {
                if (j%2==0)
                    p[j] = new SquarePacket(pos, GameConfig.squareLife);
                else
                    p[j] = new TrianglePacket(pos);
                p[j].setMobile(false);
                baseLeft.enqueuePacket(p[j]);
                hud.incrementTotalPackets();
                System.out.println("packet " + j + " created");
                System.out.println(Arrays.toString(baseLeft.getQueuedPackets().toArray()));
            }
        }
        for (Node n : nodes) {
            n.setPacketEventListener(hud);   // HUDState already implements PacketEventListener
        }
        coinService.addCoins(10);
        initialState = snapshot();
    }

    public void createTestLevel2() {
        packets.clear();
        nodes.clear();
        connections.clear();
        hud.reset();
        gameOver = false;

        BasicNode baseLeft = new BasicNode(50, 200, cfg.nodeWidth, cfg.nodeHeight);
        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(
                baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 3));
        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(
                baseLeft.getWidth() - cfg.portSize / 2,
                2 * baseLeft.getHeight() / 3));
        baseLeft.addInputPort(Port.PortType.SQUARE, new Vector2D(
                -cfg.portSize/2,
                baseLeft.getHeight()/2));
        baseLeft.setBaseLeft(true);
        nodes.add(baseLeft);

        // … mid1, mid3, baseRight setup …

        for (int i = 0; i < cfg.numberOfPacketsLevel2; i++) {
            Vector2D pos = new Vector2D(
                    baseLeft.getPosition().x() + baseLeft.getWidth()/2,
                    baseLeft.getPosition().y() + baseLeft.getHeight()/2);
            Packet p = (Math.random() > 0.5)
                    ? new SquarePacket(pos, GameConfig.squareLife)
                    : new TrianglePacket(pos);
            p.setMobile(false);
            baseLeft.enqueuePacket(p);
            hud.incrementTotalPackets();
        }
        nodes.forEach(n -> n.setPacketEventListener(hud));
        initialState = snapshot();
    }


    /* expose it if UI / other classes need direct access */
    public CoinService getCoinService() { return coinService; }
}
