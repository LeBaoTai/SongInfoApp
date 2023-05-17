package main.java.server;

import main.java.client.connect.Connect;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket server = null;
    private int port;

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            this.port = port;
        } catch (Exception e) {
            System.out.println("Can't create SERVER");
        }
    }

    private void ipConfig() {
        try {
            Socket socket = new Socket("bing.com", 80);
            String localIP = socket.getLocalAddress().toString().substring(1);

            String api = "https://api-generator.retool.com/m9rbpd/data/1"; // Ghi vào dòng 1 trong DB
            String jsonData = "{\"ip\":\"" + localIP + "\"}";
            Jsoup.connect(api)
                    .ignoreContentType(true).ignoreHttpErrors(true)
                    .header("Content-Type", "application/json")
                    .requestBody(jsonData)
                    .method(Connection.Method.PUT).execute();
        } catch (Exception e) {
            System.out.println("Can't config IP!!!");
        }
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
