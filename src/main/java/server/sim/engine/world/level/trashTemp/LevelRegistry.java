//// com.mygame.level.LevelRegistry.java
//package server.sim.engine.world.level;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//public final class LevelRegistry {
//    private static final Map<String, Level> MAP = new LinkedHashMap<>();
//    static {
//        register(new LevelGeneral());
//        register(new LevelBase2());
//        // debug/sandboxes below…
//        register(new SandboxBulk());
//        register(new SandboxProtected());
//        register(new SandboxVPN());
//        register(new SandboxTrojan());
//    }
//    private static void register(LevelBase l) { MAP.put(l.id(), l); }
//    public static LevelBase byId(String id)   { return MAP.get(id); }
//    public static Map<String, LevelBase> all(){ return MAP; }
//}
