package ru.gb.storage.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.gb.storage.commons.file.FileInfo;
import ru.gb.storage.commons.message.*;
import ru.gb.storage.server.db.Database;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private final int BUFFER_SIZE = 1024 * 64;

    private static final String rootDir = "./server/files";
    private Path rootPath = Paths.get(rootDir);
    private Path currentPath = Paths.get(rootDir);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("New active channel");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws IOException, SQLException {
        log.debug("Received message type: " + msg.getMessageType());
        Path serverPath;
        switch (msg.getMessageType()) {
            case FILE_LIST_REQUEST:
                FileListResponse fileListResponse =
                        new FileListResponse(Files.list(currentPath)
                                .map(FileInfo::new)
                                .collect(Collectors.toList()));
                if (!rootPath.equals(currentPath)) {
                    FileInfo parent = new FileInfo("..", FileInfo.FileType.BACKWARD);
                    fileListResponse.getFileInfoList().add(parent);
                }
                ctx.writeAndFlush(fileListResponse);
                break;
            case DIR_CREATE_REQUEST:
                DirCreateRequest dirCreateRequest = (DirCreateRequest) msg;
                serverPath = Paths.get(currentPath.toFile().getPath(), dirCreateRequest.getName());
                if (Files.exists(serverPath)) {
                    ctx.writeAndFlush(new DirCreateResponse(-1, String.format("Directory %s already exists", dirCreateRequest.getName())));
                } else {
                    Files.createDirectory(serverPath);
                    ctx.writeAndFlush(new DirCreateResponse(0, "Successful"));
                }
                break;
            case DIR_DOWN_REQUEST:
                DirDownRequest dirDownRequest = (DirDownRequest) msg;
                currentPath = Paths.get(currentPath.toFile().getPath(), dirDownRequest.getName());
                ctx.writeAndFlush(new DirDownResponse(0, "Successful"));
                break;
            case DIR_UP_REQUEST:
                if (!currentPath.equals(rootPath)) {
                    currentPath = currentPath.getParent();
                    ctx.writeAndFlush(new DirUpResponse(0, "Successful"));
                }
                break;
            case FILE_DELETE_REQUEST:
                FileDeleteRequest fileDeleteRequest = (FileDeleteRequest) msg;
                serverPath = Paths.get(currentPath.toFile().getPath(), fileDeleteRequest.getName());
                if (Files.isDirectory(serverPath) && serverPath.toFile().list().length != 0) {
                    ctx.writeAndFlush(new FileDeleteResponse(-1, "Directory is not empty"));
                } else {
                    Files.deleteIfExists(serverPath);
                    ctx.writeAndFlush(new FileDeleteResponse(0, "Successful"));
                }
                break;
            case AUTH_REQUEST:
                AuthRequest authRequest = (AuthRequest) msg;
                String userName = Database.getUsername(authRequest.getLogin(), authRequest.getPassword());
                AuthResponse authResponse;
                if (userName == null) {
                    authResponse = new AuthResponse(null, -1, "Incorrect login or password");
                } else {
                    authResponse = new AuthResponse(userName, 0, "Successful");
                    rootPath = Paths.get(rootDir, authRequest.getLogin());
                    currentPath = Paths.get(rootDir, authRequest.getLogin());
                    if (!Files.exists(currentPath)) {
                        Files.createDirectory(currentPath);
                    }
                }
                ctx.writeAndFlush(authResponse);
                break;
            case FILE_UPLOAD_REQUEST:
                FileUploadRequest fileUploadRequest = (FileUploadRequest) msg;
                serverPath = Paths.get(currentPath.toFile().getPath(), fileUploadRequest.getName());
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(serverPath.toFile(), "rw")) {
                    randomAccessFile.seek(fileUploadRequest.getPosition());
                    randomAccessFile.write(fileUploadRequest.getContent());
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                if (fileUploadRequest.isEndOfFile()) {
                    ctx.writeAndFlush(new FileUploadResponse(0, "Successful"));
                    break;
                }
                break;
            case FILE_DOWNLOAD_REQUEST:
                FileDownloadRequest fileDownloadRequest = (FileDownloadRequest) msg;
                Path savePath = fileDownloadRequest.getSavePath();
                serverPath = Paths.get(currentPath.toFile().getPath(), fileDownloadRequest.getFileName());
                Thread thread = new Thread(() -> {
                    try (RandomAccessFile randomAccessFile = new RandomAccessFile(serverPath.toFile(), "r")) {
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

                            ctx.writeAndFlush(new FileDownloadResponse(savePath, content, position, endOfFile)).sync();

                        } while (randomAccessFile.getFilePointer() < fileLength);

                    } catch (IOException | InterruptedException e) {
                        log.error(e.getMessage());
                    }
                });
                thread.setDaemon(true);
                thread.start();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Client disconnect");
    }
}
