// com.mygame.level.Level.java
package com.mygame.engine.world.level;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.world.World;
import com.mygame.model.Port;
import com.mygame.model.Port.PortType;
import com.mygame.model.node.*;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.messengerPacket.types.SquarePacket;
import com.mygame.model.packet.messengerPacket.types.TrianglePacket;

import java.util.Arrays;
import java.util.function.Supplier;

public final class Level{
    final String levelID;

    public int getLevelInt() {
        return levelInt;
    }

    final int levelInt;

    public Level(int level) {
        levelInt = level;
        if (level>0 && level<6)
            this.levelID = "level"+level;
        else if (level==91)
            this.levelID = "sandboxBulk";
        else if (level==92)
            this.levelID = "sandboxProtected";
        else if (level==93)
            this.levelID = "sandboxTrojan";
        else if (level==94)
            this.levelID = "sandboxVPN";
        else throw new IllegalArgumentException("Unknown level num: " + level);
    }
    public String id()    { return levelID; }
    public String title() { return levelID; }

    public void build(World world) {
        GameConfig cfg = GameConfig.defaultConfig();
        world.clearAll();               // helper in World (§3)

        // ——— paste your createTestLevel1 body here, BUT without the initialState = snapshot() line ———

        // Base Left Node (emitter)
        switch (id()) {
            case "level1" -> buildLevel1(world, cfg);
            case "level2" -> buildLevel2(world, cfg);

            case "level3" -> buildLevel3(world, cfg);
            case "level4" -> buildLevel4(world, cfg);
            case "level5" -> buildLevel5(world, cfg);



            /*test levels*/
            case "sandboxBulk" -> sandboxBulk(world, cfg);        /* Distribute & Merge pathing*/
            case "sandboxProtected" -> sandboxProtected(world, cfg);   /* Spy blocks unless protected*/
            case "sandboxTrojan" -> sandboxTrojan(world, cfg);      /* Trojan + AntiTrojan cleanup loop*/
            case "sandboxVPN" -> sandboxVPN(world, cfg);         /* Protected routing / VPN pass-through*/

            default -> throw new IllegalArgumentException("Unknown level: " + levelID);
        }
        finalizeWorld(world); // freezes initial snapshot inside World
    }

    private void buildLevel1(World world, GameConfig cfg) {
        BasicNode baseLeft = new BasicNode(100, 250, cfg.nodeWidth, cfg.nodeHeight);
        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 3));
        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                2 * baseLeft.getHeight() / 3));
        baseLeft.addInputPort(
                PortType.SQUARE,
                new Vector2D(-cfg.portSize / 2, baseLeft.getHeight() * 0.5 - 7));
        baseLeft.setBaseLeft(true);
        world.getNodes().add(baseLeft);
        baseLeft.getPortsPrinted();
        baseLeft.dumpPorts();

        // Intermediate Node 1
        VPNNode mid1 = new VPNNode(400, 350, cfg.nodeWidth, cfg.nodeHeight);
        mid1.addInputPort(PortType.SQUARE, new Vector2D(-cfg.portSize / 2,
                baseLeft.getHeight() / 3));
        mid1.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2,
                2 * baseLeft.getHeight() / 3));
        mid1.getPortsPrinted();
        mid1.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                2 * baseLeft.getHeight() / 3));
        world.getNodes().add(mid1);

        // Intermediate Node 2
        BasicNode mid2 = new BasicNode(250, 250, cfg.nodeWidth, cfg.nodeHeight);
        mid2.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2,
                baseLeft.getHeight() / 2));
        mid2.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 2));
        world.getNodes().add(mid2);

        BasicNode baseRight = new BasicNode(600, 150, cfg.nodeWidth, cfg.nodeHeight);
        baseRight.addInputPort(PortType.SQUARE, new Vector2D(-cfg.portSize / 2,
                2 * baseLeft.getHeight() / 4));
        baseRight.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 2)); // loop back
        world.getNodes().add(baseRight);

        for (Node node : world.getNodes())
            node.getPortsPrinted();


        // Packets start flowing from left node periodically
        for (int i = 0; i < 1; i++) {
            Vector2D pos = new Vector2D(baseLeft.getPosition().x() + baseLeft.getWidth() / 2,
                    baseLeft.getPosition().y() + baseLeft.getHeight() / 2);
            Packet[] p = new Packet[(int) cfg.numberOfPacketsLevel1];
            for (int j = 0; j < cfg.numberOfPacketsLevel1; j++) {
                if (j % 2 == 0)
                    p[j] = new SquarePacket(pos, GameConfig.squareLife, GameConfig.squareSize);
                else
                    p[j] = new TrianglePacket(pos, GameConfig.triangleLife, GameConfig.triangleSize);
                p[j].setMobile(false);
                baseLeft.enqueuePacket(p[j]);
                world.getHudState().incrementTotalPackets();
                System.out.println("packet " + j + " created");
                System.out.println(Arrays.toString(baseLeft.getQueuedPackets().toArray()));
            }
        }
        for (Node n : world.getNodes()) {
            n.setPacketEventListener(world.getHudState());   // HUDState already implements PacketEventListener
        }
        world.getCoinService().addCoins(10);
    }

    private void buildLevel2(World world, GameConfig cfg) {
        BasicNode baseLeft = new BasicNode(50, 200, cfg.nodeWidth, cfg.nodeHeight);
        baseLeft.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                baseLeft.getHeight() / 3));
        baseLeft.addOutputPort(PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() - cfg.portSize / 2,
                2 * baseLeft.getHeight() / 3));
        baseLeft.addInputPort(Port.PortType.SQUARE, new Vector2D(-cfg.portSize / 2, baseLeft.getHeight()/2));
        baseLeft.setBaseLeft(true);
        world.getNodes().add(baseLeft);

        BasicNode mid1 = new BasicNode(250, 50, cfg.nodeWidth, cfg.nodeHeight);
        mid1.addInputPort(Port.PortType.SQUARE, new Vector2D(-cfg.portSize / 2, baseLeft.getHeight()/2));
        mid1.addOutputPort(Port.PortType.SQUARE, new Vector2D(baseLeft.getWidth() -cfg.portSize / 2, baseLeft.getHeight()/2));
        world.getNodes().add(mid1);

//        SystemNode mid2 = new SystemNode(200, 350);
//        mid2.addInputPort(Port.PortType.TRIANGLE, new Vector2D(-PORT_SIZE / 2, baseLeft.getHeight()/2));
//        mid2.addOutputPort(Port.PortType.TRIANGLE, new Vector2D(baseLeft.getWidth() -PORT_SIZE / 2, baseLeft.getHeight()/2));
//        nodes.add(mid2);

        BasicNode mid3 = new BasicNode(450, 300, cfg.nodeWidth, cfg.nodeHeight);
        mid3.addInputPort(Port.PortType.SQUARE, new Vector2D(-cfg.portSize / 2, 1*baseLeft.getHeight()/3));
        mid3.addInputPort(Port.PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2, 2*baseLeft.getHeight()/3));
        mid3.addOutputPort(Port.PortType.SQUARE, new Vector2D(baseLeft.getWidth() -cfg.portSize / 2, baseLeft.getHeight()/2));
        world.getNodes().add(mid3);

        BasicNode baseRight = new BasicNode(650, 400, cfg.nodeWidth, cfg.nodeHeight);
        baseRight.addInputPort(PortType.TRIANGLE, new Vector2D(-cfg.portSize / 2, baseLeft.getHeight()/2));
        baseRight.addOutputPort(PortType.SQUARE, new Vector2D(baseLeft.getWidth() -cfg.portSize / 2, baseLeft.getHeight()/2));
        world.getNodes().add(baseRight);

        for (int i = 0; i < cfg.numberOfPacketsLevel2; i++) {
            Vector2D pos = new Vector2D(
                    baseLeft.getPosition().x() + baseLeft.getWidth()/2,
                    baseLeft.getPosition().y() + baseLeft.getHeight()/2);
            Packet p = (Math.random() > 0.0)
                    ? new SquarePacket(pos, GameConfig.squareLife, GameConfig.squareSize)
                    : new TrianglePacket(pos, GameConfig.triangleLife, GameConfig.triangleSize);
            p.setMobile(false);
            baseLeft.enqueuePacket(p);
            world.getHudState().incrementTotalPackets();
        }
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── Level 3: “Introduce Spy hazard” ────────────────── */
    private void buildLevel3(World world, GameConfig cfg) {
        BasicNode baseL = addBasicNode(world, 80, 250, cfg);
        baseL.addOutputPort(PortType.SQUARE,   pointMaker(baseL.getWidth() - cfg.portSize/2, baseL.getHeight()/3));
        baseL.addOutputPort(PortType.TRIANGLE, pointMaker(baseL.getWidth() - cfg.portSize/2, 2*baseL.getHeight()/3));
        baseL.setBaseLeft(true);

        // Spy in the middle (blocks unprotected) — per spec, protected should pass.
        // If SpyNode isn’t implemented yet, comment these two lines:
        SpyNode spy = new SpyNode(360, 260, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(spy);

        // A VPN near the spy so players learn to protect first
        VPNNode vpn = new VPNNode(240, 260, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(vpn);

        BasicNode sink = addBasicNode(world, 600, 250, cfg);
        sink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sink.getHeight()/2));

        enqueueRandomMessenger(baseL, (int) (cfg.numberOfPacketsLevel2 + 3), world);
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── Level 4: “Trojan + AntiTrojan radius” ──────────── */
    private void buildLevel4(World world, GameConfig cfg) {
        BasicNode baseL = addBasicNode(world, 80, 300, cfg);
        baseL.addOutputPort(PortType.SQUARE, pointMaker(baseL.getWidth() - cfg.portSize/2, baseL.getHeight()/2));
        baseL.setBaseLeft(true);

        // TODO if you have SaboteurNode that can convert to Trojan, add it between baseL and sink:
        // SaboteurNode sab = new SaboteurNode(300, 300, cfg.nodeWidth, cfg.nodeHeight);
        // world.getNodes().add(sab);

        // AntiTrojan “aura” that cleans trojans in radius
        AntiTrojanNode anti = new AntiTrojanNode(460, 300, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(anti);

        BasicNode sink = addBasicNode(world, 650, 300, cfg);
        sink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sink.getHeight()/2));

        enqueueRandomMessenger(baseL, (int) (cfg.numberOfPacketsLevel2 + 4), world);
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── Level 5: “Distribute + Merge” ──────────────────── */
    private void buildLevel5(World world, GameConfig cfg) {
        BasicNode baseL = addBasicNode(world, 60, 260, cfg);
        baseL.addOutputPort(PortType.SQUARE, pointMaker(baseL.getWidth() - cfg.portSize/2, baseL.getHeight()/2));
        baseL.setBaseLeft(true);

        // TODO: plug in real Distributor/Merger once implemented; placeholder with Basics now.
        // DistributorNode dist = new DistributorNode(260, 260, cfg.nodeWidth, cfg.nodeHeight);
        // MergerNode      mrg  = new MergerNode(520, 260, cfg.nodeWidth, cfg.nodeHeight);
        // world.getNodes().add(dist); world.getNodes().add(mrg);

        BasicNode midA = addBasicNode(world, 260, 200, cfg);
        BasicNode midB = addBasicNode(world, 260, 320, cfg);
        BasicNode mrg  = addBasicNode(world, 520, 260, cfg);
        BasicNode sink = addBasicNode(world, 700, 260, cfg);
        sink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sink.getHeight()/2));

        // If you have a BulkPacket, enqueue a few; otherwise just messenger packets to test pathing.
        // enqueueBulk(baseL, 2, world);
        enqueueRandomMessenger(baseL, (int) (cfg.numberOfPacketsLevel2 + 6), world);

        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── Sandbox 91: VPN/Protected ─────────────────────── */
    private void sandboxVPN(World world, GameConfig cfg) {
        BasicNode baseL = addBasicNode(world, 80, 260, cfg);
        baseL.addOutputPort(PortType.SQUARE, pointMaker(baseL.getWidth() - cfg.portSize/2, baseL.getHeight()/2));
        baseL.setBaseLeft(true);

        VPNNode vpn = new VPNNode(280, 260, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(vpn);

        // Spy immediately after VPN to verify protected packets pass
        SpyNode spy = new SpyNode(420, 260, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(spy);

        BasicNode sink = addBasicNode(world, 620, 260, cfg);
        sink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sink.getHeight()/2));

        enqueueRandomMessenger(baseL, 8, world);
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── Sandbox 92: Trojan/AntiTrojan ─────────────────── */
    private void sandboxTrojan(World world, GameConfig cfg) {
        BasicNode baseL = addBasicNode(world, 80, 300, cfg);
        baseL.addOutputPort(PortType.SQUARE, pointMaker(baseL.getWidth() - cfg.portSize/2, baseL.getHeight()/2));
        baseL.setBaseLeft(true);

        // Optional: Saboteur converts to Trojan between these two
        // SaboteurNode sab = new SaboteurNode(260, 300, cfg.nodeWidth, cfg.nodeHeight);
        // world.getNodes().add(sab);

        AntiTrojanNode anti = new AntiTrojanNode(440, 300, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(anti);

        BasicNode sink = addBasicNode(world, 640, 300, cfg);
        sink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sink.getHeight()/2));

        // If you have a TrojanPacket type, enqueue 2 to ensure the anti node reverts them.
        // for (int i = 0; i < 2; i++) baseL.enqueuePacket(new TrojanPacket(centerOf(baseL)));
        enqueueRandomMessenger(baseL, 6, world);

        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── Sandbox 93: Protected vs Spy ──────────────────── */
    private void sandboxProtected(World world, GameConfig cfg) {
        BasicNode baseL = addBasicNode(world, 80, 260, cfg);
        baseL.addOutputPort(PortType.SQUARE, pointMaker(baseL.getWidth() - cfg.portSize/2, baseL.getHeight()/2));
        baseL.setBaseLeft(true);

        SpyNode spy = new SpyNode(340, 260, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(spy);

        // Secondary route through VPN (players should discover protection first → pass spy)
        VPNNode vpn = new VPNNode(220, 140, cfg.nodeWidth, cfg.nodeHeight);
        world.getNodes().add(vpn);

        BasicNode sink = addBasicNode(world, 620, 260, cfg);
        sink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sink.getHeight()/2));

        enqueueRandomMessenger(baseL, 8, world);
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── Sandbox 94: Bulk/Distribute/Merge ─────────────── */
    private void sandboxBulk(World world, GameConfig cfg) {
        BasicNode baseL = addBasicNode(world, 60, 260, cfg);
        baseL.addOutputPort(PortType.SQUARE, pointMaker(baseL.getWidth() - cfg.portSize/2, baseL.getHeight()/2));
        baseL.setBaseLeft(true);

        // TODO: real Distributor/Merger once implemented. Placeholders:
        // DistributorNode dist = new DistributorNode(240, 260, cfg.nodeWidth, cfg.nodeHeight);
        // MergerNode      mrg  = new MergerNode(520, 260, cfg.nodeWidth, cfg.nodeHeight);
        // world.getNodes().add(dist); world.getNodes().add(mrg);

        BasicNode midA = addBasicNode(world, 300, 200, cfg);
        BasicNode midB = addBasicNode(world, 300, 320, cfg);
        BasicNode mrg  = addBasicNode(world, 560, 260, cfg);
        BasicNode sink = addBasicNode(world, 720, 260, cfg);
        sink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sink.getHeight()/2));

        // If you have BulkPacket, enqueue a couple for the round‑trip through dist→mrg,
        // otherwise just messenger packets to test multi‑branch routing:
        // enqueueBulk(baseL, 2, world);
        enqueueRandomMessenger(baseL, 10, world);

        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
    }

    /* ───────────────────────── helpers ───────────────────────── */
    private BasicNode addBasicNode(World w, double x, double y, GameConfig cfg) {
        BasicNode n = new BasicNode(x, y, cfg.nodeWidth, cfg.nodeHeight);
        w.getNodes().add(n);
        return n;
    }
    private Vector2D pointMaker(double x, double y) { return new Vector2D(x, y); }

    private Vector2D centerOf(Node n) {
        return new Vector2D(n.getPosition().x() + n.getWidth()/2.0,
                n.getPosition().y() + n.getHeight()/2.0);
    }


    // TODO: 8/21/2025 add other packets to the below
    private void enqueueAlternating(Node emitter,
                                    Supplier<? extends Packet> a,
                                    Supplier<? extends Packet> b,
                                    int count,
                                    World world) {
        Packet[] arr = new Packet[count];
        for (int i = 0; i < count; i++) {
            arr[i] = (i % 2 == 0) ? a.get() : b.get();
            arr[i].setMobile(false);
            emitter.enqueuePacket(arr[i]);
            world.getHudState().incrementTotalPackets();
        }
        System.out.println("Queued: " + Arrays.toString(arr));
    }

    private void enqueueRandomMessenger(Node emitter, int count, World world) {
        var posSquare   = (Supplier<Packet>) () -> new SquarePacket(centerOf(emitter), GameConfig.squareLife, GameConfig.squareSize);
        var posTriangle = (Supplier<Packet>) () -> new TrianglePacket(centerOf(emitter), GameConfig.triangleLife, GameConfig.triangleSize);
        enqueueAlternating(emitter, posSquare, posTriangle, count, world);
    }

    /** Call after build() to freeze initial snapshot inside World */
    private void finalizeWorld(World w) {
        w.captureInitialSnapshot(); // we'll add this method in World (see §3)
    }
}
