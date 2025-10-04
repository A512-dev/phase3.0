// ────────────────────────── com/mygame/view/WorldView.java
package client.view;

import java.awt.*;

import server.sim.snapshot.WorldSnapshot;

/** Renders one immutable snapshot of the world. No references to live state. */
public class WorldView {
    private final PacketView     packetView     = new PacketView();
    private final ConnectionView connectionView = new ConnectionView();
    private final SystemNodeView systemNodeView = new SystemNodeView();
    private final HUDView        hudView        = new HUDView();

    public void renderAll(Graphics2D g, WorldSnapshot snap) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);

        snap.nodes().forEach(n -> systemNodeView.render(g, n));
        snap.connections().forEach(c -> connectionView.render(g, c));
        snap.packets().forEach(p -> packetView.render(g, p));
        hudView.render(g, snap.hud());
    }
}
