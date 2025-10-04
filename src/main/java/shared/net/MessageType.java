// shared/net/MessageType.java
package shared.net;
public enum MessageType {
    JOIN, JOIN_OK,
    START_GAME,         // ← add this
    INPUT_COMMAND,
    FRAME_UPDATE,
    SCOREBOARD,
    PING, PONG,
    DISCONNECT
}
