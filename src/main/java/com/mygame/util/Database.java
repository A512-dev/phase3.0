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
    public static final double TRIANGLE_BASE_IMPULSE = 100;
    public static final double IMPULSE_DECAY = 3.0;
    public static final double ACC_DECAY = 200;
    public static final int NUMBER_OF_RUNS = 1;
    public static double timeMultiplier = 1.0;
    public static double fastGameSpeed = 3;
    public static final int timeSendFromPort = (int) (3); // seconds
    public static final int MAX_QUEUE_SIZE = 5;


    public static double speedOfPackets = 50;
}
