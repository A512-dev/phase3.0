package com.mygame.model;

public class HUDState {
    private int coins = 0;

    private double gameTime = 0;
    private int lostPackets = 0;
    private int totalPackets = 0;
    private int successfulPackets = 0;
    public void incrementSuccessful() { successfulPackets++; }
    public int getSuccessful() { return successfulPackets; }

    public int getTotalPackets() { return totalPackets; }
    public void incrementTotalPackets() { totalPackets++; }
    public int getLostPackets() { return lostPackets; }
    public void incrementLostPackets() { lostPackets++; }
    public double getPacketLossRatio() {
        return totalPackets == 0 ? 0 : (double) lostPackets / totalPackets;
    }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public void setLostPackets(int loss) { this.lostPackets = loss; }

    public double getGameTime() { return gameTime; }
    public void setGameTime(double time) { this.gameTime = time; }

    public void resetGameTime() {
        this.gameTime = 0;
    }
    public void reset() {
        totalPackets = 0;
        lostPackets = 0;
        coins = 0;
        gameTime = 0;
    }
}
