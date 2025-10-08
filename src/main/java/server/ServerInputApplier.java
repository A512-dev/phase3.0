// server/ServerInputApplier.java
package server;

import shared.Vector2D;
import shared.net.ClientCommand;
import server.sim.engine.world.World;
import server.sim.model.Connection;
import server.sim.model.Port;
import shared.model.PortDirection;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

final class ServerInputApplier {
    private final World world;

    // draft state (server-authoritative)
    private Port draftFrom = null;
    private Port draftToCandidate = null;
    private Vector2D draftMouse = null;
    private final List<Vector2D> draftBends = new ArrayList<>();

    // bend-drag state on existing connection
    private Connection dragConn = null;
    private int dragBend = -1;

    ServerInputApplier(World world){ this.world = world; }

    // server/ServerInputApplier.java
    void apply(ClientCommand cmd) {
        System.out.printf("[APPLY] %s  x=%.1f y=%.1f btn=%d%n", cmd.kind, cmd.x, cmd.y, cmd.button);

        String k = cmd.kind;
        if (k.startsWith("MOUSE_DOWN")) { onMouseDown(cmd); return; }
        if (k.startsWith("MOUSE_DRAG")) { onMouseDrag(cmd); return; }
        if (k.startsWith("MOUSE_MOVE")) { onMouseMove(cmd); return; }
        if (k.startsWith("MOUSE_UP"))   { onMouseUp(cmd);   return; }
        if (k.equals("KEY_DOWN"))       { onKey(cmd.keyCode); return; } // only on DOWN to avoid double-toggles
        if (k.equals("KEY_UP"))         { return; }

        System.out.println("[APPLY] unknown kind: " + cmd.kind);
    }


    /* ───────────── mouse handlers ───────────── */

    private void onMouseDown(ClientCommand c){
        System.out.printf("[DOWN] btn=%d at(%.1f, %.1f)%n", c.button, c.x, c.y);
        Vector2D p = new Vector2D(c.x, c.y);
        if (isRight(c)) { System.out.println("[DOWN] right-click"); /* ... */ return; }

        Port hit = world.findPortAtPosition(p);
        System.out.println("[DOWN] hit port: " + hit);
        if (hit == null) return;

        if (hit.getDirection() != PortDirection.OUTPUT) {
            System.out.println("[DOWN] not an OUTPUT port, ignoring");
            return;
        }

        if (hit.getConnectedPort() != null) world.disconnectPort(hit);

        draftFrom = hit;
        draftBends.clear();
        draftMouse = p.copy();
        System.out.println("[DOWN] draftFrom set: " + hit + " bends=0");
    }

    private void onMouseUp(ClientCommand c){
        System.out.printf("[UP] btn=%d at(%.1f, %.1f)%n", c.button, c.x, c.y);

        if (!isLeft(c)) return;
        if (draftFrom == null) { System.out.println("[UP] no draftFrom — ignoring"); return; }

        Vector2D p = new Vector2D(c.x, c.y);
        Port end = world.findPortAtPosition(p);
        System.out.println("[UP] endPort=" + end);

        if (end != null && isValidPair(draftFrom, end)) {
            Connection conn = new Connection(draftFrom, end, new ArrayList<>(draftBends));
            System.out.println("[UP] try add conn: bends=" + draftBends.size());
            if (conn.isValidAgainst(world.getNodes())) {
                world.addConnection(conn);
                System.out.println("[UP] ADDED. total connections=" + world.getConnections().size());
                world.captureInitialSnapshot();
            } else {
                System.out.println("[UP] REJECTED: intersects a node");
            }
        } else {
            System.out.println("[UP] invalid port pair or null end");
        }

        draftFrom = null;
        draftBends.clear();
        draftMouse = null;
    }


    private void onMouseDrag(ClientCommand c){
        if (draftFrom != null) System.out.printf("[DRAG] draft from %s -> (%.1f, %.1f)%n", draftFrom, c.x, c.y);
        Vector2D p = new Vector2D(c.x, c.y);

        // dragging an existing bend?
        if (dragConn != null) {
            if (dragBend >= 0 && dragBend < dragConn.getBends().size())
                dragConn.moveBend(dragBend, p);
            return;
        }

        // live update draft mouse
        if (draftFrom != null) draftMouse = p;
    }

    private void onMouseMove(ClientCommand c){
        // keep the “ghost” wire following the cursor
        if (draftFrom != null) draftMouse = new Vector2D(c.x, c.y);
    }


    private void onKey(int keyCode){
        var tc = world.getTimeController();
        System.out.println("[KEY] code=" + keyCode +
                " paused=" + tc.isPaused() +
                " waiting=" + tc.isWaitingToStart());

        if (keyCode == KeyEvent.VK_SPACE) {
            if (tc.isWaitingToStart()) {
                tc.startFromFreeze();   // start at t=0
                // If your game starts frozen-at-0, also unfreeze:
                if (tc.isFrozen()) tc.toggleFrozen();
                System.out.println("[KEY] SPACE -> startFromFreeze");
            } else {
                tc.toggleFrozen();
                System.out.println("[KEY] SPACE -> toggleFrozen now=" + tc.isFrozen());
            }
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            tc.togglePause();
            System.out.println("[KEY] ESC -> togglePause now=" + tc.isPaused());
        }
    }

    /* ───────────── helpers (same math as offline) ───────────── */

    private boolean isLeft(ClientCommand c){ return c.button == 1 || c.button == 0; }
    private boolean isRight(ClientCommand c){ return c.button == 3; }

    private boolean isValidPair(Port a, Port b){
        if (a == b) return false;
        // same rule you used offline: just be different directions
        if (a.getDirection() == b.getDirection()) return false;
        // and match types
        return a.getType() == b.getType();
    }

    private List<Vector2D> buildDraftPath(Vector2D tail){
        List<Vector2D> path = new ArrayList<>();
        if (draftFrom != null) path.add(draftFrom.getCenter());
        path.addAll(draftBends);
        path.add(tail != null ? tail : draftMouse);
        return path;
    }

    private boolean clickIsOnPath(List<Vector2D> pts, Vector2D p){
        if (pts == null || pts.size() < 2) return false;
        final double THICK = 6.0;
        for (int i = 0; i < pts.size() - 1; i++)
            if (Vector2D.distPointToSegment(p, pts.get(i), pts.get(i+1)) <= THICK)
                return true;
        return false;
    }

    private int segmentIndexAtClick(List<Vector2D> pts, Vector2D p){
        if (pts == null || pts.size() < 2) return -1;
        for (int i = 0; i < pts.size() - 1; i++)
            if (Vector2D.distPointToSegment(p, pts.get(i), pts.get(i+1)) <= 6.0)
                return i;
        return -1;
    }

    private int bendIndexHit(Connection c, Vector2D p){
        return indexOfClickedBend(c.getBends(), p);
    }
    private boolean isClickOnExistingBend(List<Vector2D> bends, Vector2D p){
        return indexOfClickedBend(bends, p) != -1;
    }
    private int indexOfClickedBend(List<Vector2D> bends, Vector2D p){
        final double R = 8.0;
        for (int i = 0; i < bends.size(); i++)
            if (bends.get(i).distanceTo(p) <= R) return i;
        return -1;
    }
}
