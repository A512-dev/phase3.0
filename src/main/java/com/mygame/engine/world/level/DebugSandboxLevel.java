// com.mygame.level.DebugSandboxLevel.java
package com.mygame.engine.world.level;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.world.World;
import com.mygame.engine.world.level.LevelDefinition;
import com.mygame.model.node.*;
import com.mygame.model.Port;

public final class DebugSandboxLevel implements LevelDefinition {
    @Override public String name() { return "Feature/Debug Sandbox"; }

    @Override public void apply(World world) {
        // 1) HUD: infinite wire (or lots), no game over
        world.getHudState().setWireLengthRemaining(GameConfig.defaultConfig().maxWireLength * 50);
        world.setViewOnlyMode(false); // you can still play time

        // 2) Drop a few basic systems to wire quickly
        //    Adjust these to your Node constructors and sizes
        Node sourceA = new BasicNode("Source A", new Vector2D(120, 160), 120, 70);
        Node midA    = new BasicNode("Mid",      new Vector2D(360, 160), 120, 70);
        Node sinkA   = new BasicNode("Sink A",   new Vector2D(600, 160), 120, 70);

        world.addNode(sourceA);
        world.addNode(midA);
        world.addNode(sinkA);

        // 3) Specialized systems (placeholders—replace with your actual classes)
        Node spy        = new SpyNode("Spy",          new Vector2D(200, 320), 120, 70);
        Node saboteur   = new SaboteurNode("Saboteur",new Vector2D(360, 320), 120, 70);
        Node vpn        = new VPNNode("VPN",          new Vector2D(520, 320), 120, 70);
        Node antiTrojan = new AntiTrojanNode("AntiT",  new Vector2D(680, 320), 120, 70);

        Node distributor= new DistributorNode("Dist",  new Vector2D(260, 460), 120, 70);
        Node merger     = new MergerNode("Merger",     new Vector2D(520, 460), 120, 70);

        world.addNode(spy);
        world.addNode(saboteur);
        world.addNode(vpn);
        world.addNode(antiTrojan);
        world.addNode(distributor);
        world.addNode(merger);

        // 4) Optional: pre-wire a couple of obvious connections so you can press Space and see movement
        //    (Use your existing editor: right‑click bends still work)
        // world.addConnection(new Connection(sourceA.getOutput(PortType.SQUARE), midA.getInput(PortType.SQUARE)));
        // world.addConnection(new Connection(midA.getOutput(PortType.SQUARE),   sinkA.getInput(PortType.SQUARE)));

        // 5) Make sure time starts frozen; you control it with SPACE
        world.getTimeController().waitToStart();
    }
}
