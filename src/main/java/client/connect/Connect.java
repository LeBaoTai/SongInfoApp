package main.java.client.connect;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.spec.RSAOtherPrimeInfo;

public class Connect {
    private DatagramPacket sendPacket;
    private DatagramPacket receivePacket;

    private DatagramSocket socket;
    private InetAddress des;
    private int port;
    private String input;
    private String output;
    public Connect(int port, String hostname) throws Exception{
        this.port = port;
        socket = new DatagramSocket();
        des = InetAddress.getByName(hostname);
    }

    public void send() {
        try {
            byte[] bf = this.input.getBytes();
            sendPacket = new DatagramPacket(bf, bf.length, des, port);
            socket.send(sendPacket);
        } catch (Exception e) {
            System.out.println("That bai");
        }
    }

    public void receive() {
        try {
            receivePacket = new DatagramPacket(new byte[1024], 1024);
            socket.receive(receivePacket);
            output = new String(receivePacket.getData(), 0, receivePacket.getLength());
        } catch (Exception e) {
            System.out.println("That bai");
        }
    }
    public void setInput(String str) {
        input = str;
    }

    public String getOutput() {
        return output;
    }
}
