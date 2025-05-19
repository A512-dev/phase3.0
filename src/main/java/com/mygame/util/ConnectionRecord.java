package com.mygame.util;

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
    public ConnectionRecord(int fn, int fp, int tn, int tp) {
        this.fromNodeIndex = fn;
        this.fromPortIndex = fp;
        this.toNodeIndex   = tn;
        this.toPortIndex   = tp;
    }
}