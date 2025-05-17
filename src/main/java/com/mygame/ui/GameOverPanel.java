package com.mygame.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class GameOverPanel extends JPanel {
    public GameOverPanel(int total, int lost, Runnable onRestart) {
        setLayout(new BorderLayout());
        this.setBackground(new Color(222, 4, 4));

        JLabel title = new JLabel("Game Finished", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        String stats = String.format("Lost: %d / %d  (%.1f%%)", lost, total,
                (lost * 100.0) / Math.max(1, total));
        JLabel info = new JLabel(stats, SwingConstants.CENTER);
        JLabel declaration;
        if ((lost * 100.0) / Math.max(1, total)>49.999) {
            JLabel win = new JLabel("YOU WON", SwingConstants.CENTER);
            declaration = win;
        }
        else {
            JLabel lose = new JLabel("YOU LOSE!!!", SwingConstants.CENTER);
            declaration = lose;
        }


        JButton restart = new JButton("Restart");
        restart.setForeground(Color.orange);
        restart.addActionListener(e -> onRestart.run());

        JPanel center = new JPanel(new GridLayout(3, 1));
        center.add(title);
        center.add(declaration);
        center.add(info);

        add(center, BorderLayout.CENTER);
        add(restart, BorderLayout.SOUTH);
        this.setBackground(Color.CYAN);
    }
}
