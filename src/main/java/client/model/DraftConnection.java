package client.model;

import shared.Vector2D;
import server.sim.model.Port;

import java.util.ArrayList;
import java.util.List;

public class DraftConnection {
    public Port getFrom() {
        return from;
    }

    private Port from;
    private Port toCandidate;  // ممکن است هنوز null باشد

    public Vector2D getMousePos() {
        return mousePos;
    }

    private Vector2D mousePos;
    private List<Vector2D> bends = new ArrayList<>();

    public DraftConnection(Port from) {
        this.from = from;
    }

    public void setMouse(Vector2D pos) {
        this.mousePos = pos;
    }

    public void setToCandidate(Port to) {
        this.toCandidate = to;
    }

    public void addBend(Vector2D pos) {
        bends.add(pos.copy());
    }

    public void removeBend(int index) {
        if (index >= 0 && index < bends.size()) bends.remove(index);
    }

    public List<Vector2D> getBends() {
        return bends;
    }

    public List<Vector2D> getPath() {
        List<Vector2D> path = new ArrayList<>();
        path.add(from.getCenter());
        path.addAll(bends);
        path.add((toCandidate != null) ? toCandidate.getCenter() : mousePos);
        return path;
    }

    public boolean isComplete() {
        return toCandidate != null;
    }
}
