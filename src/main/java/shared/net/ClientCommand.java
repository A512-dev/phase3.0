
package shared.net;
public final class ClientCommand {
    public String clientId;
    public String kind;     // e.g., "MOUSE_DOWN","MOUSE_DRAG","KEY"
    public double x, y;     // optional
    public int keyCode;     // optional
}
