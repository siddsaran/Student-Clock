package edu.ucsd.studentclock.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlDataSource implements IDataSource {
    private final String url;

    /**
     * SQLite implementation of IDataSource.
     */
    public SqlDataSource(String dbPath) {
        if (dbPath == null) {
            throw new NullPointerException("dbPath must not be null");
        }
        this.url = "jdbc:sqlite:" + dbPath;
    }

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }
    
    
}
