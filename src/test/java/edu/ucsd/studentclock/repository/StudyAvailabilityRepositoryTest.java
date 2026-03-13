package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.StudyAvailability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 2: Unit tests for domain and model logic (StudyAvailabilityRepository).
 * MS1: US5 (Define weekly study availability).
 */
@DisplayName("DS7-2: StudyAvailabilityRepository")
class StudyAvailabilityRepositoryTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private StudyAvailabilityRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        repository = new StudyAvailabilityRepository(() -> connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("load returns empty when no availability has been saved")
    void loadWhenNothingSavedReturnsEmpty() {
        Optional<StudyAvailability> loaded = repository.load();
        assertTrue(loaded.isEmpty());
    }

    @Test
    @DisplayName("save and load persist total weekly hours")
    void saveAndLoadPersistsWeeklyHours() {
        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);

        repository.save(availability);

        Optional<StudyAvailability> loaded = repository.load();
        assertTrue(loaded.isPresent());
        assertEquals(10, loaded.get().getTotalWeeklyHours());
    }

    @Test
    @DisplayName("save and load persist available study days")
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
    @DisplayName("save and load persist daily study limits")
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
    @DisplayName("saving new availability replaces previously stored availability")
    void saveReplacesExistingAvailability() {
        StudyAvailability first = new StudyAvailability();
        first.setTotalWeeklyHours(8);
        first.setAvailable(DayOfWeek.MONDAY, true);
        first.setDailyLimit(DayOfWeek.MONDAY, 2);
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

        assertFalse(loaded.isAvailable(DayOfWeek.MONDAY));
        assertEquals(0, loaded.getDailyLimit(DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("save throws when availability configuration is invalid")
    void saveWithInvalidAvailabilityThrows() {
        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(2);
        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setDailyLimit(DayOfWeek.MONDAY, 3);

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(availability));
    }

    @Test
    @DisplayName("save throws when availability is null")
    void saveWithNullAvailabilityThrows() {
        assertThrows(NullPointerException.class,
                () -> repository.save(null));
    }
}