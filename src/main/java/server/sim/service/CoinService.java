package server.sim.service;

import server.sim.model.state.HUDState;

public class CoinService {
    private final HUDState hud;

    public CoinService(HUDState hud) {
        this.hud = hud;
    }

    /** Try to spend the given number of coins. Return true if successful. */
    public boolean trySpend(int amount) {
        if (hud.getCoins() >= amount) {
            hud.setCoins(hud.getCoins() - amount);
            return true;
        }
        return false;
    }

    /** Add coins to the player. */
    public void addCoins(int amount) {
        if (amount<0) return;
        hud.setCoins(hud.getCoins() + amount);
    }

    /** Return the current coin count. */
    public int getCoins() {
        return hud.getCoins();
    }

    /** Check if the player can afford a given amount. */
    public boolean canAfford(int cost) {
        return hud.getCoins() >= cost;
    }
}
