// GamePanel.java
package com.mygame.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import com.mygame.engine.GameLoop;
import com.mygame.engine.TimeController;
import com.mygame.model.Connection;
import com.mygame.model.Port;
import com.mygame.model.World;
import com.mygame.util.Database;
import com.mygame.util.Vector2D;
import com.mygame.view.WorldView;

public class GamePanel extends JPanel {
    private  World         world          = new World();

    public World getWorld() {
        return world;
    }

    private  WorldView     worldView      = new WorldView();
    private JPanel gameOverOverlay;
    private GameLoop gameLoop;
    private JButton resumeButton;
    private JButton restartButton;

    private JSlider timeSlider;
    private Port selectedPort = null;
    private double maxWire = Database.MAX_WIRE_LENGTH; // limit maximum length




    public GamePanel(Runnable restartLevel) {
//        setPreferredSize(new Dimension(800, 600));
//        setDoubleBuffered(true);
//        setFocusable(true);
//        requestFocusInWindow();
        this.world = new World();
        this.worldView = new WorldView();
        // Start logic & render threads at 60 UPS/FPS in GamePanel
        gameLoop = new GameLoop(this, Database.ups, Database.fps);
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
                System.out.println("Key pressed: " + e.getKeyCode());
                int code = e.getKeyCode();

                if (code == KeyEvent.VK_ESCAPE) {
                    // Toggle pause/resume only if game already started
                    if (!world.getTimeController().isWaitingToStart()) {
                        world.getTimeController().togglePause();
                        resumeButton.setEnabled(true);
                        resumeButton.setVisible(true);
                        System.out.println("paused");
                    }
                }

                if (code == KeyEvent.VK_SPACE) {
                    TimeController tc = world.getTimeController();

                    if (tc.isWaitingToStart()) {
                        // 🟦 Initial state → start sim
                        tc.startFromFreeze();
                        tc.toggleFrozen();
                        tc.setTimeMultiplier(1.0);  // normal speed
                        System.out.println("⏯ Starting from zero");
                        world.emitQueuedOnStart(world.getPackets());
                        //tc.setFirstStart(true);
                    } else {
                        // ⏸️ Toggle freeze
                        tc.toggleFrozen();
                        System.out.println(tc.isFrozen() ? "⏸ Paused" : "▶️ Resumed");
                    }
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Vector2D mousePos = new Vector2D(e.getX(), e.getY());
                System.out.println("Mouse="+mousePos.toString());
                selectedPort = world.findPortAtPosition(mousePos);
                if (selectedPort!=null)
                    System.out.println("selectedPort:"+selectedPort.getPosition().toString());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedPort != null) {
                    Vector2D mousePos = new Vector2D(e.getX(), e.getY());
                    Port targetPort = world.findPortAtPosition(mousePos);

//                    if (targetPort != null) {
//                        double dist = selectedPort.getPosition().distanceTo(targetPort.getPosition());
//                        if (dist <= maxWire) {
//                            // Connect ports
//                            selectedPort.setConnectedPort(targetPort);
//                            targetPort.setConnectedPort(selectedPort);
//
//                            world.addConnection(new Connection(selectedPort, targetPort));
//                        } else {
//                            JOptionPane.showMessageDialog(null, "Wire too long!");
//                        }
//                    }
                    if (targetPort != null && targetPort.getCenter() != selectedPort.getCenter() &&
                            targetPort.getDirection() != selectedPort.getDirection()) {

                        double dist = selectedPort.getPosition().distanceTo(targetPort.getPosition());
                        if (dist <= maxWire) {
                            // Connect ports
                            selectedPort.setConnectedPort(targetPort);
                            targetPort.setConnectedPort(selectedPort);

                            world.addConnection(new Connection(selectedPort, targetPort));
                            maxWire -= dist;
                        } else {
                            JOptionPane.showMessageDialog(null, "Wire too long!");
                        }
                    }
                    selectedPort = null;
                }
            }
        });

        //resume Button and it's listeners
        {
        SwingUtilities.invokeLater(this::requestFocusInWindow);
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
        SwingUtilities.invokeLater(this::requestFocusInWindow);
        }
        //restart button and it's listeners
        {
            restartButton = new JButton("Restart");
            restartButton.setFocusable(false);
            restartButton.setVisible(false);
            restartButton.addActionListener(e -> restartLevel.run());
            add(restartButton);
        }
        //Time Slider and it's listeners
        {
        timeSlider = new JSlider(0, 30, 0); // range 0 to 30 seconds
        timeSlider.setMajorTickSpacing(10); // spacing between ticks
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        timeSlider.setFocusable(false);
        timeSlider.setVisible(false);

        // Optional: precise labels (cleaner than auto-generated)
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0s"));
        labelTable.put(10, new JLabel("10s"));
        labelTable.put(20, new JLabel("20s"));
        labelTable.put(30, new JLabel("30s"));
        timeSlider.setLabelTable(labelTable);

        timeSlider.setBounds(10, getHeight() - 40, 300, 30);
        timeSlider.setFocusable(true);
        timeSlider.addChangeListener(e -> {
            System.out.println("slider movenewjwfwefkwfwfkwefwefl");
            if (!timeSlider.getValueIsAdjusting()) {
                double target = timeSlider.getValue();
                world.resetToSnapshot(world.getInitialState());
                world.getHudState().resetGameTime();
                if (target > 0) {
//                    world.resetDynamic();
//                    world.getHudState().resetGameTime();  // redundant if resetDynamic does it
//                    world.getTimeController().setTimeMultiplier(8);
//                    world.getTimeController().jumpTo(target);
//                    if (world.getTimeController().isFrozen())
//                        world.getTimeController().toggleFrozen();
                    world.getTimeController().setTimeMultiplier(30);
                    Database.timeMultiplier = 30;
                    world.getTimeController().jumpTo(target);
                    if (world.getTimeController().isFrozen())
                        world.getTimeController().toggleFrozen();
                    //world.getTimeController().startFromFreeze();  // allow sim to run
                } else {
                    // Reset and freeze at t=0
                    world.getTimeController().waitToStart();
                }

                requestFocusInWindow();  // regain key focus after slider interaction
            }
//            if (!timeSlider.getValueIsAdjusting()) {
//                double target = timeSlider.getValue();
//                if (target>0) {
//
//                    // 1. Reset to initial state
//                    world.getTimeController().setTimeMultiplier(20.0);
//                    world.getTimeController().jumpTo(target);
//                    world.getTimeController().startFromFreeze();  // let sim run
//                }
//                if (!world.getTimeController().isWaitingToStart()) {
//                    world.getTimeController().waitToStart(); // stand still for deterministic control
//                }
//            }
        });
        add(timeSlider);
        setFocusable(true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }
        setDoubleBuffered(true);
    }

    /**
     * Called by GameLoop once per logic tick.
     */
    public void updateLogic(double dt) {
        world.updateAll(dt);
//        world.getTimeController().updateRealTime(dt);
//        double simDt = world.getTimeController().getDeltaSeconds();
//        if (simDt > 0) {
//            world.updateAll(simDt);
//        }
    }
    public void stop() {
        if (gameLoop != null) gameLoop.stop();
    }


    @Override
    protected void paintComponent(Graphics g) {
        long start = System.nanoTime();
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
            int bx = getWidth()/2 - buttonWidth/2;
            int by = getHeight()/2 + buttonHeight + 20;
            restartButton.setBounds(bx, by, buttonWidth, buttonHeight);
            restartButton.setVisible(true);

        } else {
            resumeButton.setVisible(false);
            restartButton.setVisible(false);
        }

        timeSlider.setVisible(true);
        timeSlider.setBounds(getWidth() / 2 - 150, getHeight() - 50, 300, 30);
        //g.drawString("Time: " + String.format("%.2f", world.getHudState().getGameTime()), 10, 20);

        long end = System.nanoTime();
        //System.out.println("RENDER took " + (end - start) / 1_000_000.0 + " ms");
    }
    private Runnable onGameOver;

    public void setOnGameOver(Runnable callback) {
        this.onGameOver = callback;
    }


    /** Expose jump/pause controls to your UI/buttons */
    public void jumpTo(double seconds) {
        world.getTimeController().jumpTo(seconds);
    }

    public void togglePause() {
        world.getTimeController().togglePause();
    }
}
