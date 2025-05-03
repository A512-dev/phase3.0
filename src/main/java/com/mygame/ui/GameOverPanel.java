package com.mygame.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class GameOverPanel extends JPanel {
    public GameOverPanel(int total, int lost, Runnable onRestart) {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Game Over", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        String stats = String.format("Lost: %d / %d  (%.1f%%)", lost, total,
                (lost * 100.0) / Math.max(1, total));
        JLabel info = new JLabel(stats, SwingConstants.CENTER);

        JButton restart = new JButton("Restart");
        restart.addActionListener(e -> onRestart.run());

        JPanel center = new JPanel(new GridLayout(2, 1));
        center.add(title);
        center.add(info);

        add(center, BorderLayout.CENTER);
        add(restart, BorderLayout.SOUTH);
    }
}
