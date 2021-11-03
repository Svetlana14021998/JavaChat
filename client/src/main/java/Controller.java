import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextField textField, loginField;

    @FXML
    TextArea textArea;
    @FXML
    VBox mainBox;

    @FXML
    HBox authPanel, msgPanel;

    @FXML
    PasswordField passField;

    @FXML
    Button sendMsgBt;


    @FXML
    ListView<String> clientsList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private boolean authorized;
    private ObservableList<String> clients;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        authorized = false;
        Platform.runLater(() -> ((Stage) mainBox.getScene().getWindow()).setOnCloseRequest(t -> {
            sendMsg("/end");
            Platform.exit();
        }));
        textField.textProperty().addListener((observableValue, s, t1) -> sendMsgBt.setDisable(t1.isEmpty()));

        clients = FXCollections.observableArrayList();
        clientsList.setItems(clients);
    }

    private void connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket("localhost", 8189);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                new Thread(() -> {
                    try {
                        while (true) {
                            String str = in.readUTF();
                            // /authok nick
                            if (str.startsWith("/authok")) {
                                nickname = str.split(" ")[1];
                                setAuthorized(true);
                                break;
                            }
                        }
                        while (true) {
                            String str = in.readUTF();
                            if (!str.startsWith("/")) {
                                textArea.appendText(str + System.lineSeparator());
                            } else if (str.startsWith("/clientsList")) {
                                //clientsList nick1 nick2
                                String[] subStr = str.split(" ");
                                Platform.runLater(() -> {
                                    clients.clear();
                                    for (int i = 1; i < subStr.length; i++) {
                                        clients.add(subStr[i]);
                                    }
                                });


                            }else if (str.startsWith("/renameOK")){
                                nickname = str.split(" ")[1];
                                renameUser(nickname);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorized(false);
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setAuthorized(boolean authorized) {
        if (authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            nickname = "";
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        }
       renameUser(nickname);

    }
public void renameUser(String nick){
    Platform.runLater(() -> {
        if (nick.isEmpty()) {
            ((Stage) mainBox.getScene().getWindow()).setTitle("Java Chat Client");
        } else {
            ((Stage) mainBox.getScene().getWindow()).setTitle("Java Chat Client: " + nickname);
        }
    });
}
    public void sendMsg() {

        try {
            if (socket != null && !socket.isClosed()) {
                String str = textField.getText();
                out.writeUTF(str);
                textField.clear();
                textField.requestFocus();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            if (socket != null && !socket.isClosed()) {
                if (!msg.isEmpty()) {
                    out.writeUTF(msg);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAuth() {
        connect();
        // /auth login1 password1
        sendMsg("/auth " + loginField.getText() + " " + passField.getText());
        loginField.clear();
        passField.clear();

    }

    public void registerBtn() {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registration.fxml"));
            Parent root = fxmlLoader.load();
            stage.setTitle("Chat client");
            stage.setScene(new Scene(root, 400, 240));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
