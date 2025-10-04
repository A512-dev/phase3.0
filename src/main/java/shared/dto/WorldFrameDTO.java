// shared/dto/WorldFrameDTO.java
package shared.dto;

public final class WorldFrameDTO {
    public long   frame;       // monotonically increasing
    public double gameTime;    // seconds
    public double  simTime;     // simulation time from HUD
    public boolean gameOver;    // whether the game has ended


    // add more later: packets, systems, wires...
    public WorldFrameDTO() {}
    public WorldFrameDTO(long frame, double gameTime){
        this.frame = frame; this.gameTime = gameTime;
    }
}
