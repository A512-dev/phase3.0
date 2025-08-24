package com.mygame.ui;

import com.mygame.core.GameState;


import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {
    public GameOverPanel(int total, int lost, Runnable onRestart) {
        setLayout(new BorderLayout());


        JLabel title = new JLabel("Game Finished", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        String stats = String.format("Lost: %d / %d  (%.1f%%)", lost, total,
                (lost * 100.0) / Math.max(1, total));
        JLabel info = new JLabel(stats, SwingConstants.CENTER);
        JLabel declaration;
        JButton nextLevelBtn = null;
        if ((lost * 100.0) / Math.max(1, total)<49.999) {
            declaration = new JLabel("YOU WON", SwingConstants.CENTER);

            if (!GameState.currentLevelID.equals("level5")) {
                nextLevelBtn = new JButton("Proceed to Level"+(GameState.currentLevelInt+1));
                nextLevelBtn.addActionListener(e -> {
                    GameState.currentLevelID = "level"+(GameState.currentLevelInt+1);
                    GameState.currentLevelInt = GameState.currentLevelInt+1;
                    onRestart.run();
                });
            }
            else {
                declaration = new JLabel("YOU WON THE GAME", SwingConstants.CENTER);
            }
        }
        else {
            declaration = new JLabel("YOU LOSE!!!", SwingConstants.CENTER);
        }


        JButton restart = new JButton("Restart");
        restart.setForeground(Color.orange);
        restart.addActionListener(e -> onRestart.run());

        JPanel center = new JPanel(new GridLayout(3, 1));
        center.add(title);
        center.add(declaration);
        center.add(info);
        if (null != nextLevelBtn)
            add(nextLevelBtn, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(restart, BorderLayout.SOUTH);
        this.setBackground(Color.cyan);
    }
}
