package com.mygame.engine.modelUpdates;

import com.mygame.engine.physics.MovementStrategy;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Connection;
import com.mygame.model.packet.Packet;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.mygame.engine.physics.Vector2D.distPointToSegment;

public final class MovementSystem implements MovementStrategy {

//    @Override
//    public void update(Packet p, double dt) {
//        System.out.println("Moving packet " + p + " by dt=" + dt);
//        p.advance(dt);
//    }

    /** Move each in-transit packet along its wire at its own speed. */
    @Override
    public void update(double dt, List<Connection> wires) {
        for (Connection c : wires) updateWire(c, dt);
    }

    private void keepForward(Packet pkt){
        Connection w = pkt.getWire();
        if (w == null) return;                       // داخل نود
        Vector2D dir = w.getTo().getCenter()
                .subtracted(w.getFrom().getCenter())
                .normalized();               // بردارِ سیم

        /* جدا کردن سرعت به مؤلفه‌ ها */
        Vector2D v       = pkt.getVelocity();
        double   forward = v.dot(dir);               // مؤلفه در امتداد
        Vector2D lateral = v.subtracted(dir.multiplied(forward));

        /* اگر منفی شد، مقدار مثبت حداقلِ دلخواه بده */
        if (forward < 0){
            forward = Math.abs(forward);       // ۲۰٪ انرژی برگشتی
        }

        /* بازترکیب و ست‌کردن */
        pkt.setVelocity( dir.multiplied(forward).added(lateral) );
    }

    private void updateWire(Connection c, double dt) {
        /* tunables – adjust once, use everywhere */
        final double ARRIVE_R = 6.0;   // ≤ this → reached destination port
        final double DEAD_R   = 18.0;  // ≥ this → “falls off” the wire
        final double DAMPING  = 0.995; // 1 → no damping
        final double TURN_R = 1.0;   // px within which we “snap” at bend


        List<Vector2D> path = c.getPath();                // from ⟶ bends ⟶ to

        for (Iterator<Connection.PacketOnWire> it = c.inTransitIterator(); it.hasNext(); ) {

            Connection.PacketOnWire pow = it.next();
            Packet pkt = pow.pkt;                         // convenience ref

            /* 1 ─ physics:  a → v → x  */
            pkt.setVelocity( pkt.getVelocity()
                    .added( pkt.getAcceleration().multiplied(dt) ) );

            // >>> add transient impulse channel here
            pkt.setVelocity( pkt.getVelocity().added( pkt.getImpulse() ) );

            pkt.setPosition( pkt.getPosition()
                    .added( pkt.getVelocity().multiplied(dt) ) );


            // 3) decay the impulse AFTER using it
            pkt.decayImpulse(dt);

            // update velocity direction
            Projection proj = closestPointOnPath(pkt.getPosition(), path);

            pow.s = proj.s;

            Vector2D[] pts = path.toArray(Vector2D[]::new);
            int seg = findSegmentIndex(pts, proj.s);


            /* ── snap to next segment if we’re right at the bend ─────────────── */
            if (seg < pts.length - 2) {
                for (int i=0; i<50; i++)
                    System.out.println(seg);
                if (proj.point.distanceTo(pts[seg + 1]) <= TURN_R) {
                    for (int i=0; i<50; i++)
                        System.out.println("fffffffffffffffffffffffffff");
                    seg++;                                     // pretend we’re already in it
                }
            }
            if (seg != pow.segmentIndex) {
                pow.segmentIndex = seg;  // update tracker
                for (int i=0; i<50; i++)
                    System.out.println("trtttttt");

                // update velocity to match new segment
                Vector2D tangent = pts[seg + 1].subtracted(pts[seg]).normalized();
                double speed = pkt.getVelocity().length();
                pkt.setVelocity(tangent.multiplied(speed));
            }



            /* 2 ─ arrival check (true geometric distance to the port centre) */
            if (pkt.getPosition().distanceTo( c.getTo().getCenter() ) <= ARRIVE_R) {
                c.getTo().deliver(pkt);                   // node handles onDelivered()
                it.remove();
                continue;
            }

            /* 3 ─ off-wire check (shortest distance to poly-line) */
            double lateral = distanceToPolyline(pkt.getPosition(), path);
            if (lateral >= DEAD_R) {                      // too far away → lose it
                pkt.setAlive(false);
                it.remove();
                continue;
            }

            /* 4 ─ mild damping so residual sideways speed disappears */
//            pkt.setVelocity( pkt.getVelocity().multiplied(DAMPING) );

            /* 5 ─ optional bookkeeping:  keep ‘s’ for HUD/debug, not required */
            pow.s = closestPointOnPath(pkt.getPosition(), path).s;
        }
    }

    /** Linear interpolation along a poly-line by distance s. */
    private Vector2D interpolate(Vector2D[] pts, double s) {
        for (int i = 0; i < pts.length - 1; i++) {
            double seg = pts[i].distanceTo(pts[i + 1]);
            if (s <= seg) {
                double t = s / seg;
                return new Vector2D(
                        pts[i].x() + t * (pts[i + 1].x() - pts[i].x()),
                        pts[i].y() + t * (pts[i + 1].y() - pts[i].y())
                );
            }
            s -= seg;
        }
        return pts[pts.length - 1].copy(); // end of path
    }

    private record Projection(Vector2D point, double s, double distance){}
    private Projection closestPointOnPath(Vector2D p, List<Vector2D> pts){
        double acc = 0, bestS = 0, bestDist = Double.MAX_VALUE;
        Vector2D bestPt = pts.get(0);

        for(int i=0;i<pts.size()-1;i++){
            Vector2D a = pts.get(i), b = pts.get(i+1);
            Vector2D ab = b.subtracted(a);
            double  len = ab.length();

            double t = Math.max(0, Math.min(1,
                    p.subtracted(a).dot(ab) / (len*len)));   // projection scalar
            Vector2D q = a.added(ab.multiplied(t));            // projected point
            double   d = q.distanceTo(p);

            if (d < bestDist){
                bestDist = d;
                bestPt   = q;
                bestS    = acc + len*t;
            }
            acc += len;
        }
        return new Projection(bestPt,bestS,bestDist);
    }
    static double distanceToPolyline(Vector2D p, List<Vector2D> path) {
        double best = Double.MAX_VALUE;
        for (int i = 0; i < path.size() - 1; i++) {
            best = Math.min(best, distPointToSegment(
                    p, path.get(i), path.get(i + 1)));
        }
        return best;
    }
    /** Finds the tangent direction at distance s along the polyline. */
    private Vector2D getTangentDirection(Vector2D[] pts, double s) {
        for (int i = 0; i < pts.length - 1; i++) {
            double segLen = pts[i].distanceTo(pts[i + 1]);
            if (s <= segLen) {
                return pts[i + 1].subtracted(pts[i]).normalized();  // ← tangent of this segment
            }
            s -= segLen;
        }
        // If s > total length, return direction of last segment
        return pts[pts.length - 1].subtracted(pts[pts.length - 2]).normalized();
    }

    private int findSegmentIndex(Vector2D[] pts, double s) {
        for (int i = 0; i < pts.length - 1; i++) {
            double segLen = pts[i].distanceTo(pts[i + 1]);
            if (s <= segLen) return i;
            s -= segLen;
        }
        return pts.length - 2; // final segment
    }

}
