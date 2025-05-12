package com.mygame.engine;

import com.mygame.audio.AudioManager;
import com.mygame.model.HUDState;
import com.mygame.model.Packet;
import com.mygame.model.World;
import com.mygame.model.powerups.PowerUpType;
import com.mygame.util.Database;
import com.mygame.util.Vector2D;

import java.util.*;

public class CollisionManager {
    private final double CELL_SIZE = Database.CELL_SIZE;
    private final double IMPACT_RADIUS = Database.IMPACT_RADIUS;
    private final double impulseForce = Database.IMPULSE_FORCE;
    private final World world;
    public CollisionManager(World world) {
        this.world = world;
    }
    public void checkCollisions(List<Packet> packets) {
        /* inside checkCollisions(…) right at the start */
        if (world.isPowerUpActive(PowerUpType.O_AIRYAMAN)) return;   // skip all collisions
        Map<Long, List<Packet>> grid = new HashMap<>();

        // Broad phase: put packets into grid cells
        for (Packet p : packets) {
            long key = hash(p.getPosition().x, p.getPosition().y);
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
        }

        Set<Packet> checked = new HashSet<>();

        // For each packet, only check neighbors
        for (Packet a : packets) {
            if (!a.isAlive()) continue;
            if (!a.isMobile()) continue;
            Vector2D posA = a.getPosition();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    long neighborKey = hash(posA.x + dx * CELL_SIZE, posA.y + dy * CELL_SIZE);
                    List<Packet> nearby = grid.getOrDefault(neighborKey, Collections.emptyList());

                    for (Packet b : nearby) {
                        if (a == b || !b.isAlive() || checked.contains(b) || !b.isMobile()) continue;

                        double distance = a.getPosition().distanceTo(b.getPosition());
                        double combined = a.getSize() / 2 + b.getSize() / 2;

                        if (distance <= combined) {
                            a.onCollision();
                            b.onCollision();
                            AudioManager.get().playFx("pop");

                            Vector2D impact = a.getPosition().added(b.getPosition()).multiplied(0.5);
                            applyImpactWave(packets, impact);
                        }
                    }
                }
            }
            checked.add(a);
        }
    }

    private void applyImpactWave(List<Packet> packets, Vector2D center) {
        /* 1 ─ Power-up: impact suppression */
        if (world.getHudState().isPowerUpActive(PowerUpType.O_ATAR)) return;

        for (Packet p : packets) {
            if (!p.isAlive() || !p.isMobile()) continue;


            double d = p.getPosition().distanceTo(center);
            if (d < IMPACT_RADIUS && d > 0) {
                Vector2D dir = p.getPosition().subtracted(center).normalized();
                /* 2 ─ Directional impulse (unchanged) */
                double falloff = (1 - d / IMPACT_RADIUS);        // 1 → 0
                Vector2D impulse = dir.multiplied(falloff * impulseForce);
                p.getImpulse().add(impulse);   // ✅ new method to access impactImpulse

                // **IMMEDIATE OFF-TRACK CHECK**
                // pretend we moved by impulse for this frame:
                Vector2D predictedPos = p.getPosition().added(impulse.multiplied(1.0/60.0));
                if (p.isOffTrackLine(Database.maxDistanceToBeOfTheLine, predictedPos)) {
                    System.out.println("offfff");
                    p.setUnAlive();
                    AudioManager.get().playFx("loss");
                }
            }
        }
    }


    private long hash(double x, double y) {
        long gx = (long)Math.floor(x / CELL_SIZE);
        long gy = (long)Math.floor(y / CELL_SIZE);
        return (gx << 32) | (gy & 0xffffffffL);
    }

}
