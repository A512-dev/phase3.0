//// GamePanel.java
//package com.mygame.ui;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.util.Hashtable;
//
//import com.mygame.core.GameConfig;
//import com.mygame.model.Port;
//import com.mygame.model.Connection;
//import com.mygame.engine.GameLoop;
//import com.mygame.engine.loop.TimeController;
//import com.mygame.engine.world.World;
//import com.mygame.model.*;
//import com.mygame.engine.physics.Vector2D;
//import com.mygame.view.WorldView;
//
//public class GamePanel extends JPanel {
//    GameConfig cfg = GameConfig.defaultConfig();
//    private World world          = new World();
//
//    public World getWorld() {
//        return world;
//    }
//
//    private  WorldView     worldView      = new WorldView();
//    private JPanel gameOverOverlay;
//    private GameLoop gameLoop;
//    private JButton resumeButton;
//    private JButton restartButton;
//
//    private JSlider timeSlider;
//    private Port selectedPort = null;
//    private double maxWire = cfg.maxWireLength; // limit maximum length
//
//
//
//
//    private ShopPanel shopPanel;
//    private boolean   shopOpen = false;
//
//    // ⬇️  put the override anywhere in the class body
//    @Override
//    public void doLayout() {
//        super.doLayout();                         // let JPanel lay out its children
//        if (shopPanel != null) {
//            // keep the overlay stretched to the current panel size
//            shopPanel.setBounds(0, 0, getWidth(), getHeight());
//        }
//    }
//
//
//
//
//
//    public GamePanel(Runnable restartLevel) {
////        setPreferredSize(new Dimension(800, 600));
////        setDoubleBuffered(true);
////        setFocusable(true);
////        requestFocusInWindow();
//        this.world = new World();
//        maxWire = world.getHudState().getWireLengthRemaining();
//        // 🔁 Run the slider logic again f`or second pass
//        world.setOnReachedTarget(this::goToTime);
//        world.setPacketEventListener(world.getHudState());
//        this.worldView = new WorldView();
//        // Start logic & render threads at 60 UPS/FPS in GamePanel
//        gameLoop = new GameLoop(this, cfg.ups, cfg.fps);
//        gameLoop.start();
//
//
//        setLayout(new OverlayLayout(this)); // Overlay layout allows stacking
//        setPreferredSize(new Dimension(800, 600));
//        setBackground(new Color(234, 147, 197));
//
//        if (GameState.currentLevel==1 && GameState.isLevel1Passed()) {
//
//            JButton playAgainBtn = new JButton("Play Again");
//            playAgainBtn.setBounds(100, 10, 120, 30);
//            playAgainBtn.setFocusable(true);
//            playAgainBtn.addActionListener(e -> {
//                GameState.clearConnections(1); // new method to remove saved solution
//                restartLevel.run();
//            });
//
//            add(playAgainBtn);
//            JButton switchLevelBtn = new JButton("Level 2");
//            switchLevelBtn.setBounds(200, 10, 80, 30);
//            switchLevelBtn.setFocusable(true);
//            switchLevelBtn.addActionListener(e -> {
//                GameState.currentLevel = 2;
//                restartLevel.run();
//            });
//            add(switchLevelBtn);
//        }
//        else if (GameState.currentLevel==2) {
//            if (GameState.isLevel2Passed()) {
//                JButton playAgainBtn = new JButton("Play Again");
//                playAgainBtn.setBounds(100, 10, 120, 30);
//                playAgainBtn.setFocusable(true);
//                playAgainBtn.addActionListener(e -> {
//                    GameState.clearConnections(2); // new method to remove saved solution
//                    restartLevel.run();
//                });
//                add(playAgainBtn);
//            }
//            JButton switchLevelBtn = new JButton("Level 1");
//            switchLevelBtn.setBounds(200, 10, 80, 30);
//            switchLevelBtn.setFocusable(true);
//            switchLevelBtn.addActionListener(e -> {
//                GameState.currentLevel = 1;
//                restartLevel.run();
//            });
//            add(switchLevelBtn);
//        }
//        System.out.println("currentLevel="+GameState.currentLevel);
//
//
//
//        setLayout(null);                                // you already call this
//        shopPanel = new ShopPanel(world, world.getHudState(),
//                () -> {                         // onClose lambda
//                    world.getTimeController().togglePause();
//                    shopOpen = false;
//                    requestFocusInWindow();     // restore key focus
//                }
//                );
//        add(shopPanel);
//        shopPanel.setBounds(0,0,getPreferredSize().width,getPreferredSize().height);
//
//        // Paint layer (this panel)
//        setOpaque(true);
//
//        setFocusable(true);
//        requestFocusInWindow();
//        addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                System.out.println("Key pressed: " + e.getKeyCode());
//                int code = e.getKeyCode();
//
//                if (code == KeyEvent.VK_ESCAPE) {
//                    // Toggle pause/resume only if game already started
//                    if (!world.getTimeController().isWaitingToStart()) {
//                        world.getTimeController().togglePause();
//                        resumeButton.setEnabled(true);
//                        resumeButton.setVisible(true);
//                        System.out.println("paused");
//                    }
//                }
//
//                if (code == KeyEvent.VK_SPACE) {
//                    TimeController tc = world.getTimeController();
//
//                    if (tc.isWaitingToStart()) {
//                        // 🟦 Initial state → start sim
//                        tc.startFromFreeze();
//                        tc.toggleFrozen();
//                        tc.setTimeMultiplier(1.0);  // normal speed
//                        System.out.println("⏯ Starting from zero");
//                        // Trigger a forced update to emit packets from base nodes
//                        //world.update(1.0*Database.timeMultiplier / Database.ups);  // Run 1 logic tick immediately
//                        //world.getNodes().get(0).emitQueued(world.getPackets());
//                        //tc.setFirstStart(true);
//                    } else {
//                        // ⏸️ Toggle freeze
//                        tc.toggleFrozen();
//                        System.out.println(tc.isFrozen() ? "⏸ Paused" : "▶️ Resumed");
//                    }
//                }
//
//                if (e.getKeyCode() == KeyEvent.VK_S) {
//                    if (!shopOpen) {
//                        world.getTimeController().togglePause();  // freeze
//                        shopPanel.open();
//                    } else {
//                        shopPanel.close();
//                        world.getTimeController().togglePause();  // resume
//                    }
//                    shopOpen = !shopOpen;
//                }
//            }
//        });
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//
//                Vector2D mousePos = new Vector2D(e.getX(), e.getY());
//                System.out.println("Mouse="+mousePos.toString());
//                selectedPort = world.findPortAtPosition(mousePos);
//                if (selectedPort!=null) {
//                    System.out.println("selectedPort:"+selectedPort);
//                    System.out.println("connected::"+ selectedPort.getConnectedPort());
//                }
//                for (Connection c : world.getConnections()) {
//                    if (c.getFrom().getConnectedPort() != c.getTo())
//                        c.getFrom().setConnectedPort(c.getTo());
//                    if ((c.getTo().getConnectedPort() != c.getFrom()))
//                        c.getTo().setConnectedPort(c.getFrom());
//                }
//
//                Port connected = selectedPort.getConnectedPort();
//                System.out.println("toPort====="+connected);
//                if (connected!=null) {
//                    // 🧮 Compute wire length
//                    double removedLength = selectedPort.getPosition().distanceTo(connected.getPosition());
//
//                    // 🔁 Add it back
//                    double newRemaining = world.getHudState().getWireLengthRemaining() + removedLength;
//                    world.getHudState().setWireLengthRemaining(newRemaining);
//                    System.out.println("mmmmmmmmmsmsmsmmsssmsmsmsmsmsmsmssmsm");
//
//                    // ❌ Remove connection from world list
//                    world.removeConnectionBetween(selectedPort, connected);
//
//                    selectedPort.setConnectedPort(null);
//                    connected.setConnectedPort(null);
//                }
//                // 🧼 EXTRA: scan world for *any* connection involving this port and clean it up (failsafe)
//                world.getConnections().removeIf(conn ->
//                        conn.getFrom() == selectedPort || conn.getTo() == selectedPort
//                );
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                if (selectedPort != null) {
//
//                    Vector2D mousePos = new Vector2D(e.getX(), e.getY());
//                    Port targetPort = world.findPortAtPosition(mousePos);
////                    // Overwrite previous connections
////                    if (targetPort != null && targetPort.getCenter().distanceTo(selectedPort.getCenter())>10) {
////                        if (targetPort.getConnectedPort() != null)
////                            world.removeConnectionBetween(targetPort, targetPort.getConnectedPort());
////                        if (selectedPort.getConnectedPort() != null)
////                            world.removeConnectionBetween(selectedPort, selectedPort.getConnectedPort());
////                    }
////                    // 🔄 Break existing links
////                    if (targetPort!= null && targetPort.getConnectedPort() != null)
////                        targetPort.getConnectedPort().setConnectedPort(null);
////                    if (selectedPort.getConnectedPort() != null)
////                        selectedPort.getConnectedPort().setConnectedPort(null);
//                    if (targetPort != null && targetPort.getCenter() != selectedPort.getCenter() &&
//                            targetPort.getDirection() != selectedPort.getDirection()) {
//
//                        double dist = selectedPort.getPosition().distanceTo(targetPort.getPosition());
//                        if (dist <= world.getHudState().getWireLengthRemaining()) {
//                            double x = world.getHudState().getWireLengthRemaining();
//                            // Connect ports
//                            selectedPort.setConnectedPort(targetPort);
//                            targetPort.setConnectedPort(selectedPort);
//
//                            world.addConnection(new Connection(selectedPort, targetPort));
//                            world.getHudState().setWireLengthRemaining(x-dist);
//                        } else {
//                            JOptionPane.showMessageDialog(null, "Wire too long!");
//                        }
//                    }
//                    selectedPort = null;
//                }
//            }
//        });
//
//        //resume Button and it's listeners
//        {
//        SwingUtilities.invokeLater(this::requestFocusInWindow);
//        resumeButton = new JButton("Resume");
//        resumeButton.setFocusable(false);
//        resumeButton.setVisible(false);  // start hidden
//
//        resumeButton.addActionListener(e -> {
//            if (world.getTimeController().isPaused()) {
//                resumeButton.setVisible(false);
//                resumeButton.setBounds(0, 0, 0, 0);  // reset bounds to fully hide interaction
//                resumeButton.setEnabled(false);
//                world.getTimeController().togglePause();
//                resumeButton.setVisible(false);
//                repaint();
//            }
//        });
//
//        setLayout(null);  // we will position it manually
//        add(resumeButton);
//        SwingUtilities.invokeLater(this::requestFocusInWindow);
//        }
//        //restart button and it's listeners
//        {
//            restartButton = new JButton("Restart");
//            restartButton.setFocusable(false);
//            restartButton.setVisible(false);
//            restartButton.addActionListener(e -> restartLevel.run());
//            add(restartButton);
//        }
//        //Time Slider and it's listeners
//        {
//        timeSlider = new JSlider(0, 30, 0); // range 0 to 30 seconds
//        timeSlider.setMajorTickSpacing(10); // spacing between ticks
//        timeSlider.setPaintTicks(true);
//        timeSlider.setPaintLabels(true);
//        timeSlider.setFocusable(false);
//        timeSlider.setVisible(false);
//
//        // Optional: precise labels (cleaner than auto-generated)
//        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
//        labelTable.put(0, new JLabel("0s"));
//        labelTable.put(10, new JLabel("10s"));
//        labelTable.put(20, new JLabel("20s"));
//        labelTable.put(30, new JLabel("30s"));
//        timeSlider.setLabelTable(labelTable);
//
//        timeSlider.setBounds(10, getHeight() - 40, 300, 30);
//        timeSlider.setFocusable(true);
//        timeSlider.addChangeListener(e -> {
//            if (!timeSlider.getValueIsAdjusting()) {
//                world.getHudState().setNumOfGoToTarget(-1);
//                goToTime();
//            }
//            else {
//                // Reset and freeze at t=0
//                world.getTimeController().waitToStart();
//            }
//
//        });
//        add(timeSlider);
//        setFocusable(true);
//        SwingUtilities.invokeLater(this::requestFocusInWindow);
//    }
//        setDoubleBuffered(true);
//    }
//
//    public void goToTime() {
//
//        world.getHudState().setNumOfGoToTarget(world.getHudState().getNumOfGoToTarget()+1);
//        System.out.println("numGoTarget"+world.getHudState().getNumOfGoToTarget());
//        double target = timeSlider.getValue();
//        world.resetToSnapshot(world.getInitialState());
//        world.getHudState().resetGameTime();
//        if (target > 0) {
//            world.setViewOnlyMode(true);  // 🔁 This suppresses GameOver
//
//            world.getTimeController().setTimeMultiplier(cfg.fastGameSpeed);
//            cfg.timeMultiplier = cfg.fastGameSpeed;
//            world.getTimeController().jumpTo(target);
//            // 4) Un‐freeze so the nodes.update() actually runs
//            world.getTimeController().startFromFreeze();
//
//        }
//        else if (target == 0) {
//            world.getTimeController().stopJump();
//            world.getTimeController().waitToStart();
//            world.getTimeController().setTimeMultiplier(1.0);
//            cfg.timeMultiplier = 1.0;
//            // ⏹️ Reset simulation cleanly
//            world.setViewOnlyMode(false);
//            world.getTimeController().jumpTo(target);
//            System.out.println("From:::"+world.getConnections().get(0).getFrom());
//            System.out.println("To:::"+world.getConnections().get(0).getTo());
//            // 4) Un‐freeze so the nodes.update() actually runs
//            //world.getTimeController().startFromFreeze();
//
////            if (!world.getTimeController().isFrozen())
////                world.getTimeController().toggleFrozen();    // ensure it's frozen again
//
//            world.getHudState().setCoins(0);
//            repaint();
//            requestFocusInWindow();
//            return;
//        }
//        repaint();
//
//        requestFocusInWindow();// regain key focus after slider interaction
//
//    }
//
//    /**
//     * Called by GameLoop once per logic tick.
//     */
//    public void updateLogic(double dt) {
////        double target = world.getTimeController().getTargetTime();
////
////
////        if (target >= 0) {
////            double currentTime = world.getHudState().getGameTime();
////            double timeLeft = target - currentTime;
////
////            if (timeLeft <= 0) {
////                // we've already reached target time
////                return;
////            }
////
////            // clamp dt so we don’t overshoot target
////            dt = Math.min(dt, timeLeft);
////        }
//////
//        world.update(dt);
////        world.getTimeController().updateRealTime(dt);
////        double simDt = world.getTimeController().getDeltaSeconds(dt);
////        // 3) Only step the world if there’s positive time to advance
////        if (simDt > 0) {
////
////        }
//
//    }
//    public void stop() {
//        if (gameLoop != null) gameLoop.stop();
//    }
//
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        long start = System.nanoTime();
//        super.paintComponent(g);
////        if (world.isGameOver()) {
////            SwingUtilities.invokeLater(() -> {
////                frame.setContentPane(new GameOverPanel(
////                        world.getHudState().getTotalPackets(),
////                        world.getHudState().getLostPackets(),
////                        () -> restartLevel()
////                ));
////                frame.revalidate();
////            });
////        }
//
//
//        worldView.renderAll((Graphics2D) g, world);
//        // in paintComponent (when game is over)
//        if (world.isGameOver()) {
//            g.setColor(new Color(255, 0, 0, 100));  // semi-transparent red
//            g.fillRect(0, 0, getWidth(), getHeight());
//            g.setColor(Color.WHITE);
//            g.setFont(new Font("Arial", Font.BOLD, 22));
//            g.drawString("GAME OVER — You can still review the timeline.", getWidth()/2 - 200, 40);
//        }
//        if (world.isViewOnlyMode()) {
//            g.setColor(Color.ORANGE);
//            g.setFont(new Font("Arial", Font.BOLD, 16));
//            g.drawString("Reviewing Timeline — Simulation Paused", 10, getHeight() - 10);
//        }
//        if (world.isGameOver() && onGameOver != null && !world.isViewOnlyMode()) {
//            onGameOver.run();  // Tell the main app to switch to GameOverPanel
//        }
//        if (world.getTimeController().isPaused() && !shopOpen) {
//            // Background dim
//            g.setColor(new Color(0, 0, 0, 150));
//            g.fillRect(0, 0, getWidth(), getHeight());
//
//            // Text
//            g.setColor(Color.WHITE);
//            g.setFont(new Font("Arial", Font.BOLD, 28));
//            g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2 - 40);
//
//            g.setFont(new Font("Arial", Font.PLAIN, 16));
//            g.drawString("Press ESC to resume", getWidth() / 2 - 80, getHeight() / 2 - 10);
//
//            // Button center position
//            int buttonWidth = 120;
//            int buttonHeight = 40;
//            int x = getWidth() / 2 - buttonWidth / 2;
//            int y = getHeight() / 2 + 10;
//
//            resumeButton.setBounds(x, y, buttonWidth, buttonHeight);
//            resumeButton.setVisible(true);
//            int bx = getWidth()/2 - buttonWidth/2;
//            int by = getHeight()/2 + buttonHeight + 20;
//            restartButton.setBounds(bx, by, buttonWidth, buttonHeight);
//            restartButton.setVisible(true);
//
//        } else {
//            resumeButton.setVisible(false);
//            restartButton.setVisible(false);
//        }
//
//        timeSlider.setVisible(true);
//        timeSlider.setBounds(getWidth() / 2 - 150, getHeight() - 50, 300, 30);
//        //g.drawString("Time: " + String.format("%.2f", world.getHudState().getGameTime()), 10, 20);
//
//        long end = System.nanoTime();
//        //System.out.println("RENDER took " + (end - start) / 1_000_000.0 + " ms");
//    }
//    private Runnable onGameOver;
//
//    public void setOnGameOver(Runnable callback) {
//        this.onGameOver = callback;
//    }
//
//    public void jumpTo(double seconds) {
//        world.getTimeController().jumpTo(seconds);
//    }
//
//    public void togglePause() {
//        world.getTimeController().togglePause();
//    }
//}
// GamePanel.java  (package com.mygame.ui)
package com.mygame.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.List;
import java.util.function.Consumer;

import com.mygame.core.GameConfig;
import com.mygame.engine.WorldController;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Connection;
import com.mygame.engine.GameLoop;
import com.mygame.engine.loop.TimeController;
import com.mygame.engine.world.*;
import com.mygame.model.DraftConnection;
import com.mygame.model.Port;
import com.mygame.snapshot.WorldSnapshot;
import com.mygame.view.WorldView;

public final class GamePanel extends JPanel
        implements Consumer<WorldSnapshot>
{
    /* ────────────────────────────── static config ─────────────────────────── */
    private static final GameConfig CFG = GameConfig.defaultConfig();
    private Vector2D mousePos;

    // ─── just below “private DraftConnection draftConnection …” ───
    private Connection dragConn = null;   // wire whose bend is being dragged
    private int        dragBend = -1;     // index of that bend

    public World getWorld() {
        return world;
    }

    /* ────────────────────────────── live state ────────────────────────────── */
    private final World          world;
    private final WorldController ctrl;
    private final WorldView      renderer      = new WorldView();
    private final GameLoop       loop;
    // At the top of your UI class (e.g. GamePanel or EditorCanvas)
    private DraftConnection draftConnection = null;


    /** latest immutable snapshot handed off from GameLoop (volatile ⇒ EDT safe) */
    private volatile WorldSnapshot snap;

    /* ────────────────────────────── overlay widgets ───────────────────────── */
    private final ShopPanel  shopPanel;
    private final JButton    resumeButton  = new JButton("Resume");
    private final JButton    restartButton = new JButton("Restart");
    private final JSlider    timeSlider    = new JSlider(0, 30, 0);

    private       boolean    shopOpen      = false;
    private       Port       selectedPort  = null;
    private       Runnable   onGameOverCb;



    /* ────────────────────────────── ctor ───────────────────────────────────── */
    public GamePanel(Runnable restartLevel) {
        setPreferredSize(new Dimension(800, 600));
        setDoubleBuffered(true);
        setFocusable(true);
        world = new World();
        ctrl  = new WorldController(world);

        world.setPacketEventListener(world.getHudState());

        /* 1️⃣  GameLoop — fixed-step logic on a dedicated thread */
        loop = new GameLoop(ctrl, this::accept);
        loop.start();
        new Thread(loop, "LogicThread").start();

        /* 2️⃣  Overlay layout lets us stack pause/shop panels */
        setLayout(new OverlayLayout(this));

        /* 3️⃣  Shop panel */
        shopPanel = new ShopPanel(
                world,
                world.getHudState(),
                () -> {  // onClose
                    world.getTimeController().togglePause();
                    shopOpen = false;
                    requestFocusInWindow();
                }
        );
        add(shopPanel);

        /* 4️⃣  Pause & restart buttons (initially invisible) */
        resumeButton.setVisible(false);   resumeButton.setFocusable(false);
        restartButton.setVisible(false);  restartButton.setFocusable(false);
        resumeButton.addActionListener(e -> togglePauseIfNeeded());
        restartButton.addActionListener(e -> restartLevel.run());
        add(resumeButton);
        add(restartButton);

        /* 5️⃣  Time-slider (timeline review) */
        initTimeSlider();

        /* 6️⃣  Input listeners */
        initKeyControls(restartLevel);
        initMouseControls();

        /* 7️⃣  World callbacks */
        world.setPacketEventListener(world.getHudState());
        world.setOnReachedTarget(this::goToTime);

        /* 8️⃣  Focus */
        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    /* ───────────────────────── snapshot hand-off ───────────────────────────── */
    /** called from GameLoop thread once per frame */
    @Override public void accept(WorldSnapshot s) {
        this.snap = s;
        SwingUtilities.invokeLater(this::repaint);
    }

    /* ───────────────────────── paint ──────────────────────────────────────── */
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        /*  draw game world  */
        if (snap != null) {
            renderer.renderAll((Graphics2D) g, snap);
        }

        // 2) draw the in-progress wire
        if (draftConnection != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(Color.GRAY);

            var path = draftConnection.getPath();
            for (int i = 0; i < path.size() - 1; i++) {
                Vector2D a = path.get(i);
                Vector2D b = path.get(i + 1);              // ← next vertex, not mousePos
                g2.drawLine((int) a.x(), (int) a.y(),
                        (int) b.x(), (int) b.y());
                //System.out.println("rrrrrrrrrrrrrrrrr");
            }


            /* bends as orange dots */
            g2.setColor(Color.ORANGE);
            for (Vector2D b : draftConnection.getBends())
                g2.fillOval((int) b.x() - 5, (int) b.y() - 5, 10, 10);
        }

        /*  overlays  */
        if (snap != null && snap.isGameOver()) {
            drawGameOverOverlay(g);
            if (onGameOverCb != null && !snap.isViewOnly()) onGameOverCb.run();
        }
        if (world.getTimeController().isPaused() && !shopOpen) {
            drawPauseOverlay(g);
        }

        /*  timeline slider stays visible */
        timeSlider.setVisible(true);
        timeSlider.setBounds(getWidth() / 2 - 150, getHeight() - 50, 300, 30);
    }

    /* ───────────────────────── public helpers ─────────────────────────────── */
    public void setOnGameOver(Runnable r)    { onGameOverCb = r; }
    public void stop()                       { loop.stop();     }
    public void jumpTo(double s)             { world.getTimeController().jumpTo(s); }
    public void togglePauseIfNeeded() {
        if (world.getTimeController().isPaused()) {
            world.getTimeController().togglePause();
            resumeButton.setVisible(false);
            restartButton.setVisible(false);
            repaint();
        }
    }

    /* ───────────────────────── internal UI helpers ────────────────────────── */
    private void initKeyControls(Runnable restartLevel) {
        System.out.println();
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ESCAPE) {
                    world.getTimeController().togglePause();
                }
                if (code == KeyEvent.VK_SPACE) {
                    TimeController tc = world.getTimeController();
                    if (tc.isWaitingToStart())      tc.startFromFreeze();
                    else                            tc.toggleFrozen();
                }
                if (code == KeyEvent.VK_S) {
                    shopOpen = !shopOpen;
                    tc().togglePause();
                    if (shopOpen) shopPanel.open(); else shopPanel.close();
                }
            }
            private TimeController tc() { return world.getTimeController(); }
        });
    }

    private void initMouseControls() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    selectedPort = world.findPortAtPosition(new Vector2D(e.getX(), e.getY()));
                    if (selectedPort == null)
                        return;
                    draftConnection = new DraftConnection(selectedPort);
                    draftConnection.setMouse(new Vector2D(e.getX(), e.getY()));
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    mousePos = new Vector2D(e.getX(), e.getY());

                    /* ── B) add a new bend on an existing wire ────────────────── */
                    for (Connection c : world.getConnections()) {

                        //return;
                    }
                    /* ── A) bend-drag start? ───────────────────────────────────── */
                    for (Connection c : world.getConnections()) {
                        int idx = bendIndexHit(c, mousePos);
                        if (idx == -1) {
                            if (!clickIsOnPath(c.getPath(), mousePos)) continue;

                            if (c.getBends().size() >= 3) { showError("حداکثر ۳ انحنا."); return; }
                            if (!world.getCoinService().trySpend(1))    { showError("سکه کافی نیست!"); return; }

                            int seg = segmentIndexAtClick(c.getPath(), mousePos);
                            c.addBendAt(seg, mousePos);          // new helper you added to Connection
                            repaint();
                        }
                        if (idx != -1) {                 // clicked on an existing bend
                            dragConn = c;
                            dragBend = idx;
                            return;                      // start drag, don’t add a bend
                        }
                    }





                    /* ── C) fallback: same logic you already had for draftConnection ── */
                    if (draftConnection != null) {
                        if (clickIsOnExistingBend(draftConnection.getBends(), mousePos)) {
                            // remove bend
                            int idx = indexOfClickedBend(draftConnection.getBends(), mousePos);
                            draftConnection.removeBend(idx);
                            repaint();
                        } else if (clickIsOnPath(draftConnection.getPath(), mousePos)) {
                            // add bend
                            if (draftConnection.getBends().size() < 3 &&
                                    world.getCoinService().trySpend(1)) {
                                draftConnection.addBend(mousePos);
                                repaint();
                            } else {
                                showError("can't add a new bend");
                            }
                        }
                    }


                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                // در MouseReleased
                Port destination = world.findPortAtPosition(new Vector2D(e.getX(), e.getY()));
                if (draftConnection != null && destination!=null) {
                    draftConnection.setToCandidate(destination);
                    /* break old links cleanly */
                    world.removeConnectionBetween(draftConnection.getFrom(), draftConnection.getFrom().getConnectedPort());
                    world.removeConnectionBetween(destination,        destination.getConnectedPort());

                    draftConnection.getFrom().setConnectedPort(destination);
                    destination.setConnectedPort(draftConnection.getFrom());

                    // مسیر را تبدیل به Connection واقعی بکنیم
                    Connection newConn = new Connection(draftConnection.getFrom(), destination, draftConnection.getBends());

                    if (!newConn.isValidAgainst(world.getNodes())) {
                        showError("it goes through a node");
                    } else {
                        world.addConnection(newConn);
                    }

                    draftConnection = null; // پاک کردن حالت موقت
                    repaint();
                }
                // dragging a bend on a finished wire?
                if (dragConn != null) {
                    dragConn = null;
                    repaint();
                    return;
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mousePos = new Vector2D(e.getX(), e.getY());
                if (draftConnection != null) {
                    draftConnection.setMouse(mousePos);
                    //System.out.println("trueeeeeeeeeeeeeeeeeeee");
                    repaint();
                }
                // dragging a bend on a finished wire?
                if (dragConn != null) {
                    dragConn.moveBend(dragBend, mousePos);
                    repaint();
                    return;
                }
            }
        });
    }



    private void initTimeSlider() {
        timeSlider.setMajorTickSpacing(10);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(0, new JLabel("0s"));
        labels.put(10, new JLabel("10s"));
        labels.put(20, new JLabel("20s"));
        labels.put(30, new JLabel("30s"));
        timeSlider.setLabelTable(labels);
        timeSlider.addChangeListener(e -> {
            if (!timeSlider.getValueIsAdjusting()) goToTime();
            else world.getTimeController().waitToStart();
        });
        add(timeSlider);
    }

    private void drawGameOverOverlay(Graphics g) {
        g.setColor(new Color(255, 0, 0, 100));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("GAME OVER — You can still review the timeline.",
                getWidth()/2 - 220, 40);
    }

    private void drawPauseOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("PAUSED", getWidth() / 2 - 70, getHeight() / 2 - 40);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Press ESC to resume", getWidth() / 2 - 80, getHeight() / 2 - 10);

        int bw = 120, bh = 40;
        int x = getWidth()/2 - bw/2;
        int y = getHeight()/2 + 10;
        resumeButton.setBounds(x, y, bw, bh);
        resumeButton.setVisible(true);

        int bx = getWidth()/2 - bw/2;
        int by = y + bh + 10;
        restartButton.setBounds(bx, by, bw, bh);
        restartButton.setVisible(true);
    }

    /* ───────────────────────── go-to-time (timeline review) ───────────────── */
    private void goToTime() {
        double t = timeSlider.getValue();
        world.resetToSnapshot(world.getInitialState());
        world.getHudState().resetGameTime();
        if (t > 0) {
            world.setViewOnlyMode(true);
            world.getTimeController().setTimeMultiplier(CFG.fastGameSpeed);
            world.getTimeController().jumpTo(t);
            world.getTimeController().startFromFreeze();
        } else {
            world.setViewOnlyMode(false);
            world.getTimeController().stopJump();
            world.getTimeController().waitToStart();
        }
        repaint();
        requestFocusInWindow();
    }



    /**Helpers */


    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "error", JOptionPane.ERROR_MESSAGE);
    }
    /** radius-based hit-test on existing bends */
    private boolean clickIsOnExistingBend(java.util.List<Vector2D> bends, Vector2D p){
        return indexOfClickedBend(bends, p) != -1;
    }
    private int indexOfClickedBend(List<Vector2D> bends, Vector2D p){
        final double R = 8.0;
        for (int i = 0; i < bends.size(); i++)
            if (bends.get(i).distanceTo(p) <= R) return i;
        return -1;
    }

    /** rough distance test to decide if click was near the poly-line */
    private boolean clickIsOnPath(java.util.List<Vector2D> pts, Vector2D p){
        final double THICK = 6.0;
        for (int i = 0; i < pts.size() - 1; i++)
            if (Vector2D.distPointToSegment(p, pts.get(i), pts.get(i+1)) <= THICK)
                return true;
        return false;
    }
    /** true if click is on an existing bend of the wire (within 8 px) */
    private int bendIndexHit(Connection c, Vector2D p) {
        return indexOfClickedBend(c.getBends(), p);   // −1 ⇢ miss
    }
    private int segmentIndexAtClick(List<Vector2D> pts, Vector2D p){
        for (int i = 0; i < pts.size()-1; i++)
            if (Vector2D.distPointToSegment(p, pts.get(i), pts.get(i+1)) <= 6) return i;
        return pts.size()-2;
    }
}
