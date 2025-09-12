// src/main/java/com/mygame/debug/LossDebug.java
package com.mygame.engine.debug;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.Packet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Central place to annotate *why* a packet was lost. */
public final class LossDebug {
    private LossDebug() {}

    // keep last ~500 reasons; auto-evict oldest
    private static final Map<Integer, String> REASONS = new LinkedHashMap<>(512, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Integer, String> e) {
            return size() > 500;
        }
    };

    /** Attach a reason without changing any packet state. */
    public static void mark(Packet p, String reason) {
        if (p == null) return;
        try { REASONS.put(p.getId(), reason); } catch (Throwable ignored) {}
    }

    /** Fetch & remove a previously marked reason for this packet (if any). */
    public static Optional<String> consume(Packet p) {
        if (p == null) return Optional.empty();
        try { return Optional.ofNullable(REASONS.remove(p.getId())); }
        catch (Throwable t) { return Optional.empty(); }
    }

    /** Pretty helper if you want to log a standard line. */
    public static String formatLine(String reason, Packet p, double gameTimeSec) {
        Vector2D pos = (p != null ? p.getPosition() : new Vector2D());
        String where = (p != null && p.getWire() != null) ? "on-wire" : "in-world";
        String cls = (p != null ? p.getClass().getSimpleName() : "?");
        int id = (p != null ? p.getId() : -1);
        return String.format("💀 t=%.3fs  id=%d  %s  %s  pos=(%.1f,%.1f)",
                gameTimeSec, id, cls, reason, pos.x(), pos.y());
    }
}
