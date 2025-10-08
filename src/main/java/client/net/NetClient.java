package client.net;

import shared.net.Envelope;
import shared.net.MessageType;
import shared.ser.Json;
import shared.snapshot.WorldSnapshot;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public final class NetClient {
    private final String host;
    private final int port;
    private Socket sock;
    private PrintWriter out;
    private Thread rx;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private BiConsumer<MessageType, String> onMsg;  // callback for incoming messages

    public NetClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** Try to connect to the given host/port. Returns true if successful. */
    public boolean connect() {
        try {
            sock = new Socket(host, port);
            out  = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
            running.set(true);

            rx = new Thread(this::readLoop, "Client-Read");
            rx.setDaemon(true);
            rx.start();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Read incoming messages in a background thread. */
    private void readLoop() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {
            String line;
            while (running.get() && (line = in.readLine()) != null) {
                try {

                    Envelope env = Json.from(line, Envelope.class);
                    //System.out.println("[CLIENT] RX " + env.type + " (" + (env.payload==null?0:env.payload.length()) + " bytes)");

                    if (onMsg != null) onMsg.accept(env.type, env.payload);
                } catch (Exception ex) {
                    System.err.println("⚠ Failed to parse message: " + line);
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("🔌 Connection closed by server.");
        } finally {
            running.set(false);
            close();
        }
    }

    /** Register a callback for server messages. */
    public void onMessage(BiConsumer<MessageType, String> handler) {
        this.onMsg = handler;
    }

    /** Send a JSON-encoded Envelope to the server. */
    public void send(MessageType type, String payloadJson) {
        if (out == null) return;
        Envelope env = new Envelope(type, payloadJson);
        out.println(Json.to(env));
        // NetClient.send(...)
        System.out.printf("[NET SEND] %s %s%n", type, payloadJson);


    }

    /** Gracefully close the socket and stop the read thread. */
    public void close() {
        running.set(false);
        try {
            if (sock != null && !sock.isClosed()) sock.close();
        } catch (IOException ignore) {}
        if (rx != null && rx.isAlive()) rx.interrupt();
    }

    public boolean isConnected() {
        return running.get() && sock != null && sock.isConnected();
    }
    public void sendJoinWithLevel(int level) {
        // minimalist payload; a tiny record is fine too
        class JoinPayload { public int level; JoinPayload(int l){ level=l; } }
        send(MessageType.JOIN, Json.to(new JoinPayload(level)));
    }

    // optional: expose a convenience for frame updates
    public void onFrame(java.util.function.Consumer<WorldSnapshot> h){
        onMessage((t,p)->{
            if (t == MessageType.FRAME) {
                h.accept(Json.from(p, WorldSnapshot.class));
            }
        });
    }

}
