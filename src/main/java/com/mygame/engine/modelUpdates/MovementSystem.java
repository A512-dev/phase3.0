package com.mygame.engine.modelUpdates;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.MovementStrategy;
import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Connection;

import com.mygame.model.packet.Packet;
import com.mygame.model.packet.bulkPacket.types.BulkPacketA;
import com.mygame.model.packet.bulkPacket.types.BulkPacketB;


import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.mygame.engine.physics.Vector2D.distPointToSegment;

public final class MovementSystem implements MovementStrategy {
    final double base = com.mygame.core.GameConfig.SPEED_OF_CONFIDENTIAL_SMALL_PACKET;
    final double Rslow   = com.mygame.core.GameConfig.CSP_APPROACH_RADIUS;      // e.g., 120
    final double slowFac = com.mygame.core.GameConfig.CSP_SLOW_FACTOR;          // e.g., 0.35
    final double margin  = com.mygame.core.GameConfig.CSP_SOFTSTOP_MARGIN;      // e.g., 8
    final double rate    = com.mygame.core.GameConfig.CSP_LERP_RATE;            // e.g., 12

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

//    private void keepForward(Packet pkt){
//        Connection w = pkt.getWire();
//        if (w == null) return;                       // داخل نود
//        Vector2D dir = w.getTo().getCenter()
//                .subtracted(w.getFrom().getCenter())
//                .normalized();               // بردارِ سیم
//
//        /* جدا کردن سرعت به مؤلفه‌ ها */
//        Vector2D v       = pkt.getVelocity();
//        double   forward = v.dot(dir);               // مؤلفه در امتداد
//        Vector2D lateral = v.subtracted(dir.multiplied(forward));
//
//        /* اگر منفی شد، مقدار مثبت حداقلِ دلخواه بده */
//        if (forward < 0){
//            forward = Math.abs(forward);       // ۲۰٪ انرژی برگشتی
//        }
//
//        /* بازترکیب و ست‌کردن */
//        pkt.setVelocity( dir.multiplied(forward).added(lateral) );
//    }

    private void updateWire(Connection c, double dt) {
        /* tunables – adjust once, use everywhere */
        final double ARRIVE_R = 6.0;   // ≤ this → reached destination port
        final double DEAD_R   = 18.0;  // ≥ this → “falls off” the wire
//        final double DAMPING  = 0.995; // 1 → no damping
        final double TURN_R = 1.0;   // px within which we “snap” at bend
          /* ───── NEW: Bulk A tuning ─────
       Add these to GameConfig:
       BULK_A_SPEED_FLAT, BULK_A_ACCEL_CURVE, BULK_A_VMAX_CURVE, CURVE_ZONE_RADIUS
    */
        final double bulkFlatSpeed = GameConfig.SPEED_OF_BULKPACKET_A_PACKET;      // e.g., 90 px/s
        final double bulkCurveAcc  = com.mygame.core.GameConfig.BULK_A_ACCEL_CURVE;     // e.g., 240 px/s^2
        final double bulkCurveVmax = com.mygame.core.GameConfig.BULK_A_VMAX_CURVE;      // e.g., 160 px/s
        final double curveZoneR    = com.mygame.core.GameConfig.CURVE_ZONE_RADIUS;      // e.g., 14 px around bends
        // ─── Bulk B (constant forward speed with periodic lateral deviation) ───
        final double bulkBSpeed   = com.mygame.core.GameConfig.BULK_B_SPEED;
        final double bulkBAmpl    = com.mygame.core.GameConfig.BULK_B_DEVIATION_AMPL;
        final double bulkBWave    = com.mygame.core.GameConfig.BULK_B_DEVIATION_WAVELENGTH;
        final double bulkBTrack   = com.mygame.core.GameConfig.BULK_B_LATERAL_TRACK_RATE; // convergence rate





        List<Vector2D> path = c.getPath();                // from ⟶ bends ⟶ to

        for (Iterator<Connection.PacketOnWire> it = c.inTransitIterator(); it.hasNext(); ) {

            Connection.PacketOnWire pow = it.next();
            Packet pkt = pow.pkt;// convenience ref





            /* 1 ─ physics:  a → v → x  */
            pkt.setVelocity(pkt.getVelocity()
                    .added(pkt.getAcceleration().multiplied(dt)));

            // >>> add transient impulse channel here
            pkt.setVelocity(pkt.getVelocity().added(pkt.getImpulse()));

            pkt.setPosition(pkt.getPosition()
                    .added(pkt.getVelocity().multiplied(dt)));


            // 3) decay the impulse AFTER using it
            pkt.decayImpulse(dt);


            // update velocity direction
            Projection proj = closestPointOnPath(pkt.getPosition(), path);

            pow.s = proj.s;

            Vector2D[] pts = path.toArray(Vector2D[]::new);
            int seg = findSegmentIndex(pts, proj.s);


            /* ── snap to next segment if we’re right at the bend ─────────────── */
            if (seg < pts.length - 2) {
                for (int i = 0; i < 50; i++)
                    System.out.println(seg);
                if (proj.point.distanceTo(pts[seg + 1]) <= TURN_R) {
                    for (int i = 0; i < 50; i++)
                        System.out.println("fffffffffffffffffffffffffff");
                    seg++;                                     // pretend we’re already in it
                }
            }
            if (seg != pow.segmentIndex) {
                pow.segmentIndex = seg;  // update tracker
                for (int i = 0; i < 50; i++)
                    System.out.println("trtttttt");

                // update velocity to match new segment
                Vector2D tangent = pts[seg + 1].subtracted(pts[seg]).normalized();
                double speed = pkt.getVelocity().length();
                pkt.setVelocity(tangent.multiplied(speed));
            }


            // ---- ConfidentialSmallPacket: constant 2D speed unless slowing near busy dest ----
            if (pkt instanceof com.mygame.model.packet.confidentialPacket.types.ConfidentialSmallPacket) {
                boolean destBusy = (c.getTo() != null
                        && c.getTo().getOwner() != null
                        && !c.getTo().getOwner().getQueuedPackets().isEmpty());

                assert c.getTo() != null;
                double distToDest = pkt.getPosition().distanceTo(c.getTo().getCenter());

                // choose target speed (pure scalar), direction stays whatever it currently is
                double targetSpeed = base;
                if (destBusy && distToDest <= Rslow) targetSpeed = base * slowFac;

                // soft stop just outside the arrive ring
                double minStopDist = ARRIVE_R + margin;
                if (destBusy && distToDest <= minStopDist) targetSpeed = 0.0;

                // smooth toward target speed without touching direction (no projection)
                Vector2D v = pkt.getVelocity();
                double current = v.length();
                if (current > 1e-9) {
                    double k = 1.0 - Math.exp(-rate * dt);                    // 0..1
                    double newSpeed = current + (targetSpeed - current) * k;  // scalar blend
                    double scale = newSpeed / current;
                    pkt.setVelocity(v.multiplied(scale));                     // scale the 2D vector
                } else {
                    // if we're at rest and should move, give it a direction along the path segment
                    if (targetSpeed > 0) {
                        Vector2D[] pts2 = path.toArray(Vector2D[]::new);
                        Vector2D dir = pts2[seg + 1].subtracted(pts2[seg]).normalized();
                        pkt.setVelocity(dir.multiplied(targetSpeed));
                    } else {
                        pkt.setVelocity(new Vector2D());
                    }
                }
            }
            // ---- ConfidentialLargePacket: maintain spacing by moving fwd/back along the wire ----
            if (pkt instanceof com.mygame.model.packet.confidentialPacket.types.ConfidentialLargePacket) {

                final double D = com.mygame.core.GameConfig.CLP_TARGET_GAP;
                final double vmaxFwd = com.mygame.core.GameConfig.CLP_MAX_SPEED_FWD;
                final double vmaxBack = com.mygame.core.GameConfig.CLP_MAX_SPEED_BACK;
                final double rateCLP = com.mygame.core.GameConfig.CLP_LERP_RATE;


                // Current segment tangent for direction (we already computed seg & pts above)
                Vector2D tangent = pts[seg + 1].subtracted(pts[seg]).normalized();

                // Find nearest neighbors on THIS wire by arc-length s
                double sSelf = pow.s;
                double nearestAhead = Double.POSITIVE_INFINITY; // Δs > 0, smallest
                double nearestBehind = Double.POSITIVE_INFINITY; // |Δs| for Δs < 0, smallest

                for (ListIterator<Connection.PacketOnWire> jt = c.inTransitIterator(); jt.hasNext(); ) {
                    Connection.PacketOnWire other = jt.next();
                    if (other == pow) continue;
                    double ds = other.s - sSelf;
                    if (ds > 0) {
                        // ahead
                        if (ds < nearestAhead) nearestAhead = ds;
                    } else if (ds < 0) {
                        // behind (store absolute)
                        double behind = -ds;
                        if (behind < nearestBehind) nearestBehind = behind;
                    }
                }

                // Compute "pressures" to increase spacing:
                // - If someone is too close ahead (< D), push backward (negative speed).
                // - If someone is too close behind (< D), push forward (positive speed).
                double pAhead = (nearestAhead < D) ? (1.0 - (nearestAhead / D)) : 0.0; // 0..1
                double pBehind = (nearestBehind < D) ? (1.0 - (nearestBehind / D)) : 0.0; // 0..1

                // Desired signed speed: positive → forward, negative → backward
                double targetSpeed = (pBehind * vmaxFwd) - (pAhead * vmaxBack);

                // If nothing is close on either side, no need to drift: hold position (targetSpeed = 0)
                // (If you prefer slow cruising instead, set a small bias here.)
                if (pAhead == 0.0 && pBehind == 0.0) {
                    targetSpeed = 0.0;
                }

                // Smooth speed toward target without messing with 2D direction math
                Vector2D v = pkt.getVelocity();
                double currentSpeed = v.length();

                // Determine current direction sign relative to tangent
                double sign = (currentSpeed > 1e-9 && v.dot(tangent) < 0) ? -1.0 : +1.0;
                double currentSigned = currentSpeed * sign;

                double k = 1.0 - Math.exp(-rateCLP * dt);          // 0..1 smoothing
                double newSigned = currentSigned + (targetSpeed - currentSigned) * k;

                // Rebuild velocity strictly along the wire (this packet is "disciplinary")
                Vector2D newV = tangent.multiplied(newSigned);
                pkt.setVelocity(newV);
            }
            // 3c) BulkPacketA: constant speed on straight, accelerate through bends (“curves”)
            if (pkt instanceof BulkPacketA) {
                // Tangent of the current straight segment
                Vector2D tangent = pts[seg + 1].subtracted(pts[seg]).normalized();

                // Are we inside a bend zone near the next vertex?
                boolean inCurve = pts[seg + 1].distanceTo(proj.point) <= curveZoneR;

                Vector2D v = pkt.getVelocity();
                double speed = v.length();

                if (!inCurve) {
                    // Clamp to flat constant speed and align to tangent
                    if (speed == 0) {
                        pkt.setVelocity(tangent.multiplied(bulkFlatSpeed));
                    } else {
                        pkt.setVelocity(tangent.multiplied(bulkFlatSpeed));
                    }
                } else {
                    // Apply tangential acceleration, limited by bulkCurveVmax
                    double newSpeed = Math.min(bulkCurveVmax, speed + bulkCurveAcc * dt);
                    pkt.setVelocity(tangent.multiplied(newSpeed));
                }
            }
            // 3e) BulkPacketB: constant forward speed; periodic lateral deviation like Impact
            if (pkt instanceof BulkPacketB) {
                // tangent & normal at the current segment
                Vector2D tangent = pts[seg + 1].subtracted(pts[seg]).normalized();
                Vector2D normal = new Vector2D(-tangent.y(), tangent.x()); // 90° left

                // target lateral offset as a function of arc-length s
                // offset(s) = A * sin(2π * s / λ)
                double phase = (2.0 * Math.PI / Math.max(1e-6, bulkBWave)) * pow.s;
                double desiredOffset = bulkBAmpl * Math.sin(phase);

                // current lateral offset relative to the path's closest point
                Vector2D toPos = pkt.getPosition().subtracted(proj.point);
                double curOff = toPos.dot(normal); // signed distance off the wire

                // drive the packet’s center toward the desired lateral position
                // v_lateral ≈ (desired - current) * rate
                double k = 1.0 - Math.exp(-bulkBTrack * dt);         // [0..1] smoothing
                double targetLateralSpeed = (desiredOffset - curOff) * (bulkBTrack); // px/s
                double newLateralSpeed = (1.0 - k) * (toPos.dot(normal) /*proxy, no speed*/) + k * targetLateralSpeed;

                // compose final velocity: constant forward + lateral correction
                Vector2D forwardV = tangent.multiplied(bulkBSpeed);
                Vector2D lateralV = normal.multiplied(newLateralSpeed);

                pkt.setVelocity(forwardV.added(lateralV));

                // optional: clamp absolute lateral deviation so we don't "fall off"
                // keep it comfortably within the DEAD_R guard
                double maxSafe = 0.6 * DEAD_R;
                if (Math.abs(desiredOffset) > maxSafe) {
                    // scale desired amplitude if config is too large relative to DEAD_R
                    double scale = maxSafe / Math.abs(desiredOffset);
                    pkt.setVelocity(forwardV.added(normal.multiplied(newLateralSpeed * scale)));
                }
            }

            {
                // local tangent of current segment (from pts[seg] → pts[seg+1])
                Vector2D tangent = pts[seg + 1].subtracted(pts[seg]).normalized();

                // decompose current velocity
                Vector2D v = pkt.getVelocity();
                double vPar = v.dot(tangent);                         // along-wire component
                Vector2D vPerp = v.subtracted(tangent.multiplied(vPar)); // lateral component

                // CLP may legally move backward for spacing; others shouldn’t “run away” backward
                boolean allowBackward = (pkt instanceof com.mygame.model.packet.confidentialPacket.types.ConfidentialLargePacket);

                if (!allowBackward && vPar < 0) {
                    // strong exponential damping of backward component + cap its magnitude
                    double k = Math.exp(-GameConfig.BACKWARD_BRAKE_PER_S * dt); // 0..1 per frame
                    double cappedBack = -Math.min(GameConfig.BACKWARD_MAX_SPEED, Math.abs(vPar));
                    double newVPar = Math.max(cappedBack, vPar * k);            // still ≤0, shrinking

                    // also quell sideways drift while wrong-way
                    vPerp = vPerp.multiplied(GameConfig.BACKWARD_LATERAL_DAMP);

                    // rebuild the velocity
                    pkt.setVelocity(tangent.multiplied(newVPar).added(vPerp));
                }
            }
// ── Auto-resume along the wire when no longer meaningfully backward ──
            {
                // use the same local tangent we computed for the brake
                Vector2D tangent = pts[seg + 1].subtracted(pts[seg]).normalized();

                // read current velocity & its decomposition
                Vector2D v = pkt.getVelocity();
                double   vPar  = v.dot(tangent);                         // along-wire component
                Vector2D vPerp = v.subtracted(tangent.multiplied(vPar)); // lateral part

                // If we are not significantly backward anymore (vPar >= ~0), but not rolling forward either,
                // give a small forward push and gently damp sideways motion so it locks back to the path.
                if (vPar >= -1.0 && vPar < GameConfig.FORWARD_RECOVER_MIN_SPEED) {
                    double cap = baseForwardCap(pkt, bulkFlatSpeed, bulkBSpeed); // per-type sensible cap

                    // accelerate forward along tangent
                    double newVPar = Math.min(cap, vPar + GameConfig.FORWARD_RECOVER_ACCEL * dt);

                    // quell sideways drift while re-acquiring forward motion
                    vPerp = vPerp.multiplied(GameConfig.FORWARD_RECOVER_LATERAL_DAMP);

                    // rebuild velocity
                    pkt.setVelocity(tangent.multiplied(newVPar).added(vPerp));
                }
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
//    private Vector2D interpolate(Vector2D[] pts, double s) {
//        for (int i = 0; i < pts.length - 1; i++) {
//            double seg = pts[i].distanceTo(pts[i + 1]);
//            if (s <= seg) {
//                double t = s / seg;
//                return new Vector2D(
//                        pts[i].x() + t * (pts[i + 1].x() - pts[i].x()),
//                        pts[i].y() + t * (pts[i + 1].y() - pts[i].y())
//                );
//            }
//            s -= seg;
//        }
//        return pts[pts.length - 1].copy(); // end of path
//    }

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
    /** Cap the “recover” forward speed per packet type. */
    private double baseForwardCap(Packet pkt, double bulkFlatSpeed, double bulkBSpeed) {
        if (pkt instanceof com.mygame.model.packet.confidentialPacket.types.ConfidentialSmallPacket)
            return GameConfig.SPEED_OF_CONFIDENTIAL_SMALL_PACKET;
        if (pkt instanceof com.mygame.model.packet.confidentialPacket.types.ConfidentialLargePacket)
            return GameConfig.SPEED_OF_CONFIDENTIAL_Large_PACKET;
        if (pkt instanceof com.mygame.model.packet.bulkPacket.types.BulkPacketA)
            return bulkFlatSpeed;
        if (pkt instanceof com.mygame.model.packet.bulkPacket.types.BulkPacketB)
            return bulkBSpeed;
        // fallback for generic packets
        return GameConfig.defaultConfig().packetMaxSpeed;
    }


}
