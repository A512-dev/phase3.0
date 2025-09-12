package com.mygame;

import javax.swing.*;

import com.mygame.audio.AudioManager;
import com.mygame.core.GameState;
import com.mygame.core.save.SaveManager;
import com.mygame.core.save.SaveRecord;
import com.mygame.engine.world.level.Level;

import com.mygame.model.Port;
import com.mygame.model.node.Node;
import com.mygame.model.Connection;
import com.mygame.engine.world.World;
import com.mygame.snapshot.WorldSnapshot;
import com.mygame.ui.*;
import com.mygame.core.ConnectionRecord;

import java.util.List;

public class Main {


    // Main.java
    private SaveManager saveManager;   // <- add this




    private final JFrame frame;
    private final MainMenu mainMenu;
    private GamePanel gamePanel;

    public Main() {
        frame = new JFrame("Blueprint Hell – Phase 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Show main menu on startup
        mainMenu = new MainMenu(this::startGame, this::openSettings);
        frame.setContentPane(mainMenu);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();

        /* 🔊 start BG music on first play */
        //AudioManager.get().loopMusic("pacman background music");
    }

    /** Called when the user clicks "Play" */
    //private String currentLevelId = GameState.currentLevelID;  // or restore from GameState
    //private int currentLevelInt = GameState.currentLevelInt;  // or restore from GameState
    private int currentLevelInt = 1;
    private String currentLevelId;

    private void startGame_BEFORE() {
        Level level = new Level(currentLevelInt);
        //System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL"+level.id());
        currentLevelId = level.id();
        gamePanel = new GamePanel(this::restartLevel, this::exitToMenu,level, saveManager);

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
                frame.pack();
                frame.setLocationRelativeTo(null);
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
    private void startGame() {
        LevelSelectPanel picker = new LevelSelectPanel(
                () -> { frame.setContentPane(mainMenu); frame.revalidate(); },
                this::launchLevel
        );
        frame.setContentPane(picker);
        frame.revalidate();
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
        if (gamePanel != null) gamePanel.stop();
        if (saveManager != null) { try { saveManager.close(); } catch (Exception ignore) {} }


        frame.getContentPane().removeAll();
        assert Integer.parseInt(currentLevelId.substring(5)) == currentLevelInt;
        Level level = new Level(currentLevelInt);

        // new SaveManager for this run/level id
        saveManager = new SaveManager(level.id());



        gamePanel = new GamePanel(this::restartLevel, this::exitToMenu,  level, saveManager);
//        if (GameState.isLevelPassed(currentLevelId)) {
//            restoreConnections(currentLevelId);
//        }
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



        // Start autosave cadence
        saveManager.start();



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


    private void launchLevel(int chosenLevel) {
        this.currentLevelInt = chosenLevel;

        Level level = new Level(currentLevelInt);
        currentLevelId = level.id();
        // init SaveManager (but don't start pushing frames yet)
        saveManager = new SaveManager(level.id());

        boolean resume = false;
        if (saveManager.hasAutosave()) {
            int choice = javax.swing.JOptionPane.showConfirmDialog(
                    frame,
                    "A previous run was interrupted. Continue from autosave?",
                    "Resume?",
                    JOptionPane.YES_NO_OPTION
            );
            resume = (choice == JOptionPane.YES_OPTION);
        }

        gamePanel = new GamePanel(this::restartLevel, this::exitToMenu, level, saveManager);



        // if resume, load and apply snapshot BEFORE showing the panel
        if (resume) {
            try {
                SaveRecord rec = saveManager.load();     // verifies HMAC + version
                if (rec != null && level.id().equals(rec.levelId())) {
                    gamePanel.getWorld().resetToSnapshot(rec.snapshot());
                    // Short preview (per spec): show a little motion, then continue
                    gamePanel.jumpTo(Math.min(3.0, gamePanel.getWorld().getHudState().getGameTime()));
                    gamePanel.togglePauseIfNeeded();
                } else {
                    saveManager.clearAutosave(); // mismatched level file
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // corrupted or mismatched → start fresh and delete file
                saveManager.clearAutosave();
            }
        } else {
            // player declined → delete autosave per spec
            saveManager.clearAutosave();
        }

        // now that the panel is ready, start autosave cadence
        saveManager.start();





//
//        // restore old connections if you want:
//        if (GameState.isLevelPassed(level.id()))
//            restoreConnections(level.id());

        gamePanel.setOnGameOver(() -> {
            SwingUtilities.invokeLater(() -> {
                double successRatio = (double) gamePanel.getWorld().getHudState().getSuccessful() /
                        gamePanel.getWorld().getHudState().getTotalPackets();

                if (successRatio >= 0.5) {
                    GameState.saveConnections(
                            level.id(),
                            gamePanel.getWorld().getConnections(),
                            gamePanel.getWorld().getNodes()
                    );
                }

                frame.setContentPane(new GameOverPanel(
                        gamePanel.getWorld().getHudState().getTotalPackets(),
                        gamePanel.getWorld().getHudState().getLostPackets(),
                        this::restartLevel
                ));
                frame.pack();                        // <-- picks up GamePanel.getPreferredSize()
                frame.setLocationRelativeTo(null);   // optional: keep it centered
                frame.revalidate();
                frame.repaint();
            });
        });

        frame.setContentPane(gamePanel);
        frame.pack();                        // <-- picks up GamePanel.getPreferredSize()
        frame.setLocationRelativeTo(null);   // optional: keep it centered
        frame.revalidate();
        frame.repaint();
    }

    // put inside Main
    private void exitToMenu() {
        if (gamePanel != null) {
            gamePanel.stop();
        }
        if (saveManager != null) {
            try {
                // Intentioned exit: delete autosave so resume prompt doesn’t appear
                saveManager.clearAutosave();
                saveManager.close();  // stop background scheduler
            } catch (Exception ignore) {}
            saveManager = null;
        }
        frame.getContentPane().removeAll();
        frame.setContentPane(mainMenu);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }





    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
