// MainMenu.java
package com.mygame.ui;

import com.mygame.core.GameConfig;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JPanel {
    public MainMenu(Runnable onPlay, Runnable onSettings) {
        this.setBackground(Color.green);
        setLayout(new GridBagLayout());
        JButton playBtn = new JButton("Play");
        playBtn.addActionListener(e -> onPlay.run());

        JButton settingsBtn = new JButton("Settings");
        settingsBtn.addActionListener(e -> onSettings.run());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);

        gbc.gridy = 0; add(playBtn, gbc);
        gbc.gridy = 1; add(settingsBtn, gbc);


        setPreferredSize(GameConfig.level1Size);
        setMinimumSize(GameConfig.level1Size);

    }
}
