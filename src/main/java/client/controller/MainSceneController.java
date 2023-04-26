package main.java.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import main.java.client.connect.Connect;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;


public class MainSceneController {
    private Socket connect;

    @FXML
    private Label nameSongLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TextArea lyricArea;

    private final String host = "localhost";
    private final int port = 7777;

    @FXML
    private void searchBtnClick(ActionEvent event) {
        try {
            connect = new Socket(host, port);
            BufferedReader input = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(connect.getOutputStream()));

//            InputStream

            // truyen du lieu qua server
            output.write(searchField.getText() + "\n");
            output.flush();

            ObjectInputStream inputOb = new ObjectInputStream(connect.getInputStream());
            HashMap<String, String> responseData = (HashMap<String, String>) inputOb.readObject();

            // kiem tra du lieu tu server
            if (responseData != null) {
                lyricArea.setText(responseData.get("songLyric"));
                nameSongLabel.setText(responseData.get("songName"));
            }

            // nhan du lieu tu cai client
//            String reponseData = input.readLine();
//            nameSongLabel.setText(reponseData);
//            lyricArea.setText(reponseData);
//            System.out.println(responseData.get("singerName"));

            // dong socket, stream
            connect.close();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
