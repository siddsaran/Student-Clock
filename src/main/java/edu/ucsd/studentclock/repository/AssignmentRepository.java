package edu.ucsd.studentclock.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Assignment;

public class AssignmentRepository {

    /**
     * Persists and retrieves assignments using JDBC with SQLite.
     */
    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS assignments (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "courseID TEXT NOT NULL, " +
                "seriesId TEXT, " +
                "start TEXT NOT NULL, " +
                "deadline TEXT NOT NULL, " +
                "lateDaysAllowed INTEGER, " +
                "estimatedHours REAL, " +
                "remainingHours REAL, " +
                "done INTEGER)";

    private static final String INSERT_SQL =
        "INSERT OR REPLACE INTO assignments " +
                "(id, name, courseID, seriesId, start, deadline, " +
                "lateDaysAllowed, estimatedHours, remainingHours, done) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_COURSE_SQL =
            "SELECT * FROM assignments WHERE courseID = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM assignments";

    private static final String DELETE_BY_ID_SQL =
        "DELETE FROM assignments WHERE id = ?";

    private final Connection connection;

    /**
     * Creates a repository that uses the given data source for all operations.
     *
     * @param dataSource a valid data source (e.g. SQLite)
     */
    public AssignmentRepository(IDataSource dataSource) {
        if (dataSource == null){
            throw new NullPointerException("dataSource must not be null");
        }
        this.connection = dataSource.getConnection();
        createTableIfNotExists();
    }

    /**
     * Creates the assignments table if it does not already exist.
     * Adds seriesId column to existing tables that do not have it.
     */
    private void createTableIfNotExists() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
            statement.execute("ALTER TABLE assignments ADD COLUMN seriesId TEXT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column name")) {
                throw new RuntimeException("Failed to create or migrate assignments table", e);
            }
        }
    }

    /**
     * Stores an assignment in the database. If an assignment with the same id already
     * exists, it will be replaced.
     *
     * @param assignment the assignment to persist (must not be null)
     */
    public void addAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new NullPointerException("assignment must not be null");
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, assignment.getID());
            statement.setString(2, assignment.getName());
            statement.setString(3, assignment.getCourseID());
            statement.setString(4, assignment.getSeriesId());
            statement.setString(5, assignment.getStart().toString());
            statement.setString(6, assignment.getDeadline().toString());
            statement.setInt(7, assignment.getLateDaysAllowed());
            statement.setDouble(8, assignment.getEstimatedHours());
            statement.setDouble(9, assignment.getRemainingHours());
            statement.setBoolean(10, assignment.isDone());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add assignment", e);
        }
    }

    /**
     * Deletes the assignment with the given id. This method does nothing if the id
     * is null or blank.
     *
     * @param id the assignment id to delete
     */
    public void deleteAssignment(String id) {
        if (id == null) return;
        String trimmed = id.trim();
        if (trimmed.isEmpty()) return;

        try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            statement.setString(1, trimmed);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete assignment", e);
        }
    }

    /**
     * Returns all assignments associated with the given course id.
     *
     * @param courseID the course id
     * @return list of assignments for the course (never null)
     */
    public List<Assignment> getAssignmentsForCourse(String courseID) {
        List<Assignment> assignmentList = new ArrayList<>();
        if (courseID == null) return List.of();

        try (PreparedStatement statement =
                     connection.prepareStatement(SELECT_BY_COURSE_SQL)) {

            statement.setString(1, courseID);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignmentList.add(mapRow(resultSet));
                }
            }

            return List.copyOf(assignmentList);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get assignments for course", e);
        }
    }

    /**
     * Returns all assignments across all courses.
     *
     * @return list of all assignments (never null)
     */
    public List<Assignment> getAllAssignments() {
        List<Assignment> assignmentList = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_SQL)) {

            while (resultSet.next()) {
                assignmentList.add(mapRow(resultSet));
            }

            return List.copyOf(assignmentList);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all assignments", e);
        }
    }

    private static final String SELECT_BY_SERIES_SQL =
            "SELECT * FROM assignments WHERE seriesId = ?";

    /**
     * Returns all assignments in the given series.
     *
     * @param seriesId the series id (null returns empty list)
     * @return list of assignments in the series (never null)
     */
    public List<Assignment> getAssignmentsBySeries(String seriesId) {
        if (seriesId == null) {
            return List.of();
        }
        List<Assignment> assignmentList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_SERIES_SQL)) {
            statement.setString(1, seriesId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    assignmentList.add(mapRow(resultSet));
                }
            }
            return List.copyOf(assignmentList);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get assignments by series", e);
        }
    }

    private static Assignment mapRow(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        String name = resultSet.getString("name");
        String cid = resultSet.getString("courseID");
        String seriesId = resultSet.getString("seriesId");
        LocalDateTime start = LocalDateTime.parse(resultSet.getString("start"));
        LocalDateTime deadline = LocalDateTime.parse(resultSet.getString("deadline"));
        int lateDays = resultSet.getInt("lateDaysAllowed");
        double estimated = resultSet.getDouble("estimatedHours");
        double remaining = resultSet.getDouble("remainingHours");
        boolean done = resultSet.getBoolean("done");
        return Assignment.fromDatabase(id, name, cid, seriesId, start, deadline, lateDays, estimated, remaining, done);
    }
}
