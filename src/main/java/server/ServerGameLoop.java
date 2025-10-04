// server/ServerGameLoop.java
package server;

import shared.dto.WorldFrameDTO;
import shared.net.ClientCommand;
import server.sim.engine.GameLoop;
import server.sim.engine.world.World;
import server.sim.engine.world.WorldController;
import server.sim.engine.world.level.Level;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

final class ServerGameLoop {
    private final World world;
    private final WorldController ctrl;
    private final GameLoop loop;
    private final ConcurrentLinkedQueue<ClientCommand> inputs = new ConcurrentLinkedQueue<>();
    private final Consumer<WorldFrameDTO> onFrame;
    private volatile boolean running;

    ServerGameLoop(int level, Consumer<WorldFrameDTO> onFrame){
        this.onFrame = onFrame;
        this.world = new World(new Level(level));
        this.ctrl  = new WorldController(world);
        this.loop  = new GameLoop(ctrl, snap -> {
            // before sending, apply any queued inputs to world
            applyInputs();
            onFrame.accept(WorldAdapter.toDto(world));
        });
    }

    void start(){ running = true; new Thread(loop, "ServerLoop").start(); }
    void stop(){ running = false; loop.stop(); }

    void applyInput(String sessionId, ClientCommand cmd){
        inputs.add(cmd);
    }
    private void applyInputs(){
        ClientCommand c;
        while ((c = inputs.poll()) != null) {
            switch (c.kind) {
                case "MOUSE_DOWN" -> {}/* translate to your server-side controller action */
                case "MOUSE_DRAG" -> {}/* … */
                case "MOUSE_UP"   -> {}/* … */
                case "KEY"        -> {}/* … */
            }
        }
    }
}
