package edu.ucsd.studentclock.model;

import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private CourseRepository repository;
    private SeriesRepository seriesRepository;
    private Model model;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        repository = new CourseRepository(connection);
        seriesRepository = new SeriesRepository(connection);
        model = new Model(repository, seriesRepository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void addCourseAndGetCourseReturnsStoredCourse() {
        Course course = new Course("CSE 110", "Software Engineering");
        model.addCourse(course);

        Optional<Course> retrieved = model.getCourse("CSE 110");
        assertTrue(retrieved.isPresent());
        assertEquals("CSE 110", retrieved.get().getId());
        assertEquals("Software Engineering", retrieved.get().getName());
    }

    @Test
    void getCourseWithUnknownIdReturnsEmpty() {
        Optional<Course> retrieved = model.getCourse("CSE 999");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getAllCoursesReturnsAllStoredCourses() {
        model.addCourse(new Course("CSE 110", "Software Engineering"));
        model.addCourse(new Course("CSE 101", "Intro to CS"));

        List<Course> allCourses = model.getAllCourses();
        assertEquals(2, allCourses.size());
        assertTrue(allCourses.stream().anyMatch(c -> "CSE 110".equals(c.getId())));
        assertTrue(allCourses.stream().anyMatch(c -> "CSE 101".equals(c.getId())));
    }

    @Test
    void getAllCoursesWhenEmptyReturnsEmptyList() {
        List<Course> allCourses = model.getAllCourses();
        assertTrue(allCourses.isEmpty());
    }

    @Test
    void modelWithNullCourseRepositoryThrows() {
        assertThrows(NullPointerException.class, () -> new Model(null, seriesRepository));
    }

    @Test
    void modelWithNullSeriesRepositoryThrows() {
        assertThrows(NullPointerException.class, () -> new Model(repository, null));
    }

    @Test
    void addCourseWithValidIdAndNameStoresAndRetrieves() {
        model.addCourse("CSE 110", "Software Engineering");

        Optional<Course> retrieved = model.getCourse("CSE 110");
        assertTrue(retrieved.isPresent());
        assertEquals("CSE 110", retrieved.get().getId());
        assertEquals("Software Engineering", retrieved.get().getName());
    }

    @Test
    void addCourseWithTrimmedInputStoresTrimmedValues() {
        model.addCourse("  CSE 110  ", "  Software Engineering  ");

        Optional<Course> retrieved = model.getCourse("CSE 110");
        assertTrue(retrieved.isPresent());
        assertEquals("CSE 110", retrieved.get().getId());
        assertEquals("Software Engineering", retrieved.get().getName());
    }

    @Test
    void addCourseWithNullIdThrows() {
        assertThrows(NullPointerException.class, () -> model.addCourse(null, "Name"));
    }

    @Test
    void addCourseWithNullNameThrows() {
        assertThrows(NullPointerException.class, () -> model.addCourse("CSE 110", null));
    }

    @Test
    void addCourseWithBlankIdThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> model.addCourse("", "Software Engineering"));
        assertTrue(e.getMessage().contains("id"));
    }

    @Test
    void addCourseWithWhitespaceOnlyIdThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> model.addCourse("   ", "Software Engineering"));
        assertTrue(e.getMessage().contains("id"));
    }

    @Test
    void addCourseWithBlankNameThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> model.addCourse("CSE 110", ""));
        assertTrue(e.getMessage().contains("name"));
    }

    @Test
    void addCourseWithWhitespaceOnlyNameThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> model.addCourse("CSE 110", "   "));
        assertTrue(e.getMessage().contains("name"));
    }
}
