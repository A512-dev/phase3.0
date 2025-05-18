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
import com.mygame.model.GameState;
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




    private ShopPanel shopPanel;
    private boolean   shopOpen = false;

    // ⬇️  put the override anywhere in the class body
    @Override
    public void doLayout() {
        super.doLayout();                         // let JPanel lay out its children
        if (shopPanel != null) {
            // keep the overlay stretched to the current panel size
            shopPanel.setBounds(0, 0, getWidth(), getHeight());
        }
    }





    public GamePanel(Runnable restartLevel) {
//        setPreferredSize(new Dimension(800, 600));
//        setDoubleBuffered(true);
//        setFocusable(true);
//        requestFocusInWindow();
        this.world = new World();
        maxWire = world.getHudState().getWireLengthRemaining();
        // 🔁 Run the slider logic again f`or second pass
        world.setOnReachedTarget(this::goToTime);
        world.setPacketEventListener(world.getHudState());
        this.worldView = new WorldView();
        // Start logic & render threads at 60 UPS/FPS in GamePanel
        gameLoop = new GameLoop(this, Database.ups, Database.fps);
        gameLoop.start();


        setLayout(new OverlayLayout(this)); // Overlay layout allows stacking
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(234, 147, 197));

        if (GameState.currentLevel==1 && GameState.isLevel1Passed()) {
            System.out.println("ppppppppppppppppppppppppppppppppppppppp");
            JButton switchLevelBtn = new JButton("Level 2");
            switchLevelBtn.setBounds(10, 10, 80, 30);
            switchLevelBtn.setFocusable(true);
            switchLevelBtn.addActionListener(e -> {
                GameState.currentLevel = 2;
                restartLevel.run();
            });
            add(switchLevelBtn);
        }
        else if (GameState.currentLevel==2) {
            System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
            JButton switchLevelBtn = new JButton("Level 1");
            switchLevelBtn.setBounds(10, 10, 80, 30);
            switchLevelBtn.setFocusable(true);
            switchLevelBtn.addActionListener(e -> {
                GameState.currentLevel = 1;
                restartLevel.run();
            });
            add(switchLevelBtn);
        }
        System.out.println("currentLevel="+GameState.currentLevel);



        setLayout(null);                                // you already call this
        shopPanel = new ShopPanel(world, world.getHudState(),
                () -> {                         // onClose lambda
                    world.getTimeController().togglePause();
                    shopOpen = false;
                    requestFocusInWindow();     // restore key focus
                }
                );
        add(shopPanel);
        shopPanel.setBounds(0,0,getPreferredSize().width,getPreferredSize().height);

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
                        // Trigger a forced update to emit packets from base nodes
                        //world.updateAll(1.0*Database.timeMultiplier / Database.ups);  // Run 1 logic tick immediately
                        //world.getNodes().get(0).emitQueued(world.getPackets());
                        //tc.setFirstStart(true);
                    } else {
                        // ⏸️ Toggle freeze
                        tc.toggleFrozen();
                        System.out.println(tc.isFrozen() ? "⏸ Paused" : "▶️ Resumed");
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_S) {
                    if (!shopOpen) {
                        world.getTimeController().togglePause();  // freeze
                        shopPanel.open();
                    } else {
                        shopPanel.close();
                        world.getTimeController().togglePause();  // resume
                    }
                    shopOpen = !shopOpen;
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
                Port connected = selectedPort.getConnectedPort();
                if (selectedPort.getConnectedPort()!=null) {
                    // ✅ Remove existing connection clearly
                    world.removeConnectionBetween(selectedPort, connected);
                    selectedPort.setConnectedPort(null);
                    connected.setConnectedPort(null);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedPort != null) {
                    Vector2D mousePos = new Vector2D(e.getX(), e.getY());
                    Port targetPort = world.findPortAtPosition(mousePos);

                    if (targetPort != null && targetPort.getCenter() != selectedPort.getCenter() &&
                            targetPort.getDirection() != selectedPort.getDirection()) {

                        double dist = selectedPort.getPosition().distanceTo(targetPort.getPosition());
                        if (dist <= world.getHudState().getWireLengthRemaining()) {
                            double x = world.getHudState().getWireLengthRemaining();
                            // Connect ports
                            selectedPort.setConnectedPort(targetPort);
                            targetPort.setConnectedPort(selectedPort);

                            world.addConnection(new Connection(selectedPort, targetPort));
                            world.getHudState().setWireLengthRemaining(x-dist);
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
            if (!timeSlider.getValueIsAdjusting()) {
                world.getHudState().setNumOfGoToTarget(-1);
                goToTime();
            }
            else {
                // Reset and freeze at t=0
                world.getTimeController().waitToStart();
            }

        });
        add(timeSlider);
        setFocusable(true);
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }
        setDoubleBuffered(true);
    }

    public void goToTime() {
        System.out.println("KKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
        world.getHudState().setNumOfGoToTarget(world.getHudState().getNumOfGoToTarget()+1);
        System.out.println("numGoTarget"+world.getHudState().getNumOfGoToTarget());
        double target = timeSlider.getValue();
        world.resetToSnapshot(world.getInitialState());
        world.getHudState().resetGameTime();
        if (target > 0) {
            world.setViewOnlyMode(true);  // 🔁 This suppresses GameOver

            world.getTimeController().setTimeMultiplier(Database.fastGameSpeed);
            Database.timeMultiplier = Database.fastGameSpeed;
            world.getTimeController().jumpTo(target);
            // 4) Un‐freeze so the nodes.update() actually runs
            world.getTimeController().startFromFreeze();

        }
        repaint();

        requestFocusInWindow();// regain key focus after slider interaction

    }

    /**
     * Called by GameLoop once per logic tick.
     */
    public void updateLogic(double dt) {
//        double target = world.getTimeController().getTargetTime();
//
//
//        if (target >= 0) {
//            double currentTime = world.getHudState().getGameTime();
//            double timeLeft = target - currentTime;
//
//            if (timeLeft <= 0) {
//                // we've already reached target time
//                return;
//            }
//
//            // clamp dt so we don’t overshoot target
//            dt = Math.min(dt, timeLeft);
//        }
////
        world.updateAll(dt);
//        world.getTimeController().updateRealTime(dt);
//        double simDt = world.getTimeController().getDeltaSeconds(dt);
//        // 3) Only step the world if there’s positive time to advance
//        if (simDt > 0) {
//
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
        // in paintComponent (when game is over)
        if (world.isGameOver()) {
            g.setColor(new Color(255, 0, 0, 100));  // semi-transparent red
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.drawString("GAME OVER — You can still review the timeline.", getWidth()/2 - 200, 40);
        }
        if (world.isViewOnlyMode()) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Reviewing Timeline — Simulation Paused", 10, getHeight() - 10);
        }
        if (world.isGameOver() && onGameOver != null && !world.isViewOnlyMode()) {
            onGameOver.run();  // Tell the main app to switch to GameOverPanel
        }
        if (world.getTimeController().isPaused() && !shopOpen) {
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
