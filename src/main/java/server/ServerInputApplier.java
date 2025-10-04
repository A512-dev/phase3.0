// server/ServerInputApplier.java
package server;

import shared.net.ClientCommand;
import server.sim.engine.world.World;

final class ServerInputApplier {
    private final World world;
    ServerInputApplier(World world){ this.world = world; }

    void apply(ClientCommand cmd) {
        switch (cmd.kind) {
            case "MOUSE_DOWN" -> onMouseDown(cmd.x, cmd.y);
            case "MOUSE_DRAG" -> onMouseDrag(cmd.x, cmd.y);
            case "MOUSE_UP"   -> onMouseUp(cmd.x, cmd.y);
            case "KEY"        -> onKey(cmd.keyCode);
        }
    }

    private void onMouseDown(double x, double y){
        // Example: begin a DraftConnection on the server
        // world.beginDraftAt(new Vector2D(x, y));
    }
    private void onMouseDrag(double x, double y){
        // world.updateDraft(new Vector2D(x, y));
    }
    private void onMouseUp(double x, double y){
        // world.commitDraft(new Vector2D(x, y));
    }
    private void onKey(int keyCode){
        // e.g. pause/start in TimeController
        // world.getTimeController().togglePause();
    }
}
