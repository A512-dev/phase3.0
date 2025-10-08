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

    private final client.view.WorldView renderer = new client.view.WorldView(); // reuse


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
                //System.out.println("[CLIENT] FRAME received; assigning to field");



                this.frame = shared.ser.Json.from(payload, WorldSnapshot.class);
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
            renderer.renderAll(g2, frame);
        }
    }

    @Override protected boolean isGameOver() { return frame != null && frame.gameOver(); }
    @Override protected boolean isViewOnly() { return frame != null && frame.viewOnlyMode(); }

    /* ---------------- input -> InputCommand -> server ---------------- */

// OnlineGamePanel.java  – inside initInputListeners()

    private void initInputListeners() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                System.out.println("[CLIENT] mousePressed btn="+e.getButton());
                if (SwingUtilities.isLeftMouseButton(e))  sendMouse("MOUSE_DOWN_LEFT", e);
                else if (SwingUtilities.isRightMouseButton(e)) sendMouse("MOUSE_DOWN_RIGHT", e);
            }
            @Override public void mouseReleased(MouseEvent e) {
                System.out.println("[CLIENT] mouseReleased btn="+e.getButton());

                // we only need where you released for connecting; button not required
                sendMouse("MOUSE_UP", e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e)  { sendMouse("MOUSE_MOVE", e); }
            @Override public void mouseDragged(MouseEvent e){ sendMouse("MOUSE_DRAG", e); }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                System.out.println("[CLIENT] keyPressed " + e.getKeyCode());
                sendKey("KEY_DOWN", e);
            }
            @Override public void keyReleased(KeyEvent e) {
                System.out.println("[CLIENT] keyReleased " + e.getKeyCode());
                sendKey("KEY_UP", e);
            }
        });
    }


    // client.ui.gamePanels.OnlineGamePanel
    private void sendMouse(String kind, MouseEvent e) {
        // Prefer a factory that captures button & modifiers; if you don't have one, add it.
        ClientCommand cmd = ClientCommand.mouse(kind, e.getX(), e.getY(),
                e.getButton(),
                e.isShiftDown(),
                e.isControlDown(),
                e.isAltDown());
        net.send(MessageType.INPUT_COMMAND, Json.to(cmd));
    }



    private void sendKey(String kind, KeyEvent e) {
        ClientCommand cmd = ClientCommand.key(kind, e.getKeyCode());
        System.out.printf("[CLIENT->NET] %s keyCode=%d%n", kind, e.getKeyCode());
        net.send(MessageType.INPUT_COMMAND, Json.to(cmd));
    }
}