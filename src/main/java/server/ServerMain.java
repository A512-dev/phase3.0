package server;
public class ServerMain {
    public static void main(String[] args) {
        NetServer server = new NetServer(7777);
        server.start();
    }
}
