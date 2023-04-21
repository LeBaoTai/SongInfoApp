package main.java.server;

import main.java.client.connect.Connect;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket server = null;
    private int port;

    public Server(int port) throws Exception{
        server = new ServerSocket(port);
        this.port = port;
    }

    private void handleClient() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            System.out.println("Server port: " + port);
            while (true) {
                Socket client = server.accept();
                executor.execute(new Connect(client));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (server != null) {
            server.close();
        }
    }

    public static void main(String[] args) throws Exception{
        Server sv = new Server(7777);
        sv.handleClient();
    }
}
