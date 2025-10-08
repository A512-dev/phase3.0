// server/ServerGameLoop.java
package server;

import shared.net.ClientCommand;
import server.sim.engine.GameLoop;
import server.sim.engine.world.World;
import server.sim.engine.world.WorldController;
import server.sim.engine.world.level.Level;
import shared.snapshot.WorldSnapshot;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

final class ServerGameLoop {
    private final World world;
    private final WorldController ctrl;
    private final GameLoop loop;
    private final ConcurrentLinkedQueue<ClientCommand> inputs = new ConcurrentLinkedQueue<>();
    private final Consumer<WorldSnapshot> onFrame;

    // if you use this elsewhere you can keep it; otherwise not required
    @SuppressWarnings("FieldCanBeLocal")
    private volatile boolean running;

    // 🔌 hook up your input applier if you added it
    private final ServerInputApplier applier;

    ServerGameLoop(int level, Consumer<WorldSnapshot> onFrame){
        this.onFrame = onFrame;
        this.world = new World(new Level(level));
        this.ctrl  = new WorldController(world);
        this.applier = new ServerInputApplier(world);

        this.loop  = new GameLoop(ctrl, snap -> {
            applyInputs();                          // run on sim thread
            WorldSnapshot fresh = ctrl.snapshot();  // snapshot after inputs
            //System.out.println("[SERVER] Tick -> building frame... hud.t=" + fresh.hud().gameTimeSec());
            onFrame.accept(fresh);
        });
    }

    void start() {
        running = true;
        loop.start();
        new Thread(loop, "ServerLoop").start();
    }

    // ✅ add THIS method so NetServer can call old.stop()
    void stop() {
        running = false;
        loop.stop();            // your GameLoop.stop() should signal its run() to end
    }

    void applyInput(String sessionId, ClientCommand cmd){
        inputs.add(cmd);
    }

    private void applyInputs(){
        ClientCommand c;
        while ((c = inputs.poll()) != null) {
            applier.apply(c);              // ← use the applier
        }
    }
}
