package ru.gb.storage.server.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class Database {
    public static Connection connection;
    public static PreparedStatement statement;
    public static ResultSet resultSet;

    public static void openDB() throws ClassNotFoundException, SQLException {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:./server/db/network-storage.db3");
        log.debug("Database is open");
    }

    public static void closeDB() {
        try {
            connection.close();
            statement.close();
            resultSet.close();
            log.debug("Database is closed");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public static String getUsername(String login, String password) throws SQLException {
        if (connection == null || connection.isClosed()) return null;
        statement = connection.prepareStatement("select username from users where login = ? and pass = ?");
        statement.setString(1, login);
        statement.setString(2, password);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return rs.getString("username");
        } else {
            return null;
        }
    }
}
