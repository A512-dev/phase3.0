// server/ClientSession.java
package server;

import shared.net.Envelope;
import shared.ser.Json;
import java.io.PrintWriter;
import java.net.Socket;

final class ClientSession {
    private final NetServer server;
    private final Socket socket;
    private final PrintWriter out;
    private final String id;

    ClientSession(NetServer server, Socket socket, PrintWriter out, String id){
        this.server = server;
        this.socket = socket;
        this.out    = out;
        this.id     = id;
    }

    String id(){ return id; }

    void send(Envelope env){
        out.println(Json.to(env));
    }
}
