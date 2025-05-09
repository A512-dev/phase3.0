package com.mygame.view;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.mygame.model.World;
import com.mygame.model.Packet;
import com.mygame.model.Connection;
import com.mygame.model.SystemNode;

public class WorldView {
    private final PacketView     packetView     = new PacketView();
    private final ConnectionView connectionView = new ConnectionView();
    private final SystemNodeView systemNodeView = new SystemNodeView();
    private final HUDView        hudView        = new HUDView();

    public void renderAll(Graphics2D g, World world) {
        long start = System.nanoTime();
        for (Connection c : world.getConnections()) {
            connectionView.render(g, c);
        }
        for (SystemNode n : world.getNodes()) {
            systemNodeView.render(g, n);
        }
        List<Packet> packets = new ArrayList<>(world.getPackets());
        for (Packet p : packets) {
            long pstart = System.nanoTime();
            packetView.render(g, p);
            long pend = System.nanoTime();
            System.out.println("Packet render took: " + (pend - pstart) / 1_000_000.0 + " ms");
        }

//        for (Packet p : world.getPackets()) {
//            packetView.render(g, p);
//        }
        hudView.render(g, world.getHudState());
        long end = System.nanoTime();
        System.out.println("RENDER ALL took: " + (end - start) / 1_000_000.0 + " ms");
    }
}
