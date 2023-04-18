package main.java.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import main.java.client.connect.Connect;


public class MainSceneController {
    private Connect connect;

    @FXML
    private Label nameSongLabel;
    @FXML
    private TextField searchField;

    public MainSceneController() throws Exception {
        connect = new Connect(7777, "localhost");
    }

    @FXML
    private void songBtnClick(ActionEvent event) {
        String text = searchField.getText();
        connect.setInput(text);
        connect.send();
        connect.receive();
        nameSongLabel.setText(connect.getOutput());
    }

    @FXML
    private void singerBtnClick(ActionEvent event) {
        String text = searchField.getText();
        nameSongLabel.setText(text);
    }
}
