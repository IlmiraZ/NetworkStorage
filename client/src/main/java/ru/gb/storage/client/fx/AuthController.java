package ru.gb.storage.client.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ru.gb.storage.client.netty.NettyClient;
import ru.gb.storage.client.properties.NetworkProperties;
import ru.gb.storage.commons.message.AuthRequest;

@Slf4j
public class AuthController {

    @FXML
    private TextField loginTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Button authButton;
    @FXML
    private Button cancelButton;

    @FXML
    private void btnAuth() {
        if (loginTextField.getText().trim().isEmpty() || passwordTextField.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
            alert.setHeaderText("Enter login and password");
            alert.showAndWait();
            return;
        }
        auth();
    }

    @FXML
    private void bthCancel() {
        close();
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public void clearFields() {
        loginTextField.clear();
        passwordTextField.clear();
    }

    public void setDisableControls(boolean value) {
        loginTextField.setDisable(value);
        passwordTextField.setDisable(value);
        authButton.setDisable(value);
        cancelButton.setDisable(value);
    }

    private void auth() {
        setDisableControls(true);
        NettyClient.nettyClient.writeMessage(new AuthRequest(loginTextField.getText(), passwordTextField.getText()));
        NetworkProperties.setAuthRequestSent(true);
        NetworkProperties.incAuthCount();
        new Thread(() -> {
            while (true) {
                if (NetworkProperties.isAuthSuccess()) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) authButton.getScene().getWindow();
                        stage.close();
                    });
                    break;
                }
                if (NetworkProperties.getAuthCount() > 2) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
                        alert.setHeaderText("Exceeded the number of authorization attempts");
                        alert.showAndWait();
                        close();
                    });
                    break;
                } else {
                    Platform.runLater(() -> setDisableControls(NetworkProperties.isAuthRequestSent()));
                    sleep(100);
                }
            }
        }).start();
    }

    private void close() {
        NettyClient.nettyClient.close();
        System.exit(0);
    }
}
