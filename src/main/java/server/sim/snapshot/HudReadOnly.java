package server.sim.snapshot;

public record HudReadOnly(
        int coins,
        int lostPackets,
        int successfulPackets,
        double gameTimeSec,
        double wireRemaining
) {}