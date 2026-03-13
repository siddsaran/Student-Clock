package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.Course;
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

@DisplayName("CourseRepository")
class CourseRepositoryTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private CourseRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        repository = new CourseRepository(() -> connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("addCourse and getCourse store and retrieve a course by ID")
    void addCourseAndGetCourseReturnsStoredCourse() {
        Course course = new Course("CSE 110", "Software Engineering");
        repository.addCourse(course);

        Optional<Course> retrieved = repository.getCourse("CSE 110");
        assertTrue(retrieved.isPresent());
        assertEquals("CSE 110", retrieved.get().getId());
        assertEquals("Software Engineering", retrieved.get().getName());
    }

    @Test
    @DisplayName("getCourse returns empty for an unknown ID")
    void getCourseWithUnknownIdReturnsEmpty() {
        Optional<Course> retrieved = repository.getCourse("CSE 999");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    @DisplayName("getCourse returns empty when ID is null")
    void getCourseWithNullIdReturnsEmpty() {
        Optional<Course> retrieved = repository.getCourse(null);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    @DisplayName("getAllCourses returns all stored courses")
    void getAllCoursesReturnsAllStoredCourses() {
        Course course1 = new Course("CSE 110", "Software Engineering");
        Course course2 = new Course("CSE 101", "Intro to CS");
        repository.addCourse(course1);
        repository.addCourse(course2);

        List<Course> allCourses = repository.getAllCourses();
        assertEquals(2, allCourses.size());
        assertTrue(allCourses.stream().anyMatch(c -> "CSE 110".equals(c.getId())));
        assertTrue(allCourses.stream().anyMatch(c -> "CSE 101".equals(c.getId())));
    }

    @Test
    @DisplayName("getAllCourses returns an empty list when no courses exist")
    void getAllCoursesWhenEmptyReturnsEmptyList() {
        List<Course> allCourses = repository.getAllCourses();
        assertTrue(allCourses.isEmpty());
    }

    @Test
    @DisplayName("addCourse replaces an existing course when the same ID is used")
    void addCourseWithSameIdReplacesExisting() {
        repository.addCourse(new Course("CSE 110", "Software Engineering"));
        repository.addCourse(new Course("CSE 110", "Different Name"));

        Optional<Course> retrieved = repository.getCourse("CSE 110");
        assertTrue(retrieved.isPresent());
        assertEquals("Different Name", retrieved.get().getName());
    }

    @Test
    @DisplayName("addCourse throws NullPointerException when course is null")
    void addCourseWithNullThrows() {
        assertThrows(NullPointerException.class, () -> repository.addCourse(null));
    }

    @Test
    @DisplayName("getAllCourses returns an unmodifiable list")
    void getAllCoursesReturnsUnmodifiableList() {
        repository.addCourse(new Course("CSE 110", "Software Engineering"));
        List<Course> allCourses = repository.getAllCourses();
        assertThrows(UnsupportedOperationException.class,
                () -> allCourses.add(new Course("X", "Y")));
    }
}