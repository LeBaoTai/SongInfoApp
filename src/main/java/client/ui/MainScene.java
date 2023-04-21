package main.java.client.ui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.client.connect.Connect;
import main.java.client.controller.MainSceneController;

public class MainScene extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        String rootPath = "../../../resources/fxml/";
//        FXMLLoader fxmlLoader = FXMLLoader.load(getClass().getResource("../../../resources/fxml/MainScene.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(rootPath+"MainScene.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) throws Exception{
        launch();
    }
}
