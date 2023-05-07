package main.java.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.client.connect.Connect;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;


public class MainSceneController {
    @FXML
    private Label nameSongLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TextArea lyricArea;
    @FXML
    private Button watchBtn;

    private final String host = "localhost";
    private final int port = 7777;

    private Socket conn;
    private BufferedReader input = null;
    private BufferedWriter output = null;
    private ObjectInputStream inputOb = null;

    private HashMap<String, String> responseData;
    private Stage stage;
    private WebView webView;
    private WebEngine webEngine;

    public MainSceneController() {
        try {
            conn = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            inputOb = new ObjectInputStream(conn.getInputStream());
        } catch (Exception e) {
            System.out.println("Can't create client!!!");
        }

    }

    @FXML
    private void searchBtnClick(ActionEvent event) {
        try {

            // truyen du lieu qua server
            output.write(searchField.getText() + "\n");
            output.flush();

            responseData = (HashMap<String, String>) inputOb.readObject();

            // kiem tra du lieu tu server
            if (responseData != null) {
                lyricArea.setText(responseData.get("songLyric"));
                nameSongLabel.setText(responseData.get("songName"));
                watchBtn.setDisable(false);
            } else {
                nameSongLabel.setText("Không có lyric cho bài hát này");
                lyricArea.setText("Rỗng");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void watchVideoClick() {
        String link = responseData.get("linkVideo");
        String songName = responseData.get("songName");
        String singerName = responseData.get("singerName");
        String title = songName + " - " + singerName;
        String rootPath = "../../../resources/";

        try {
            webView = new WebView();
            webEngine = webView.getEngine();
            webEngine.load(link);
            webView.setPrefSize(800, 500);


            StackPane pane = new StackPane();
            pane.getChildren().add(webView);
            Scene secondScene = new Scene(pane);
            Stage secondStage = new Stage();


            secondStage.getIcons().add(new Image("src/main/resources/picture/video.png"));
            secondStage.setTitle(title);
            secondStage.initModality(Modality.NONE);
            secondStage.initOwner(stage);
            secondStage.setScene(secondScene);
            secondStage.show();

            secondStage.setOnCloseRequest(windowEvent -> {
                webEngine.load(null);
            });
        } catch (Exception e) {
            System.out.println("Can't load Watch Scene!!!");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void closeStream() {
        try {
            output.write("close" + "\n");
            output.flush();

            inputOb.close();
            input.close();
            output.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Can't close all STREAM!!!");
        }
    }
}
