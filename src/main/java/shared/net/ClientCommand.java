// shared/net/ClientCommand.java
package shared.net;

public class ClientCommand {
    public String kind;     // "MOUSE_DOWN" | "MOUSE_MOVE" | "MOUSE_DRAG" | "MOUSE_UP" | "KEY_*"
    public double x, y;
    public int keyCode;

    // NEW:
    public int button;      // 1=LEFT, 2=MIDDLE, 3=RIGHT (Swing’s MouseEvent.getButton())
    public boolean shift, ctrl, alt;

    private ClientCommand(){}


    public static ClientCommand mouse(String kind, int x, int y, int button, boolean shift, boolean ctrl, boolean alt) {
        ClientCommand c = new ClientCommand();
        c.kind = kind; c.x = x; c.y = y; c.button = button;
        c.shift = shift; c.ctrl = ctrl; c.alt = alt;
        return c;
    }


    public static ClientCommand key(String kind, int keyCode) {
        ClientCommand c = new ClientCommand();
        c.kind = kind; c.keyCode = keyCode;
        return c;
    }
}
