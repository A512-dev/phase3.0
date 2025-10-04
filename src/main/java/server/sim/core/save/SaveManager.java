// SaveManager.java
package server.sim.core.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SaveManager
 * ===========
 * This class implements an **autosave system** for the game.
 *
 * Features:
 *   • Periodically saves the latest game state (every 2 seconds).
 *   • Writes atomically (temp file → fsync → atomic move).
 *   • Protects save integrity with HMAC-SHA256 (tamper detection).
 *   • Reads back and validates saves at load time.
 *
 * Threading model:
 *   - Uses a single-thread ScheduledExecutorService ("Save-IO").
 *   - That thread handles all save requests, so no concurrent writes.
 *   - Game code just calls pushFrame() to push a new snapshot.
 */
public final class SaveManager implements AutoCloseable {

    /* ---------------------- CONSTANTS / SETTINGS ---------------------- */

    /** Magic string written at the start of every save file. Quick sanity check. */
    private static final String MAGIC   = "BPH_SAVE";

    /** Save file version. Bump this if the file format changes. */
    private static final int    VERSION = 1;

    /**
     * Secret key for HMAC (hardcoded for demo/coursework).
     * This makes it harder for a player to just edit the save file
     * and mark all packets as "delivered".
     */
    private static final byte[] HMAC_KEY =
            "BlueprintHell_v2_autosave_key".getBytes(StandardCharsets.UTF_8);

    /** Gson instance for JSON (immutable, thread-safe). */
    private static final Gson GSON = new GsonBuilder().create();

    /* ---------------------- EXECUTOR / THREAD ---------------------- */

    /**
     * Background thread pool that runs all IO tasks.
     * - newSingleThreadScheduledExecutor → only ONE thread.
     * - ThreadFactory here just names the thread "Save-IO" and makes it a daemon.
     * - Daemon means: if the game exits and only this thread is left,
     *   the JVM won’t stay alive just for saving.
     */
    private final ScheduledExecutorService io =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Save-IO");
                t.setDaemon(true);  // background-only, doesn't block JVM exit
                return t;
            });

    /* ---------------------- STATE ---------------------- */

    /** Save file location, e.g. autosave_level2.dat */
    private final Path path;

    /**
     * A flag toggled by the scheduler every 2s.
     * - When true → "time to save soon".
     * - pushFrame() checks this, consumes it, and triggers a write.
     * - This way we don’t save every frame, only at most once per 2s.
     */
    private final AtomicBoolean tick = new AtomicBoolean(false);

    /** The most recent snapshot pushed by the game. Volatile for thread safety. */
    private volatile SaveRecord latest;

    /* ---------------------- CONSTRUCTOR ---------------------- */

    public SaveManager(String levelId) {
        this.path = Paths.get("autosave_" + levelId + ".dat");
    }

    /* ---------------------- API METHODS ---------------------- */

    /**
     * Called by the game (usually in GamePanel.accept()) with the latest snapshot.
     * We just store it in 'latest', and if the tick flag is set we trigger an async save.
     */
    public void pushFrame(SaveRecord rec) {
        latest = rec; // keep newest snapshot in memory
        // If tick is true, reset it (set to false) and do a save
        if (tick.getAndSet(false)) writeAsync(rec);
    }

    /**
     * Start autosaving.
     * Schedules a task every 2000ms that sets tick=true.
     * (The actual writing happens in pushFrame() when a frame arrives.)
     */
    public void start() {
        io.scheduleAtFixedRate(
                () -> tick.set(true),  // every 2s, say "ok, next frame should save"
                2000,                  // initial delay
                2000,                  // period
                TimeUnit.MILLISECONDS
        );
    }

    /** Stop autosaving and kill the IO thread. */
    public void stop() { io.shutdownNow(); }

    /** Does an autosave file exist right now? */
    public boolean hasAutosave() { return Files.exists(path); }

    /** Delete the autosave file (used when player declines to continue old run). */
    public void clearAutosave()  { try { Files.deleteIfExists(path); } catch (IOException ignore) {} }

    /**
     * Load and validate a save file.
     * Reads back MAGIC, VERSION, JSON, and HMAC; verifies HMAC.
     */
    public SaveRecord load() throws Exception {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            // 1) MAGIC
            byte[] m = in.readNBytes(MAGIC.length());
            if (!MAGIC.equals(new String(m, StandardCharsets.UTF_8)))
                throw new IOException("bad magic");

            // 2) VERSION
            int ver = in.readInt();
            if (ver != VERSION) throw new IOException("version mismatch");

            // 3) JSON data
            int jsonLen = in.readInt();
            byte[] json = in.readNBytes(jsonLen);

            // 4) Signature
            int sigLen = in.readInt();
            byte[] sig = in.readNBytes(sigLen);

            // 5) Verify integrity
            if (!verify(json, sig))
                throw new SecurityException("tampered or corrupt autosave");

            // Parse JSON back to SaveRecord
            return GSON.fromJson(new String(json, StandardCharsets.UTF_8), SaveRecord.class);
        }
    }

    /* ---------------------- INTERNAL WRITE ---------------------- */

    /**
     * Schedule an async save on the IO thread.
     * This does:
     *   - Serialize SaveRecord → JSON bytes
     *   - Compute HMAC
     *   - Write MAGIC + VERSION + JSON + HMAC to a .tmp file
     *   - fsync (force write to disk)
     *   - Atomically rename tmp → final
     */
    private void writeAsync(SaveRecord rec) {
        io.execute(() -> {
            try {
                // 1) Serialize and sign
                byte[] json = GSON.toJson(rec).getBytes(StandardCharsets.UTF_8);
                byte[] sig  = sign(json);

                // 2) Temp file
                Path tmp = Paths.get(path.toString() + ".tmp");
                try (FileChannel ch = FileChannel.open(
                        tmp,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                )) {
                    // Write MAGIC + VERSION
                    ByteBuffer hdr = ByteBuffer.allocate(MAGIC.length() + Integer.BYTES);
                    hdr.put(MAGIC.getBytes(StandardCharsets.UTF_8));
                    hdr.putInt(VERSION);
                    hdr.flip();
                    ch.write(hdr);

                    // Write JSON length + JSON
                    ch.write(ByteBuffer.allocate(Integer.BYTES).putInt(json.length).flip());
                    ch.write(ByteBuffer.wrap(json));

                    // Write SIG length + SIG
                    ch.write(ByteBuffer.allocate(Integer.BYTES).putInt(sig.length).flip());
                    ch.write(ByteBuffer.wrap(sig));

                    // fsync: guarantee everything hits the disk
                    ch.force(true);
                }

                // 3) Atomic replace
                Files.move(
                        tmp,
                        path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (Exception e) {
                e.printStackTrace(); // log but don’t crash game
            }
        });
    }

    /* ---------------------- HMAC HELPERS ---------------------- */

    /** Produce HMAC-SHA256 of given bytes. */
    private static byte[] sign(byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(HMAC_KEY, "HmacSHA256"));
        return mac.doFinal(data);
    }

    /** Constant-time verify of signature vs recalculated HMAC. */
    private static boolean verify(byte[] data, byte[] sig) throws Exception {
        byte[] calc = sign(data);
        if (calc.length != sig.length) return false;
        int diff = 0;
        for (int i = 0; i < calc.length; i++) diff |= (calc[i] ^ sig[i]);
        return diff == 0;
    }

    /* ---------------------- CLEANUP ---------------------- */

    @Override public void close() { stop(); }
}
