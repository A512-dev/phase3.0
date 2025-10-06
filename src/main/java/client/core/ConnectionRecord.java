package client.core;

import shared.Vector2D;

import java.util.List;

public class ConnectionRecord {
    public final int fromNodeIndex;
    public final int fromPortIndex;

    public int getFromNodeIndex() {
        return fromNodeIndex;
    }

    public int getFromPortIndex() {
        return fromPortIndex;
    }

    public int getToNodeIndex() {
        return toNodeIndex;
    }

    public int getToPortIndex() {
        return toPortIndex;
    }

    public final int   toNodeIndex;
    public final int toPortIndex;

    public List<Vector2D> getBends() {
        return bends;
    }

    public void setBends(List<Vector2D> bends) {
        this.bends = bends;
    }

    public List<Vector2D> bends;
    public ConnectionRecord(int fn, int fp, int tn, int tp, List<Vector2D> bends) {
        this.fromNodeIndex = fn;
        this.fromPortIndex = fp;
        this.toNodeIndex   = tn;
        this.toPortIndex   = tp;
        setBends(bends);
    }
}