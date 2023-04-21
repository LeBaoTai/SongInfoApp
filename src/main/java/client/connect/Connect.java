package main.java.client.connect;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.spec.RSAOtherPrimeInfo;

public class Connect extends Thread {
    private int port;
    private Socket socket;
    private BufferedWriter ouput;
    private BufferedReader input;

    public Connect(Socket socket) throws Exception{
        this.socket = socket;
        System.out.println("Accept Client: " + socket.toString());
    }

    public void send(String data) {
        try {
            ouput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            ouput.write(data + "\n");
            ouput.flush();
//            ouput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String receive() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String data = input.readLine();
//            input.close();
            return data;
        } catch (Exception e) {
            return "";
        }
    }

    private void closeAll() throws IOException {
        socket.close();
        ouput.close();
        input.close();
    }

    public String processData(String data) {
        return data.toUpperCase();
    }

    @Override
    public void run() {
        String data = receive();
        String processedData = processData(data);
        send(processedData);
        try {
            closeAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
