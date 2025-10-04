// shared/net/ServerEvent.java
package shared.net;

public final class ServerEvent {
    public final MessageType kind;
    public final String payload; // JSON to decode by kind
    public ServerEvent(MessageType kind, String payload){ this.kind=kind; this.payload=payload; }
}
