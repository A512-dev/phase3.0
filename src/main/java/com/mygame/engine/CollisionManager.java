package com.mygame.engine;

import com.mygame.model.Packet;
import com.mygame.util.Vector2D;

import java.util.*;

public class CollisionManager {
    private final double CELL_SIZE = 50;
    private final double IMPACT_RADIUS = 60;
    private final double IMPULSE_FORCE = 150;

    public void checkCollisions(List<Packet> packets) {
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
            Vector2D posA = a.getPosition();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    long neighborKey = hash(posA.x + dx * CELL_SIZE, posA.y + dy * CELL_SIZE);
                    List<Packet> nearby = grid.getOrDefault(neighborKey, Collections.emptyList());

                    for (Packet b : nearby) {
                        if (a == b || !b.isAlive() || checked.contains(b)) continue;

                        double distance = a.getPosition().distanceTo(b.getPosition());
                        double combined = a.getSize() / 2 + b.getSize() / 2;

                        if (distance <= combined) {
                            a.onCollision();
                            b.onCollision();

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
        for (Packet p : packets) {
            if (!p.isAlive()) continue;
            double d = p.getPosition().distanceTo(center);
            if (d < IMPACT_RADIUS && d > 0) {
                Vector2D dir = p.getPosition().subtracted(center).normalized();
                Vector2D impulse = dir.multiplied((1 - d / IMPACT_RADIUS) * IMPULSE_FORCE);
                p.getImpulse().add(impulse);   // ✅ new method to access impactImpulse
            }
        }
    }

    private long hash(double x, double y) {
        long gx = (long)Math.floor(x / CELL_SIZE);
        long gy = (long)Math.floor(y / CELL_SIZE);
        return (gx << 32) | (gy & 0xffffffffL);
    }
}
