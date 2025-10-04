package server.sim.engine.physics;

import server.sim.model.packet.Packet;

import java.util.*;
import java.util.function.Consumer;

/**
 * Fixed-time-step 2-D circle–circle solver with a spatial-hash broad phase.
 * All bodies must implement {@link PhysicsBody}.
 */
public final class CollisionSystem {

    /* ───────── spatial hash helpers ───────── */


    /** add this to GameConfig */
    private static final double CELL = 24.0;           // >= max packet diameter
    private static final double CORRECT_PERCENT = 0.8; // 0..1 penetration correction
    private static final double PENETRATION_SLOP = 0.01;
    private static final double GLOBAL_DAMP = 0.99;    // tiny damping after impulse


    /** Optional: called once per contact with the midpoint (for wave effects). */
    private final Consumer<Vector2D> onContact;

    public CollisionSystem() { this(null); }
    public CollisionSystem(Consumer<Vector2D> onContact) { this.onContact = onContact; }



    /* ───────── public entry point ───────── */
    public void step(List<? extends PhysicsBody> bodies, double dt) {
        if (bodies.isEmpty()) return;

//        /* 1 ─ integrate velocities into positions */
//        bodies.forEach(b -> {
//            if (!b.canCollide()) return;
//            Vector2D p = b.getPosition();
//            Vector2D v = b.getVelocity();
//            b.setPosition(p.added(v.multiplied(dt)));      // x += v·dt   (does not touch velocity)
//        });

        /* 2-3 ─ build spatial hash */
        Map<Long, List<PhysicsBody>> grid = buildHash(bodies);

        /* 4 ─ narrow phase: gather contacts */
        List<Contact> contacts = new ArrayList<>();
        HashSet<Long> seenPairs = new HashSet<>(256);
        for (var cell : grid.values()) findContacts(cell, contacts, seenPairs);

        /* 5-6 ─ resolve */
        contacts.forEach(this::positionalCorrection);
        contacts.forEach(this::applyImpulse);



        /* 7 ─ post-collision callbacks (packets only) */
        contacts.forEach(c -> {
            ((server.sim.model.packet.Packet)c.a).onCollision((server.sim.model.packet.Packet)c.b);
            ((server.sim.model.packet.Packet)c.b).onCollision((server.sim.model.packet.Packet)c.a);
        });
    }


    private long key(int x, int y) { return (((long)x) << 32) ^ (y & 0xffffffffL); }

    private Map<Long, List<PhysicsBody>> buildHash(List<? extends PhysicsBody> bodies) {
        Map<Long, List<PhysicsBody>> g = new HashMap<>();
        for (PhysicsBody b : bodies) {
            if (!b.canCollide()) continue;

            Vector2D p = b.getPosition();
            double r = b.getRadius();

            int minCx = (int)Math.floor((p.x() - r) / CELL);
            int maxCx = (int)Math.floor((p.x() + r) / CELL);
            int minCy = (int)Math.floor((p.y() - r) / CELL);
            int maxCy = (int)Math.floor((p.y() + r) / CELL);

            for (int cx = minCx; cx <= maxCx; cx++) {
                for (int cy = minCy; cy <= maxCy; cy++) {
                    g.computeIfAbsent(key(cx, cy), k -> new ArrayList<>()).add(b);
                }
            }
        }
        return g;
    }

    /* ───────── narrow-phase ───────── */

    /** check every pair inside <cell> (no neighbour scan needed because we
     hashed each body into *all* overlapping cells)                         */

    private void findContacts(List<PhysicsBody> cell, List<Contact> out, Set<Long> seen) {
        for (int i = 0; i < cell.size(); i++) {
            PhysicsBody A = cell.get(i);
            if (!A.canCollide()) continue;
            for (int j = i + 1; j < cell.size(); j++) {
                PhysicsBody B = cell.get(j);
                if (!B.canCollide()) continue;

                long k = pairKey(A, B);
                if (!seen.add(k)) continue; // processed elsewhere

                maybeAdd(A, B, out);
            }
        }
    }
    private static long pairKey(Object a, Object b) {
        int ha = System.identityHashCode(a);
        int hb = System.identityHashCode(b);
        if (ha > hb) { int t = ha; ha = hb; hb = t; }
        return (((long)ha) << 32) ^ (hb & 0xffffffffL);
    }


    private void maybeAdd(PhysicsBody a, PhysicsBody b, List<Contact> out) {
        Vector2D n = b.getPosition().subtracted(a.getPosition());
        double radii = a.getRadius() + b.getRadius();
        double distSq = n.lengthSq();
        if (distSq >= (radii * radii)*0.6) return; // didn't work correctly
        double combined = (a.getRadius() + b.getRadius()) * 0.5;
        if (n.length()>=combined) return;//added this instead
        double dist = Math.sqrt(distSq);
        Vector2D normal = (dist > 1e-6) ? n.multiplied(1.0 / dist) : new Vector2D(1, 0); // arbitrary axis
        double penetration = radii - dist;

        out.add(new Contact(a, b, normal, penetration));
    }

    /* ───────── resolution ───────── */

    private void positionalCorrection(Contact c) {
        double invA = c.a.getInvMass(), invB = c.b.getInvMass();
        if (invA + invB == 0) return; // when they are both immovable
        double invSum = invA + invB;
        // remove tiny overlaps first (slop), then correct a percentage
        double corrMag = Math.max(c.penetration - PENETRATION_SLOP, 0.0) * (CORRECT_PERCENT / invSum);
        Vector2D corr = c.normal.multiplied(corrMag);

        c.a.setPosition(c.a.getPosition().subtracted(corr.multiplied(invA)));
        c.b.setPosition(c.b.getPosition().added(corr.multiplied(invB)));
    }

    private void applyImpulse(Contact c) {
        double invA = c.a.getInvMass(), invB = c.b.getInvMass();
        if (invA + invB == 0) return;

        Vector2D rv = c.b.getVelocity().subtracted(c.a.getVelocity()); // impulse vector after impact

        double vn = rv.dot(c.normal) * 0.1; // impulse vector after impact
        if (vn > 0) return;                      // separating

        double e = Math.min(c.a.getRestitution(), c.b.getRestitution());
        double j = -(1 + e) * vn / (invA + invB);
        Vector2D impulse = c.normal.multiplied(j);


        // ✅ Your Packet "impulse" is a Δv accumulator; add Δv = (±jn * invMass)
        ((Packet) c.a).addImpulse(impulse.multiplied(-invA)); // vA += -j*n * invA
        ((Packet) c.b).addImpulse(impulse.multiplied(+invB)); // vB +=  j*n * invB


        if (onContact != null) {
            Vector2D mid = c.a.getPosition().added(c.b.getPosition()).multiplied(0.5);
            onContact.accept(mid);
        }


        // TODO: 8/11/2025 friction?

        /* friction */
//        rv = c.b.getVelocity().subtracted(c.a.getVelocity());     // recompute
//        Vector2D tangent = rv.subtracted(c.normal.multiplied(rv.dot(c.normal)));
//        if (tangent.lengthSq() < 1e-8) return;
//        tangent = tangent.normalized();
//
//        double jt = -rv.dot(tangent) / (invA + invB);
//        double mu = Math.sqrt(c.a.getFriction() * c.b.getFriction());
//        jt = Math.max(-j * mu, Math.min(j * mu, jt));
//
//        Vector2D ft = tangent.multiplied(jt);
//
//
//        c.a.setVelocity(c.a.getVelocity().subtracted(ft.multiplied(invA)));
//        c.b.setVelocity(c.b.getVelocity().added(ft.multiplied(invB)));

    }

    /* ───────── tiny record ───────── */
    private record Contact(PhysicsBody a, PhysicsBody b,
                           Vector2D normal, double penetration) {}
}
