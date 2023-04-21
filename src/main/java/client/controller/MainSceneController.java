package main.java.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.java.client.connect.Connect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class MainSceneController {
    private Socket connect;

    @FXML
    private Label nameSongLabel;
    @FXML
    private TextField searchField;

    private final String host = "localhost";
    private final int port = 7777;

    @FXML
    private void songBtnClick(ActionEvent event) {
        try {
            connect = new Socket(host, port);
            BufferedReader input = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(connect.getOutputStream()));

            // truyen du lieu qua server
            output.write(searchField.getText() + "\n");
            output.flush();

            // nhan du lieu tu cai client
            String reponseData = input.readLine();
            nameSongLabel.setText(reponseData);


            // dong socket, stream
            connect.close();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void singerBtnClick(ActionEvent event) {
        String text = searchField.getText();
        nameSongLabel.setText(text);
    }
}
