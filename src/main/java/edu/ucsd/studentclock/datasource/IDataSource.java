package edu.ucsd.studentclock.datasource;

import java.sql.Connection;

/**
 * Abstraction for obtaining a database connection.
 */
public interface IDataSource {

    /**
     * Returns a valid JDBC connection.
     *
     * @return database connection
     */
    Connection getConnection();
}
