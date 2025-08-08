package com.mygame.engine.physics;

import com.mygame.model.Connection;
import com.mygame.model.Connection.PacketOnWire;
import com.mygame.model.packet.Packet;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class ConstantSpeed implements MovementStrategy {


    @Override
    public void update(double dt, List<Connection> wires) {
        for (Connection c : wires) {
            Iterator<Connection.PacketOnWire> it = c.inTransitIterator();
            Vector2D[] path   = c.getPath().toArray(Vector2D[]::new);
            double     length = c.getLength();

            while (it.hasNext()) {
                Connection.PacketOnWire pow = it.next();
                Packet pkt = pow.pkt();

                // ⬇️ فقط همان فیلد s را زیاد می‌کنیم؛ jitter ندارد
                // ✅ use packet's actual velocity
                double ds = pkt.getVelocity().length() * dt;
                pow.s += ds;

                if (pow.s >= length) {
                    c.getTo().deliver(pkt);      // node handles onDelivered()
                    it.remove();
                } else {
                    pkt.setPosition(interpolate(path, pow.s));
                }
            }
        }
    }


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
        return pts[pts.length - 1].copy();  // end of wire
    }
}
