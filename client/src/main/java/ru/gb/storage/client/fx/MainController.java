package ru.gb.storage.client.fx;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import ru.gb.storage.client.netty.NettyClient;
import ru.gb.storage.client.properties.NetworkProperties;
import ru.gb.storage.commons.file.FileInfo;
import ru.gb.storage.commons.message.*;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;


@Slf4j
public class MainController implements Initializable {

    private final int BUFFER_SIZE = 1024 * 64;

    private Stage authStage;
    private AuthController authController;
    @FXML
    private TableView<FileInfo> fileTableView;
    @FXML
    private TextField statusBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        openConnection(NetworkProperties.getHost(), NetworkProperties.getPort());
        openAuthDialog();
        initFileTableView();
        AddMouseListener();
    }

    private void createAuthStage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/auth.fxml"));
            Parent parent = loader.load();
            authController = loader.getController();
            authStage = new Stage();
            authStage.setOnCloseRequest(this::handle);
            authStage.setTitle("Connection");
            authStage.setScene(new Scene(parent));
            authStage.initModality(Modality.APPLICATION_MODAL);
            authStage.setResizable(false);
        } catch (IOException e) {
            log.error("Error creating the authorization window", e);
            closeApp();
        }
    }

    public void sleep(long millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    private void openAuthDialog() {
        long waitTime = System.currentTimeMillis();

        createAuthStage();

        while (!NettyClient.nettyClient.isConnected()) {
            if (System.currentTimeMillis() - waitTime > 5000) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
                alert.setHeaderText("Failed to connect to the server");
                alert.showAndWait();
                closeApp();
            }
            sleep(100);
        }

        while (true) {
            if (NetworkProperties.isAuthSuccess()) {
                authStage.close();
                break;
            } else if (!NetworkProperties.isAuthRequestSent()) {
                authStage.showAndWait();
                sleep(100);
            }
        }
    }

    private void initFileTableView() {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(25);
        fileTypeColumn.setCellFactory(column -> {
            final Image dirUpImage = new Image("icons/dir-up.png");
            final Image dirImage = new Image("icons/dir.png");
            final Image fileImage = new Image("icons/file.png");
            TableCell<FileInfo, String> cell = new TableCell<>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setGraphic(null);
                    } else {
                        if (item.equals("B")) {
                            imageView.setImage(dirUpImage);
                        } else if (item.equals("D")) {
                            imageView.setImage(dirImage);
                        } else {
                            imageView.setImage(fileImage);
                        }
                        imageView.setFitHeight(16);
                        imageView.setFitWidth(16);
                        setGraphic(imageView);
                    }
                }
            };
            return cell;
        });

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        fileNameColumn.setPrefWidth(300);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(150);
        fileSizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item <= -1L) {
                        text = "";
                    }
                    setText(text);
                }
            }
        });

        fileTableView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn);
        fileTableView.getSortOrder().add(fileTypeColumn);
    }

    private void AddMouseListener() {
        fileTableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                FileInfo fileInfo = getSelectedFileInfo();
                if (fileInfo == null) {
                    return;
                }
                switch (fileInfo.getType()) {
                    case DIRECTORY:
                        sendDirDownRequest(fileInfo.getName());
                        break;
                    case BACKWARD:
                        sendDirUpRequest();
                        break;
                }
            }
        });
    }

    public void openConnection(String host, int port) {
        NettyClient.nettyClient = new NettyClient(msg -> {
            log.debug("Received message type: {}", msg.getClass());
            switch (msg.getMessageType()) {
                case FILE_LIST_RESPONSE:
                    FileListResponse fileListResponse = (FileListResponse) msg;
                    List<FileInfo> fileInfoList = fileListResponse.getFileInfoList();
                    Platform.runLater(() -> refreshFileTableView(fileInfoList));
                    break;
                case DIR_CREATE_RESPONSE:
                    DirCreateResponse dirCreateResponse = (DirCreateResponse) msg;
                    if (dirCreateResponse.getResultCode() == -1) {
                        showErrorAlert(dirCreateResponse.getResultMessage());
                    } else {
                        sendFileListRequestMessage();
                    }
                    break;
                case DIR_UP_RESPONSE:
                case DIR_DOWN_RESPONSE:
                    sendFileListRequestMessage();
                    break;
                case FILE_DELETE_RESPONSE:
                    FileDeleteResponse fileDeleteResponse = (FileDeleteResponse) msg;
                    if (fileDeleteResponse.getResultCode() == -1) {
                        showErrorAlert(fileDeleteResponse.getResultMessage());
                    } else {
                        sendFileListRequestMessage();
                    }
                    break;
                case AUTH_RESPONSE:
                    AuthResponse authResponse = (AuthResponse) msg;
                    if (authResponse.getResultCode() == 0) {
                        statusBar.setText("Username: " + authResponse.getUserName());
                        NetworkProperties.setAuthSuccess(true);
                        sendFileListRequestMessage();
                    } else {
                        NetworkProperties.setAuthRequestSent(false);
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
                            alert.setHeaderText(authResponse.getResultMessage());
                            alert.showAndWait();
                            authController.clearFields();
                        });
                    }
                    break;
                case FILE_UPLOAD_RESPONSE:
                    FileUploadResponse fileUploadResponse = (FileUploadResponse) msg;
                    if (fileUploadResponse.getResultCode() == -1) {
                        showErrorAlert(fileUploadResponse.getResultMessage());
                    } else {
                        sendFileListRequestMessage();
                    }
                    break;
                case FILE_DOWNLOAD_RESPONSE:
                    FileDownloadResponse fileDownloadResponse = (FileDownloadResponse) msg;
                    File saveFile = fileDownloadResponse.getSavePath().toFile();
                    Thread thread = new Thread(() -> {
                        try (RandomAccessFile randomAccessFile = new RandomAccessFile(saveFile, "rw")) {
                            randomAccessFile.seek(fileDownloadResponse.getPosition());
                            randomAccessFile.write(fileDownloadResponse.getContent());
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                        if (fileDownloadResponse.isEndOfFile()) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                                alert.setHeaderText("File download complete");
                                alert.showAndWait();
                            });
                        }
                    });
                    thread.setDaemon(true);
                    thread.start();
                    break;
            }
        }, host, port);
    }


    public void closeConnection() {
        if (NettyClient.nettyClient != null) {
            NettyClient.nettyClient.close();
            log.info("Network closed");
        }
    }

    private void showErrorAlert(String headerText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
            alert.setHeaderText(headerText);
            alert.showAndWait();
        });
    }

    private void refreshFileTableView(List<FileInfo> fileInfoList) {
        fileTableView.getItems().clear();
        fileTableView.getItems().addAll(fileInfoList);
        fileTableView.sort();
    }

    private FileInfo getSelectedFileInfo() {
        if (fileTableView.getItems().isEmpty()) {
            return null;
        } else if (fileTableView.getSelectionModel().getSelectedCells().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "File not selected", ButtonType.OK);
            alert.showAndWait();
            return null;
        } else {
            TablePosition pos = fileTableView.getSelectionModel().getSelectedCells().get(0);
            int row = pos.getRow();
            return fileTableView.getItems().get(row);
        }
    }

    private void closeApp() {
        closeConnection();
        System.exit(0);
    }

    private void sendFileListRequestMessage() {
        NettyClient.nettyClient.writeMessage(new FileListRequest());
    }

    private void sendCreateDirRequestMessage(String name) {
        log.debug("execute sendCreateDirRequestMessage()");
        NettyClient.nettyClient.writeMessage(new DirCreateRequest(name));
    }

    private void fileUpload(Path path) {
        Thread thread = new Thread(() -> {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r")) {
                final long fileLength = randomAccessFile.length();
                do {
                    long position = randomAccessFile.getFilePointer();

                    final long availableBytes = fileLength - position;
                    byte[] content;
                    boolean endOfFile;
                    if (availableBytes >= BUFFER_SIZE) {
                        content = new byte[BUFFER_SIZE];
                        endOfFile = false;
                    } else {
                        content = new byte[(int) availableBytes];
                        endOfFile = true;
                    }

                    randomAccessFile.read(content);

                    NettyClient.nettyClient.writeMessageSync(new FileUploadRequest(path.getFileName().toString(), content, position, endOfFile));

                } while (randomAccessFile.getFilePointer() < fileLength);

            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
                alert.setContentText("File upload error");
                alert.showAndWait();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void sendFileDeleteRequest(String name) {
        NettyClient.nettyClient.writeMessage(new FileDeleteRequest(name));
    }

    private void sendFileDownloadRequest(String fileName, Path savePath) {
        NettyClient.nettyClient.writeMessage(new FileDownloadRequest(fileName, savePath));
    }

    private void sendDirDownRequest(String name) {
        NettyClient.nettyClient.writeMessage(new DirDownRequest(name));
    }

    private void sendDirUpRequest() {
        NettyClient.nettyClient.writeMessage(new DirUpRequest());
    }

    public void actionRefresh() {
        sendFileListRequestMessage();
    }

    public void actionCreateDir() {
        log.debug("action actionCreateDir()");
        TextInputDialog inputDialog = new TextInputDialog("");
        inputDialog.setTitle("New directory");
        inputDialog.setHeaderText("Enter directory name");
        Optional<String> result = inputDialog.showAndWait();
        if (result.isPresent() && !result.get().trim().equals("")) {
            sendCreateDirRequestMessage(result.get().trim());
        }
    }

    public void actionUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the file to upload..");
        File file = fileChooser.showOpenDialog(fileTableView.getScene().getWindow());
        if (file != null) {
            fileUpload(file.toPath());
        }
    }

    public void actionFileDelete() {
        FileInfo fileInfo = getSelectedFileInfo();
        if (fileInfo == null) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("File deletion..");
        alert.setContentText("Do you want to delete the file?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            sendFileDeleteRequest(fileInfo.getName());
        }
    }

    public void actionFileDownload() {
        FileInfo fileInfo = getSelectedFileInfo();
        if (fileInfo == null) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file as..");
        fileChooser.setInitialFileName(fileInfo.getName());
        File saveFile = fileChooser.showSaveDialog(fileTableView.getScene().getWindow());
        sendFileDownloadRequest(fileInfo.getName(), Paths.get(saveFile.getPath()));
    }

    public void actionExit() {
        closeApp();
    }

    private void handle(WindowEvent event) {
        closeApp();
    }
}

