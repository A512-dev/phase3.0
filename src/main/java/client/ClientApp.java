// client/ClientApp.java
package client;

import client.net.NetClient;
import client.ui.ConnectPanel;
import client.ui.GamePanel;
import client.ui.LevelSelectPanel;
import client.ui.MainMenu;
import server.sim.core.save.SaveManager;
import server.sim.engine.world.level.Level;


import javax.swing.*;
import java.awt.*;

public final class ClientApp {

    private final JFrame frame = new JFrame("MyGame");

    // null ⇒ offline mode
    private NetClient net;

    // The single game panel we use for both modes
    private GamePanel gamePanel;

    // Offline-only save/autosave
    private SaveManager save;

    // Track current level
    private String levelId;
    private int    levelInt;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientApp().start());
    }

    public void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        showConnect();          // first screen: connect or play offline
        frame.setVisible(true);
    }

    /* ---------------- screens ---------------- */

    /** First screen: enter IP/port and connect OR skip to menu offline */
    private void showConnect() {
        frame.setContentPane(new ConnectPanel(
                // onConnected:
                c -> {
                    this.net = c;          // remember we're online
                    showPhase2Menu();
                },
                // onPlayOffline from connect screen:
                this::showPhase2Menu
        ));
        packCenter();
    }

    /** The Phase-2 MainMenu (Play / Settings). */
    private void showPhase2Menu() {
        frame.setContentPane(new MainMenu(
                // onPlay
                this::showLevelPicker,
                // onSettings
                this::showSettings
        ));
        packCenter();
    }

    private void showSettings() {
        JOptionPane.showMessageDialog(frame, "Settings screen here…");
    }

    /** Phase-2 LevelSelectPanel; branches to online/offline when a level is picked. */
    private void showLevelPicker() {
        frame.setContentPane(new LevelSelectPanel(
                // Back → return to main menu
                this::showPhase2Menu,
                // onPickLevel(int level)
                this::startGameForCurrentMode
        ));
        packCenter();
    }

    /** Decide online/offline at runtime: if net != null → online, else offline. */
    private void startGameForCurrentMode(int chosenLevel) {
        if (net != null) startOnline(chosenLevel);
        else             startOffline(chosenLevel);
    }

    /* ---------------- OFFLINE (Phase-2 unchanged) ---------------- */

    private void startOffline(int chosenLevel) {
        // build level
        this.levelInt = chosenLevel;
        Level level   = new Level(chosenLevel);
        this.levelId  = level.id();

        // fresh SaveManager for this run
        save = new SaveManager(level.id());

        // Phase-2 GamePanel (it already accepts SaveManager)
        gamePanel = new GamePanel(
                this::restartOffline,   // restart callback
                this::exitToMenu,       // exit to menu
                level,
                save
        );

        // start autosave cadence
        save.start();

        frame.setContentPane(gamePanel);
        packCenter();
    }

    private void restartOffline() {
        if (gamePanel != null) gamePanel.stop();
        if (save != null) {
            try { save.close(); } catch (Exception ignore) {}
        }
        startOffline(levelInt);
    }

    private void exitToMenu() {
        if (gamePanel != null) gamePanel.stop();
        if (save != null) {
            try { save.clearAutosave(); save.close(); } catch (Exception ignore) {}
        }
        gamePanel = null;
        save = null;
        showPhase2Menu();
    }

    /* ---------------- ONLINE (thin client using the same GamePanel) ---------------- */

    private void startOnline(int chosenLevel) {
        // Remember level; create the same Level object for the client-side view
        this.levelInt = chosenLevel;
        Level level   = new Level(chosenLevel);
        this.levelId  = level.id();

        // Tell server which level we want (implement in NetClient)
        // e.g. send JOIN { "level": chosenLevel }
        try {
            net.sendJoinWithLevel(chosenLevel);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Failed to start online game (JOIN).\nStarting offline instead.",
                    "Network", JOptionPane.WARNING_MESSAGE);
            net = null;
            startOffline(chosenLevel);
            return;
        }

        // Start GamePanel without a SaveManager (GamePanel must already null-check it)
        gamePanel = new GamePanel(
                // For now reuse offline restart→ just bounce back to menu
                this::showPhase2Menu,
                this::showPhase2Menu,
                level,
                /* saveManager = */ null
        );

        // (Optional): if you want, subscribe GamePanel to frames from server here:
        // net.onMessage((type, json) -> { if (type == MessageType.FRAME_UPDATE) {
        //       WorldFrameDTO dto = Json.from(json, WorldFrameDTO.class);
        //       SwingUtilities.invokeLater(() -> gamePanel.applyServerFrame(dto));
        // }});

        frame.setContentPane(gamePanel);
        packCenter();
    }

    /* ---------------- helpers ---------------- */

    private void packCenter() {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }
}
