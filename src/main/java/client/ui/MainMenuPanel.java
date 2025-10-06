// client/ui/MainMenuPanel.java
package client.ui;

import client.ui.helper.GameUi;

import javax.swing.*;
import java.awt.*;

/**
 * Main menu that fits the window size from GameConfig via GameUi.
 * Preferred ctor: (onStartOffline, onStartOnline, onSettings, onExit)
 * Legacy ctor kept: (onPlay, onSettings) – routes Play -> Start Offline.
 */
public final class MainMenuPanel extends JPanel {

    // New/Preferred constructor
    public MainMenuPanel(Runnable onStartOffline, Runnable onStartOnline, Runnable onSettings, Runnable onExit) {
        setLayout(new GridBagLayout());
        GameUi.applyGameSize(this);                 // <- size from GameConfig

        JButton btnStartOffline = new JButton("Start (Offline)");
        JButton btnStartOnline  = new JButton("Start (Online)");
        JButton btnSettings     = new JButton("Settings");
        JButton btnExit         = new JButton("Exit");

        btnStartOffline.addActionListener(e -> onStartOffline.run());
        btnStartOnline.addActionListener(e -> onStartOnline.run());
        btnSettings.addActionListener(e -> onSettings.run());
        btnExit.addActionListener(e -> onExit.run());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10,10,10,10);
        g.gridy = 0; add(btnStartOffline, g);
        g.gridy = 1; add(btnStartOnline,  g);
        g.gridy = 2; add(btnSettings,     g);
        g.gridy = 3; add(btnExit,         g);
    }

    // Legacy/compat constructor: Play -> Start (Offline)
    public MainMenuPanel(Runnable onPlay, Runnable onSettings) {
        this(onPlay, /* onStartOnline */ () -> {}, onSettings, /* onExit */ () -> {});
    }
}
