package shared.net;


/** Command sent from client to server. */
public final class ClientCommand {
    public String clientId; // unique client identifier
    public String kind; // e.g., "MOUSE_DOWN", "MOUSE_DRAG", "KEY"
    public double x, y; // optional (for mouse)
    public int keyCode; // optional (for keyboard)


    public ClientCommand() {}


    public static ClientCommand mouse(String kind, double x, double y) {
        ClientCommand c = new ClientCommand();
        c.kind = kind; c.x = x; c.y = y; return c;
    }


    public static ClientCommand key(String kind, int keyCode) {
        ClientCommand c = new ClientCommand();
        c.kind = kind; c.keyCode = keyCode; return c;
    }
}