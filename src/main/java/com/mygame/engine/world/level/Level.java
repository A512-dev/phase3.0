// com.mygame.level.Level.java
package com.mygame.engine.world.level;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.world.World;
import com.mygame.model.Port;
import com.mygame.model.Port.PortType;
import com.mygame.model.node.*;
import com.mygame.model.packet.Packet;
import com.mygame.model.packet.messengerPacket.types.InfinityPacket;
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
        else if (level==95)
            this.levelID = "sandboxTest1";
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
            case "sandboxVPN" -> sandboxTest1(world, cfg);         /* Protected routing / VPN pass-through*/
            case "sandboxTest1" -> {
                System.out.println("LLLLLLLLLLSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
                sandboxTest1(world, cfg);

            }

            default -> throw new IllegalArgumentException("Unknown level: " + levelID);
        }
        finalizeWorld(world); // freezes initial snapshot inside World
    }

    private void buildLevel1Org(World world, GameConfig cfg) {
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
    private void buildLevel1(World world, GameConfig cfg) {
        // Emitter
        BasicNode emitter = addBasicNode(world, 80, 260, cfg);
        emitter.addInputPort(PortType.SQUARE,  pointMaker(-cfg.portSize/2, emitter.getHeight()/2));
        emitter.addOutputPort(PortType.SQUARE,   pointMaker(emitter.getWidth() - cfg.portSize/2, emitter.getHeight()/3));
        emitter.addOutputPort(PortType.TRIANGLE, pointMaker(emitter.getWidth() - cfg.portSize/2, 2*emitter.getHeight()/3));
        emitter.setBaseLeft(true);

        // Mid for squares
        BasicNode mid = addBasicNode(world, 300, 200, cfg);
        mid.addInputPort(PortType.SQUARE,  pointMaker(-cfg.portSize/2, mid.getHeight()/2));
        mid.addOutputPort(PortType.SQUARE, pointMaker(mid.getWidth() - cfg.portSize/2, mid.getHeight()/2));

        // Sink accepts both
        BasicNode sink = addBasicNode(world, 620, 260, cfg);
        sink.addInputPort(PortType.SQUARE,   pointMaker(-cfg.portSize/2, sink.getHeight()/3));
        sink.addInputPort(PortType.TRIANGLE, pointMaker(-cfg.portSize/2, 2*sink.getHeight()/3));
        sink.addOutputPort(PortType.SQUARE, pointMaker(mid.getWidth() - cfg.portSize/2, sink.getHeight()/2));



        // Spawns
        enqueueRandomMessenger(emitter, 10, world);

        // HUD listener
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
        world.getCoinService().addCoins(5); // no upfront bonus here
    }
    // Level.java — REPLACE the whole buildLevel2(...) with this
    private void buildLevel2(World world, GameConfig cfg) {
        // --- Emitter (left) ---
        BasicNode emitter = addBasicNode(world, 80, 260, cfg);
        emitter.addInputPort(PortType.SQUARE,   pointMaker(-cfg.portSize/2, emitter.getHeight()/2));
        emitter.addOutputPort(PortType.SQUARE,   pointMaker(emitter.getWidth() - cfg.portSize/2, emitter.getHeight()/4));
        emitter.addOutputPort(PortType.TRIANGLE, pointMaker(emitter.getWidth() - cfg.portSize/2, 2*emitter.getHeight()/4));
        emitter.addOutputPort(PortType.SQUARE,   pointMaker(emitter.getWidth() - cfg.portSize/2, 3*emitter.getHeight()/4));
        emitter.setBaseLeft(true);

        // --- Secure route: VPN -> Spy -> Sink ---
        VPNNode vpn = new VPNNode(260, 120, cfg.nodeWidth, cfg.nodeHeight);
        // inputs (left side)
        vpn.addInputPort(PortType.SQUARE,   pointMaker(-cfg.portSize/2, vpn.getHeight()/3));
        //vpn.addInputPort(PortType.TRIANGLE, pointMaker(-cfg.portSize/2, 2*vpn.getHeight()/3));
        // outputs (right side)
        //vpn.addOutputPort(PortType.SQUARE,   pointMaker(vpn.getWidth() - cfg.portSize/2, vpn.getHeight()/3));
        vpn.addOutputPort(PortType.TRIANGLE, pointMaker(vpn.getWidth() - cfg.portSize/2, 2*vpn.getHeight()/3));
        world.getNodes().add(vpn);

        SpyNode spy = new SpyNode(420, 220, cfg.nodeWidth, cfg.nodeHeight);
        // inputs (left)
        spy.addInputPort(PortType.SQUARE,   pointMaker(-cfg.portSize/2, spy.getHeight()/3));
        spy.addInputPort(PortType.TRIANGLE, pointMaker(-cfg.portSize/2, 2*spy.getHeight()/3));
        // outputs (right)
        spy.addOutputPort(PortType.SQUARE,   pointMaker(spy.getWidth() - cfg.portSize/2, spy.getHeight()/3));
        spy.addOutputPort(PortType.TRIANGLE, pointMaker(spy.getWidth() - cfg.portSize/2, 2*spy.getHeight()/3));
        world.getNodes().add(spy);

        BasicNode sink = addBasicNode(world, 640, 120, cfg);
        sink.addInputPort(PortType.SQUARE,   pointMaker(-cfg.portSize/2, sink.getHeight()/3));
        sink.addOutputPort(PortType.SQUARE,   pointMaker(sink.getWidth() - cfg.portSize/2, sink.getHeight()/3));

        // --- “Wrong” path: straight into a spy (to showcase hazards/teleport) ---
        SpyNode trapSpy = new SpyNode(260, 340, cfg.nodeWidth, cfg.nodeHeight);
        // inputs
        trapSpy.addInputPort(PortType.SQUARE,   pointMaker(-cfg.portSize/2, trapSpy.getHeight()/3));
        trapSpy.addInputPort(PortType.TRIANGLE, pointMaker(-cfg.portSize/2, 2*trapSpy.getHeight()/3));
        // outputs
        trapSpy.addOutputPort(PortType.SQUARE,   pointMaker(trapSpy.getWidth() - cfg.portSize/2, trapSpy.getHeight()/3));
        trapSpy.addOutputPort(PortType.TRIANGLE, pointMaker(trapSpy.getWidth() - cfg.portSize/2, 2*trapSpy.getHeight()/3));
        world.getNodes().add(trapSpy);

//        BasicNode trapSink = addBasicNode(world, 640, 340, cfg);
//        trapSink.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, trapSink.getHeight()/2));


        // --- Spawns ---
        enqueueRandomMessenger(emitter, 12, world);

        // --- HUD + seed coins ---
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
        world.getCoinService().addCoins(5);
    }


    private void buildLevel2Org(World world, GameConfig cfg) {
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
    /* ────────────────────── Level 3: Trojan + AntiTrojan (with VPN+Spy carryover) ────────────────────── */
    private void buildLevel3(World world, GameConfig cfg) {
        // avoid stale teleporter state when restarting/advancing
        SpyNode.resetRegistry();

        // ── 1) Emitter: two SQUARE outputs (top = protected route, bottom = unprotected route)
        BasicNode emitter = addBasicNode(world, 30, 220, cfg);
        emitter.addInputPort(PortType.SQUARE, pointMaker( - cfg.portSize/2, emitter.getHeight()/3));           //
        emitter.addInputPort(PortType.SQUARE, pointMaker( - cfg.portSize/2, 2*emitter.getHeight()/3));           //
        emitter.addOutputPort(PortType.SQUARE, pointMaker(emitter.getWidth() - cfg.portSize/2, emitter.getHeight()/3));           // top
        emitter.addOutputPort(PortType.SQUARE, pointMaker(emitter.getWidth() - cfg.portSize/2, 2*emitter.getHeight()/3));         // bottom
        emitter.setBaseLeft(true);

        // ── 2) VPN + Spy pair on the top branch (keeps Level 2 mechanic alive)
        VPNNode vpnA = new VPNNode(140, 30, cfg.nodeWidth, cfg.nodeHeight);
        vpnA.addInputPort(PortType.SQUARE,  pointMaker(-cfg.portSize/2,  vpnA.getHeight()/2));
        vpnA.addOutputPort(PortType.SQUARE, pointMaker(vpnA.getWidth() - cfg.portSize/2, vpnA.getHeight()/2));
        world.getNodes().add(vpnA);

        SpyNode spyA = new SpyNode(280, 30, cfg.nodeWidth, cfg.nodeHeight);
        spyA.addInputPort(PortType.SQUARE,  pointMaker(-cfg.portSize/2,  spyA.getHeight()/2));
        spyA.addOutputPort(PortType.SQUARE, pointMaker(spyA.getWidth() - cfg.portSize/2, spyA.getHeight()/2));
        world.getNodes().add(spyA);

        // ── 3) Teleport target spy on the right side
        SpyNode spyB = new SpyNode(180, 200, cfg.nodeWidth, cfg.nodeHeight);
        spyB.addInputPort(PortType.SQUARE,  pointMaker(-cfg.portSize/2,  spyB.getHeight()/2));
        spyB.addOutputPort(PortType.SQUARE, pointMaker(spyB.getWidth() - cfg.portSize/2, spyB.getHeight()/2));
        world.getNodes().add(spyB);
        SpyNode.linkSpies(spyA, spyB); // A<->B teleport cluster

        // ── 4) Trojanization path on the bottom branch (main focus of lvl 3)
        // Unprotected hits a lone spy (teleport), then goes through Saboteur → AntiTrojan aura → sink.
        SpyNode spyTrap = new SpyNode(170, 380, cfg.nodeWidth, cfg.nodeHeight);
        spyTrap.addInputPort(PortType.SQUARE,  pointMaker(-cfg.portSize/2,  spyTrap.getHeight()/2));
        spyTrap.addOutputPort(PortType.SQUARE, pointMaker(spyTrap.getWidth() - cfg.portSize/2, spyTrap.getHeight()/2));
        world.getNodes().add(spyTrap);

        SaboteurNode sab = new SaboteurNode(400, 180, cfg.nodeWidth, cfg.nodeHeight);
        sab.addInputPort(PortType.SQUARE,  pointMaker(-cfg.portSize/2,  sab.getHeight()/2));
        sab.addOutputPort(PortType.SQUARE, pointMaker(sab.getWidth() - cfg.portSize/2, sab.getHeight()/3));
        sab.addOutputPort(PortType.TRIANGLE, pointMaker(sab.getWidth() - cfg.portSize/2, 2*sab.getHeight()/3));
        sab.setTrojanConversionProbability(0.55); // tune as you like
        world.getNodes().add(sab);

        // AntiTrojan is an aura node (no ports needed); place it on the path Saboteur→sink
        AntiTrojanNode anti = new AntiTrojanNode(520, 130, cfg.antiTrojanNodeWidth, cfg.antiTrojanNodeHeight);
        world.getNodes().add(anti);

        // ── 5) Sinks
        BasicNode sinkOK = addBasicNode(world, 680, 70, cfg); // top/protected & teleported via spyB can end here
        sinkOK.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkOK.getHeight()/3));
        sinkOK.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, 2*sinkOK.getHeight()/3)); // extra lane if needed
        sinkOK.addOutputPort(PortType.SQUARE, pointMaker(sinkOK.getWidth() - cfg.portSize/2, sinkOK.getHeight()/2));           //


        BasicNode sinkAlt = addBasicNode(world, 680, 350, cfg); // alternative for local spyTrap fallback
        sinkAlt.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkAlt.getHeight()/2));
        sinkAlt.addOutputPort(PortType.SQUARE, pointMaker(sinkAlt.getWidth() - cfg.portSize/2, sinkAlt.getHeight()/2));           //

        // ── 6) Wiring
        // Top (protected) route: Emitter → VPN → SpyA → Sink
        world.connectASimpleWire(emitter, 0, vpnA, 0);
        world.connectASimpleWire(vpnA,    0, spyA, 0);
        world.connectASimpleWire(spyA,    0, sinkOK, 0);

        // Teleport exit path (so spyTrap can “pop out” here and continue): SpyB → Saboteur
        world.connectASimpleWire(spyB, 0, sab, 0);

        // Saboteur → through AntiTrojan’s aura (pass nearby) → sinkOK
        world.connectASimpleWire(sab, 0, sinkOK, 1);

        // Bottom (unprotected) route: Emitter → spyTrap
        // spyTrap will TRY teleport (to spyB); if no exit is ready it uses its local output to sinkAlt.
        world.connectASimpleWire(emitter, 1, spyTrap, 0);
        world.connectASimpleWire(spyTrap, 0, sinkAlt, 0); // local fallback if teleport isn’t available this tick

        // ── 7) Spawns (simple stream; protected on top due to VPN; bottom stays unprotected)
        for (int i = 0; i < 12; i++) {
            Packet p = world.getPacketFactory().messengerSmall(centerOf(emitter)); // SQUARE
            p.setMobile(false);
            emitter.enqueuePacket(p);
            world.getHudState().incrementTotalPackets();
        }

        // ── 8) HUD + coins + small teaching banner
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
        world.getCoinService().addCoins(10);
        world.postAt(6.0, () -> world.flashBanner("Protected packets pass spies locally; unprotected may teleport, then get trojanized & cleaned."));
    }


    /* ───────────────────────── Level 3: “Introduce Spy hazard” ────────────────── */
    private void buildLevel3Org(World world, GameConfig cfg) {
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
        AntiTrojanNode anti = new AntiTrojanNode(460, 300, cfg.antiTrojanNodeWidth, cfg.antiTrojanNodeHeight);
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


    private void sandboxTest1(World world, GameConfig cfg) {
        // --- Nodes
        BasicNode emitter   = addBasicNode(world,  80, 220, cfg);
        emitter.addOutputPort(PortType.SQUARE,   pointMaker(emitter.getWidth() - cfg.portSize/2, emitter.getHeight()/3));
        emitter.addOutputPort(PortType.TRIANGLE, pointMaker(emitter.getWidth() - cfg.portSize/2, 2*emitter.getHeight()/3));
        emitter.setBaseLeft(true);

        VPNNode        vpnA = new VPNNode(240, 220, cfg.nodeWidth, cfg.nodeHeight);
        vpnA.addInputPort(PortType.SQUARE,   pointMaker(- cfg.portSize/2, vpnA.getHeight()/3));
        vpnA.addOutputPort(PortType.TRIANGLE, pointMaker(vpnA.getWidth() - cfg.portSize/2, 2*vpnA.getHeight()/3));

        System.out.println("JJJJJJJJJJJJJJJJJSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS========"+vpnA.getInputs());

        SaboteurNode dist= new SaboteurNode(390, 220, cfg.nodeWidth, cfg.nodeHeight);
        dist.addInputPort(PortType.SQUARE,   pointMaker(- cfg.portSize/2, dist.getHeight()/3));
        dist.addOutputPort(PortType.TRIANGLE, pointMaker(dist.getWidth() - cfg.portSize/2, 2*dist.getHeight()/3));



        SpyNode         spyB= new SpyNode(560, 220, cfg.nodeWidth, cfg.nodeHeight);
        spyB.addInputPort(PortType.SQUARE,   pointMaker(- cfg.portSize/2, dist.getHeight()/3));
        spyB.addOutputPort(PortType.TRIANGLE, pointMaker(dist.getWidth() - cfg.portSize/2, 2*dist.getHeight()/3));

        BasicNode    sinkOK = addBasicNode(world, 740, 220, cfg);

        sinkOK.addOutputPort(PortType.TRIANGLE, pointMaker(dist.getWidth() - cfg.portSize/2, 2*dist.getHeight()/3));
        sinkOK.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkOK.getHeight()/2));

        SpyNode         spyA= new SpyNode(240, 110, cfg.nodeWidth, cfg.nodeHeight);
        spyA.addInputPort(PortType.SQUARE,   pointMaker(- cfg.portSize/2, dist.getHeight()/3));
        spyA.addOutputPort(PortType.TRIANGLE, pointMaker(dist.getWidth() - cfg.portSize/2, 2*dist.getHeight()/3));

        BasicNode  sinkSpyEx= addBasicNode(world, 740, 110, cfg);
        sinkSpyEx.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkSpyEx.getHeight()/2));
        sinkSpyEx.addOutputPort(PortType.TRIANGLE, pointMaker(dist.getWidth() - cfg.portSize/2, 2*dist.getHeight()/3));

        // Confidential lane
        BasicNode  confSrcA = addBasicNode(world,  80, 360, cfg);  // emits Confidential A
        confSrcA.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkSpyEx.getHeight()/2));
        confSrcA.addOutputPort(PortType.SQUARE, pointMaker(confSrcA.getWidth() - cfg.portSize/2, confSrcA.getHeight()/2));
        BasicNode       sysQ= addBasicNode(world, 240, 360, cfg);  // holds one packet in queue to force slow-down
        sysQ.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkSpyEx.getHeight()/2));
        sysQ.addOutputPort(PortType.SQUARE, pointMaker(confSrcA.getWidth() - cfg.portSize/2, confSrcA.getHeight()/2));

        VPNNode         vpnB= new VPNNode(420, 360, cfg.nodeWidth, cfg.nodeHeight);
        vpnB.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkSpyEx.getHeight()/2));
        vpnB.addOutputPort(PortType.TRIANGLE, pointMaker(dist.getWidth() - cfg.portSize/2, 2*dist.getHeight()/3));

        BasicNode  sinkConf = addBasicNode(world, 660, 360, cfg);
        sinkConf.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkConf.getHeight()/2));

        SpyNode         spyC= new SpyNode(420, 430, cfg.nodeWidth, cfg.nodeHeight); // kills Confidential A
        spyC.addInputPort(PortType.SQUARE, pointMaker(-cfg.portSize/2, sinkSpyEx.getHeight()/2));
        spyC.addOutputPort(PortType.TRIANGLE, pointMaker(dist.getWidth() - cfg.portSize/2, 2*dist.getHeight()/3));

        world.getNodes().addAll(java.util.List.of(vpnA, dist, spyB, sinkOK, spyA, sinkSpyEx, vpnB, sinkConf, spyC));

        // --- Wiring (your engine likely builds wires elsewhere; this names intent)
        // Top branch through VPN -> Distributor -> SpyB -> sinkOK
        // Parallel top branch direct to SpyA -> sinkSpyEx
        // Bottom Confidential lane: confSrcA -> sysQ -> vpnB -> sinkConf
        // Bottom kill lane: confSrcA -> spyC (to verify deletion)

        // (Pseudo; replace with your actual connectASimpleWire() API)
        System.out.println("emmiter="+emitter.getOutputs());
        world.connectASimpleWire(emitter, 0, vpnA, 0);            // messenger -> VPN  (protected out)
        world.connectASimpleWire(vpnA,    0, dist, 0);            // protected -> Distributor (no effect expected)
        world.connectASimpleWire(dist,    0, spyB, 0);            // then SpyB (should keep protected unchanged)
        world.connectASimpleWire(spyB,    0, sinkOK, 0);

        world.connectASimpleWire(emitter, 1, spyA, 0);            // messenger -> SpyA (should exit from SpyB path)
        world.connectASimpleWire(spyA,    0, sinkSpyEx, 0);

        world.connectASimpleWire(confSrcA,0, sysQ, 0);            // Conf A -> system with queued packet (forces slow-down)
        world.connectASimpleWire(sysQ,    0, vpnB, 0);            // Conf A -> VPN_B => becomes Conf B
        world.connectASimpleWire(vpnB,    0, sinkConf, 0);

        world.connectASimpleWire(confSrcA,0, spyC, 0);            // Conf A -> SpyC => should be deleted

        // --- Spawns
        // 1) Top path: cycle through three messenger flavors; half go through VPN (protected), half go to SpyA (unprotected)
        enqueueMessengerTriplet(emitter, 6, world); // emits SQUARE, TRIANGLE, (THIRD), repeating; queued/motion control stays with nodes

        // Route control: if you choose outputs by player wiring, keep both wires as above. If emitter chooses port by packet type,
        // ensure both outputs are used (e.g., SQUARE -> port0 (VPN path), TRIANGLE/THIRD -> port1 (SpyA path)).

        // 2) Confidential lane: emit two Conf A; one goes through sysQ then VPN_B => becomes Conf B, one directly into SpyC (lost)
        enqueueConfidentialA(confSrcA, /*count*/2, world);

        // 3) Make sysQ hold a packet briefly so Conf A slows down approaching it:
        holdAPacket(sysQ, world, cfg);

        // 4) Configure Distributor: incompatible routing + noise + trojan chance
        dist.setIncompatibleRoutingEnabled(true);
        dist.setInjectUnitNoiseIfNone(true);
        dist.setTrojanConversionProbability(0.4); // example; tune to taste

        // 5) Make Spy network aware (so entry at SpyA can exit at SpyB)
        SpyNode.linkSpies(spyA, spyB); // or register in a shared registry; depends on your implementation

        // 6) Attach listeners for assertions (probe + HUD)
        world.getNodes().forEach(n -> n.setPacketEventListener(world.getHudState()));
        installProbe(world);

        // 7) Optional: simulate a VPN failure mid-run to assert reversion of protected packets
        world.postAt(6.0, () -> {
            vpnA.setOnline(false);            // goes offline
            vpnA.revertPreviouslyProtected(); // your VPN should track IDs it converted; revert them now
        });

        world.getCoinService().addCoins(10);
    }

    /** Cycles Square, Triangle, and a third messenger type if you have it (fallback to Triangle). */
    private void enqueueMessengerTriplet(Node emitter, int count, World world) {
        for (int i = 0; i < count; i++) {
            Packet p;
            int mod = i % 3;
            if (mod == 0) {
                p = new SquarePacket(centerOf(emitter), GameConfig.squareLife, GameConfig.squareSize);
            } else if (mod == 1) {
                p = new TrianglePacket(centerOf(emitter), GameConfig.triangleLife, GameConfig.triangleSize);
            } else {
                // TODO: replace with your third messenger type (e.g., CirclePacket or InfinityPacket)
                p = new InfinityPacket(centerOf(emitter), GameConfig.infinityLife, GameConfig.infinitySize);
            }
            p.setMobile(false);
            emitter.enqueuePacket(p);
            world.getHudState().incrementTotalPackets();
        }
    }

    /** Emit two Confidential A packets. Replace class names with your own types if different. */
    private void enqueueConfidentialA(Node emitter, int count, World world) {
        for (int i = 0; i < count; i++) {
            Packet p = world.getPacketFactory().confidentialSmall(centerOf(emitter)); // or new ConfidentialPacketA(...)
            p.setMobile(false);
            emitter.enqueuePacket(p);
            world.getHudState().incrementTotalPackets();
        }
    }

    /** Keep one packet queued in sysQ briefly so approaching Confidential A slows down. */
    private void holdAPacket(BasicNode sysQ, World world, GameConfig cfg) {
        Packet blocker = new SquarePacket(centerOf(sysQ), GameConfig.squareLife, GameConfig.squareSize);
        blocker.setMobile(false);
        sysQ.enqueuePacket(blocker);
        // Release it later so traffic resumes (ensures Conf A slows only while it’s present)
        world.postAt(4.0, () -> {
            sysQ.emitFrom(0); // or however your node flushes its queue
        });
    }

    /** Records events so we can assert all feats at runtime. */
    private void installProbe(World w) {
        class Probe implements com.mygame.model.PacketEventListener {
            int delivered=0, lost=0, trojanized=0, spyTeleports=0, protectedPass=0, confBSpacingOK=0, confAKilled=0, protectedReverted=0;
            java.util.Set<Integer> protectedByVpnA = new java.util.HashSet<>();

            @Override public void onDelivered(Packet p) {
                delivered++;
                if (p.isProtectedPacket()) protectedPass++;
                if (p.hasTag("SPACING_OK")) confBSpacingOK++;
            }

            @Override
            public void onCollision(Packet a, Packet b) {

            }

            @Override public void onLost(Packet p) {
                lost++;
                if (p.isConfidentialPacket() && !p.isProtectedPacket()) confAKilled++;
            }
            @Override public void onMutation(Packet before, Packet after, String reason) {
                if ("SPY_EXIT".equals(reason)) spyTeleports++;
                if ("DISTRIBUTOR_TROJAN".equals(reason)) trojanized++;
                if ("VPN_PROTECT_A".equals(reason)) { protectedByVpnA.add(after.getId()); }
                if ("VPN_A_OFFLINE_REVERT".equals(reason) && protectedByVpnA.contains(before.getId())) protectedReverted++;
            }
        }
        Probe probe = new Probe();
        w.setPacketEventListener(probe);

        // End-of-level assertions (pop a banner or log)
        w.postAt(10.0, () -> {
            assertTrue("Spy should teleport some packets", probe.spyTeleports >= 1);
            assertTrue("Protected should pass spies", probe.protectedPass >= 1);
            assertTrue("Conf A should be killed by spy", probe.confAKilled >= 1);
            assertTrue("Distributor should trojanize some unprotected", probe.trojanized >= 1);
            assertTrue("Conf B should report spacing OK", probe.confBSpacingOK >= 1);
            assertTrue("VPN offline should revert at least one", probe.protectedReverted >= 1);
            w.getHudState().flashBanner("Protector Lab OK");
        });
    }

    private void assertTrue(String msg, boolean cond) {
        if (!cond) throw new AssertionError(msg);
    }


}
