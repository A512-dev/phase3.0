package com.mygame.core;

import com.mygame.engine.physics.Vector2D;

import java.awt.*;

/** Immutable bundle of all tunable constants. */
public final class GameConfig {

    public static final double FORWARD_RECOVER_ACCEL       = 40.0; // px/s²  push to restart
    public static final double FORWARD_RECOVER_MIN_SPEED   = 16.0;  // px/s   target “rolling” speed
    public static final double FORWARD_RECOVER_LATERAL_DAMP= 0.90; // damp sideways during restart

    // GameConfig.java (near physics tunables)
    public static final double BACKWARD_BRAKE_PER_S = 12.0; // decay rate for opposite motion
    public static final double BACKWARD_MAX_SPEED   = 24.0; // cap when moving opposite (px/s)
    public static final double BACKWARD_LATERAL_DAMP= 0.85; // damp sideways when going opposite


    public static final int CONFIDENTIAL_SMALL_PACKET_LIFE = 16;
    public static final int CONFIDENTIAL_SMALL_PACKET_COIN_VALUE = 3;
    public static final double CSP_APPROACH_RADIUS = 120.0;     // start slowing within this many px
    public static final double CSP_SLOW_FACTOR     = 0.35;      // slow to 35% of base when busy
    public static final double CSP_SOFTSTOP_MARGIN = 8.0;       // keep at least this far outside ARRIVE_R
    public static final double CSP_LERP_RATE       = 12.0;      // smoothing (1/s) for speed change


    public static final int CONFIDENTIAL_LARGE_PACKET_LIFE = 24;
    public static final int CONFIDENTIAL_LARGE_PACKET_COIN_VALUE = 4;
    // ConfidentialLargePacket spacing control
    public static final double CLP_TARGET_GAP       = 9.0;  // desired min spacing along wire (px)
    public static final double CLP_MAX_SPEED_FWD    = 28.0; // cap forward speed when adjusting (px/s)
    public static final double CLP_MAX_SPEED_BACK   = 18.0; // cap backward speed when adjusting (px/s)
    public static final double CLP_LERP_RATE        = 1.0;  // speed smoothing rate (1/s)

    public static final double STRENGTH_OF_WAVE = 0.500;
    public static final double RADIUS_OF_WAVE = 30.0;
    public static final int CONFIDENTIAL_LARGE_PACKET_SIZE = 10;

    public static final double SPEED_OF_SQUARE_PACKET_SQUARE_PORT = 30.0;
    public static final double SPEED_OF_SQUARE_PACKET_TRIANGLE_PORT = 15.0;

    public static final int CONFIDENTIAL_SMALL_PACKET_SIZE = 15;

    public static final double INF_LAUNCH_SPEED        = 90;   // سرعت شروع از پورت سازگار
    public static final double INF_COMPAT_ACCEL        = 5;  // شتاب روی مسیرِ رو به پورت سازگار
    public static final double INF_COMPAT_VMAX         = 220;  // سقف سرعت در حالت سازگار
    public static final double INF_INCOMPAT_DECEL      = 5;  // شتاب منفی به‌سمت پورت ناسازگار
    public static final double INF_MIN_SPEED         = 40;   // کف سرعت در حالت ناسازگار
    public static final double INF_COLLISION_COOLDOWN  = 0.25; // اختیاری: جلوگیری از پینگ‌پنگ برخوردها


    public static final int BULK_PACKET_A_LIFE = 8;
    public static final int BULK_PACKET_A_PAYLOAD = 8;
    public static final int BULK_PACKET_B_LIFE = 10;
    public static final int BULK_PACKET_B_PAYLOAD = 10;

    public static final int BIT_PACKET_PAYLOAD = 1;
    public static final int BIT_PACKET_LIFE = 2;


    public static final int MAX_QUEUE = 5;
    public static final double SPEED_OF_TRIANGLE_PACKET_TRIANGLE_PORT = 20.0;
    public static final double SPEED_OF_TRIANGLE_PACKET_SQUARE_PORT = 20.0;
    public static final double ACCEL_OF_TRIANGLE_PACKET_SQUARE_PORT = 1.5;
    public static final double SPEED_OF_CONFIDENTIAL_SMALL_PACKET = 25;
    /** bulk packets */
    public static final double SPEED_OF_BULKPACKET_A_PACKET = 20;
    public static final double SPEED_OF_BULKPACKET_B_PACKET = 15;
    public static final double BULK_B_SPEED                 = 15;  // px/s constant forward speed
    public static final double BULK_B_DEVIATION_AMPL        = 5;   // px lateral amplitude
    public static final double BULK_B_DEVIATION_WAVELENGTH  = 160;  // px per cycle along s
    public static final double BULK_B_LATERAL_TRACK_RATE    = 18;   // 1/s how fast it “locks” to target offset
    public static final double BULK_A_ACCEL_CURVE = 5;
    public static final double BULK_A_VMAX_CURVE = 100;
    public static final double SPEED_OF_TROJAN_PACKET = SPEED_OF_SQUARE_PACKET_TRIANGLE_PORT / 1.5;

    public static double bulkPacketSize = 10;


    public static final double CURVE_ZONE_RADIUS = 8;



    public static double squareSize = 10;
    public static double triangleSize = 10;
    public static double infinitySize = 10;

    /** bit packet */
    public static double bitPacketLife = 4;
    public static double bitPacketSize = 10;



    public static double trojanPacketSize = 10;
    public static double SPEED_OF_CONFIDENTIAL_Large_PACKET = 15;
    public static double timeCooldownPort = 1;
    public static double distanceOfAntiTrojanNodeToWork = 150;


    /* --- node geometry --- */
    public final int nodeWidth           = 90;
    public final int nodeHeight          = 150;
    public final int portSize            = 10;
    public final int portClickRadius     = 8;

    /* --- wiring / gameplay --- */
    public final double maxWireLength    = 20_000;
    public final double maxDistanceOffTrack = 50;

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
    public static final int squareLife   = 8;
    public static final int triangleLife = 12;
    public static double infinityLife = 4;

    /* --- packet counts & runs --- */
    public final int    numberOfRuns           = 1;
    public final int numberOfPacketsLevel1  = 6;
    public final double numberOfPacketsLevel2  = 40.0;
    public final double noiseDelta             = 0.2;


    /* --- speed --- */
    public double speedOfPackets                = 50;
    public double timeMultiplier = 1;
    public final double fastGameSpeed          = 5.0;

    public static final double thresholdToReachPort = 8;
    public double cellSize = 50;
    public double limitForCollision = 6;
    public double packetSize = 8;
    public double antiTrojanNodeWidth = nodeWidth/2;
    public double antiTrojanNodeHeight = nodeHeight/2;
    public double bulkPacketLife = 32;
    public int bulkPacketPayLoad = 32;
    public double spyNodeWidth = nodeWidth/1.5;
    public double spyNodeHeight = nodeHeight/1.5;

    public static Dimension level1Size = new Dimension(800, 600);
    public static Dimension level2Size = new Dimension(800, 600);
    public static Dimension level3Size = new Dimension(800, 600);
    public static Dimension level4Size = new Dimension(1000, 650);
    public static Dimension level5Size = new Dimension(800, 600);



    /* --- convenience factory --- */
    public static GameConfig defaultConfig() { return new GameConfig(); }



    private GameConfig() {}          // use defaultConfig()
}
