package client.view;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

import server.sim.core.GameConfig;
import server.sim.engine.physics.Vector2D;
import server.sim.model.node.Node;
import server.sim.snapshot.NodeSnapshot;
import server.sim.snapshot.PortSnapshot;
import client.view.assets.Draw;
import client.view.assets.ImageCache;


public class SystemNodeView implements View<NodeSnapshot> {
    private static final Color LED_OK = new Color(60, 220, 120);
    private static final Color LED_BAD= new Color(220, 80, 80);

    private final ImageCache cache = ImageCache.get();
    private final Map<Node.Type, Image> nodeImg = new EnumMap<>(Node.Type.class);
    private final Image portSquare = null;
    private final Image portTri    = null;
    private final Image portInf = cache.load("/ports/infinity.png");
    private final int portSize = GameConfig.defaultConfig().portSize;
    private static final Color NODE_FILL = new Color(18,171,224);


    public SystemNodeView() {
        // map your concrete node types to images (add as many as you have)
        nodeImg.put(Node.Type.BASIC,      null);
        nodeImg.put(Node.Type.SPY,         cache.load("/nodes/SpyNodePic.jpg"));
        nodeImg.put(Node.Type.SABOTEUR,    cache.load("/nodes/SaboteurNodePic.jpg"));
        nodeImg.put(Node.Type.VPN,         cache.load("/nodes/VPNNodePic.jpg"));
        nodeImg.put(Node.Type.ANTITROJAN,  cache.load("/nodes/AntiTrojanNodePic.jpg"));
        nodeImg.put(Node.Type.DISTRIBUTOR, cache.load("/nodes/DistributorNodePic.jpg"));
        nodeImg.put(Node.Type.MERGER,      cache.load("/nodes/MergerNodePic.jpg"));
    }

    @Override public void render(Graphics2D g, NodeSnapshot node) {
        Vector2D pos = node.position();

        int x = (int) pos.x();
        int y = (int) pos.y();
        int w =  node.width();
        int h =  node.height();

        Image img = nodeImg.get(node.type()); // requires NodeSnapshot.type()
        if (img != null) {
            //System.out.println(node.type());;
            // center the sprite within node bounds (assuming art roughly matches box)
            Draw.imageCentered(g, img, x + w / 2.0, y + h / 2.0, w, h, 1f, 0);
        } else {
            // fallback: your previous rectangle
            g.setColor(new Color(18,171,224));
            g.fillRect(x, y, w, h);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, w, h);
        }


        // connection status LED (example: allConnected comes from your snapshot, or compute)
        g.setColor(node.isAllConnected() ? LED_OK : LED_BAD);
        g.fillRect(x + w - 18, y + 6, 12, 6);

//        g.setColor(NODE_FILL);
//        g.fillRect(x, y, w, h);
//        g.setColor(Color.WHITE);
//        g.drawRect(x, y, w, h);

        // connection status led
        //g.setColor(node.isAllConnected() ? Color.GREEN : Color.RED);
//        g.fillRect(x + w - 10, y + 6, 20, 6);


        node.ports().forEach(p -> drawPort(g, p));
    }

    private void drawPort(Graphics2D g, PortSnapshot port) {
        Vector2D pos = port.position();
        Image pi = switch (port.type()) {
            case SQUARE -> portSquare;
            case TRIANGLE -> portTri;
            case INFINITY -> portInf;
        };
        if (pi != null) {
            Draw.imageCentered(g, pi, pos.x() + portSize / 2.0, pos.y() + portSize / 2.0
                    , portSize, portSize, 1f, 0);
        }
        else {
            int px = (int) pos.x();
            int py = (int) pos.y();
            switch (port.type()) {
                case SQUARE -> {
                    g.setColor(Color.CYAN);
                    g.fillRect(px, py, portSize, portSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(px, py, portSize, portSize);
                }
                case TRIANGLE -> {
                    g.setColor(Color.MAGENTA);
                    int[] xs = {px + portSize/2, px, px + portSize};
                    int[] ys = {py, py + portSize, py + portSize};
                    g.fillPolygon(xs, ys, 3);
                    g.setColor(Color.BLACK);
                    g.drawPolygon(xs, ys, 3);
                }
            }
        }

    }

}
