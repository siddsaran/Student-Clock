package edu.ucsd.studentclock.model;

import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.datasource.SqlDataSource;
import edu.ucsd.studentclock.repository.AssignmentRepository;
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
    private AssignmentRepository assignmentRepository;
    private Model model;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        repository = new CourseRepository(connection);
        assignmentRepository = new AssignmentRepository(() -> connection);
        model = new Model(repository, assignmentRepository);
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
    void modelWithNullRepositoryThrows() {
        assertThrows(NullPointerException.class, () -> new Model(null, null));
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

    @Test
    void deleteCourseDeletesAssignmentsForThatCourse() {
        model.addCourse("CSE 110", "Software Engineering");

        assignmentRepository.addAssignment(new Assignment(
                "Quiz 2 Study", "CSE 110",
                java.time.LocalDateTime.of(2026, 2, 1, 9, 0),
                java.time.LocalDateTime.of(2026, 2, 5, 23, 59),
                0, 0
        ));
        assignmentRepository.addAssignment(new Assignment(
                "MVP", "CSE 110",
                java.time.LocalDateTime.of(2026, 2, 1, 9, 0),
                java.time.LocalDateTime.of(2026, 2, 6, 23, 59),
                0, 0
        ));

        assertEquals(2, assignmentRepository.getAssignmentsForCourse("CSE 110").size());

        model.deleteCourse("CSE 110");

        assertTrue(model.getCourse("CSE 110").isEmpty());

        assertTrue(assignmentRepository.getAssignmentsForCourse("CSE 110").isEmpty());
    }

}
