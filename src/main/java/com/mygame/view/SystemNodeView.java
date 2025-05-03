package com.mygame.view;

import java.awt.*;
import com.mygame.model.SystemNode;
import com.mygame.model.Port;
import com.mygame.util.Vector2D;

public class SystemNodeView implements View<SystemNode> {
    @Override
    public void render(Graphics2D g, SystemNode node) {
        int x = (int) node.getX();
        int y = (int) node.getY();
        int w = (int) node.getWidth();
        int h = (int) node.getHeight();

        // Draw main node box
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, w, h);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, w, h);

        // Port size
        int portSize = 10;

        // Draw input ports on the left
        int inputCount = node.getInputs().size();
        for (int i = 0; i < inputCount; i++) {
            Port port = node.getInputs().get(i);
            int py = y + 15 + i * 20;
            drawPort(g, x - portSize, py, port.getType(), portSize);
        }

        // Draw output ports on the right
        int outputCount = node.getOutputs().size();
        for (int i = 0; i < outputCount; i++) {
            Port port = node.getOutputs().get(i);
            int py = y + 15 + i * 20;
            drawPort(g, x + w, py, port.getType(), portSize);
        }
    }

    private void drawPort(Graphics2D g, int px, int py, Port.Type type, int size) {
        if (type == Port.Type.SQUARE) {
            g.setColor(Color.CYAN);
            g.fillRect(px, py, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(px, py, size, size);
        } else if (type == Port.Type.TRIANGLE) {
            g.setColor(Color.MAGENTA);
            int[] xPoints = {px + size / 2, px, px + size};
            int[] yPoints = {py, py + size, py + size};
            g.fillPolygon(xPoints, yPoints, 3);
            g.setColor(Color.BLACK);
            g.drawPolygon(xPoints, yPoints, 3);
        }
    }
}
