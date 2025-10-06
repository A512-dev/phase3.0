// client/view/ConnectionView.java
package client.view;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import shared.Vector2D;
import shared.snapshot.ConnectionSnapshot;



public class ConnectionView /* implements View<ConnectionSnapshot> */ {
    private static final Stroke WIRE_STROKE = new BasicStroke(3f);
    private static final Color  WIRE_COLOR  = new Color(33, 59, 11);

    // OFFLINE
    public void render(Graphics2D g, ConnectionSnapshot snap) {
        g.setStroke(WIRE_STROKE);
        g.setColor(WIRE_COLOR);

        Vector2D a = snap.fromPos();
        Vector2D b = snap.toPos();
        List<Vector2D> pts = new ArrayList<>();
        pts.add(a);
        pts.addAll(snap.bends());
        pts.add(b);
        for (int i = 0; i < pts.size()-1; i++)
            g.drawLine((int)pts.get(i).x(), (int)pts.get(i).y(),
                    (int)pts.get(i+1).x(), (int)pts.get(i+1).y());

        g.setColor(Color.ORANGE);
        for (Vector2D bend : snap.bends())
            g.fillOval((int)bend.x()-5, (int)bend.y()-5, 10, 10);
    }
}
