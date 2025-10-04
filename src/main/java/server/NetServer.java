// server/NetServer.java  (trimmed)
package server;

import shared.net.*;
import shared.ser.Json;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public final class NetServer {
    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Map<String,ClientSession> sessions = new ConcurrentHashMap<>();
    private final Map<String,ServerGameLoop> games   = new ConcurrentHashMap<>();

    public NetServer(int port){ this.port = port; }

    public void start() {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                System.out.println("Server listening on 0.0.0.0:" + port);
                while (true) pool.submit(() -> {
                    try {
                        acceptOne(ss.accept());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) { e.printStackTrace(); }
        }, "Server-Accept").start();
    }

    private void acceptOne(Socket s){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out   = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);

            String sessionId = UUID.randomUUID().toString();
            ClientSession cs = new ClientSession(this, s, out, sessionId);
            sessions.put(sessionId, cs);

            // greet
            cs.send(new Envelope(MessageType.JOIN_OK, sessionId));

            String line;
            while ((line = in.readLine()) != null){
                Envelope env = Json.from(line, Envelope.class);
                onEnvelope(cs, env);
            }
        } catch (IOException ignore) {
        } finally {
            // cleanup
            // stop game if running
        }
    }

    private void onEnvelope(ClientSession s, Envelope env){
        switch (env.type) {
            case START_GAME -> {
                int level = Json.from(env.payload, shared.dto.StartGameDTO.class).level;
                startGameFor(s, level);
            }
            case INPUT_COMMAND -> {
                ClientCommand cmd = Json.from(env.payload, ClientCommand.class);
                ServerGameLoop g = games.get(s.id());
                if (g != null) g.applyInput(s.id(), cmd);
            }
            default -> { /* ignore */ }
        }
    }

    private void startGameFor(ClientSession s, int level){
        // if already running, restart
        ServerGameLoop old = games.remove(s.id());
        if (old != null) old.stop();

        ServerGameLoop g = new ServerGameLoop(level, frameDto -> {
            // send frames only to THIS client
            s.send(new Envelope(MessageType.FRAME_UPDATE, Json.to(frameDto)));
        });
        games.put(s.id(), g);
        g.start();
    }
}
