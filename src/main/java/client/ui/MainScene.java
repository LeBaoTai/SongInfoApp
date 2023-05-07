package main.java.client.ui;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.java.client.connect.Connect;
import main.java.client.controller.MainSceneController;

public class MainScene extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        String rootPath = "../../../resources/";
//        FXMLLoader fxmlLoader = FXMLLoader.load(getClass().getResource("../../../resources/fxml/MainScene.fxml"));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(rootPath+"fxml/MainScene.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        stage.setTitle("Ứng dụng tra cứu thông tin bài hát và ca sĩ");
        stage.getIcons().add(new Image(MainScene.class.getResourceAsStream(rootPath+"picture/microphone.png")));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        MainSceneController controller = (MainSceneController) fxmlLoader.getController();
        controller.setStage(stage);


        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            close(stage, fxmlLoader);
        });
    }

    private void close(Stage stage, FXMLLoader fxmlLoader) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thoát");
        alert.setHeaderText("Bạn đang thoát app!!!");
        alert.setContentText("Bạn chắc chắn chứ :<<");

        if (alert.showAndWait().get() == ButtonType.OK) {
            System.out.println("Closed");
            MainSceneController controller = (MainSceneController) fxmlLoader.getController();
            controller.closeStream();
            stage.close();
        }
    }

    public static void main(String[] args) throws Exception{
        launch();
    }
}
