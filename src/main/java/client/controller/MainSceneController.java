package main.java.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.client.connect.Connect;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Stack;


public class MainSceneController {
    @FXML
    private Label nameSongLabel;
    @FXML
    private Label singerSongLabel;
    @FXML
    private TextField searchField;
    @FXML
    private TextArea lyricArea;
    @FXML
    private Button watchBtn;
    @FXML
    private Label composerNameLabel;
    @FXML
    private ListView<String> infoListView;

    private String host;
    private final int port = 7777;

    private Socket conn;
    private BufferedReader input = null;
    private BufferedWriter output = null;
    private ObjectInputStream inputOb = null;

    private HashMap<String, String> responseData;
    private Stage stage;
    private WebView webView;
    private WebEngine webEngine;
    private final String SECRET_KEY = "khumchodauleuleu";

    private PublicKey publicKeyServer;

    public MainSceneController() {
        try {
            conn = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            inputOb = new ObjectInputStream(conn.getInputStream());

            receivePublicKey();

        } catch (Exception e) {
            System.out.println("Can't create client!!!");
        }

    }

    private void receivePublicKey() {
        try {
            LinkedHashMap<String, Key> keyHashMap = (LinkedHashMap<String, Key>) inputOb.readObject();
            publicKeyServer = (PublicKey) keyHashMap.get("publicKey");
            sendAES();
        } catch (Exception e) {
            System.out.println("Can't receive public key!!!");
        }
    }

    private void sendAES() {
        try {
            X509EncodedKeySpec x509 = new X509EncodedKeySpec(publicKeyServer.getEncoded());
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey1 = factory.generatePublic(x509);
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, publicKey1);
            byte[] ecryptByte = c.doFinal(SECRET_KEY.getBytes());
            String encryptStr = Base64.getEncoder().encodeToString(ecryptByte);
            output.write(encryptStr + "\n");
            output.flush();
        } catch (Exception e) {
            System.out.println("Can't send AES key to SERVER!!!");
        }

    }

    private String encryptDataAES(String data) {
        try {
            SecretKeySpec spec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, spec);
            byte[] encryptedByte = c.doFinal(data.getBytes());
            String encryptedData = Base64.getEncoder().encodeToString(encryptedByte);

            return encryptedData;
        } catch (Exception e) {
            System.out.println("Can't encrypt data");
            return null;
        }
    }

    private byte[] decryptDataAES(String data) {
        try {
            SecretKeySpec spec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, spec);
            byte[] decryptedByte = c.doFinal(Base64.getDecoder().decode(data.getBytes()));
            return decryptedByte;
        } catch (Exception e) {
            System.out.println("Can't decrypt data!!!");
            return null;
        }
    }

    private LinkedHashMap<String, String> converByteToHashMap(byte[] bytes) {
        try {
            ByteArrayInputStream ByteIn = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(ByteIn);
            LinkedHashMap<String, String> data = (LinkedHashMap<String, String>) in.readObject();
            return data;
        } catch (Exception e) {
            System.out.println("Can't convert to hash map!!!");
            return null;
        }
    }

    @FXML
    private void searchBtnClick(ActionEvent event) {
        try {

            // truyen du lieu qua server
            String encryptedData = encryptDataAES(searchField.getText());
            output.write(encryptedData + "\n");
            output.flush();

            String eccyptedData = input.readLine();
            byte[] decryptByte = decryptDataAES(eccyptedData);
            responseData = converByteToHashMap(decryptByte);

            clear();

            String find = "";
            if (responseData == null) {
                infoListView.getItems().clear();
                nameSongLabel.setText("Rỗng");
                singerSongLabel.setText("");
                composerNameLabel.setText("Rỗng");
                lyricArea.setText("Rỗng");
            } else {
                find = responseData.get("find");
            }


            // kiem tra du lieu tu server
            if (responseData != null && find.equals("song")) {
                String[] singerName = responseData.get("singerName").split(" ");
                String name = "";

                for (int i = 3; i < singerName.length; i++) {
                    name += singerName[i] + " ";
                }

                lyricArea.setText(responseData.get("songLyric"));
                nameSongLabel.setText(responseData.get("songName") + " - " + name.strip());

                if (name.contains("và")) {
                    name = name.strip();
                    name = name.split("và")[0];
                }

                singerSongLabel.setText(name.strip());
                if(responseData.get("songComposer") == null) {
                    composerNameLabel.setText("Sáng tác: Unknown");
                } else {
                    composerNameLabel.setText("Sáng tác: " + responseData.get("songComposer"));
                }
                watchBtn.setVisible(true);
                watchBtn.setDisable(false);


                infoListView.getItems().clear();
                // kiểm tra thông tin có bị null kh
                String fullName = responseData.get("fullName");
                if(fullName != null)
                    infoListView.getItems().add("Tên: " + fullName);

                String dateOfBirth = responseData.get("dateOfBirth");
                if (dateOfBirth != null)
                    infoListView.getItems().add("Ngày sinh: " + dateOfBirth);

                String placeOfBirth = responseData.get("placeOfBirth");
                if (placeOfBirth != null)
                    infoListView.getItems().add("Nơi sinh: " + placeOfBirth);

                String occupation = responseData.get("occupation");
                if (occupation != null)
                    infoListView.getItems().add("Nghề nghiệp: " + occupation);

                String genre = responseData.get("genre");
                if (genre != null)
                    infoListView.getItems().add("Dòng nhạc: " + genre);

            } else if (responseData != null && find.equals("singer")) {
                infoListView.getItems().clear();
                // kiểm tra thông tin có bị null kh
                String fullName = responseData.get("fullName");
                if(fullName != null)
                    infoListView.getItems().add("Tên: " + fullName);

                String dateOfBirth = responseData.get("dateOfBirth");
                if (dateOfBirth != null)
                    infoListView.getItems().add("Ngày sinh: " + dateOfBirth);

                String placeOfBirth = responseData.get("placeOfBirth");
                if (placeOfBirth != null)
                    infoListView.getItems().add("Nơi sinh: " + placeOfBirth);

                String occupation = responseData.get("occupation");
                if (occupation != null)
                    infoListView.getItems().add("Nghề nghiệp: " + occupation);

                String genre = responseData.get("genre");
                if (genre != null)
                    infoListView.getItems().add("Dòng nhạc: " + genre);

                lyricArea.setText(responseData.get("songs").replaceAll(",", "\n\n"));
                nameSongLabel.setText(responseData.get("title"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clear() {
        lyricArea.setText("");
        nameSongLabel.setText("");
        singerSongLabel.setText("");
        watchBtn.setVisible(false);
        composerNameLabel.setText("");
        infoListView.getItems().clear();
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

    public void setHost(String host){
        this.host = host;
    }
}
