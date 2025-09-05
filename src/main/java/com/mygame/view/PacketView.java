// ────────────────────────── com/mygame/view/PacketView.java
package com.mygame.view;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;
import com.mygame.snapshot.PacketSnapshot;
import com.mygame.view.assets.Draw;
import com.mygame.view.assets.ImageCache;


public class PacketView implements View<PacketSnapshot> {
    private final ImageCache cache = ImageCache.get();
    private final Map<Packet.Shape, Image> base = new EnumMap<>(Packet.Shape.class);

    // overlays
    private final Image antiTrojanNode  = cache.load("/nodes/AntiTrojanNodePic.jpg");
    private final Image distributorNode = cache.load("/nodes/DistributorNodePic.jpg");
    private final Image mergerNode = cache.load("/nodes/MergerNodePic.jpg");
    private final Image saboteurNode = cache.load("/nodes/SaboteurNodePic.jpg");
    private final Image spyNode = cache.load("/nodes/SpyNodePic.jpg");
    private final Image vpnNode = cache.load("/nodes/VPNNodePic.jpg");

    //packets
    private final Image squarePacket  = null;
    private final Image trianglePacket  = null;
    private final Image infinityPacket  = cache.load("/packets/infinity.png");
    private final Image hexagonPacket  = cache.load("/packets/hexagon.png");
    private final Image bulkAPacket  = cache.load("/packets/bulk_a.png");
    private final Image bulkBPacket  = cache.load("/packets/bulk_b.png");
    private final Image bitPacket  = cache.load("/nodes/bit.png");
    private final Image confidentialAPacket  = cache.load("/packets/confidential_a.png");
    private final Image confidentialBPacket  = cache.load("/packets/confidential_b.png");
    private final Image protectedPacket  = cache.load("/nodes/protected.png");
    private final Image trojanPacket  = cache.load("/nodes/trojan.png");


    public PacketView() {
        System.out.println();
        base.put(Packet.Shape.SQUARE,        null);
        base.put(Packet.Shape.TRIANGLE,      null);
        base.put(Packet.Shape.INFINITY,      cache.load("/packets/infinity.png"));
        base.put(Packet.Shape.HEXAGON,       cache.load("/packets/hexagon.png"));
        base.put(Packet.Shape.BULK_A,        cache.load("/packets/bulkPacketA.png"));
        base.put(Packet.Shape.BULK_B,        cache.load("/packets/bulkPacketB.png"));
        base.put(Packet.Shape.CONFIDENTIAL_S,cache.load("/packets/confidentialSmall.png"));
        base.put(Packet.Shape.CONFIDENTIAL_L,cache.load("/packets/confidentialLarge.png"));
        base.put(Packet.Shape.LOCK,          cache.load("/packets/protected.png")); // fallback
        base.put(Packet.Shape.TROJAN,       null); // optional
    }

    public void render(Graphics2D g, PacketSnapshot s) {
        Vector2D pos  = s.position();
        int       size  = (int) s.size();
        int x = (int) Math.round(pos.x() - size / 2.0);
        int y = (int) Math.round(pos.y() - size / 2.0);

        final double  alpha = s.opacity();
        Image img = base.get(s.shape());

        if (img == null) {
            // graceful fallback to old primitive if art is missing
            drawPrimitiveFallback(g, s);
            return;
        }
        //System.out.println(s.shape());

        // base sprite
        Draw.imageCentered(g, img, pos.x(), pos.y(), size, alpha, 0);

        // overlays (if snapshot exposes them)
        if (has(s::protectedByVPN)) {
            Draw.imageCentered(g, protectedPacket, pos.x(), pos.y() - size * 0.45, size * 0.45
                    , alpha, 0);
        }
        if (has(s::trojan)) {
            Draw.imageCentered(g, trojanPacket, pos.x() + size * 0.28, pos.y() - size * 0.28, size * 0.35
                    , alpha, 0);
        }
        if (has(s::bit)) {
            Draw.imageCentered(g, bitPacket, pos.x(), pos.y(), size * 1.05, alpha, 0);
        }




        /* body */
        if (s.shape() == Packet.Shape.SQUARE) {
            g.setColor(new Color(0,0,255,(int)(s.opacity()*255)));
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size, size);
        }
        else if (s.shape() == Packet.Shape.TRIANGLE){
            int half = size / 2;
            g.setColor(new Color(255,0,0,(int)(s.opacity()*255)));
            int[] xs = { (int) Math.round(pos.x()), (int) Math.round(pos.x()-half), (int) Math.round(pos.x()+half) };
            int[] ys = { y, y+size,       y+size };
            g.fillPolygon(xs, ys, 3);
            g.setColor(Color.BLACK);
            g.drawPolygon(xs, ys, 3);
        }
        else if (s.shape() == Packet.Shape.TROJAN) {
            int half = size / 2;
            g.setColor(Color.black);
            g.drawOval(x+half, y+half, half, half);
        }


    }
    private boolean has(BooleanSupplierEx b) {
        try { return b.getAsBoolean(); } catch (Throwable ignored) { return false; }
    }
    @FunctionalInterface private interface BooleanSupplierEx { boolean getAsBoolean(); }


    /** Only used if the sprite is missing */
    private void drawPrimitiveFallback(Graphics2D g, PacketSnapshot s) {
        Vector2D pos = s.position();
        int sz = (int) s.size();
        int x = (int) Math.round(pos.x() - sz / 2.0);
        int y = (int) Math.round(pos.y() - sz / 2.0);

        switch (s.shape()) {
            case SQUARE -> {
                g.setColor(new Color(0,0,255,(int)(s.opacity()*255)));
                g.fillRect(x, y, sz, sz);
                g.setColor(Color.BLACK); g.drawRect(x,y,sz,sz);
            }
            case TRIANGLE -> {
                int half = sz/2;
                g.setColor(new Color(255,0,0,(int)(s.opacity()*255)));
                int[] xs = {(int)pos.x(), (int)(pos.x()-half), (int)(pos.x()+half)};
                int[] ys = {y, y+sz, y+sz};
                g.fillPolygon(xs, ys, 3);
                g.setColor(Color.BLACK); g.drawPolygon(xs, ys, 3);
            }
            default -> {
                g.setColor(new Color(80,80,80,(int)(s.opacity()*255)));
                g.fillOval(x, y, sz, sz);
                g.setColor(Color.BLACK); g.drawOval(x,y,sz,sz);
            }
        }
    }
}
