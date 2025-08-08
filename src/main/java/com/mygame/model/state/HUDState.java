package com.mygame.model.state;

import com.mygame.core.GameConfig;
import com.mygame.snapshot.HudReadOnly;
import com.mygame.model.PacketEventListener;
import com.mygame.model.packet.Packet;
import com.mygame.model.powerup.PowerUpType;

public class HUDState implements PacketEventListener {
    private final GameConfig cfg;

    private int coins = 0;
    private double gameTime = 0;
    private int lostPackets = 0;
    private int totalPackets = 0;
    private int successfulPackets = 0;
    private PowerUpType powerUpActive = null;

    private boolean isShopOpen = false;

    private double wireLengthRemaining;

    private int numOfGoToTarget = -1;

    public HUDState(GameConfig cfg) {
        this.cfg = cfg;
        // initialize from config instead of Database
        this.wireLengthRemaining = cfg.maxWireLength;
    }

    public boolean isShopOpen() {
        return isShopOpen;
    }

    public void setShopOpen(boolean shopOpen) {
        isShopOpen = shopOpen;
    }

    public double getWireLengthRemaining() {
        return wireLengthRemaining;
    }

    public void setWireLengthRemaining(double wireLengthRemaining) {
        this.wireLengthRemaining = wireLengthRemaining;
    }

    public int getNumOfGoToTarget() {
        return numOfGoToTarget;
    }

    public void setNumOfGoToTarget(int numOfGoToTarget) {
        this.numOfGoToTarget = numOfGoToTarget;
    }

    public void incrementSuccessful() {
        successfulPackets++;
    }

    public int getSuccessful() {
        return successfulPackets;
    }

    public int getTotalPackets() {
        return totalPackets;
    }

    public void incrementTotalPackets() {
        totalPackets++;
    }

    public int getLostPackets() {
        return lostPackets;
    }

    public void incrementLostPackets() {
        lostPackets++;
    }

    public double getPacketLossRatio() {
        return totalPackets == 0 ? 0 : (double) lostPackets / totalPackets;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setLostPackets(int loss) {
        this.lostPackets = loss;
    }

    public double getGameTime() {
        return gameTime;
    }

    public void setGameTime(double time) {
        this.gameTime = time;
    }

    public void resetGameTime() {
        this.gameTime = 0;
    }

    public void reset() {
        totalPackets = 0;
        lostPackets = 0;
        coins = 0;
        gameTime = 0;
    }

    @Override
    public void onLost(Packet p) {
        lostPackets++;
        coins = Math.max(0, coins - 1);
    }

    @Override
    public void onDelivered(Packet p) {
        successfulPackets++;
    }

    @Override
    public void onCollision(Packet a, Packet b) {
        // no-op
    }

    public boolean isPowerUpActive(PowerUpType oAtar) {
        return powerUpActive != null && powerUpActive == PowerUpType.O_ATAR;
    }

    public HudReadOnly readOnlyCopy() {
        return new HudReadOnly(coins, lostPackets, successfulPackets, gameTime, wireLengthRemaining);
    }
}
