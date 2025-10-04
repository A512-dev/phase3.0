package server.sim.core.save;

import server.sim.snapshot.WorldSnapshot;

// SaveRecord.java (sketch)
public final class SaveRecord {
    private final String levelId;
    private final WorldSnapshot snapshot;

    public SaveRecord(String levelId, WorldSnapshot snapshot) {
        this.levelId = levelId;
        this.snapshot = snapshot;
    }
    public String levelId() { return levelId; }
    public WorldSnapshot snapshot() { return snapshot; }

    public static SaveRecord from(String levelId, WorldSnapshot s) {
        return new SaveRecord(levelId, s);
    }
}