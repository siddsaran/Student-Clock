package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Assignment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentRepositoryTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private AssignmentRepository repository;

    /**
     * Wrap an existing Connection in an IDataSource so AssignmentRepository can use it.
     */
    private static class TestDataSource implements IDataSource {
        private final Connection connection;

        TestDataSource(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection getConnection() {
            return connection;
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        repository = new AssignmentRepository(new TestDataSource(connection));
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void addAssignmentAndGetAssignmentsForCourseReturnsStoredAssignment() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        Assignment a = new Assignment("Quiz 2 Study", "CSE 110", start, deadline, 0, 0);
        repository.addAssignment(a);

        List<Assignment> retrieved = repository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, retrieved.size());

        Assignment r = retrieved.get(0);
        assertEquals("Quiz 2 Study", r.getName());
        assertEquals("CSE 110", r.getCourseID());
        assertEquals(start, r.getStart());
        assertEquals(deadline, r.getDeadline());
        assertEquals(0, r.getLateDaysAllowed());
    }

    @Test
    void getAssignmentsForCourseWithUnknownIdReturnsEmptyList() {
        List<Assignment> retrieved = repository.getAssignmentsForCourse("CSE 999");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getAssignmentsForCourseWithNullIdReturnsEmptyList() {
        List<Assignment> retrieved = repository.getAssignmentsForCourse(null);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getAllAssignmentsReturnsAllStoredAssignments() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);

        repository.addAssignment(new Assignment(
                "Quiz 2 Study", "CSE 110",
                start, LocalDateTime.of(2026, 2, 3, 23, 59),
                0, 0
        ));
        repository.addAssignment(new Assignment(
                "PA1", "CSE 101",
                start, LocalDateTime.of(2026, 2, 6, 23, 59),
                0, 0
        ));

        List<Assignment> all = repository.getAllAssignments();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(x -> "Quiz 2 Study".equals(x.getName())));
        assertTrue(all.stream().anyMatch(x -> "PA1".equals(x.getName())));
    }

    @Test
    void getAllAssignmentsWhenEmptyReturnsEmptyList() {
        List<Assignment> all = repository.getAllAssignments();
        assertTrue(all.isEmpty());
    }

    @Test
    void addMultipleAssignmentsToDifferentCoursesKeepsThemSeparated() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);

        // 2 assignments for CSE 110
        repository.addAssignment(new Assignment(
                "Quiz 2 Study", "CSE 110",
                start, LocalDateTime.of(2026, 2, 3, 23, 59),
                0, 0
        ));
        repository.addAssignment(new Assignment(
                "MVP", "CSE 110",
                start, LocalDateTime.of(2026, 2, 5, 23, 59),
                2, 0
        ));

        // 1 assignment for CSE 101
        repository.addAssignment(new Assignment(
                "PA1", "CSE 101",
                start, LocalDateTime.of(2026, 2, 6, 23, 59),
                0, 0
        ));

        List<Assignment> cse110 = repository.getAssignmentsForCourse("CSE 110");
        List<Assignment> cse101 = repository.getAssignmentsForCourse("CSE 101");

        assertEquals(2, cse110.size());
        assertTrue(cse110.stream().allMatch(a -> "CSE 110".equals(a.getCourseID())));

        assertEquals(1, cse101.size());
        assertTrue(cse101.stream().allMatch(a -> "CSE 101".equals(a.getCourseID())));
    }

    @Test
    void addAssignmentWithNullThrows() {
        assertThrows(NullPointerException.class, () -> repository.addAssignment(null));
    }

    @Test
    void constructorThrowsWhenDataSourceNull() {
        assertThrows(NullPointerException.class, () -> new AssignmentRepository(null));
    }

    @Test
    void getAllAssignmentsReturnsUnmodifiableList() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        repository.addAssignment(new Assignment(
                "Quiz 2 Study", "CSE 110",
                start, LocalDateTime.of(2026, 2, 3, 23, 59),
                0, 0
        ));

        List<Assignment> all = repository.getAllAssignments();
        assertThrows(UnsupportedOperationException.class,
                () -> all.add(new Assignment("X", "Y", start, start.plusDays(1), 0, 0)));
    }

    @Test
    void deleteAssignmentRemovesOnlyThatAssignment() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);

        Assignment a1 = new Assignment(
                "Quiz 2 Study",
                "CSE 110",
                start,
                LocalDateTime.of(2026, 2, 3, 23, 59),
                0,
                0
        );

        Assignment a2 = new Assignment(
                "MVP",
                "CSE 110",
                start,
                LocalDateTime.of(2026, 2, 5, 23, 59),
                2,
                0
        );

        repository.addAssignment(a1);
        repository.addAssignment(a2);

        // sanity check
        assertEquals(2, repository.getAssignmentsForCourse("CSE 110").size());

        // delete only a1
        repository.deleteAssignment(a1.getID());

        List<Assignment> remaining = repository.getAssignmentsForCourse("CSE 110");

        assertEquals(1, remaining.size());
        assertEquals("MVP", remaining.get(0).getName());
    }

    @Test
    void deleteAssignmentWithNonexistentIdDoesNothing() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);

        Assignment assignment = new Assignment(
                "Quiz 2 Study",
                "CSE 110",
                start,
                LocalDateTime.of(2026, 2, 3, 23, 59),
                0,
                0
        );

        repository.addAssignment(assignment);

        // Attempt to delete an ID that was never stored
        repository.deleteAssignment("non-existent-id");

        // Original assignment should still be there
        List<Assignment> remaining = repository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, remaining.size());
        assertEquals("Quiz 2 Study", remaining.get(0).getName());
    }

    @Test
    void persistAndLoadAssignmentWithSeriesIdRestoresSeriesId() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        Assignment a = new Assignment("PA1", "CSE 110", "pa-series-1", start, deadline, 0, 2.0);
        repository.addAssignment(a);

        List<Assignment> byCourse = repository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, byCourse.size());
        assertEquals("pa-series-1", byCourse.get(0).getSeriesId());
    }

    @Test
    void getAssignmentsBySeriesReturnsOnlyAssignmentsInThatSeries() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);

        repository.addAssignment(new Assignment(
                "PA1", "CSE 110", "pa-series",
                start, LocalDateTime.of(2026, 2, 5, 23, 59),
                0, 0
        ));
        repository.addAssignment(new Assignment(
                "PA2", "CSE 110", "pa-series",
                start, LocalDateTime.of(2026, 2, 12, 23, 59),
                0, 0
        ));
        repository.addAssignment(new Assignment(
                "Quiz 2", "CSE 110", null,
                start, LocalDateTime.of(2026, 2, 3, 23, 59),
                0, 0
        ));

        List<Assignment> inSeries = repository.getAssignmentsBySeries("pa-series");
        assertEquals(2, inSeries.size());
        assertTrue(inSeries.stream().allMatch(a -> "pa-series".equals(a.getSeriesId())));
    }

    @Test
    void getAssignmentsBySeriesWithUnknownIdReturnsEmptyList() {
        List<Assignment> list = repository.getAssignmentsBySeries("unknown-series");
        assertTrue(list.isEmpty());
    }

    @Test
    void getAssignmentsBySeriesWithNullReturnsEmptyList() {
        List<Assignment> list = repository.getAssignmentsBySeries(null);
        assertTrue(list.isEmpty());
    }
}
