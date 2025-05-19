package com.mygame.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mygame.util.ConnectionRecord;

import java.io.*;
import java.util.*;

public class GameState {
    public static int currentLevel = 1;
    public static final Map<Integer, List<ConnectionRecord>> connectionHistory = new HashMap<>();
    private static final String CONNECTIONS_FILE1 = "level1_connections.json";
    private static final String CONNECTIONS_FILE2 = "level2_connections.json";

    public static void saveConnections(int level, List<Connection> connections, List<SystemNode> nodes) {
        if (level==1) {
            try (Writer writer = new FileWriter(CONNECTIONS_FILE1)) {
                List<ConnectionRecord> records = new ArrayList<>();
                for (Connection c : connections) {
                    int fromNode = nodes.indexOf(c.getFrom().getOwner());
                    int toNode = nodes.indexOf(c.getTo().getOwner());
                    int fromPort = c.getFrom().getOwner().getPorts().indexOf(c.getFrom());
                    int toPort = c.getTo().getOwner().getPorts().indexOf(c.getTo());
                    records.add(new ConnectionRecord(fromNode, fromPort, toNode, toPort));
                }
                new Gson().toJson(records, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (level==2) {
            try (Writer writer = new FileWriter(CONNECTIONS_FILE2)) {
                List<ConnectionRecord> records = new ArrayList<>();
                for (Connection c : connections) {
                    int fromNode = nodes.indexOf(c.getFrom().getOwner());
                    int toNode = nodes.indexOf(c.getTo().getOwner());
                    int fromPort = c.getFrom().getOwner().getPorts().indexOf(c.getFrom());
                    int toPort = c.getTo().getOwner().getPorts().indexOf(c.getTo());
                    records.add(new ConnectionRecord(fromNode, fromPort, toNode, toPort));
                }
                new Gson().toJson(records, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static List<ConnectionRecord> loadConnectionsLevel1() {
        try (Reader reader = new FileReader(CONNECTIONS_FILE1)) {
            return new Gson().fromJson(reader, new TypeToken<List<ConnectionRecord>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>(); // file doesn't exist yet
        }
    }
    public static List<ConnectionRecord> loadConnectionsLevel2() {
        try (Reader reader = new FileReader(CONNECTIONS_FILE1)) {
            return new Gson().fromJson(reader, new TypeToken<List<ConnectionRecord>>(){}.getType());
        } catch (IOException e) {
            return new ArrayList<>(); // file doesn't exist yet
        }
    }
    public static boolean isLevel1Passed() {
        File file = new File(CONNECTIONS_FILE1);
        return file.exists();
    }
    public static boolean isLevel2Passed() {
        File file = new File(CONNECTIONS_FILE2);
        return file.exists();
    }
    public static void clearConnections(int level) {
        String file = (level == 1) ? CONNECTIONS_FILE1 : CONNECTIONS_FILE2;
        try {
            new File(file).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
