// SettingsMenu.java
package com.mygame.ui;

import javax.swing.*;
import java.awt.*;

public class SettingsMenu extends JPanel {
    public SettingsMenu(Runnable onBack) {
        setLayout(new BorderLayout());
        JSlider volume = new JSlider(0, 100, 50);
        volume.setMajorTickSpacing(25);
        volume.setPaintTicks(true);
        volume.setPaintLabels(true);

        JButton back = new JButton("Back");
        back.addActionListener(e -> onBack.run());

        add(new JLabel("Volume"), BorderLayout.NORTH);
        add(volume, BorderLayout.CENTER);
        add(back, BorderLayout.SOUTH);
    }
}
