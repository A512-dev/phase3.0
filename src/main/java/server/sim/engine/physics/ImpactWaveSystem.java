package server.sim.engine.physics;

import server.sim.model.Connection;
import server.sim.model.packet.Packet;
import shared.Vector2D;

import java.util.*;

public final class ImpactWaveSystem {

    public static final class Wave {
        public final Vector2D center;
        public final double   radius;
        public final double   strength;
        private double ttl; // seconds

        public Wave(Vector2D c, double r, double s, double lifeSec) {
            this.center = c; this.radius = r; this.strength = s; this.ttl = lifeSec;
        }
    }

    private final List<Wave> waves = new ArrayList<>();

    public void emit(Vector2D center, double radius, double strength) {
        // lifetime can be short; we only need 1–2 frames of kick
        waves.add(new Wave(center, radius, strength, 0.05));
    }

    /** Apply wave impulses to packets on all wires, then age out expired waves. */
    public void update(double dt, List<Connection> wires, boolean suppress) {
        if (suppress || waves.isEmpty()) return;

        for (Connection c : wires) {
            for (Iterator<Connection.PacketOnWire> it = c.inTransitIterator(); it.hasNext();) {
                Packet pkt = it.next().pkt;
                if (!pkt.canCollide()) continue;

                for (Wave w : waves) {
                    double d = pkt.getPosition().distanceTo(w.center);
                    if (d <= 0 || d >= w.radius) continue;

                    double falloff = 1.0 - (d / w.radius);       // 1 → 0
                    Vector2D dir   = pkt.getPosition().subtracted(w.center).normalized();
                    pkt.addImpulse( dir.multiplied(w.strength * falloff) );
                }
            }
        }

        // age out
        for (Iterator<Wave> it = waves.iterator(); it.hasNext();) {
            Wave w = it.next();
            w.ttl -= dt;
            if (w.ttl <= 0) it.remove();
        }
    }

    public void clear() { waves.clear(); }
}
