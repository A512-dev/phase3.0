package com.mygame.view;

import java.awt.*;
import com.mygame.model.SystemNode;
import com.mygame.model.Port;
import com.mygame.util.Database;
import com.mygame.util.Vector2D;

public class SystemNodeView implements View<SystemNode> {
    @Override
    public void render(Graphics2D g, SystemNode node) {
        int x = (int) node.getX();
        int y = (int) node.getY();
        Vector2D pos = node.getPosition();
        int nodeWidth = (int) node.getWidth();
        int nodeHeight = (int) node.getHeight();

        // Draw main node box
        g.setColor(new Color(18, 171, 224));
        g.fillRect(x, y, nodeWidth, nodeHeight);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, nodeWidth, nodeHeight);
//        // Node background: Light Blue
//        g.setColor(new Color(18, 171, 224)); // Light blue
//        g.fillRect((int) pos.x - nodeWidth/2, (int) pos.y - nodeHeight/2, nodeWidth, nodeHeight);

        // Connection status indicator
        boolean allConnected = node.isAllConnected();

        // Green if all connected, else Red
        g.setColor(allConnected ? Color.GREEN : Color.RED);
        g.fillRect((int) pos.x + nodeWidth/2 - 10, (int) pos.y + 20, 20, 6);


        // Port size
        int portSize = Database.PORT_SIZE;

        // Draw input ports on the left
        int inputCount = node.getInputs().size();
        for (int i = 0; i < inputCount; i++) {
            Port port = node.getInputs().get(i);
            int px = (int) port.getPosition().x;
            int py = (int) port.getPosition().y;
            drawPort(g, px, py, port.getType(), portSize);
        }

        // Draw output ports on the right
        int outputCount = node.getOutputs().size();
        for (int i = 0; i < outputCount; i++) {
            Port port = node.getOutputs().get(i);
            int px = (int) port.getPosition().x;
            int py = (int) port.getPosition().y;
            drawPort(g, px, py, port.getType(), portSize);
        }
    }

    private void drawPort(Graphics2D g, int px, int py, Port.PortType type, int size) {
        if (type == Port.PortType.SQUARE) {
            g.setColor(Color.CYAN);
            g.fillRect(px, py, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(px, py, size, size);
        } else if (type == Port.PortType.TRIANGLE) {
            g.setColor(Color.MAGENTA);
            int[] xPoints = {px + size / 2, px, px + size};
            int[] yPoints = {py, py + size, py + size};
            g.fillPolygon(xPoints, yPoints, 3);
            g.setColor(Color.BLACK);
            g.drawPolygon(xPoints, yPoints, 3);
        }
    }
}
