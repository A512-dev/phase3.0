package com.mygame.view;

import java.awt.*;

import com.mygame.model.Connection;
import com.mygame.model.Port;

public class ConnectionView implements View<Connection> {
    @Override
    public void render(Graphics2D g, Connection connection) {
        Port a = connection.getFrom();
        Port b = connection.getTo();
        g.setStroke(new BasicStroke(3));  // ✅ thicker wire (3 pixels wide)
        g.setColor(new Color(33, 59, 11));
        g.drawLine((int) a.getCenter().x, (int) a.getCenter().y,
                (int) b.getCenter().x, (int) b.getCenter().y);
        g.setStroke(new BasicStroke(1)); // Reset stroke for other drawing
    }
}
