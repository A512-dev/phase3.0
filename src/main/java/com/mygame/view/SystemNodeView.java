package com.mygame.view;

import java.awt.*;
import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Port;
import com.mygame.model.node.BasicNode;
import com.mygame.model.node.Node;
import com.mygame.snapshot.NodeSnapshot;
import com.mygame.snapshot.PortSnapshot;


public class SystemNodeView implements View<NodeSnapshot> {
    private final int portSize = GameConfig.defaultConfig().portSize;
    private static final Color NODE_FILL = new Color(18,171,224);

    @Override public void render(Graphics2D g, NodeSnapshot node) {
        Vector2D pos = node.position();

        int x = (int) pos.x();
        int y = (int) pos.y();
        int w =  node.width();
        int h =  node.height();

        g.setColor(NODE_FILL);
        g.fillRect(x, y, w, h);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, w, h);

        // connection status led
        //g.setColor(node.isAllConnected() ? Color.GREEN : Color.RED);
        g.fillRect(x + w - 10, y + 6, 20, 6);


        node.ports().forEach(p -> drawPort(g, p));
    }

    private void drawPort(Graphics2D g, PortSnapshot port) {
        Vector2D pos = port.position();
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
