// client.view.assets.ImageCache.java
package client.view.assets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageCache {
    private static final ImageCache I = new ImageCache();
    private final Map<String, Image> cache = new ConcurrentHashMap<>();
    private ImageCache() {}
    public static ImageCache get() { return I; }

    /** path like "/assets/packets/square.png" (on classpath, e.g. in resources) */
    public Image load(String path) {
        return cache.computeIfAbsent(path, p -> {
            try {
                var url = ImageCache.class.getResource(p);
                if (url == null) throw new IllegalArgumentException("Missing resource: " + p);
                BufferedImage raw = ImageIO.read(url);
                return raw; // keep original; scale at draw time (less memory, fine for your scale)
            } catch (Exception e) {
                System.err.println("⚠️ Image load failed: " + p + " -> " + e.getMessage());
                return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }
        });
    }
}
