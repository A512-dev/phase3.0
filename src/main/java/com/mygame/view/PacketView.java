package com.mygame.view;

import java.awt.*;

import com.mygame.model.Packet;
import com.mygame.model.SquarePacket;
import com.mygame.model.TrianglePacket;
import com.mygame.util.Vector2D;

public class PacketView implements View<Packet> {
    @Override
    public void render(Graphics2D g, Packet packet) {
        Vector2D pos = packet.getPosition();
        double size = packet.getSize();
        int s = (int) size;
        int x = (int) (pos.x - size / 2);
        int y = (int) (pos.y - size / 2);

        ////making the opacity related to life
        {
            float alpha = packet.getOpacity();
            Composite original = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }

        if (packet instanceof SquarePacket) {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, s, s);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, s, s);
        } else if (packet instanceof TrianglePacket) {
            g.setColor(Color.RED);
            int half = s / 2;
            int[] xPoints = {(int) pos.x, (int) (pos.x - half), (int) (pos.x + half)};
            int[] yPoints = {(int) (pos.y - half), (int) (pos.y + half), (int) (pos.y + half)};
            g.fillPolygon(xPoints, yPoints, 3);
            g.setColor(Color.BLACK);
            g.drawPolygon(xPoints, yPoints, 3);
        } else {
            g.setColor(Color.GRAY);
            g.fillOval(x, y, s, s);
        }
    }
}
