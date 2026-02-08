package edu.ucsd.studentclock.datasource;

import java.sql.Connection;

/**
 * Returns the back navigation button.
 *
 * @return back button
 */
public interface IDataSource {

    /**
     * Returns a valid JDBC connection.
     *
     * @return database connection
     */
    Connection getConnection();
}
