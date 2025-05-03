// GameLoop.java
package com.mygame.engine;

import javax.swing.SwingUtilities;
import com.mygame.ui.GamePanel;

public class GameLoop {
    private final GamePanel panel;

    private Thread logicThread;
    private Thread renderThread;
    private volatile boolean running = false;

    /**
     * ups: updates per second for your game logic
     * fps: frames per second for repainting
     */
    private final double ups, fps;

    public GameLoop(GamePanel panel, double ups, double fps) {
        this.panel = panel;
        this.ups   = ups;
        this.fps   = fps;
    }

    public void start() {
        if (running) return;
        running = true;

        // Logic Thread
        logicThread = new Thread(() -> {
            final long nsPerUpdate = (long)(1_000_000_000.0 / ups);
            long lastTime = System.nanoTime();
            while (running) {
                long now = System.nanoTime();
                long elapsed = now - lastTime;
                //                if (elapsed >= nsPerUpdate) {
//                    double dt = elapsed / 1_000_000_000.0;
//                    panel.updateLogic(dt);
//                    lastTime = now;
//                } else {
                if (elapsed >= nsPerUpdate) {
                    double realDt = elapsed / 1_000_000_000.0;

                    panel.getWorld().getTimeController().updateRealTime(realDt);
                    double gameDt = panel.getWorld().getTimeController().getDeltaSeconds();

                    if (gameDt > 0) {
                        panel.updateLogic(gameDt);
                    }


                    lastTime = now;
                } else {
                    long sleepNanos = nsPerUpdate - elapsed;
                    try {
                        Thread.sleep(
                                sleepNanos / 1_000_000,
                                (int)(sleepNanos % 1_000_000)
                        );
                    } catch (InterruptedException ignored) {}
                }
            }
        }, "LogicThread");

        // Render Thread

        renderThread = new Thread(() -> {
            final long sleepMillis = (long)(1000.0 / fps);
            while (running) {
                z++;
                if (z%60==0)
                    System.out.println("hud="+panel.getWorld().getHudState().getGameTime()+" Target="+panel.getWorld().getTimeController().getTargetTime());
                SwingUtilities.invokeLater(panel::repaint);
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ignored) {}
            }
        }, "RenderThread");

        logicThread.start();
        renderThread.start();
    }
    int z = 0;
    public void stop() {
        running = false;
        try {
            if (logicThread != null) logicThread.join();
            if (renderThread != null) renderThread.join();
        } catch (InterruptedException ignored) {}
    }

}
