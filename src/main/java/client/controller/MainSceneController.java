package main.java.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class MainSceneController {

    @FXML
    private Label myLabel;

    public void buttonClick(ActionEvent event) {
        myLabel.setText("CC");
        Label nLabel = new Label("This is new window");
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(nLabel);

        Scene scene = new Scene(stackPane, 440, 100);
        Stage stage = new Stage();
        stage.setTitle("New window");
        stage.setScene(scene);
        stage.show();
    }
}
