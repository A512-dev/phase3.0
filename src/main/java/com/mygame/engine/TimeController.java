//// TimeController.java
//package com.mygame.engine;
//
//public class TimeController {
//    private static double realTimeAccumulator = 0;
//    private static double targetTime = -1;  // if >=0, we’re jumping to this timestamp
//    private static boolean paused = false;
//    private static double timeMultiplier = 1.0;
//    private static boolean waitingToStart = true;
//
//    public void startFromFreeze() {
//        waitingToStart = true;
//    }
//
//    public boolean isWaitingToStart() {
//        return waitingToStart;
//    }
////
//    public void waitToStart() {
//        waitingToStart = true;
//    }
//
//    public void setTimeMultiplier(double multiplier) {
//        this.timeMultiplier = multiplier;
//    }
//    private static double dtForFrame() {
//        return 1.0 / 60.0;
//    }
//
//    /** Called each loop with real‐world Δt */
//    public static void updateRealTime(double dt) {
//        if (paused) return;
//        if (targetTime >= 0) {
//            realTimeAccumulator = targetTime;
//            targetTime = -1;
//        } else {
//            realTimeAccumulator += dt;
//        }
//    }
//
//    /** Returns the “game” Δt to use when updating objects */
//    public static double getDeltaSeconds() {
//        if (waitingToStart) return 0;
//        if (paused) return 0;
//        if (targetTime >= 0) return 0;
//
//        return dtForFrame() * timeMultiplier;
//    }
//
//
//
//    /** Jump to an absolute game‐time (e.g. slider) */
//    public void jumpTo(double seconds) {
//        this.targetTime = seconds;
//    }
//
//    public boolean isPaused() {
//        return paused;
//    }
//
//    public void togglePause() {
//        paused = !paused;
//    }
//}
package com.mygame.engine;

public class TimeController {
    private double realTimeAccumulator = 0;
    private double targetTime = -1;
    private boolean paused = false;
    private double timeMultiplier = 1.0;
    private boolean waitingToStart = true;
    private boolean frozen = true;
    public void toggleFrozen() {
        frozen = !frozen;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void updateRealTime(double dt) {
        if (paused) return;
//        if (targetTime >= 0) {
//            realTimeAccumulator = targetTime;
//            targetTime = -1;
//        } else {
//            realTimeAccumulator += dt;
//        }
        realTimeAccumulator += dt;
    }

    public double getDeltaSeconds() {
        if (waitingToStart || paused || frozen) return 0;
        return dtForFrame() * timeMultiplier;
    }

    private double dtForFrame() {
        return 1.0 / 60.0;
    }

    public void jumpTo(double seconds) {
        this.targetTime = seconds;
    }

    public double getTargetTime() {
        return targetTime;
    }

    public void stopJump() {
        this.targetTime = -1;
    }

    public boolean isPaused() {
        return paused;
    }

    public void togglePause() {
        paused = !paused;
    }

    public void setTimeMultiplier(double multiplier) {
        this.timeMultiplier = multiplier;
    }

    public void waitToStart() {
        waitingToStart = true;
    }

    public void startFromFreeze() {
        waitingToStart = false;
    }

    public boolean isWaitingToStart() {
        return waitingToStart;
    }
}
