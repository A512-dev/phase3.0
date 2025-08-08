package com.mygame.core;

import com.mygame.engine.physics.Vector2D;

/** Immutable bundle of all tunable constants. */
public final class GameConfig {

    /* --- node geometry --- */
    public final int nodeWidth           = 90;
    public final int nodeHeight          = 150;
    public final int portSize            = 10;
    public final int portClickRadius     = 8;

    /* --- wiring / gameplay --- */
    public final double maxWireLength    = 2_000;
    public final double maxDistanceOffTrack = 10;

    /* --- simulation --- */
    public final double ups              = 120;
    public final double fps              = 60;

    public final Vector2D basePacketVel  = new Vector2D(20, 0);

    /* --- physics --- */
    public final double impactRadius         = 60;
    public final double impulseForceSq       = 1.7;
    public final double impulseForceTri      = 1.2;
    public final double packetMaxSpeed       = 120;
    public final double pathCorrectionStr    = 90;
    public final double impulseDecay         = 3.0;

    /* --- triangle-specific --- */
    public final double triangleBaseImpulse  = 1.5;
    public final double triangleAccDecay     = 60;

    /* --- queue & port timings --- */
    public final int    portCooldownSec      = 3;
    public final int    maxQueueSize         = 5;

    /* --- life / health --- */
    public static final int squareLife   = 10;
    public final int triangleLife = 3;

    /* --- packet counts & runs --- */
    public final int    numberOfRuns           = 1;
    public final int numberOfPacketsLevel1  = 2;
    public final double numberOfPacketsLevel2  = 40.0;
    public final double noiseDelta             = 0.2;
    public final double fastGameSpeed          = 5.0;

    /* --- speed --- */
    public double speedOfPackets                = 50;
    public double timeMultiplier = 1;
    public static final double thresholdToReachPort = 8;
    public double cellSize = 50;
    public double limitForCollision = 6;
    public double packetSize = 8;

    /* --- convenience factory --- */
    public static GameConfig defaultConfig() { return new GameConfig(); }

    private GameConfig() {}          // use defaultConfig()
}
