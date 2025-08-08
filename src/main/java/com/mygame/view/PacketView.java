// ────────────────────────── com/mygame/view/PacketView.java
package com.mygame.view;

import java.awt.*;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.node.Node;
import com.mygame.model.packet.Packet;
import com.mygame.snapshot.PacketSnapshot;


public class PacketView implements View<PacketSnapshot> {

    public void render(Graphics2D g, PacketSnapshot s) {
        Vector2D pos  = s.position();
        int       sz  = (int) s.size();
        int x = (int) Math.round(pos.x() - sz / 2.0);
        int y = (int) Math.round(pos.y() - sz / 2.0);


        /* body */
        if (s.shape() == Packet.Shape.SQUARE) {
            g.setColor(new Color(0,0,255,(int)(s.opacity()*255)));
            g.fillRect(x, y, sz, sz);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, sz, sz);
        }
        else if (s.shape() == Packet.Shape.TRIANGLE){
            int half = sz / 2;
            g.setColor(new Color(255,0,0,(int)(s.opacity()*255)));
            int[] xs = { (int) Math.round(pos.x()), (int) Math.round(pos.x()-half), (int) Math.round(pos.x()+half) };
            int[] ys = { y, y+sz,       y+sz };
            g.fillPolygon(xs, ys, 3);
            g.setColor(Color.BLACK);
            g.drawPolygon(xs, ys, 3);
        }
    }

}
