    // ───────────── com/mygame/model/Connection.java
    package com.mygame.model;

    import com.mygame.core.GameConfig;
    import com.mygame.engine.physics.Vector2D;
    import com.mygame.model.node.Node;

    import com.mygame.model.packet.Packet;
    import com.mygame.model.packet.ProtectedPacket;
    import com.mygame.model.packet.TrojanPacket;
    import com.mygame.model.packet.bulkPacket.types.BulkPacketA;
    import com.mygame.model.packet.bulkPacket.types.BulkPacketB;
    import com.mygame.model.packet.confidentialPacket.ConfidentialPacket;
    import com.mygame.model.packet.confidentialPacket.types.ConfidentialLargePacket;
    import com.mygame.model.packet.confidentialPacket.types.ConfidentialSmallPacket;
    import com.mygame.model.packet.messengerPacket.types.InfinityPacket;
    import com.mygame.model.packet.messengerPacket.types.SquarePacket;
    import com.mygame.model.packet.messengerPacket.types.TrianglePacket;

    import java.util.*;

    /** Phase-2 wire: straight segments + up to 3 user-defined bends. */
    public final class Connection {

        /* ── immutable endpoints ─────────────────────────────────── */
        private final Port from;
        private final Port to;

        /* ── mutable geometry ────────────────────────────────────── */
        private final List<Vector2D> bends = new ArrayList<>(3);        // max 3
        private final List<PacketOnWire> inTransit = new ArrayList<>();


        private double length;                                          // cached

        /* ── packets currently travelling along this wire ────────── */
        public final static class PacketOnWire {
            public Packet pkt;
            public double       s;        // distance along wire
            public Vector2D     off;      // lateral offset from centre-line
            public int          segmentIndex;   // NEW: track current segment


            PacketOnWire(Packet p) { pkt = p; s = 0; off = new Vector2D(); segmentIndex = 0;}

            public Packet pkt() {
                return pkt;
            }
        }

        /* ── ctor ────────────────────────────────────────────────── */
        public Connection(Port from, Port to, List<Vector2D> bendsD) {
            this.from = from;
            this.to   = to;


            for (Vector2D bend: bendsD)
                addBend(bend);

            this.length = from.getPosition().distanceTo(to.getPosition());

            recalcLength();
        }

        /* ───────────────────────  bends  ────────────────────────── */
        /** Add bend without charging coins (used when copying from draftConnection) */
        public boolean addBend(Vector2D pos) {
            if (bends.size() >= 3) return false;
            bends.add(pos.copy());
            recalcLength();
            return true;
        }
        /*  insert in Connection.java  */
        public boolean addBendAt(int segIndex, Vector2D pos){
            if (bends.size() >= 3) return false;
            bends.add(segIndex, pos.copy());   // split the segment
            recalcLength();
            return true;
        }

        /** Move existing bend `idx` within a radius (UI will enforce radius). */
        public void moveBend(int idx, Vector2D newPos) {
            if (idx < 0 || idx >= bends.size()) return;
            bends.set(idx, newPos.copy());
            recalcLength();
        }

        public List<Vector2D> getBends() { return List.copyOf(bends); }



        /** Advance every packet; deliver at far end when s ≥ length. */
        public void update(double dt) {

    //        if (inTransit.isEmpty()) return;
    //        final double ARRIVE_EPS = 4.0;          // شعاع تحویل
    //        final double MAX_OFF    = 8.0;          // حداکثر فاصله از سیم
    //
    //        final double ARRIVE_R = 6;      // ≤ this → reached port
    //        final double KILL_R   = 18;     // ≥ this → packet “falls” off wire
    //
    //        List<Vector2D> path = getPath();        // از before: from, bends…, to
    //
    //        for (Iterator<PacketOnWire> it = inTransit.iterator(); it.hasNext(); ) {
    //
    //            PacketOnWire pow = it.next();
    //            Packet       p   = pow.pkt();
    //
    //            /* 1) شتاب → سرعت → موقعیت */
    //            p.setVelocity( p.getVelocity().added( p.getAcceleration().multiplied(dt) ) );
    //            p.setPosition( p.getPosition().added( p.getVelocity().multiplied(dt) ) );
    //
    //
    //
    //            /* 2) آیا رسیده؟ */
    //            if (p.getPosition().distanceTo( to.getCenter() ) <= ARRIVE_EPS) {
    //                to.deliver(p);          // onDelivered در نود مقصد
    //                it.remove();
    //                continue;
    //            }
    //
    //            /* 3) آیا از سیم پرت شده؟ */
    //            if (distanceToPolyline(p.getPosition(), path) > MAX_OFF) {
    //                p.setAlive(false);      // یا هر منطق «مرگ» که دارید
    //                it.remove();
    //                continue;
    //            }
    //
    //            /* 3 ─ optional: mild damping so sideways velocity fades naturally */
    //            p.setVelocity( p.getVelocity().multiplied(0.99) );
    //        }
        }

        /** فاصلهٔ عمودیِ کمینه از یک نقطه به پلی‌لاین دلخواه */
        static double distanceToPolyline(Vector2D p, List<Vector2D> path) {
            double best = Double.MAX_VALUE;
            for (int i = 0; i < path.size() - 1; i++) {
                best = Math.min(best, distPointToSegment(p, path.get(i), path.get(i+1)));
            }
            return best;
        }
        private static double distPointToSegment(Vector2D p, Vector2D a, Vector2D b){
            Vector2D ap = p.subtracted(a), ab = b.subtracted(a);
            double t = ap.dot(ab) / ab.lengthSq();        // پارامتر فرافکنی
            t = Math.max(0, Math.min(1, t));              // گیره بین 0..1
            Vector2D proj = a.added(ab.multiplied(t));
            return proj.distanceTo(p);
        }



        /* ────────────────────  helpers  ─────────────────────────── */
        public Port  getFrom()   { return from; }
        public Port  getTo()     { return to; }
        /** Returns the full poly-line path:  from ⟶ bends* ⟶ to. */
        public List<Vector2D> getPath() {
            List<Vector2D> path = new ArrayList<>(bends.size() + 2);
            path.add(from.getCenter());
            path.addAll(bends);
            path.add(to.getCenter());
            return path;
        }
        public double getLength(){ return length; }

        /** Recomputes cached length whenever bends change. */
        private void recalcLength() {
            length = 0;
            Vector2D prev = from.getCenter();
            for (Vector2D b : bends) {
                length += prev.distanceTo(b);
                prev = b;
            }
            length += prev.distanceTo(to.getCenter());
        }

        /* ------------------------------------------------ packets in transit  */

        /** External systems iterate safely through the live list. */
        public ListIterator<PacketOnWire> inTransitIterator() {
            return inTransit.listIterator();
        }

        /** Called by Node when it pushes a packet onto this wire. */
        public void transmit(Packet p) {
            List<Vector2D> path = getPath();
            if (path.size() < 2) return;

            Vector2D dir = path.get(1).subtracted(path.get(0)).normalized();
            double   v0  = Math.max(p.speed(), 30.0);   // 30px/s حداقل

            if (p instanceof SquarePacket) {
                if (getFrom().getType() == Port.PortType.SQUARE) {
                    v0 = GameConfig.SPEED_OF_SQUARE_PACKET_SQUARE_PORT;
                    p.setVelocity(dir.multiplied(v0));
                    p.setAcceleration(new Vector2D());
                }
                else if (getFrom().getType() == Port.PortType.TRIANGLE) {
                    v0 = GameConfig.SPEED_OF_SQUARE_PACKET_TRIANGLE_PORT;
                    p.setVelocity(dir.multiplied(v0));
                    p.setAcceleration(new Vector2D());
                }
            }
            else if (p instanceof TrianglePacket) {
                if (getFrom().getType() == Port.PortType.TRIANGLE) {
                    v0 = GameConfig.SPEED_OF_TRIANGLE_PACKET_TRIANGLE_PORT;
                    p.setVelocity(dir.multiplied(v0));
                    p.setAcceleration(new Vector2D());
                }
                else if (getFrom().getType() == Port.PortType.SQUARE) {
                    v0 = GameConfig.SPEED_OF_TRIANGLE_PACKET_SQUARE_PORT;
                    p.setVelocity(dir.multiplied(v0));
                    double a0 = GameConfig.ACCEL_OF_TRIANGLE_PACKET_SQUARE_PORT;
                    p.setAcceleration(dir.multiplied(a0));
                }
            }
            else if (p instanceof InfinityPacket) {
                // TODO: 8/13/2025 infinity packet compatible?
            }
            else if (p instanceof TrojanPacket) {
                v0 = GameConfig.SPEED_OF_TROJAN_PACKET;
                p.setVelocity(dir.multiplied(v0));
                p.setAcceleration(new Vector2D());
            }
            else if (p instanceof ProtectedPacket) {
                if (((ProtectedPacket) p).getMovementType() == ProtectedPacket.MovementType.SQUARE) {
                    if (getFrom().getType() == Port.PortType.SQUARE) {
                        v0 = GameConfig.SPEED_OF_SQUARE_PACKET_SQUARE_PORT;
                        p.setVelocity(dir.multiplied(v0));
                        p.setAcceleration(new Vector2D());
                    }
                    else if (getFrom().getType() == Port.PortType.TRIANGLE) {
                        v0 = GameConfig.SPEED_OF_SQUARE_PACKET_TRIANGLE_PORT;
                        p.setVelocity(dir.multiplied(v0));
                        p.setAcceleration(new Vector2D());
                    }
                }
                else if (((ProtectedPacket) p).getMovementType() == ProtectedPacket.MovementType.TRIANGLE) {
                    if (getFrom().getType() == Port.PortType.TRIANGLE) {
                        v0 = GameConfig.SPEED_OF_TRIANGLE_PACKET_TRIANGLE_PORT;
                        p.setVelocity(dir.multiplied(v0));
                        p.setAcceleration(new Vector2D());
                    }
                    else if (getFrom().getType() == Port.PortType.SQUARE) {
                        v0 = GameConfig.SPEED_OF_TRIANGLE_PACKET_SQUARE_PORT;
                        p.setVelocity(dir.multiplied(v0));
                        double a0 = GameConfig.ACCEL_OF_TRIANGLE_PACKET_SQUARE_PORT;
                        p.setAcceleration(dir.multiplied(a0));
                    }
                }
                else{
                    // TODO: 8/13/2025 infinity packet compatible?
                }
            }
            else if (p instanceof ConfidentialSmallPacket) {
                v0 = GameConfig.SPEED_OF_CONFIDENTIAL_SMALL_PACKET;
                p.setVelocity(dir.multiplied(v0));
                p.setAcceleration(new Vector2D());
                /**
                 * این پکت با سرعت ثابت روی اتصالات حرکت می‌کند
                 * (مگر در اثر Impact)
                 * اما در صورتی که چنین پکتی در مسیر حرکت به سمت یک سیستم از شبکه قرار داشته باشد و پکت دیگری در این سیستم ذخیره شده باشد،
                 * سرعت خود را حد مشخصی کاهش می‌دهد تا همزمان با پکت دیگری در این سیستم حضور نداشته باشد
                 * complete the movement logic in updateWire()
                 */
            }
            else if (p instanceof ConfidentialLargePacket) {
                v0 = GameConfig.SPEED_OF_CONFIDENTIAL_Large_PACKET;
                p.setVelocity(dir.multiplied(v0));
                p.setAcceleration(new Vector2D());
                /**
                 این پکت در طی عبور یک پکت محرمانه عادی (مورد قبلی) از یک سیستم
                 VPN
                 به وجود می‌آید.
                 این پکت در هر لحظه تلاش خواهد کرد
                 فاصله مشخصی را (با حرکت به سمت جلو یا عقب روی اتصالات شبکه)
                 با تمام پکت‌های دیگر موجود روی سیم‌های شبکه حفظ کند
                 * complete the movement logic in updateWire()
                 */
            }
            else if (p instanceof BulkPacketA) {
                /**
                 * حرکت آن بر روی سیم های صاف با سرعت ثابت
                 * و بر روی انحنا‌ها، با شتاب ثابت است.
                 * (مگر در اثر Impact)
                 * * complete the movement logic in updateWire()
                 */
                v0 = GameConfig.SPEED_OF_BULKPACKET_A_PACKET;
                p.setVelocity(dir.multiplied(v0));
                p.setAcceleration(new Vector2D());
            }
            else if (p instanceof BulkPacketB) {
                /**
                 * حرکت این پکت روی تمام سیم‌ها با سرعت ثابت است
                 * اما به ازای طی مسافت مشخصی روی سیم‌ها،
                 * مرکز آن به مقدار مشخصی از روی سیم منحرف می‌شود.
                 * (مشابه اثر Impact روی حرکت پکت‌های دیگر در حال حرکت)
                 * * complete the movement logic in updateWire()
                 */
                v0 = GameConfig.SPEED_OF_BULKPACKET_B_PACKET;
                p.setVelocity(dir.multiplied(v0));
                p.setAcceleration(new Vector2D());
            }
            // TODO: 8/13/2025 complete the movement logic in updateWire()


//            p.setVelocity(dir.multiplied(v0));
            p.setPosition(path.get(0));  // from.getCenter()
            p.setRoute(from, to);
            p.attachToWire(this);
            inTransit.add(new PacketOnWire(p));
        }

        /** When a packet reaches the far port, hand it over to the node. */
        public void deliver(Packet p) {
            to.getOwner().onDelivered(p, to);   // simple delegation
        }


        /** Returns world-coordinate point at distance `s` from start. */
        private Vector2D interpolateAlongPath(double s) {
            Vector2D prev = from.getCenter();
            for (Vector2D b : bends) {
                double segLen = prev.distanceTo(b);
                if (s <= segLen) return prev.added(b.subtract(prev).multiply(s / segLen));
                s -= segLen;
                prev = b;
            }
            // last segment
            Vector2D end = to.getCenter();
            double segLen = prev.distanceTo(end);
            double t = (segLen == 0) ? 0 : s / segLen;
            return prev.added(end.subtract(prev).multiply(t));
        }

        /* ─────────────────  snapshot copy  ──────────────────────── */
        public Connection copy() {
            Connection c = new Connection(from, to, getBends());
            c.length = length;
            return c;
        }

        /* ─────────────────  validation stub  ───────────────────────*/
        public boolean isValidAgainst(List<Node> nodes) {
            List<Vector2D> path = getPath();

            for (int i = 0; i < path.size() - 1; i++) {
                Vector2D a = path.get(i);
                Vector2D b = path.get(i + 1);

                for (Node node : nodes) {
                    // Check if this segment intersects node’s bounding box
                    if (segmentIntersectsNode(a, b, node)) {
                        return false; // invalid
                    }
                }
            }
            return true;
        }
        private boolean segmentIntersectsNode(Vector2D a, Vector2D b, Node node) {
            Vector2D posNode = node.getPosition();
            Vector2D center = new Vector2D(posNode.x()+node.getWidth()/2, posNode.y()+node.getHeight()/2);
            double radius = node.getWidth()/2; // یا طول نصف ضلع مربع

            // می‌توان بررسی کرد آیا فاصله نقطه به قطعه کمتر از شعاع است:
            double d = Vector2D.distPointToSegment(center, a, b);
            return d <= radius;
        }
        // in com.mygame.model.Connection
        public boolean replaceInTransit(Packet oldPkt, Packet newPkt) {
            for (PacketOnWire pow : inTransit) {
                if (pow.pkt == oldPkt) {
                    pow.pkt = newPkt;
                    return true;
                }
            }
            return false;
        }

    }

