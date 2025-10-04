// com/mygame/shared/net/Envelope.java
package shared.net;
public final class Envelope {
    public MessageType type;
    public String      payload; // JSON of a DTO or command
    public Envelope() {}
    public Envelope(MessageType t, String p){ this.type=t; this.payload=p; }
    @Override public String toString() {
        return "Envelope{type=" + type + ", payload=" + payload + "}";
    }

}
