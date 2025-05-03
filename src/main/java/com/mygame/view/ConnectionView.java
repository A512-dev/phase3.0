package com.mygame.view;

import java.awt.Color;
import java.awt.Graphics2D;
import com.mygame.model.Connection;
import com.mygame.model.Port;

public class ConnectionView implements View<Connection> {
    @Override
    public void render(Graphics2D g, Connection connection) {
        Port a = connection.getFrom();
        Port b = connection.getTo();

        g.setColor(Color.LIGHT_GRAY);
        g.drawLine((int) a.getPosition().x, (int) a.getPosition().y,
                (int) b.getPosition().x, (int) b.getPosition().y);
    }
}
