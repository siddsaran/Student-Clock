package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Series;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persists and retrieves series using JDBC with SQLite.
 */
public class SeriesRepository implements ISeriesRepository {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS series (id TEXT PRIMARY KEY, courseId TEXT NOT NULL, name TEXT NOT NULL, defaultLateDays INTEGER NOT NULL)";
    private static final String INSERT_SQL =
            "INSERT OR REPLACE INTO series (id, courseId, name, defaultLateDays) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL =
            "SELECT id, courseId, name, defaultLateDays FROM series WHERE id = ?";
    private static final String SELECT_BY_COURSE_SQL =
            "SELECT id, courseId, name, defaultLateDays FROM series WHERE courseId = ?";

    private final Connection connection;

    /**
     * Creates a repository that uses the given data source for all operations.
     *
     * @param dataSource a valid data source (e.g. SQLite)
     */
    public SeriesRepository(IDataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource must not be null");
        }
        this.connection = dataSource.getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create series table", e);
        }
    }

    /**
     * Stores a series. If a series with the same id already exists, it is replaced.
     *
     * @param series the series to add (must not be null)
     */
    public void addSeries(Series series) {
        if (series == null) {
            throw new NullPointerException("series must not be null");
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, series.getId());
            statement.setString(2, series.getCourseId());
            statement.setString(3, series.getName());
            statement.setInt(4, series.getDefaultLateDays());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add series", e);
        }
    }

    /**
     * Returns the series with the given id, or empty if not found.
     *
     * @param id the series id (null is treated as absent, returns Optional.empty())
     * @return Optional containing the series if found, otherwise empty
     */
    public Optional<Series> getSeries(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get series", e);
        }
    }

    /**
     * Returns all series for the given course in an unmodifiable list.
     *
     * @param courseId the course id
     * @return list of series for the course (never null, may be empty)
     */
    public List<Series> getSeriesByCourse(String courseId) {
        if (courseId == null) {
            return List.of();
        }
        List<Series> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_COURSE_SQL)) {
            statement.setString(1, courseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapRow(resultSet));
                }
            }
            return List.copyOf(list);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get series by course", e);
        }
    }

    private static Series mapRow(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        String cid = resultSet.getString("courseId");
        String name = resultSet.getString("name");
        int defaultLateDays = resultSet.getInt("defaultLateDays");
        return new Series(id, cid, name, defaultLateDays);
    }
}
