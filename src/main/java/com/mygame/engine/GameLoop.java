package com.mygame.engine;

import com.mygame.snapshot.WorldSnapshot;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public final class GameLoop implements Runnable {
    private static final double STEP = 1.0 / 120.0;   // 120 UPS
    private static final double MAX_FRAME_TIME = 0.25; // clamp
    private volatile boolean running;
    private double speed = 1.0;        // 1× normal, >1 fast-forward
    private final WorldController ctrl;
    private final Consumer<WorldSnapshot> onFrame; // supplied by GamePanel
    public GameLoop(WorldController worldController, Consumer<WorldSnapshot> onFrame) {
        this.ctrl   = worldController;
        this.onFrame = onFrame;
    }
    @Override public void run() {
        double acc = 0;
        long prev = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double frameTime = Math.min(MAX_FRAME_TIME,
                    (now - prev) / 1_000_000_000.0) * speed;
            acc += frameTime;
            prev = now;

            while (acc >= STEP) {
                ctrl.tick(STEP);   // deterministic
                acc -= STEP;
            }
            onFrame.accept(ctrl.snapshot()); // immutable DTO
            LockSupport.parkNanos(1_000_000); // ~1 ms, lets EDT breathe
        }
    }
    public void setSimulationSpeed(double s) { speed = s; }
    public void jumpTo(double t) { /* reset + fast-forward as described */ }
    // inside com.mygame.engine.GameLoop
    public void stop() {
        running = false;                 // flag checked in run() loop
    }

    public void start() {
        running = true;
    }
}
