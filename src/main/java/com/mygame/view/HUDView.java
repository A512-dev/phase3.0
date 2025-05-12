package com.mygame.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import com.mygame.model.HUDState;
import com.mygame.util.Database;

public class HUDView implements View<HUDState> {
    @Override
    public void render(Graphics2D g, HUDState hud) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.RED);
        g.drawString("Coins: " + hud.getCoins(), 10, 20);
        g.drawString("Packet Loss: " + ((double)hud.getLostPackets()) + "%", 10, 40);
        g.drawString("Packet Reached: " + ((double)hud.getSuccessful()) + "%", 10, 60);
        g.drawString("Time: " + String.format("%.1fs", hud.getGameTime()), 10, 80);
        g.drawString("Wire remaining: " + String.format("%.1fs", hud.getWireLengthRemaining()), 10, 100);


    }
}
