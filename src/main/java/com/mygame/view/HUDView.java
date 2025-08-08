package com.mygame.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.mygame.model.node.Node;
import com.mygame.snapshot.HudReadOnly;

public class HUDView implements View<HudReadOnly> {
    @Override
    public void render(Graphics2D g, HudReadOnly hud) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.RED);
        g.drawString("Coins: " + hud.coins(), 10, 20);
        g.drawString("Packet Loss: " + ((double)hud.lostPackets()) + "%", 10, 40);
        g.drawString("Packet Reached: " + ((double)hud.successfulPackets()) + "%", 10, 60);
        g.drawString("Time: " + String.format("%.1fs", hud.gameTimeSec()), 10, 80);
        g.drawString("Wire remaining: " + String.format("%.1fs", hud.wireRemaining()), 10, 100);


    }

}
