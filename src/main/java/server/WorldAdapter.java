// server/WorldAdapter.java
package server;

import shared.dto.WorldFrameDTO;
import server.sim.engine.world.World;

public final class WorldAdapter {
    private static long seq = 0;

    // Keep the simple one:
    public static WorldFrameDTO toDto(World w){
        return toDto(w, ++seq);
    }

    // And the detailed one if you want to set frame explicitly:
    public static WorldFrameDTO toDto(World w, long frameNo){
        WorldFrameDTO dto = new WorldFrameDTO();
        dto.frame    = frameNo;
        dto.gameTime = w.getHudState().getGameTime();
        // TODO: map nodes / wires / packets as needed
        return dto;
    }
}
