package main.java.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
    private DatagramSocket socket;
    private DatagramPacket receivePacket;
    private DatagramPacket sendPacket;
    public Server(int port) throws Exception{
        socket = new DatagramSocket(port);
        receivePacket = new DatagramPacket(new byte[1024], 1024);
    }

    private void handleClient() {
        try {
            while (true) {
                socket.receive(receivePacket);
                String input = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Server get: " + input);

                sendPacket = new DatagramPacket(
                        input.toUpperCase().getBytes(),
                        input.getBytes().length,
                        receivePacket.getAddress(),
                        receivePacket.getPort());

                socket.send(sendPacket);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processData() {
        try {
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws Exception{
        Server sv = new Server(7777);
        sv.handleClient();
    }
}
