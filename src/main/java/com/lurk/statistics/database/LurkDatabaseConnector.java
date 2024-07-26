package com.lurk.statistics.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LurkDatabaseConnector {

    private static final Logger log = LoggerFactory.getLogger(LurkDatabaseConnector.class);

    private final String url;
    private final String username;
    private final String password;

    public LurkDatabaseConnector(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection establishConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            log.error("Error occured while establishing connection to the database", e);
            return null;
        }
    }
}
