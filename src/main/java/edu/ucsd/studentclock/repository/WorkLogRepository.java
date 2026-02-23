package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.IDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;

/**
 * Persists work log entries (hours logged manually or via clock in/out) with timestamps.
 * Used to compute total hours logged in the current week for study hours remaining.
 */
public class WorkLogRepository {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS work_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "hours REAL NOT NULL, " +
                    "logged_at TEXT NOT NULL)";

    private static final String INSERT_SQL =
            "INSERT INTO work_log (hours, logged_at) VALUES (?, ?)";

    private static final String SUM_THIS_WEEK_SQL =
            "SELECT COALESCE(SUM(hours), 0) AS total FROM work_log WHERE logged_at >= ? AND logged_at < ?";

    private final Connection connection;

    public WorkLogRepository(IDataSource dataSource) {
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
            throw new RuntimeException("Failed to create work_log table", e);
        }
    }

    /**
     * Records a work log entry.
     *
     * @param hours   hours worked
     * @param loggedAt when the work was logged
     */
    public void addWorkLog(double hours, LocalDateTime loggedAt) {
        if (hours < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }
        if (loggedAt == null) {
            throw new NullPointerException("loggedAt must not be null");
        }
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            ps.setDouble(1, hours);
            ps.setString(2, loggedAt.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add work log", e);
        }
    }

    /**
     * Returns total hours logged in the current week (Monday–Sunday, ISO week).
     */
    public double getTotalHoursLoggedThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.ISO.dayOfWeek(), 1);  // Monday
        LocalDate weekEnd = weekStart.plusWeeks(1);

        String startStr = weekStart.atStartOfDay().toString();
        String endStr = weekEnd.atStartOfDay().toString();

        try (PreparedStatement ps = connection.prepareStatement(SUM_THIS_WEEK_SQL)) {
            ps.setString(1, startStr);
            ps.setString(2, endStr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("total") : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get total hours logged this week", e);
        }
    }
}
