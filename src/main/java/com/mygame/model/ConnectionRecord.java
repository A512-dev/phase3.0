package com.mygame.model;

public class ConnectionRecord {
    final int fromNodeIndex, fromPortIndex;

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

    final int   toNodeIndex,   toPortIndex;
    ConnectionRecord(int fn, int fp, int tn, int tp) {
        this.fromNodeIndex = fn;
        this.fromPortIndex = fp;
        this.toNodeIndex   = tn;
        this.toPortIndex   = tp;
    }
}