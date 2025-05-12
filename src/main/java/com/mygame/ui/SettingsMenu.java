// SettingsMenu.java
package com.mygame.ui;

import com.mygame.audio.AudioManager;

import javax.swing.*;
import java.awt.*;

public class SettingsMenu extends JPanel {
    public SettingsMenu(Runnable onBack) {
        setLayout(new BorderLayout());
        JSlider volume = new JSlider(0, 100, 50);
        volume.setMajorTickSpacing(25);
        volume.setPaintTicks(true);
        volume.setPaintLabels(true);
        volume.addChangeListener(e -> AudioManager.get().setVolume(volume.getValue()));
        this.add(new JLabel("Master volume"));
        JButton back = new JButton("Back");
        back.addActionListener(e -> onBack.run());

        add(new JLabel("Volume"), BorderLayout.NORTH);
        add(volume, BorderLayout.CENTER);
        add(back, BorderLayout.SOUTH);
    }
}
