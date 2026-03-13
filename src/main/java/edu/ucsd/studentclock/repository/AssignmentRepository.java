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
import edu.ucsd.studentclock.model.AssignmentBuilder;
import edu.ucsd.studentclock.util.ValidationUtils;

public class AssignmentRepository implements IAssignmentRepository {

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
                    "cumulativeHours REAL, " +
                    "done INTEGER)";

    private static final String INSERT_SQL =
            "INSERT OR REPLACE INTO assignments " +
                    "(id, name, courseID, seriesId, start, deadline, " +
                    "lateDaysAllowed, estimatedHours, remainingHours, cumulativeHours, done) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_COURSE_SQL =
            "SELECT * FROM assignments WHERE courseID = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM assignments";

    private static final String DELETE_BY_ID_SQL =
            "DELETE FROM assignments WHERE id = ?";

    private static final String DELETE_BY_COURSEID_SQL =
            "DELETE FROM assignments WHERE courseID = ?";

    private static final String UPDATE_SERIES_BY_ID_SQL =
            "UPDATE assignments SET seriesId = ?, lateDaysAllowed = ? WHERE id = ?";

    private static final String SELECT_BY_SERIES_SQL =
            "SELECT * FROM assignments WHERE seriesId = ?";

    private final Connection connection;

    /**
     * Creates a repository that uses the given data source for all operations.
     *
     * @param dataSource a valid data source (e.g. SQLite)
     */
    public AssignmentRepository(IDataSource dataSource) {
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
            throw new RuntimeException("Failed to create assignments table", e);
        }
        addColumnIfMissing("seriesId TEXT");
        addColumnIfMissing("cumulativeHours REAL");
    }

    private void addColumnIfMissing(String columnDef) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE assignments ADD COLUMN " + columnDef);
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column name")) {
                throw new RuntimeException("Failed to add column: " + columnDef, e);
            }
        }
    }

    public void addAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new NullPointerException("assignment must not be null");
        }
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, assignment.getId());
            statement.setString(2, assignment.getName());
            statement.setString(3, assignment.getCourseId());
            statement.setString(4, assignment.getSeriesId());
            statement.setString(5, assignment.getStart().toString());
            statement.setString(6, assignment.getDeadline().toString());
            statement.setInt(7, assignment.getLateDaysAllowed());
            statement.setDouble(8, assignment.getEstimatedHours());
            statement.setDouble(9, assignment.getRemainingHours());
            statement.setDouble(10, assignment.getCumulativeHours());
            statement.setBoolean(11, assignment.isDone());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add assignment", e);
        }
    }

    public void deleteAssignment(String id) {
        String trimmedId = ValidationUtils.normalizeNullable(id);
        if (trimmedId == null) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            statement.setString(1, trimmedId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete assignment", e);
        }
    }

    public List<Assignment> getAssignmentsForCourse(String courseID) {
        List<Assignment> assignmentList = new ArrayList<>();
        String trimmedCourseId = ValidationUtils.normalizeNullable(courseID);
        if (trimmedCourseId == null) {
            return List.of();
        }

        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_COURSE_SQL)) {
            statement.setString(1, trimmedCourseId);
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

    public void deleteAssignmentsForCourse(String courseID) {
        String trimmedCourseId = ValidationUtils.normalizeNullable(courseID);
        if (trimmedCourseId == null) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(DELETE_BY_COURSEID_SQL)) {
            ps.setString(1, trimmedCourseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete assignments for course", e);
        }
    }

    public void setSeriesForAssignments(String seriesId, int defaultLateDays, List<String> assignmentIds) {
        String trimmedSeriesId = ValidationUtils.normalizeNullable(seriesId);
        if (trimmedSeriesId == null || assignmentIds == null || assignmentIds.isEmpty()) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SERIES_BY_ID_SQL)) {
            for (String assignmentId : assignmentIds) {
                String trimmedAssignmentId = ValidationUtils.normalizeNullable(assignmentId);
                if (trimmedAssignmentId == null) {
                    continue;
                }
                statement.setString(1, trimmedSeriesId);
                statement.setInt(2, defaultLateDays);
                statement.setString(3, trimmedAssignmentId);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set series for assignments", e);
        }
    }

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

    public List<Assignment> getAssignmentsBySeries(String seriesId) {
        String trimmedSeriesId = ValidationUtils.normalizeNullable(seriesId);
        if (trimmedSeriesId == null) {
            return List.of();
        }
        List<Assignment> assignmentList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_SERIES_SQL)) {
            statement.setString(1, trimmedSeriesId);
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

    private static Assignment mapRow(ResultSet rs) throws SQLException {
        return new AssignmentBuilder()
                .setId(rs.getString("id"))
                .setName(rs.getString("name"))
                .setCourseId(rs.getString("courseID"))
                .setSeriesId(rs.getString("seriesId"))
                .setStart(LocalDateTime.parse(rs.getString("start")))
                .setDeadline(LocalDateTime.parse(rs.getString("deadline")))
                .setLateDaysAllowed(rs.getInt("lateDaysAllowed"))
                .setEstimatedHours(rs.getDouble("estimatedHours"))
                .setRemainingHours(rs.getDouble("remainingHours"))
                .setCumulativeHours(rs.getDouble("cumulativeHours"))
                .setDone(rs.getBoolean("done"))
                .build();
    }
}