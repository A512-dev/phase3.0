package com.mygame.engine.physics;

import java.util.*;

/**
 * Fixed-time-step 2-D circle–circle solver with a spatial-hash broad phase.
 * All bodies must implement {@link PhysicsBody}.
 */
public final class CollisionSystem {

    /* ───────── public entry point ───────── */
    public void step(List<? extends PhysicsBody> bodies, double dt) {
        if (bodies.isEmpty()) return;

        /* 1 ─ integrate velocities into positions */
        bodies.forEach(b -> {
            if (!b.canCollide()) return;
            Vector2D p = b.getPosition();
            Vector2D v = b.getVelocity();
            b.setPosition(p.added(v.multiplied(dt)));      // x += v·dt   (does not touch velocity)
        });

        /* 2-3 ─ build spatial hash */
        Map<Long, List<PhysicsBody>> grid = buildHash(bodies);

        /* 4 ─ narrow phase: gather contacts */
        List<Contact> contacts = new ArrayList<>();
        for (var cell : grid.values())
            findContacts(cell, contacts);

        /* 5-6 ─ resolve */
        contacts.forEach(this::positionalCorrection);
        contacts.forEach(this::applyImpulse);



        /* 7 ─ post-collision callbacks (packets only) */
        contacts.forEach(c -> {
            ((com.mygame.model.packet.Packet)c.a).onCollision((com.mygame.model.packet.Packet)c.b);
            ((com.mygame.model.packet.Packet)c.b).onCollision((com.mygame.model.packet.Packet)c.a);
        });
    }

    /* ───────── spatial hash helpers ───────── */

    private static final double CELL = 24;        // >= 2 × max packet radius

    private long key(int x, int y) { return (((long)x) << 32) ^ (y & 0xffffffffL); }

    private Map<Long, List<PhysicsBody>> buildHash(List<? extends PhysicsBody> bodies) {
        Map<Long, List<PhysicsBody>> g = new HashMap<>();
        for (PhysicsBody b : bodies) {
            if (!b.canCollide()) continue;
            int cx = (int)Math.floor(b.getPosition().x() / CELL);
            int cy = (int)Math.floor(b.getPosition().y() / CELL);
            g.computeIfAbsent(key(cx, cy), k -> new ArrayList<>()).add(b);
        }
        return g;
    }

    /* ───────── narrow-phase ───────── */

    /** check every pair inside <cell> (no neighbour scan needed because we
     hashed each body into *all* overlapping cells)                         */
    private void findContacts(List<PhysicsBody> cell, List<Contact> out) {
        for (int i = 0; i < cell.size(); i++) {
            PhysicsBody A = cell.get(i);
            if (!A.canCollide()) continue;
            for (int j = i + 1; j < cell.size(); j++) {
                PhysicsBody B = cell.get(j);
                if (!B.canCollide()) continue;
                maybeAdd(A, B, out);
            }
        }
    }

    private void maybeAdd(PhysicsBody a, PhysicsBody b, List<Contact> out) {
        Vector2D n = b.getPosition().subtracted(a.getPosition());
        double radii = a.getRadius() + b.getRadius();
        double distSq = n.lengthSq();
        if (distSq >= radii * radii) return;      // not overlapping

        double dist = Math.sqrt(distSq);
        Vector2D normal = (dist > 1e-6) ? n.multiplied(1 / dist)
                : new Vector2D(1, 0);        // arbitrary
        out.add(new Contact(a, b, normal, radii - dist));
    }

    /* ───────── resolution ───────── */

    private void positionalCorrection(Contact c) {
        double invA = c.a.getInvMass(), invB = c.b.getInvMass();
        if (invA + invB == 0) return; // todo: when does this happen

        double percent = 0.8;                    // penetration percent to correct
        Vector2D corr = c.normal.multiplied(percent * c.penetration / (invA + invB));
        c.a.setPosition(c.a.getPosition().subtracted(corr.multiplied(invA)));
        c.b.setPosition(c.b.getPosition().added(corr.multiplied(invB)));
    }

    private void applyImpulse(Contact c) {
        double invA = c.a.getInvMass(), invB = c.b.getInvMass();
        if (invA + invB == 0) return;

//        Vector2D rv = c.b.getVelocity().subtracted(c.a.getVelocity());
        Vector2D rv = c.b.getVelocity().added(c.a.getVelocity());

        double vn = rv.dot(c.normal) * 0.1;
        if (vn > 0) return;                      // separating

        double e = Math.min(c.a.getRestitution(), c.b.getRestitution());
        double j = -(1 + e) * vn / (invA + invB);
        Vector2D impulse = c.normal.multiplied(j);

        c.a.setVelocity(c.a.getVelocity().subtracted(impulse.multiplied(invA)));
        c.b.setVelocity(c.b.getVelocity().added(impulse.multiplied(invB)));

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

        /* ── global damping : 1 line ───────────────────────── */
        c.a.setVelocity( c.a.getVelocity().multiplied(0.99) );   // <── here
        c.b.setVelocity( c.b.getVelocity().multiplied(0.99) );   // and here

    }

    /* ───────── tiny record ───────── */
    private record Contact(PhysicsBody a, PhysicsBody b,
                           Vector2D normal, double penetration) {}
}
