// com.mygame.ui.ShopPanel
package com.mygame.ui;

import com.mygame.model.World;
import com.mygame.model.HUDState;
import com.mygame.model.powerups.PowerUpType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ShopPanel extends JPanel {

    private final World     world;
    private final HUDState  hud;
    private final JPanel    card;
    private final Runnable onClose;   // ⚡ new
    public ShopPanel(World world, HUDState hud,Runnable onClose) {
        this.world = world;
        this.hud   = hud;
        this.onClose = onClose;

        setOpaque(false);           // let us paint translucent bg
        setLayout(null);            // manual layout

        /* -------- inner white card -------- */
        card = new JPanel(new GridLayout(0,1, 8,8));
        card.setBackground(new Color(255,255,255,240));
        card.setBorder(BorderFactory.createEmptyBorder(16,24,16,24));

        for (PowerUpType t : PowerUpType.values()) {
            JButton b = new JButton(
                    t.name().replace('_',' ') + "  (" + t.cost + "🪙)");
            b.addActionListener(e -> {
                if (!world.tryActivate(t, hud)) {
                    JOptionPane.showMessageDialog(
                            this, "Not enough coins!",
                            "Shop", JOptionPane.WARNING_MESSAGE);
                }
                refreshButtons();
            });
            card.add(b);
        }
        add(card);

        /* ----- click outside card closes shop ----- */
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (!card.getBounds().contains(e.getPoint())) close();
            }
        });

        setVisible(false);
    }

    /* -------- public API -------- */
    public void open() {
        refreshButtons();
        setVisible(true);
        requestFocusInWindow();      // eat keys if needed
    }
    public void close() {
        setVisible(false);
        if (onClose != null) onClose.run();   // 🔔 resume game
    }


    /* -------- helpers -------- */
    private void refreshButtons() {
        int coins = hud.getCoins();
        Component[] comps = card.getComponents();
        for (int i=0;i<PowerUpType.values().length;i++) {
            comps[i].setEnabled(coins >= PowerUpType.values()[i].cost);
        }
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0,0,0,150));      // dim bg
        g.fillRect(0,0,getWidth(),getHeight());
    }
    @Override public void doLayout() {
        int w = 260, h = 190;
        card.setBounds(getWidth()/2 - w/2,
                getHeight()/2 - h/2, w, h);
    }
}
