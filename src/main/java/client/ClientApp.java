// client/ClientApp.java
package client;

import client.net.NetClient;
import client.ui.*;
import client.ui.helper.GameUi;

import server.sim.core.save.SaveManager;   // transitional
import server.sim.engine.world.level.Level; // transitional

import shared.net.MessageType;
import shared.ser.Json;

import javax.swing.*;

public final class ClientApp {
    private final JFrame frame = new JFrame("MyGame");

    private NetClient net;                 // null if offline
    private int levelInt = 1;

    // Phase-2 offline panel
    private client.ui.GamePanel offlinePanel; // ← use your Phase-2 GamePanel class
    private SaveManager save;                 // transitional

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientApp().start());
    }

    private void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        showMainMenu();
        frame.setVisible(true);
    }

    /* ──────────────── FLOW ──────────────── */

    private void showMainMenu() {
        var menu = new MainMenuPanel(
                /* onStartOffline */ this::showLevelSelectOffline,
                /* onStartOnline  */ this::showConnect,
                /* onSettings     */ () -> { /* open your settings panel */ },
                /* onExit         */ frame::dispose
        );
        GameUi.showSized(frame, menu);
    }


    private void showConnect() {
        var connect = new client.ui.ConnectPanel(
                /* onConnected */ (NetClient c) -> { this.net = c; showLevelSelectOnline(); },
                /* onOffline   */ this::showLevelSelectOffline
        );
        // use full game window size for visual consistency
        GameUi.showSized(frame, connect);
    }

    private void showLevelSelectOffline() {
        var lvl = new LevelSelectPanel(
                (int chosen) -> { levelInt = chosen; startOffline(chosen); },
                this::showMainMenu
        );
        GameUi.showSized(frame, lvl);
    }

    private void showLevelSelectOnline() {
        var lvl = new LevelSelectPanel(
                (int chosen) -> { levelInt = chosen; startOnline(chosen); },
                () -> { this.net = null; showMainMenu(); }
        );
        GameUi.showSized(frame, lvl);
    }

    /* ───────────── OFFLINE (Phase-2 GamePanel) ───────────── */

    private void startOffline(int chosenLevel) {
        // Phase-2 uses its own GamePanel that internally starts loop & renders snapshots.
        Level level = new Level(chosenLevel);
        save = new SaveManager(level.id()); // used by your panel if needed

        offlinePanel = new client.ui.GamePanel(
                this::restartOffline,
                this::exitToMenu,
                level,
                save
        );
        GameUi.showSized(frame, offlinePanel);
    }

    private void restartOffline() {
        if (offlinePanel != null) offlinePanel.stop();
        if (save != null) { try { save.close(); } catch (Exception ignore) {} }
        startOffline(levelInt);
    }

    private void exitToMenu() {
        if (offlinePanel != null) offlinePanel.stop();
        if (save != null) { try { save.clearAutosave(); save.close(); } catch (Exception ignore) {} }
        offlinePanel = null; save = null;
        showMainMenu();
    }

    /* ───────────── ONLINE ───────────── */

    private void startOnline(int chosenLevel) {
        var onlinePanel = new client.ui.gamePanels.OnlineGamePanel(net, this::showMainMenu);
        GameUi.showSized(frame, onlinePanel);

        net.send(
                shared.net.MessageType.START_GAME,
                shared.ser.Json.to(new Object(){ public final int level = chosenLevel; })
        );


        // Tell server we joined this level (ensures it starts streaming frames)
        //net.send(MessageType.JOIN, Json.to(new Object() { public final int level = chosenLevel; }));
    }
}
