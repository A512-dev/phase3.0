package com.mygame;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.mygame.audio.AudioManager;
import com.mygame.core.GameState;
import com.mygame.engine.world.level.Level;

import com.mygame.model.Port;
import com.mygame.model.node.Node;
import com.mygame.model.Connection;
import com.mygame.engine.world.World;
import com.mygame.ui.*;
import com.mygame.core.ConnectionRecord;

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
    //private String currentLevelId = GameState.currentLevelID;  // or restore from GameState
    //private int currentLevelInt = GameState.currentLevelInt;  // or restore from GameState
    private int currentLevelInt = 1;
    private String currentLevelId;

    private void startGame() {
        Level level = new Level(currentLevelInt);
        System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL"+level.id());
        currentLevelId = level.id();
        gamePanel = new GamePanel(this::restartLevel, level);

        // restore old connections if you want:
        if (GameState.isLevelPassed(level.id()))
            restoreConnections(level.id());

        gamePanel.setOnGameOver(() -> {
            SwingUtilities.invokeLater(() -> {
                double successRatio = (double)gamePanel.getWorld().getHudState().getSuccessful() /
                        gamePanel.getWorld().getHudState().getTotalPackets();
                if (successRatio>=0.5)
                    GameState.saveConnections(level.id(), gamePanel.getWorld().getConnections()
                            , gamePanel.getWorld().getNodes());

                frame.setContentPane(new GameOverPanel(
                        gamePanel.getWorld().getHudState().getTotalPackets(),
                        gamePanel.getWorld().getHudState().getLostPackets(),
                        this::restartLevel  // define this to reset
                ));
                frame.revalidate();
            });
        });



        // onGameOver logic unchanged…
        frame.setContentPane(gamePanel);
        frame.validate();



        //former way
        {
//            if (GameState.isLevel1Passed())
//                GameState.currentLevel=2;
//
//            // Swap in the game panel
//            gamePanel = new GamePanel(this::restartLevel);
//
//            if (GameState.currentLevel == 2 && GameState.isLevel2Passed()) {
//                gamePanel.getWorld().createTestLevel2();
//                restoreConnections(2);
//            } else if (GameState.currentLevel==1 && GameState.isLevel1Passed()){
//                gamePanel.getWorld().createTestLevel1();
//                restoreConnections(1);
//            }
//            gamePanel.setOnGameOver(() -> {
//                SwingUtilities.invokeLater(() -> {
//                    double successRatio = (double)gamePanel.getWorld().getHudState().getSuccessful() /
//                            gamePanel.getWorld().getHudState().getTotalPackets();
//
//                    if (GameState.currentLevel == 1 && successRatio >= 0.5) {
//                        GameState.saveConnections(1, gamePanel.getWorld().getConnections(), gamePanel.getWorld().getNodes());
//                    }
//                    if (GameState.currentLevel == 2 & successRatio>=0.5) {
//                        GameState.saveConnections(2, gamePanel.getWorld().getConnections(), gamePanel.getWorld().getNodes());
//                    }
//
//
//
//                    frame.setContentPane(new GameOverPanel(
//                            gamePanel.getWorld().getHudState().getTotalPackets(),
//                            gamePanel.getWorld().getHudState().getLostPackets(),
//                            this::restartLevel  // define this to reset
//                    ));
//                    frame.revalidate();
//                });
//            });
//
//            frame.setContentPane(gamePanel);
//            frame.validate();
        }


        // Start logic & render threads at 60 UPS/FPS in GamePanel

    }
    private void restoreConnections(String levelID) {
        List<ConnectionRecord> connections = null;
        connections = GameState.loadConnections(levelID);
        World world = gamePanel.getWorld();
        assert connections != null;
        for (ConnectionRecord rec : connections) {
            Node fromNode = world.getNodes().get(rec.getFromNodeIndex());
            Node toNode = world.getNodes().get(rec.getToNodeIndex());
            Port fromPort = fromNode.getPorts().get(rec.getFromPortIndex());
            Port toPort = toNode.getPorts().get(rec.getToPortIndex());
            fromPort.setConnectedPort(toPort);
            toPort.setConnectedPort(fromPort);
            world.addConnection(new Connection(fromPort, toPort, rec.getBends()));
        }
    }
    private void restartLevel() {
        gamePanel.stop();
        frame.getContentPane().removeAll();
        assert Integer.parseInt(currentLevelId.substring(5)) == currentLevelInt;
        Level level = new Level(currentLevelInt);

        gamePanel = new GamePanel(this::restartLevel, level);
        if (GameState.isLevelPassed(currentLevelId)) {
            restoreConnections(currentLevelId);
        }
        gamePanel.setOnGameOver(() -> {
            double successRatio = (double)gamePanel.getWorld().getHudState().getSuccessful() /
                    gamePanel.getWorld().getHudState().getTotalPackets();

            if (successRatio >= 0.5) {
                GameState.saveConnections(currentLevelId
                        , gamePanel.getWorld().getConnections(), gamePanel.getWorld().getNodes());
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
