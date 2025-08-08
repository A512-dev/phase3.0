package com.mygame.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.mygame.engine.physics.Vector2D;
import com.mygame.snapshot.ConnectionSnapshot;

public class ConnectionView implements View<ConnectionSnapshot> {
    private static final Stroke WIRE_STROKE = new BasicStroke(3f);
    private static final Color  WIRE_COLOR  = new Color(33, 59, 11);

    public void render(Graphics2D g, ConnectionSnapshot snap) {
        Stroke old = g.getStroke();
        g.setStroke(WIRE_STROKE);
        g.setColor(WIRE_COLOR);

        Vector2D a = snap.fromPos();
        Vector2D b = snap.toPos();
        ArrayList<Vector2D> points = new ArrayList<>();
        points.add(a);
        points.addAll(snap.bends());
        points.add(b);
        for (int i=0; i<points.size()-1; i++) {
            g.drawLine((int) points.get(i).x(), (int) points.get(i).y(),
                    (int) points.get(i+1).x(), (int) points.get(i+1).y());
        }
        for (Vector2D bend: snap.bends()) {
            g.setColor(Color.ORANGE);

            g.fillOval((int) bend.x()-5, (int) bend.y()-5, 10,10);
        }
        //g.drawLine((int) a.x(), (int) a.y(), (int) b.x(), (int) b.y());

        g.setStroke(old); // restore
    }
}
