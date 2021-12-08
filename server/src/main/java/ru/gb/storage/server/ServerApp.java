package ru.gb.storage.server;

import ru.gb.storage.server.server.Server;

public class ServerApp {
    private static final int PORT = 9000;

    public static void main(String[] args) throws InterruptedException {
        new Server(PORT).start();
    }
}
