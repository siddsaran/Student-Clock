package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.IDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Persists work log entries per assignment (hours, logged_at) for Big Picture burndown.
 */
public class AssignmentWorkLogRepository {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS assignment_work_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "assignment_id TEXT NOT NULL, " +
                    "hours REAL NOT NULL, " +
                    "logged_at TEXT NOT NULL)";

    private static final String INSERT_SQL =
            "INSERT INTO assignment_work_log (assignment_id, hours, logged_at) VALUES (?, ?, ?)";

    private static final String CUMULATIVE_BY_DATE_SQL =
            "SELECT assignment_id, SUM(hours) AS total " +
                    "FROM assignment_work_log " +
                    "WHERE logged_at <= ? " +
                    "GROUP BY assignment_id";

    private final Connection connection;

    public AssignmentWorkLogRepository(IDataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource must not be null");
        }
        this.connection = dataSource.getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        try (Statement st = connection.createStatement()) {
            st.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create assignment_work_log table", e);
        }
    }

    /**
     * Records work logged for an assignment at a given time.
     */
    public void addWorkLog(String assignmentId, double hours, LocalDateTime loggedAt) {
        if (assignmentId == null || assignmentId.isBlank()) {
            throw new IllegalArgumentException("assignmentId must not be null or blank");
        }
        if (hours < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }
        if (loggedAt == null) {
            throw new NullPointerException("loggedAt must not be null");
        }
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            ps.setString(1, assignmentId);
            ps.setDouble(2, hours);
            ps.setString(3, loggedAt.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add assignment work log", e);
        }
    }

    /**
     * Returns cumulative hours logged for each assignment on or before endOfDay.
     * Map: assignmentId -> total hours logged.
     * Assignments with no work log rows are not in the map (use 0 or fallback).
     */
    public Map<String, Double> getCumulativeHoursByEndOf(LocalDate endOfDay) {
        Map<String, Double> result = new HashMap<>();
        LocalDateTime cutoff = endOfDay.plusDays(1).atStartOfDay();

        try (PreparedStatement ps = connection.prepareStatement(CUMULATIVE_BY_DATE_SQL)) {
            ps.setString(1, cutoff.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("assignment_id"), rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get cumulative hours", e);
        }
        return result;
    }
}

