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
    @FXML
    private Label nameSongLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TextArea lyricArea;

    private final String host = "localhost";
    private final int port = 7777;
    private Socket conn;
    private BufferedReader input = null;
    private BufferedWriter output = null;
    private ObjectInputStream inputOb = null;

    public MainSceneController() {
        try {
            conn = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            inputOb = new ObjectInputStream(conn.getInputStream());
        } catch (Exception e) {
            System.out.println("Khong the tao client");
        }

    }

    @FXML
    private void searchBtnClick(ActionEvent event) {
        try {

            // truyen du lieu qua server
            output.write(searchField.getText() + "\n");
            output.flush();

            HashMap<String, String> responseData = (HashMap<String, String>) inputOb.readObject();

            // kiem tra du lieu tu server
            if (responseData != null) {
                lyricArea.setText(responseData.get("songLyric"));
                nameSongLabel.setText(responseData.get("songName"));
            } else {
                nameSongLabel.setText("Không có lyric cho bài hát này");
                lyricArea.setText("Rỗng");
            }


            // nhan du lieu tu cai client
//            String reponseData = input.readLine();
//            nameSongLabel.setText(reponseData);
//            lyricArea.setText(reponseData);
//            System.out.println(responseData.get("singerName"));

            // dong socket, stream
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
