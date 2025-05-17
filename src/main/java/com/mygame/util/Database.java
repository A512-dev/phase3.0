package com.mygame.util;

public class Database {
    public static final int widthNodes = 90;
    public static final int lengthNodes = 150;
    public static final int PORT_SIZE = 10;
    public static final int maxDistanceToBeOfTheLine = 10;
    public static final double MAX_WIRE_LENGTH = 2000; // limit maximum length
    public static final double PORT_CLICK_RADIUS = 8.0; // Radius of clickable area
    public static final Vector2D baseVelocityOfPackets = new Vector2D(30, 0);
    public static final double ups=120, fps=60;
    public static final double THRESHOLD_FOR_REACHING_PORT = 3;
    public static final double TRIANGLE_BASE_IMPULSE = 1.5;
    public static final double IMPULSE_DECAY = 3.0;
    public static final double ACC_DECAY = 200;
    public static final int NUMBER_OF_RUNS = 1;
    public static final double NUMBER_OF_PACKETS_LEVEL1 = 5.0;
    public static final double NUMBER_OF_PACKETS_LEVEL2 = 40.0;
    public static final double NOISE_DELTA = 0.2;

    /* … existing constants … */
    public static final double IMPULSE_FORCE           = 4;  // ↓ from 0.5
    public static final double IMPULSE_FORCE_TRIANGLE  = 2;  // ↓ from 0.1


    public static final double PATH_CORRECTION_STRENGTH = 60.0;  // how quickly packets correct toward path
    public static final double PACKET_MAX_SPEED = 120.0;         // maximum speed allowed for packets after collision

    public static final double IMPACT_RADIUS = 60;
    public static final double CELL_SIZE = 50;
    public static final int SQUARE_LIFE = 2;
    public static final int TRIANGLE_LIFE = 3;
    public static final double MAX_KNOCKBACK_SPEED = 300;
    public static final double LIMIT_FOR_COLLISION = 6;
    public static final double TRIANGLE_ACC_DECAY = 60;
    public static double timeMultiplier = 1;
    public static double fastGameSpeed = 5;
    public static final int timeSendFromPort = (int) (3); // seconds
    public static final int MAX_QUEUE_SIZE = 5;


    public static double speedOfPackets = 50;
}
