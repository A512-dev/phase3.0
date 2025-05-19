package com.mygame;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.mygame.audio.AudioManager;
import com.mygame.model.*;
import com.mygame.ui.*;
import com.mygame.util.ConnectionRecord;

import java.util.List;

public class Main {
    private final JFrame frame;
    private final MainMenu mainMenu;
    private GamePanel gamePanel;

    public Main() {
        frame = new JFrame("Blueprint Hell – Phase 1");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Show main menu on startup
        mainMenu = new MainMenu(this::startGame, this::openSettings);
        frame.setContentPane(mainMenu);
        frame.setVisible(true);
        /* 🔊 start BG music on first play */
        AudioManager.get().loopMusic("pacman background music");
    }

    /** Called when the user clicks "Play" */
    private void startGame() {
        if (GameState.isLevel1Passed())
            GameState.currentLevel=2;
        // Swap in the game panel
        gamePanel = new GamePanel(this::restartLevel);

        if (GameState.currentLevel == 2 && GameState.isLevel2Passed()) {
            gamePanel.getWorld().createTestLevel2();
            restoreConnections(2);
        } else if (GameState.currentLevel==1 && GameState.isLevel1Passed()){
            gamePanel.getWorld().createTestLevel1();
            restoreConnections(1);
        }
        gamePanel.setOnGameOver(() -> {
            SwingUtilities.invokeLater(() -> {
                double successRatio = (double)gamePanel.getWorld().getHudState().getSuccessful() /
                        gamePanel.getWorld().getHudState().getTotalPackets();

                if (GameState.currentLevel == 1 && successRatio >= 0.5) {
                    GameState.saveConnections(1, gamePanel.getWorld().getConnections(), gamePanel.getWorld().getNodes());
                }
                if (GameState.currentLevel == 2 & successRatio>=0.5) {
                    GameState.saveConnections(2, gamePanel.getWorld().getConnections(), gamePanel.getWorld().getNodes());
                }



                frame.setContentPane(new GameOverPanel(
                        gamePanel.getWorld().getHudState().getTotalPackets(),
                        gamePanel.getWorld().getHudState().getLostPackets(),
                        this::restartLevel  // define this to reset
                ));
                frame.revalidate();
            });
        });

        frame.setContentPane(gamePanel);
        frame.validate();

        // Start logic & render threads at 60 UPS/FPS in GamePanel

    }
    private void restoreConnections(int level) {
        List<ConnectionRecord> connections = null;
        if (level==1) {
            connections = GameState.loadConnectionsLevel1();
        }
        else if (level==2)
            connections = GameState.loadConnectionsLevel2();

        World world = gamePanel.getWorld();
        assert connections != null;
        for (ConnectionRecord rec : connections) {
            SystemNode fromNode = world.getNodes().get(rec.getFromNodeIndex());
            SystemNode toNode = world.getNodes().get(rec.getToNodeIndex());
            Port fromPort = fromNode.getPorts().get(rec.getFromPortIndex());
            Port toPort = toNode.getPorts().get(rec.getToPortIndex());
            fromPort.setConnectedPort(toPort);
            toPort.setConnectedPort(fromPort);
            world.addConnection(new Connection(fromPort, toPort));
        }
    }
    private void restartLevel() {
        gamePanel.stop();
        frame.getContentPane().removeAll();
        gamePanel = new GamePanel(this::restartLevel);

        if (GameState.currentLevel == 2 && GameState.isLevel2Passed()) {
            gamePanel.getWorld().createTestLevel2();
            restoreConnections(2);
        } else if (GameState.currentLevel==1 && GameState.isLevel1Passed()){
            gamePanel.getWorld().createTestLevel1();
            restoreConnections(1);
        }


        gamePanel.setOnGameOver(() -> {
            double successRatio = (double)gamePanel.getWorld().getHudState().getSuccessful() /
                    gamePanel.getWorld().getHudState().getTotalPackets();

            if (GameState.currentLevel == 1 && successRatio >= 0.5) {
                GameState.saveConnections(1, gamePanel.getWorld().getConnections(), gamePanel.getWorld().getNodes());
            }
            if (GameState.currentLevel == 2 & successRatio>=0.5) {
                GameState.saveConnections(2, gamePanel.getWorld().getConnections(), gamePanel.getWorld().getNodes());
            }

            SwingUtilities.invokeLater(() -> {
                frame.setContentPane(new GameOverPanel(
                        gamePanel.getWorld().getHudState().getTotalPackets(),
                        gamePanel.getWorld().getHudState().getLostPackets(),
                        this::restartLevel  // define this to reset
                ));
                frame.revalidate();
            });
        });
        frame.setContentPane(gamePanel);
        frame.revalidate();
        /* 🔊 start BG music on first play */
        //AudioManager.get().loopMusic("bg_loop");
    }


    /** Called when the user clicks "Settings" */
    private void openSettings() {
        SettingsMenu settingsMenu = new SettingsMenu(() -> frame.setContentPane(mainMenu));
        frame.setContentPane(settingsMenu);
        frame.validate();
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
