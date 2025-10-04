package server.sim.engine;

import server.sim.engine.world.WorldController;
import server.sim.snapshot.WorldSnapshot;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public final class GameLoop implements Runnable {
    private static final double STEP = 1.0 / 120.0;   // 120 UPS
    private static final double MAX_FRAME_TIME = 0.25;

    private volatile boolean running;
    private double speed = 1.0;

    private final WorldController ctrl;
    private final Consumer<WorldSnapshot> onFrame;

    // 🔹 NEW: task queue executed on the loop thread
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public GameLoop(WorldController worldController, Consumer<WorldSnapshot> onFrame) {
        this.ctrl = worldController;
        this.onFrame = onFrame;
    }

    /** Schedule a task to run on the game-loop thread (async). */
    public void post(Runnable r) {
        if (r != null) tasks.add(r);
    }

    /** Schedule a task and wait until it finishes on the game-loop thread. */
    public void postBlocking(Runnable r) {
        if (r == null) return;
        CountDownLatch latch = new CountDownLatch(1);
        tasks.add(() -> {
            try { r.run(); } finally { latch.countDown(); }
        });
        try { latch.await(); } catch (InterruptedException ignored) {}
    }

    @Override
    public void run() {
        double acc = 0;
        long prev = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            double frameTime = Math.min(MAX_FRAME_TIME, (now - prev) / 1_000_000_000.0) * speed;
            prev = now;
            acc += frameTime;

            // 🔹 Drain UI-posted tasks BEFORE ticking the world
            for (Runnable r; (r = tasks.poll()) != null; ) {
                try { r.run(); } catch (Throwable t) { t.printStackTrace(); }
            }

            // 🔹 Fixed-step simulation
            while (acc >= STEP) {
                try { ctrl.tick(STEP); }
                catch (Throwable t) { t.printStackTrace(); }  // don't let the loop die
                acc -= STEP;
            }

            // 🔹 Snapshot for rendering / saving
            try { onFrame.accept(ctrl.snapshot()); }
            catch (Throwable t) { t.printStackTrace(); }

            // small park to let EDT breathe
            LockSupport.parkNanos(1_000_000);
        }
    }

    public void setSimulationSpeed(double s) { speed = s; }

    /** Optional: you can implement a fast-forward here using postBlocking if needed. */
    public void jumpTo(double t) { /* not used anymore by UI; handled on UI via postBlocking */ }

    public void stop() { running = false; }
    public void start() { running = true; }
}
