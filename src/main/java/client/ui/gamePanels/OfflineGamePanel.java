package client.ui.gamePanels;

import server.sim.engine.GameLoop;
import server.sim.engine.world.WorldController;
import server.sim.engine.world.level.Level;
import shared.snapshot.WorldSnapshot;
import server.sim.core.save.SaveManager;
import client.view.WorldView;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.function.Consumer;

/**
 * Runs local world & loop like Phase-2. Transitional: still imports server.sim.*
 */
public final class OfflineGamePanel extends BaseGamePanel implements Consumer<WorldSnapshot> {

    private final WorldView renderer = new WorldView();
    private final server.sim.engine.world.World world;
    private final WorldController ctrl;
    private final GameLoop loop;
    private final SaveManager saveManager; // may be null
    private final Level level;

    private volatile WorldSnapshot snap; // EDT-safe frame

    public OfflineGamePanel(Runnable restartLevel, Runnable exitToMenu, Level level, SaveManager saveManager) {
        super();
        this.level = level;
        this.saveManager = saveManager;

        if (this.saveManager != null) this.saveManager.start();

        this.world = new server.sim.engine.world.World(level);
        this.ctrl  = new WorldController(world);
        this.loop  = new GameLoop(ctrl, this::accept);

        // buttons
        bindExitToMenu(() -> { stop(); exitToMenu.run(); });
        resumeButton.addActionListener(e -> togglePauseIfNeeded());
        restartButton.addActionListener(e -> restartLevel.run());

        initTimeSlider();
        initInputListeners();

        loop.start();
        new Thread(loop, "LogicThread").start();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    @Override protected void renderGame(Graphics2D g2) {
        if (snap != null) renderer.renderAll(g2, snap);
        // position the (offline) time slider visibly
        timeSlider.setVisible(true);
        timeSlider.setBounds(getWidth()/2 - 150, getHeight() - 50, 300, 30);
    }

    @Override protected boolean isGameOver() { return snap != null && snap.gameOver(); }
    @Override protected boolean isViewOnly() { return snap != null && snap.viewOnlyMode(); }

    @Override public void accept(WorldSnapshot s) {
        this.snap = s;
        if (saveManager != null) {
            // SaveManager.pushFrame(...) — keep your existing call if available
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    public void stop() {
        if (loop != null) loop.stop();
        if (saveManager != null) saveManager.stop();
    }

    private void togglePauseIfNeeded() {
        var tc = world.getTimeController();
        if (tc.isPaused()) {
            tc.togglePause();
            resumeButton.setVisible(false);
            restartButton.setVisible(false);
            exitButton.setVisible(false);
            repaint();
        }
    }

    private void initTimeSlider() {
        timeSlider.setMajorTickSpacing(10);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        var labels = new Hashtable<Integer, JLabel>();
        labels.put(0, new JLabel("0s")); labels.put(10, new JLabel("10s"));
        labels.put(20, new JLabel("20s")); labels.put(30, new JLabel("30s"));
        timeSlider.setLabelTable(labels);
        timeSlider.addChangeListener(e -> {
            if (!timeSlider.getValueIsAdjusting()) goToTime();
            else world.getTimeController().waitToStart();
        });
    }

    private void goToTime() {
        double t = timeSlider.getValue();
        world.resetToSnapshot(world.getInitialState());
        world.getHudState().resetGameTime();
        if (t > 0) {
            world.setViewOnlyMode(true);
            world.getTimeController().setTimeMultiplier(5.0); // was CFG.fastGameSpeed
            world.getTimeController().jumpTo(t);
            if (world.getTimeController().isWaitingToStart())
                world.getTimeController().startFromFreeze();
            else
                world.getTimeController().toggleFrozen();
        } else {
            world.setViewOnlyMode(false);
            world.getTimeController().stopJump();
            world.getTimeController().waitToStart();
        }
        repaint();
        requestFocusInWindow();
    }

    private void initInputListeners() {
        // Keep your existing key & mouse handlers here (omitted for brevity)
        // IMPORTANT: avoid mutating world collections on the EDT while loop ticks.
        // Queue heavy mutations to the loop thread if possible.
    }
}