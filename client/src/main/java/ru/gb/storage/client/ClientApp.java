package ru.gb.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.gb.storage.client.fx.MainController;
import ru.gb.storage.client.properties.NetworkProperties;

public class ClientApp extends Application {
    private static final String HOST = "localhost";
    private static final int PORT = 9000;
    private static MainController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        NetworkProperties.setHost(HOST);
        NetworkProperties.setPort(PORT);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("NetworkStorage");
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.setOnCloseRequest(event -> {
            controller = loader.getController();
            controller.closeConnection();
            System.exit(0);
        });
        primaryStage.show();
    }
}
