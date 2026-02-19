package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.StudyAvailability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StudyAvailabilityRepositoryTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private StudyAvailabilityRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        repository = new StudyAvailabilityRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void loadWhenNothingSavedReturnsEmpty() {
        Optional<StudyAvailability> loaded = repository.load();
        assertTrue(loaded.isEmpty());
    }

    @Test
    void saveAndLoadPersistsWeeklyHours() {
        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);

        repository.save(availability);

        Optional<StudyAvailability> loaded = repository.load();
        assertTrue(loaded.isPresent());
        assertEquals(10, loaded.get().getTotalWeeklyHours());
    }

    @Test
    void saveAndLoadPersistsAvailableDays() {
        StudyAvailability availability = new StudyAvailability();
        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setAvailable(DayOfWeek.WEDNESDAY, true);

        repository.save(availability);

        StudyAvailability loaded = repository.load().orElseThrow();
        assertTrue(loaded.isAvailable(DayOfWeek.MONDAY));
        assertTrue(loaded.isAvailable(DayOfWeek.WEDNESDAY));
        assertFalse(loaded.isAvailable(DayOfWeek.FRIDAY));
    }

    @Test
    void saveAndLoadPersistsDailyLimits() {
        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);              
        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setDailyLimit(DayOfWeek.MONDAY, 3);

        repository.save(availability);

        StudyAvailability loaded = repository.load().orElseThrow();
        assertEquals(3, loaded.getDailyLimit(DayOfWeek.MONDAY));
    }

    @Test
    void saveReplacesExistingAvailability() {
        StudyAvailability first = new StudyAvailability();
        first.setTotalWeeklyHours(8);
        repository.save(first);

        StudyAvailability updated = new StudyAvailability();
        updated.setTotalWeeklyHours(12);
        updated.setAvailable(DayOfWeek.TUESDAY, true);
        updated.setDailyLimit(DayOfWeek.TUESDAY, 4);

        repository.save(updated);

        StudyAvailability loaded = repository.load().orElseThrow();
        assertEquals(12, loaded.getTotalWeeklyHours());
        assertTrue(loaded.isAvailable(DayOfWeek.TUESDAY));
        assertEquals(4, loaded.getDailyLimit(DayOfWeek.TUESDAY));
    }

    @Test
    void saveWithInvalidAvailabilityThrows() {
        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(2);
        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setDailyLimit(DayOfWeek.MONDAY, 3); // exceeds total

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(availability));
    }

    @Test
    void saveWithNullAvailabilityThrows() {
        assertThrows(NullPointerException.class,
                () -> repository.save(null));
    }
}
