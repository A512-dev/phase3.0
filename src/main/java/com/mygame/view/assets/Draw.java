// com.mygame.view.assets.Draw.java
package com.mygame.view.assets;

import java.awt.*;
import java.awt.geom.AffineTransform;

public final class Draw {
    private Draw() {}
    /** Draws img centered at (cx,cy), scaled to (size x size), with opacity and rotation (radians). */
    public static void imageCentered(Graphics2D g, Image img, double cx, double cy,
                                     double width, double height, double opacity, double rotationRad) {
        if (img == null) return;
        Composite oldComp = g.getComposite();
        AffineTransform oldTx = g.getTransform();

        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;

        double sx = width / iw;
        double sy = height / ih;

        AffineTransform tx = new AffineTransform();
        tx.translate(cx, cy);
        tx.rotate(rotationRad);
        tx.scale(sx, sy);
        tx.translate(-iw / 2.0, -ih / 2.0);

        if (opacity < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0f, Math.min(1f, opacity))));
        g.drawImage(img, tx, null);

        g.setComposite(oldComp);
        g.setTransform(oldTx);
    }
    public static void imageCentered(Graphics2D g, Image img, double cx, double cy,
                                     double size, double opacity, double rotationRad) {
        if (img == null) return;
        Composite oldComp = g.getComposite();
        AffineTransform oldTx = g.getTransform();

        int iw = img.getWidth(null);
        int ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;

        double sx = size / iw;
        double sy = size / ih;

        AffineTransform tx = new AffineTransform();
        tx.translate(cx, cy);
        tx.rotate(rotationRad);
        tx.scale(sx, sy);
        tx.translate(-iw / 2.0, -ih / 2.0);

        if (opacity < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0f, Math.min(1f, opacity))));
        g.drawImage(img, tx, null);

        g.setComposite(oldComp);
        g.setTransform(oldTx);
    }
}
