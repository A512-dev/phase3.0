package client.ui.gamePanels;

import client.ui.helper.GameUi;


import javax.swing.*;
import java.awt.*;

/**
 * Shared UI chrome; subclasses provide the actual render and state (offline snapshot vs online DTO).
 */
public abstract class BaseGamePanel extends JPanel {
    protected final JButton resumeButton  = new JButton("Resume");
    protected final JButton restartButton = new JButton("Restart");
    protected final JButton exitButton    = new JButton("Exit to Menu");
    protected final JSlider timeSlider    = new JSlider(0, 30, 0); // offline only; hidden online

    private Runnable onExitToMenu;

    protected BaseGamePanel() {
        setLayout(new OverlayLayout(this));
        setDoubleBuffered(true);
        setFocusable(true);

        // overlay buttons (initially hidden)
        exitButton.setVisible(false);    exitButton.setFocusable(false);
        resumeButton.setVisible(false);  resumeButton.setFocusable(false);
        restartButton.setVisible(false); restartButton.setFocusable(false);

        add(exitButton);
        add(resumeButton);
        add(restartButton);

        add(timeSlider); // subclass may show/hide/position
    }

    public void bindExitToMenu(Runnable r) {
        this.onExitToMenu = r;
        exitButton.addActionListener(e -> onExitToMenu.run());
    }

    /** Subclasses decide how to paint (snapshot vs dto). */
    protected abstract void renderGame(Graphics2D g2);

    /** Subclasses may expose these flags from their state. Defaults are safe. */
    protected boolean isGameOver() { return false; }
    protected boolean isViewOnly() { return false; }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        renderGame(g2);

        if (isGameOver()) drawGameOverOverlay(g2);
        layoutOverlayButtons();
    }

    private void drawGameOverOverlay(Graphics2D g) {
        g.setColor(new Color(255, 0, 0, 100));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(getFont().deriveFont(Font.BOLD, 22f));
        g.drawString("GAME OVER — You can still review the timeline.", getWidth()/2 - 220, 40);
    }

    private void layoutOverlayButtons() {
        // Keep default hidden; subclasses toggle visibility as needed
        int bw = 120, bh = 40;
        int x = getWidth()/2 - bw/2;
        int y = getHeight()/2 + 10;
        resumeButton.setBounds(x, y, bw, bh);
        restartButton.setBounds(x, y + bh + 10, bw, bh);
        exitButton.setBounds(x, y + 2*bh + 20, bw, bh);
    }

    @Override public Dimension getPreferredSize() {
        // Delegate to GameConfig via GameUi to ensure consistent sizing
        return GameUi.gameWindowSize();
    }


}