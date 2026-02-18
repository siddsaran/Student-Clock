package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.StudyAvailability;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.Optional;

public class StudyAvailabilityRepository {

    private static final String KEY = "singleton";

    private static final String CREATE_WEEKLY_SQL =
            "CREATE TABLE IF NOT EXISTS study_availability (" +
                    "id TEXT PRIMARY KEY, " +
                    "total_weekly_hours INTEGER NOT NULL" +
            ")";

    private static final String CREATE_DAYS_SQL =
            "CREATE TABLE IF NOT EXISTS study_availability_days (" +
                    "availability_id TEXT NOT NULL, " +
                    "day_of_week INTEGER NOT NULL, " +   // 1..7
                    "available INTEGER NOT NULL, " +     // 0/1
                    "daily_limit INTEGER NOT NULL, " +
                    "PRIMARY KEY (availability_id, day_of_week), " +
                    "FOREIGN KEY (availability_id) REFERENCES study_availability(id) ON DELETE CASCADE" +
            ")";

    private static final String UPSERT_WEEKLY_SQL =
            "INSERT OR REPLACE INTO study_availability (id, total_weekly_hours) VALUES (?, ?)";

    private static final String UPSERT_DAY_SQL =
            "INSERT OR REPLACE INTO study_availability_days " +
                    "(availability_id, day_of_week, available, daily_limit) VALUES (?, ?, ?, ?)";

    private static final String SELECT_WEEKLY_SQL =
            "SELECT total_weekly_hours FROM study_availability WHERE id = ?";

    private static final String SELECT_DAYS_SQL =
            "SELECT day_of_week, available, daily_limit FROM study_availability_days WHERE availability_id = ?";

    private final Connection connection;

    public StudyAvailabilityRepository(Connection connection) {
        if (connection == null) throw new NullPointerException("connection must not be null");
        this.connection = connection;
        createTables();
        enableForeignKeys();
    }

    private void createTables() {
        try (Statement st = connection.createStatement()) {
            st.execute(CREATE_WEEKLY_SQL);
            st.execute(CREATE_DAYS_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create study availability tables", e);
        }
    }

    private void enableForeignKeys() {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to enable foreign keys", e);
        }
    }

    public void save(StudyAvailability availability) {
        if (availability == null) throw new NullPointerException("availability must not be null");

        String err = availability.validate();
        if (err != null) throw new IllegalArgumentException("Invalid StudyAvailability: " + err);

        boolean oldAutoCommit;
        try {
            oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(UPSERT_WEEKLY_SQL)) {
                ps.setString(1, KEY);
                ps.setInt(2, availability.getTotalWeeklyHours());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(UPSERT_DAY_SQL)) {
                for (DayOfWeek d : DayOfWeek.values()) {
                    ps.setString(1, KEY);
                    ps.setInt(2, d.getValue()); // 1..7
                    ps.setInt(3, availability.isAvailable(d) ? 1 : 0);
                    ps.setInt(4, availability.getDailyLimit(d));
                    ps.executeUpdate();
                }
            }

            connection.commit();
            connection.setAutoCommit(oldAutoCommit);
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException("Failed to save study availability", e);
        }
    }

    public Optional<StudyAvailability> load() {
        try (PreparedStatement weeklyPs = connection.prepareStatement(SELECT_WEEKLY_SQL)) {
            weeklyPs.setString(1, KEY);

            try (ResultSet weeklyRs = weeklyPs.executeQuery()) {
                if (!weeklyRs.next()) return Optional.empty();

                StudyAvailability a = new StudyAvailability();
                a.setTotalWeeklyHours(weeklyRs.getInt("total_weekly_hours"));

                try (PreparedStatement daysPs = connection.prepareStatement(SELECT_DAYS_SQL)) {
                    daysPs.setString(1, KEY);
                    try (ResultSet rs = daysPs.executeQuery()) {
                        while (rs.next()) {
                            DayOfWeek day = DayOfWeek.of(rs.getInt("day_of_week"));
                            boolean available = rs.getInt("available") != 0;
                            int limit = rs.getInt("daily_limit");

                            a.setAvailable(day, available);
                            a.setDailyLimit(day, limit); // ok because availability set first
                        }
                    }
                }
                return Optional.of(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load study availability", e);
        }
    }
}
