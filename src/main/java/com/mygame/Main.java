package com.mygame;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.mygame.audio.AudioManager;
import com.mygame.model.World;
import com.mygame.ui.GameOverPanel;
import com.mygame.ui.GamePanel;
import com.mygame.ui.MainMenu;
import com.mygame.ui.SettingsMenu;
import com.mygame.engine.GameLoop;

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
        // Swap in the game panel
        gamePanel = new GamePanel(this::restartLevel);
        gamePanel.setOnGameOver(() -> {
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
        frame.validate();

        // Start logic & render threads at 60 UPS/FPS in GamePanel

    }
    private void restartLevel() {
        gamePanel.stop();
        frame.getContentPane().removeAll();
        gamePanel = new GamePanel(this::restartLevel);
        gamePanel.setOnGameOver(() -> {
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
