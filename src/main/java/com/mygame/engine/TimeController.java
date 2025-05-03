// TimeController.java
package com.mygame.engine;

public class TimeController {
    private static double realTimeAccumulator = 0;
    private static double targetTime = -1;  // if >=0, we’re jumping to this timestamp
    private static boolean paused = false;
    private static double timeMultiplier = 1.0;

    public void setTimeMultiplier(double multiplier) {
        this.timeMultiplier = multiplier;
    }
    private static double dtForFrame() {
        return 1.0 / 60.0;
    }

    /** Called each loop with real‐world Δt */
    public static void updateRealTime(double dt) {
        if (paused) return;
        if (targetTime >= 0) {
            realTimeAccumulator = targetTime;
            targetTime = -1;
        } else {
            realTimeAccumulator += dt;
        }
    }

    /** Returns the “game” Δt to use when updating objects */
    public static double getDeltaSeconds() {
        if (paused) {
            return 0;
        }

        if (targetTime >= 0) {
            // We're currently fast-forwarding toward a target time
            return 0; // No game delta this frame (used only to track real time)
        }

        return dtForFrame() * timeMultiplier;
    }


    /** Jump to an absolute game‐time (e.g. slider) */
    public void jumpTo(double seconds) {
        this.targetTime = seconds;
    }

    public boolean isPaused() {
        return paused;
    }

    public void togglePause() {
        paused = !paused;
    }
}
