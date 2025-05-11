package com.mygame.model;

public class ConnectionRecord {
    final int fromNodeIndex, fromPortIndex;
    final int   toNodeIndex,   toPortIndex;
    ConnectionRecord(int fn, int fp, int tn, int tp) {
        this.fromNodeIndex = fn;
        this.fromPortIndex = fp;
        this.toNodeIndex   = tn;
        this.toPortIndex   = tp;
    }
}