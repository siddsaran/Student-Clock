package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.Course;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    void addCourseAndGetCourseReturnsStoredCourse() {
        Course course = new Course("CSE 110", "Software Engineering");
        repository.addCourse(course);

        Optional<Course> retrieved = repository.getCourse("CSE 110");
        assertTrue(retrieved.isPresent());
        assertEquals("CSE 110", retrieved.get().getId());
        assertEquals("Software Engineering", retrieved.get().getName());
    }

    @Test
    void getCourseWithUnknownIdReturnsEmpty() {
        Optional<Course> retrieved = repository.getCourse("CSE 999");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getCourseWithNullIdReturnsEmpty() {
        Optional<Course> retrieved = repository.getCourse(null);
        assertTrue(retrieved.isEmpty());
    }

    @Test
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
    void getAllCoursesWhenEmptyReturnsEmptyList() {
        List<Course> allCourses = repository.getAllCourses();
        assertTrue(allCourses.isEmpty());
    }

    @Test
    void addCourseWithSameIdReplacesExisting() {
        repository.addCourse(new Course("CSE 110", "Software Engineering"));
        repository.addCourse(new Course("CSE 110", "Different Name"));

        Optional<Course> retrieved = repository.getCourse("CSE 110");
        assertTrue(retrieved.isPresent());
        assertEquals("Different Name", retrieved.get().getName());
    }

    @Test
    void addCourseWithNullThrows() {
        assertThrows(NullPointerException.class, () -> repository.addCourse(null));
    }

    @Test
    void getAllCoursesReturnsUnmodifiableList() {
        repository.addCourse(new Course("CSE 110", "Software Engineering"));
        List<Course> allCourses = repository.getAllCourses();
        assertThrows(UnsupportedOperationException.class, () -> allCourses.add(new Course("X", "Y")));
    }
}
