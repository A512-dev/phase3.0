package client.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import server.sim.model.Connection;
import server.sim.model.node.Node;

import java.io.*;
import java.util.*;

public class GameState {
    public static String currentLevelID = "level1";
    public static int currentLevelInt = 1;
    public static final Map<Integer, List<ConnectionRecord>> connectionHistory = new HashMap<>();
    private static final String CONNECTIONS_FILE1 = "level1_connections.json";
    private static final String CONNECTIONS_FILE2 = "level2_connections.json";

    public static void saveConnections(String level, List<Connection> connections, List<Node> nodes) {
        String path = getPathOfLevel(level);
        try (Writer writer = new FileWriter(path)) {
            List<ConnectionRecord> records = new ArrayList<>();
            for (Connection c : connections) {
                int fromNode = nodes.indexOf(c.getFrom().getOwner());
                int toNode = nodes.indexOf(c.getTo().getOwner());
                int fromPort = c.getFrom().getOwner().getPorts().indexOf(c.getFrom());
                int toPort = c.getTo().getOwner().getPorts().indexOf(c.getTo());
                records.add(new ConnectionRecord(fromNode, fromPort, toNode, toPort, c.getBends()));
            }
            new Gson().toJson(records, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<ConnectionRecord> loadConnections(String levelID) {
        String path = getPathOfLevel(levelID);
        try (Reader reader = new FileReader(path)) {
            return new Gson().fromJson(reader, new TypeToken<List<ConnectionRecord>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>(); // file doesn't exist yet
        }
    }
    public static boolean isLevelPassed(String levelId) {
        String path = getPathOfLevel(levelId);
        File file = new File(path);
        return file.exists();
    }

    private static String getPathOfLevel(String levelId) {
        return levelId+"_connections.json";
    }

    public static void clearConnections(int level) {
        String file = getPathOfLevel("level"+level);
        try {
            new File(file).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
