package client.ui.gamePanels;

import client.net.NetClient;

import shared.net.ClientCommand;
import shared.net.MessageType;
import shared.ser.Json;
import shared.snapshot.WorldSnapshot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Thin viewer for ONLINE mode. */
public final class OnlineGamePanel extends BaseGamePanel {

    private final NetClient net;
    private volatile WorldSnapshot frame; // last server frame

    public OnlineGamePanel(NetClient net, Runnable exitToMenu) {
        super();
        this.net = net;

        // hide offline-only widgets
        timeSlider.setVisible(false);

        bindExitToMenu(exitToMenu);
        wireNetworkHandlers();
        initInputListeners();

        SwingUtilities.invokeLater(this::requestFocusInWindow);
    }

    private void wireNetworkHandlers() {

        net.onMessage((type, payload) -> {
            System.out.println("Client got: " + type);

            if (type == MessageType.FRAME) {
                WorldSnapshot dto = Json.from(payload, WorldSnapshot.class);
                this.frame = dto;
                SwingUtilities.invokeLater(this::repaint);
            }
        });


    }

    @Override protected void renderGame(Graphics2D g2) {
        // Replace with your WorldView adapter that can draw from DTOs directly.
        if (frame == null) {
            g2.setColor(Color.GRAY);
            g2.drawString("Waiting for server frames…", 20, 30);
        } else {
            // TODO: renderer.renderAll(g2, adapter.fromDTO(frame))
            g2.setColor(Color.WHITE);
            g2.drawString("t=" + frame.hud().gameTimeSec() + "s", 20, 30);
            if (frame.isGameOver()) g2.drawString("GAME OVER", 20, 50);
        }
    }

    @Override protected boolean isGameOver() { return frame != null && frame.isGameOver(); }
    @Override protected boolean isViewOnly() { return frame != null && frame.isViewOnly(); }

    /* ---------------- input -> InputCommand -> server ---------------- */

    private void initInputListeners() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { sendMouse("MOUSE_DOWN", e); }
            @Override public void mouseReleased(MouseEvent e) { sendMouse("MOUSE_UP", e); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { sendMouse("MOUSE_MOVE", e); }
            @Override public void mouseDragged(MouseEvent e) { sendMouse("MOUSE_DRAG", e); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { sendKey("KEY_DOWN", e); }
            @Override public void keyReleased(KeyEvent e) { sendKey("KEY_UP", e); }
        });
    }


    private void sendMouse(String kind, MouseEvent e) {
        ClientCommand cmd = ClientCommand.mouse(kind, e.getX(), e.getY());
        net.send(MessageType.INPUT_COMMAND, Json.to(cmd));
    }

    private void sendKey(String kind, KeyEvent e) {
        ClientCommand cmd = ClientCommand.key(kind, e.getKeyCode());
        net.send(MessageType.INPUT_COMMAND, Json.to(cmd));
    }
}