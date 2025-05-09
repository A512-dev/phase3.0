package com.mygame.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import com.mygame.model.HUDState;

public class HUDView implements View<HUDState> {
    @Override
    public void render(Graphics2D g, HUDState hud) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.RED);
        g.drawString("Coins: " + hud.getCoins(), 10, 20);
        g.drawString("Packet Loss: " + hud.getLostPackets() + "%", 10, 40);
        g.drawString("Time: " + String.format("%.1fs", hud.getGameTime()), 10, 60);
    }
}
