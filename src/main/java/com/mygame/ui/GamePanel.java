// GamePanel.java
package com.mygame.ui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import com.mygame.engine.GameLoop;
import com.mygame.engine.TimeController;
import com.mygame.model.HUDState;
import com.mygame.model.World;
import com.mygame.view.WorldView;

public class GamePanel extends JPanel {
    private final TimeController timeController = new TimeController();
    private  World         world          = new World();

    public World getWorld() {
        return world;
    }

    private  WorldView     worldView      = new WorldView();
    private JPanel gameOverOverlay;
    private GameLoop gameLoop;
    private JButton resumeButton;

    private JSlider timeSlider;


    public GamePanel() {
//        setPreferredSize(new Dimension(800, 600));
//        setDoubleBuffered(true);
//        setFocusable(true);
//        requestFocusInWindow();
        this.world = new World();
        this.worldView = new WorldView();
        // Start logic & render threads at 60 UPS/FPS in GamePanel
        gameLoop = new GameLoop(this, 120.0, 60.0);
        gameLoop.start();

        setLayout(new OverlayLayout(this)); // Overlay layout allows stacking
        setPreferredSize(new Dimension(800, 600));

        // Paint layer (this panel)
        setOpaque(true);

        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    world.getTimeController().togglePause();
                    resumeButton.setEnabled(true);
                    resumeButton.setVisible(true);
                    System.out.println("paused");
                }
            }
        });
        // TODO: add key/mouse listeners here
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
        resumeButton = new JButton("Resume");
        resumeButton.setFocusable(false);
        resumeButton.setVisible(false);  // start hidden

        resumeButton.addActionListener(e -> {
            if (world.getTimeController().isPaused()) {
                resumeButton.setVisible(false);
                resumeButton.setBounds(0, 0, 0, 0);  // reset bounds to fully hide interaction
                resumeButton.setEnabled(false);
                world.getTimeController().togglePause();
                resumeButton.setVisible(false);
                repaint();
            }
        });

        setLayout(null);  // we will position it manually
        add(resumeButton);
        SwingUtilities.invokeLater(() -> requestFocusInWindow());


        timeSlider = new JSlider(0, 60 * 10);  // 0 to 600 tenths of a second (1 decimal precision)
        timeSlider.setBounds(10, getHeight() - 40, 300, 30);
        timeSlider.setFocusable(true);
        timeSlider.addChangeListener(e -> {
            if (timeSlider.getValueIsAdjusting()) {
                double t = timeSlider.getValue() / 10.0;  // 1 decimal precision
                world.getTimeController().jumpTo(t);
            }
        });
        timeSlider.setVisible(true);
        timeSlider.setBounds(getWidth() / 2 - 150, getHeight() - 50, 300, 30);
        timeSlider.addChangeListener(e -> {
            if (!timeSlider.getValueIsAdjusting()) {
                double target = timeSlider.getValue();

                // 1. Reset to initial state
                world.resetToSnapshot(world.getInitialState());
                world.getHudState().resetGameTime(); // gameTime = 0
                timeController.setTimeMultiplier(20.0); // fast
                timeController.jumpTo(target);

                if (!timeController.isPaused()) {
                    timeController.togglePause(); // pause for deterministic control
                }
            }
        });
        add(timeSlider);

    }

    /**
     * Called by GameLoop once per logic tick.
     */
    public void updateLogic(double dt) {
        timeController.updateRealTime(dt);
        double simDt = timeController.getDeltaSeconds();
        if (simDt > 0) {
            world.updateAll(simDt);
        }
    }
    public void stop() {
        if (gameLoop != null) gameLoop.stop();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
//        if (world.isGameOver()) {
//            SwingUtilities.invokeLater(() -> {
//                frame.setContentPane(new GameOverPanel(
//                        world.getHudState().getTotalPackets(),
//                        world.getHudState().getLostPackets(),
//                        () -> restartLevel()
//                ));
//                frame.revalidate();
//            });
//        }

        worldView.renderAll((Graphics2D) g, world);
        if (world.isGameOver() && onGameOver != null) {
            onGameOver.run();  // Tell the main app to switch to GameOverPanel
        }
        if (world.getTimeController().isPaused()) {
            // Background dim
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Text
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2 - 40);

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Press ESC to resume", getWidth() / 2 - 80, getHeight() / 2 - 10);

            // Button center position
            int buttonWidth = 120;
            int buttonHeight = 40;
            int x = getWidth() / 2 - buttonWidth / 2;
            int y = getHeight() / 2 + 10;

            resumeButton.setBounds(x, y, buttonWidth, buttonHeight);
            resumeButton.setVisible(true);
        } else {
            resumeButton.setVisible(false);
        }

        timeSlider.setVisible(true);
        timeSlider.setBounds(getWidth() / 2 - 150, getHeight() - 50, 300, 30);
        int sliderTime = (int) (world.getHudState().getGameTime() * 10);
        if (!timeSlider.getValueIsAdjusting()) {
            timeSlider.setValue(sliderTime);
        }

    }
    private Runnable onGameOver;

    public void setOnGameOver(Runnable callback) {
        this.onGameOver = callback;
    }


    /** Expose jump/pause controls to your UI/buttons */
    public void jumpTo(double seconds) {
        timeController.jumpTo(seconds);
    }

    public void togglePause() {
        timeController.togglePause();
    }
}
