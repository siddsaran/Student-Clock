package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 2: Unit tests for domain and model logic (AssignmentRepository).
 * MS1: US2 (Add assignments with start dates and deadlines), US2-1, US2-2, US2-3.
 */
@DisplayName("DS7-2: AssignmentRepository")
class AssignmentRepositoryTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private static final LocalDateTime START = LocalDateTime.of(2026, 2, 1, 9, 0);

    private Connection connection;
    private AssignmentRepository repository;

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

    private static Assignment build(
            String name,
            String courseId,
            String seriesId,
            LocalDateTime deadline,
            int lateDays,
            double hours) {
        return new AssignmentBuilder()
                .setName(name)
                .setCourseId(courseId)
                .setSeriesId(seriesId)
                .setStart(START)
                .setDeadline(deadline)
                .setLateDaysAllowed(lateDays)
                .setEstimatedHours(hours)
                .build();
    }

    private static Assignment build(
            String name,
            String courseId,
            LocalDateTime deadline,
            int lateDays,
            double hours) {
        return build(name, courseId, null, deadline, lateDays, hours);
    }

    @Test
    @DisplayName("addAssignment and getAssignmentsForCourse store and retrieve an assignment")
    void addAssignmentAndGetAssignmentsForCourseReturnsStoredAssignment() {
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);
        Assignment a = build("Quiz 2 Study", "CSE 110", deadline, 0, 0);
        repository.addAssignment(a);

        List<Assignment> retrieved = repository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, retrieved.size());

        Assignment r = retrieved.get(0);
        assertEquals("Quiz 2 Study", r.getName());
        assertEquals("CSE 110", r.getCourseId());
        assertEquals(START, r.getStart());
        assertEquals(deadline, r.getDeadline());
        assertEquals(0, r.getLateDaysAllowed());
    }

    @Test
    @DisplayName("getAssignmentsForCourse returns an empty list for an unknown course ID")
    void getAssignmentsForCourseWithUnknownIdReturnsEmptyList() {
        List<Assignment> retrieved = repository.getAssignmentsForCourse("CSE 999");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    @DisplayName("getAssignmentsForCourse returns an empty list when course ID is null")
    void getAssignmentsForCourseWithNullIdReturnsEmptyList() {
        List<Assignment> retrieved = repository.getAssignmentsForCourse(null);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    @DisplayName("getAllAssignments returns all stored assignments")
    void getAllAssignmentsReturnsAllStoredAssignments() {
        repository.addAssignment(build("Quiz 2 Study", "CSE 110",
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0));
        repository.addAssignment(build("PA1", "CSE 101",
                LocalDateTime.of(2026, 2, 6, 23, 59), 0, 0));

        List<Assignment> all = repository.getAllAssignments();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(x -> "Quiz 2 Study".equals(x.getName())));
        assertTrue(all.stream().anyMatch(x -> "PA1".equals(x.getName())));
    }

    @Test
    @DisplayName("getAllAssignments returns an empty list when no assignments exist")
    void getAllAssignmentsWhenEmptyReturnsEmptyList() {
        List<Assignment> all = repository.getAllAssignments();
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("Assignments from different courses remain separated by course")
    void addMultipleAssignmentsToDifferentCoursesKeepsThemSeparated() {
        repository.addAssignment(build("Quiz 2 Study", "CSE 110",
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0));
        repository.addAssignment(build("MVP", "CSE 110",
                LocalDateTime.of(2026, 2, 5, 23, 59), 2, 0));
        repository.addAssignment(build("PA1", "CSE 101",
                LocalDateTime.of(2026, 2, 6, 23, 59), 0, 0));

        List<Assignment> cse110 = repository.getAssignmentsForCourse("CSE 110");
        List<Assignment> cse101 = repository.getAssignmentsForCourse("CSE 101");

        assertEquals(2, cse110.size());
        assertTrue(cse110.stream().allMatch(a -> "CSE 110".equals(a.getCourseId())));

        assertEquals(1, cse101.size());
        assertTrue(cse101.stream().allMatch(a -> "CSE 101".equals(a.getCourseId())));
    }

    @Test
    @DisplayName("addAssignment throws NullPointerException when assignment is null")
    void addAssignmentWithNullThrows() {
        assertThrows(NullPointerException.class, () -> repository.addAssignment(null));
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when data source is null")
    void constructorThrowsWhenDataSourceNull() {
        assertThrows(NullPointerException.class, () -> new AssignmentRepository(null));
    }

    @Test
    @DisplayName("getAllAssignments returns an unmodifiable list")
    void getAllAssignmentsReturnsUnmodifiableList() {
        repository.addAssignment(build("Quiz 2 Study", "CSE 110",
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0));

        List<Assignment> all = repository.getAllAssignments();
        assertThrows(UnsupportedOperationException.class,
                () -> all.add(build("X", "Y", START.plusDays(1), 0, 0)));
    }

    @Test
    @DisplayName("deleteAssignment removes only the matching assignment")
    void deleteAssignmentRemovesOnlyThatAssignment() {
        Assignment a1 = build("Quiz 2 Study", "CSE 110",
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0);
        Assignment a2 = build("MVP", "CSE 110",
                LocalDateTime.of(2026, 2, 5, 23, 59), 2, 0);

        repository.addAssignment(a1);
        repository.addAssignment(a2);

        assertEquals(2, repository.getAssignmentsForCourse("CSE 110").size());

        repository.deleteAssignment(a1.getId());

        List<Assignment> remaining = repository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, remaining.size());
        assertEquals("MVP", remaining.get(0).getName());
    }

    @Test
    @DisplayName("deleteAssignment does nothing when the ID does not exist")
    void deleteAssignmentWithNonexistentIdDoesNothing() {
        Assignment assignment = build("Quiz 2 Study", "CSE 110",
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0);
        repository.addAssignment(assignment);

        repository.deleteAssignment("non-existent-id");

        List<Assignment> remaining = repository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, remaining.size());
        assertEquals("Quiz 2 Study", remaining.get(0).getName());
    }

    @Test
    @DisplayName("Persisted assignments preserve series ID when reloaded")
    void persistAndLoadAssignmentWithSeriesIdRestoresSeriesId() {
        Assignment a = build("PA1", "CSE 110", "pa-series-1",
                LocalDateTime.of(2026, 2, 5, 23, 59), 0, 2.0);
        repository.addAssignment(a);

        List<Assignment> byCourse = repository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, byCourse.size());
        assertEquals("pa-series-1", byCourse.get(0).getSeriesId());
    }

    @Test
    @DisplayName("deleteAssignmentsForCourse removes only assignments from the specified course")
    void deleteAssignmentsForCourseRemovesOnlyAssignmentsFromThatCourse() {
        Assignment a1 = build("Quiz 2 Study", "CSE 110",
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0);
        Assignment a2 = build("MVP", "CSE 110",
                LocalDateTime.of(2026, 2, 5, 23, 59), 2, 0);
        Assignment a3 = build("PA1", "CSE 101",
                LocalDateTime.of(2026, 2, 6, 23, 59), 0, 0);

        repository.addAssignment(a1);
        repository.addAssignment(a2);
        repository.addAssignment(a3);

        assertEquals(3, repository.getAllAssignments().size());

        repository.deleteAssignmentsForCourse("CSE 110");

        List<Assignment> remaining = repository.getAllAssignments();
        assertEquals(1, remaining.size());
        assertEquals("CSE 101", remaining.get(0).getCourseId());
        assertEquals("PA1", remaining.get(0).getName());
    }

    @Test
    @DisplayName("getAssignmentsBySeries returns only assignments linked to that series")
    void getAssignmentsBySeriesReturnsOnlyAssignmentsInThatSeries() {
        repository.addAssignment(build("PA1", "CSE 110", "pa-series",
                LocalDateTime.of(2026, 2, 5, 23, 59), 0, 0));
        repository.addAssignment(build("PA2", "CSE 110", "pa-series",
                LocalDateTime.of(2026, 2, 12, 23, 59), 0, 0));
        repository.addAssignment(build("Quiz 2", "CSE 110", null,
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0));

        List<Assignment> inSeries = repository.getAssignmentsBySeries("pa-series");
        assertEquals(2, inSeries.size());
        assertTrue(inSeries.stream().allMatch(a -> "pa-series".equals(a.getSeriesId())));
    }

    @Test
    @DisplayName("getAssignmentsBySeries returns an empty list for an unknown series ID")
    void getAssignmentsBySeriesWithUnknownIdReturnsEmptyList() {
        List<Assignment> list = repository.getAssignmentsBySeries("unknown-series");
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("getAssignmentsBySeries returns an empty list when series ID is null")
    void getAssignmentsBySeriesWithNullReturnsEmptyList() {
        List<Assignment> list = repository.getAssignmentsBySeries(null);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("setSeriesForAssignments links only the requested assignments and updates late days")
    void setSeriesForAssignmentsLinksOnlyRequestedIds() {
        Assignment a1 = build("PA1", "CSE 110",
                LocalDateTime.of(2026, 2, 5, 23, 59), 0, 0);
        Assignment a2 = build("PA2", "CSE 110",
                LocalDateTime.of(2026, 2, 12, 23, 59), 0, 0);
        Assignment a3 = build("Quiz", "CSE 110",
                LocalDateTime.of(2026, 2, 3, 23, 59), 0, 0);
        repository.addAssignment(a1);
        repository.addAssignment(a2);
        repository.addAssignment(a3);

        repository.setSeriesForAssignments("pa-series", 2, List.of(a1.getId(), a2.getId()));

        List<Assignment> inSeries = repository.getAssignmentsBySeries("pa-series");
        assertEquals(2, inSeries.size());
        assertTrue(inSeries.stream().anyMatch(a -> a1.getId().equals(a.getId())));
        assertTrue(inSeries.stream().anyMatch(a -> a2.getId().equals(a.getId())));
        assertEquals(2, inSeries.get(0).getLateDaysAllowed());
        assertEquals(2, inSeries.get(1).getLateDaysAllowed());

        List<Assignment> all = repository.getAssignmentsForCourse("CSE 110");
        Assignment unlinked = all.stream()
                .filter(a -> a3.getId().equals(a.getId()))
                .findFirst()
                .orElseThrow();
        assertNull(unlinked.getSeriesId());
    }

    @Test
    @DisplayName("setSeriesForAssignments does nothing when series ID is blank")
    void setSeriesForAssignmentsWithBlankSeriesIdDoesNothing() {
        Assignment a1 = build("PA1", "CSE 110",
                LocalDateTime.of(2026, 2, 5, 23, 59), 0, 0);
        repository.addAssignment(a1);

        repository.setSeriesForAssignments("   ", 0, List.of(a1.getId()));

        Assignment loaded = repository.getAssignmentsForCourse("CSE 110").get(0);
        assertNull(loaded.getSeriesId());
    }

    @Test
    @DisplayName("setSeriesForAssignments does nothing when assignment ID list is empty")
    void setSeriesForAssignmentsWithEmptyIdsDoesNothing() {
        Assignment a1 = build("PA1", "CSE 110",
                LocalDateTime.of(2026, 2, 5, 23, 59), 0, 0);
        repository.addAssignment(a1);

        repository.setSeriesForAssignments("pa-series", 0, List.of());

        Assignment loaded = repository.getAssignmentsForCourse("CSE 110").get(0);
        assertNull(loaded.getSeriesId());
    }
}