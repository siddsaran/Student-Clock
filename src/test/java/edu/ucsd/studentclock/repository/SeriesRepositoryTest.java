package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.Series;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SeriesRepository")
class SeriesRepositoryTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private SeriesRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        repository = new SeriesRepository(() -> connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("addSeries and getSeries store and retrieve a series by ID")
    void addSeriesAndGetSeriesReturnsStoredSeries() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        repository.addSeries(series);

        Optional<Series> retrieved = repository.getSeries("midterm-1");
        assertTrue(retrieved.isPresent());
        assertEquals("midterm-1", retrieved.get().getId());
        assertEquals("CSE 110", retrieved.get().getCourseId());
        assertEquals("Midterm", retrieved.get().getName());
        assertEquals(0, retrieved.get().getDefaultLateDays());
    }

    @Test
    @DisplayName("getSeries returns empty for an unknown ID")
    void getSeriesWithUnknownIdReturnsEmpty() {
        Optional<Series> retrieved = repository.getSeries("unknown");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    @DisplayName("getSeries returns empty when ID is null")
    void getSeriesWithNullIdReturnsEmpty() {
        Optional<Series> retrieved = repository.getSeries(null);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    @DisplayName("getSeriesByCourse returns only series belonging to the requested course")
    void getSeriesByCourseReturnsOnlyThatCoursesSeries() {
        repository.addSeries(new Series("midterm-1", "CSE 110", "Midterm", 0));
        repository.addSeries(new Series("pa-1", "CSE 110", "PAs", 2));
        repository.addSeries(new Series("midterm-2", "CSE 101", "Midterm", 0));

        List<Series> cse110 = repository.getSeriesByCourse("CSE 110");
        assertEquals(2, cse110.size());
        assertTrue(cse110.stream().allMatch(s -> "CSE 110".equals(s.getCourseId())));

        List<Series> cse101 = repository.getSeriesByCourse("CSE 101");
        assertEquals(1, cse101.size());
        assertEquals("midterm-2", cse101.get(0).getId());
    }

    @Test
    @DisplayName("getSeriesByCourse returns an empty list when no series exist for the course")
    void getSeriesByCourseWhenEmptyReturnsEmptyList() {
        List<Series> list = repository.getSeriesByCourse("CSE 110");
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("addSeries replaces an existing series when the same ID is used")
    void addSeriesWithSameIdReplacesExisting() {
        repository.addSeries(new Series("midterm-1", "CSE 110", "Midterm", 0));
        repository.addSeries(new Series("midterm-1", "CSE 110", "Midterm Updated", 1));

        Optional<Series> retrieved = repository.getSeries("midterm-1");
        assertTrue(retrieved.isPresent());
        assertEquals("Midterm Updated", retrieved.get().getName());
        assertEquals(1, retrieved.get().getDefaultLateDays());
    }

    @Test
    @DisplayName("addSeries throws NullPointerException when series is null")
    void addSeriesWithNullThrows() {
        assertThrows(NullPointerException.class, () -> repository.addSeries(null));
    }

    @Test
    @DisplayName("getSeriesByCourse returns an unmodifiable list")
    void getSeriesByCourseReturnsUnmodifiableList() {
        repository.addSeries(new Series("midterm-1", "CSE 110", "Midterm", 0));
        List<Series> list = repository.getSeriesByCourse("CSE 110");
        assertThrows(UnsupportedOperationException.class,
                () -> list.add(new Series("x", "CSE 110", "X", 0)));
    }
}