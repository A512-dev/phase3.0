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
    private volatile boolean running;

    ServerGameLoop(int level, Consumer<WorldSnapshot> onFrame){
        this.onFrame = onFrame;
        this.world = new World(new Level(level));
        this.ctrl  = new WorldController(world);
        this.loop  = new GameLoop(ctrl, snap -> {
            // 1) Apply any queued inputs for THIS tick/frame on the simulation thread
            applyInputs();

            // 2) Take a fresh snapshot AFTER inputs are applied
            WorldSnapshot fresh = ctrl.snapshot();

            // 3) Send a DTO built from that fresh snapshot
            System.out.println("Tick -> building frame...");
            try {
                onFrame.accept(fresh);
            } catch (Exception ex) {
                ex.printStackTrace();                   // <— SEE THE REAL CAUSE IF ANY
            }
        });
    }

    void start(){
        running = true;
        loop.start();                       // <-- MISSING
        new Thread(loop, "ServerLoop").start();
    }
    void stop(){ running = false; loop.stop(); }

    void applyInput(String sessionId, ClientCommand cmd){
        inputs.add(cmd);
    }

    private void applyInputs(){
        ClientCommand c;
        while ((c = inputs.poll()) != null) {
            switch (c.kind) {
                case "MOUSE_DOWN" -> { /* TODO: translate to world/controller action */ }
                case "MOUSE_DRAG" -> { /* ... */ }
                case "MOUSE_UP"   -> { /* ... */ }
                case "KEY_DOWN"   -> { /* ... (split KEY/UP/DOWN if you like) */ }
                case "KEY_UP"     -> { /* ... */ }
                default -> { /* ignore unknown kinds safely */ }
            }
        }
    }
}
