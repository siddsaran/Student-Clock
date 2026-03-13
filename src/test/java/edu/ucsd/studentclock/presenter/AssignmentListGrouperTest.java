package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;
import edu.ucsd.studentclock.repository.StudyAvailabilityRepository;
import edu.ucsd.studentclock.service.TimeService;
import edu.ucsd.studentclock.view.AssignmentListEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS6, Task 2: Tests for AssignmentListGrouper.
 * Series rows receive presenter-provided tag colors.
 * MS1: US3 (Group related assignments into a series), US9 (View all open assignments).
 */
@DisplayName("DS6-2: AssignmentListGrouper")
class AssignmentListGrouperTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private Model model;
    private CourseRepository courseRepo;
    private SeriesRepository seriesRepo;
    private AssignmentRepository assignmentRepo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        var dataSource = (edu.ucsd.studentclock.datasource.IDataSource) () -> connection;
        courseRepo = new CourseRepository(dataSource);
        seriesRepo = new SeriesRepository(dataSource);
        var saRepo = new StudyAvailabilityRepository(dataSource);
        assignmentRepo = new AssignmentRepository(dataSource);
        model = new Model(courseRepo, assignmentRepo, seriesRepo, saRepo,
                new edu.ucsd.studentclock.repository.WorkLogRepository(dataSource),
                new edu.ucsd.studentclock.repository.AssignmentWorkLogRepository(dataSource), new TimeService());
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private Assignment makeAssignment(String name, String courseId, String seriesId) {
        return new AssignmentBuilder()
                .setName(name)
                .setCourseId(courseId)
                .setSeriesId(seriesId)
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(2.0)
                .build();
    }

    @Nested
    @DisplayName("series rows have tag colors")
    class SeriesTagColors {

        @Test
        @DisplayName("series assignment rows have non-null tagColor from presenter")
        void seriesRows_haveTagColor() {
            courseRepo.addCourse(new Course("CSE 110", "Software Engineering"));
            model.addSeries(new Series("pa-series", "CSE 110", "Programming Assignments", 2));
            Assignment a = makeAssignment("PA1", "CSE 110", "pa-series");
            assignmentRepo.addAssignment(a);

            List<AssignmentListEntry> entries = AssignmentListGrouper.buildGroupedList(
                    assignmentRepo.getAllAssignments(),
                    false,
                    "All Courses",
                    "All Courses",
                    model);

            AssignmentListEntry rowEntry = entries.stream()
                    .filter(e -> !e.isHeader() && e.getAssignment() != null)
                    .findFirst()
                    .orElse(null);
            assertNotNull(rowEntry);
            assertEquals("Programming Assignments", rowEntry.getDisplayName());
            assertNotNull(rowEntry.getTagColor(), "Presenter must provide tag color for series rows");
            assertTrue(rowEntry.getTagColor().startsWith("#"), "Tag color should be hex format");
        }

        @Test
        @DisplayName("same series gets same deterministic color")
        void sameSeries_sameColor() {
            courseRepo.addCourse(new Course("CSE 110", "Software Engineering"));
            model.addSeries(new Series("pa-series", "CSE 110", "PAs", 2));
            Assignment a1 = makeAssignment("PA1", "CSE 110", "pa-series");
            Assignment a2 = makeAssignment("PA2", "CSE 110", "pa-series");
            assignmentRepo.addAssignment(a1);
            assignmentRepo.addAssignment(a2);

            List<AssignmentListEntry> entries = AssignmentListGrouper.buildGroupedList(
                    assignmentRepo.getAllAssignments(),
                    false,
                    "All Courses",
                    "All Courses",
                    model);

            List<AssignmentListEntry> rows = entries.stream()
                    .filter(e -> !e.isHeader() && e.getTagColor() != null)
                    .collect(Collectors.toList());
            assertEquals(2, rows.size());
            assertEquals(rows.get(0).getTagColor(), rows.get(1).getTagColor(),
                    "Same series should get same tag color");
        }

        @Test
        @DisplayName("no-series rows have null tagColor")
        void noSeriesRows_haveNullTagColor() {
            courseRepo.addCourse(new Course("CSE 110", "Software Engineering"));
            Assignment a = makeAssignment("Midterm", "CSE 110", null);
            assignmentRepo.addAssignment(a);

            List<AssignmentListEntry> entries = AssignmentListGrouper.buildGroupedList(
                    assignmentRepo.getAllAssignments(),
                    false,
                    "All Courses",
                    "All Courses",
                    model);

            AssignmentListEntry rowEntry = entries.stream()
                    .filter(e -> !e.isHeader() && e.getAssignment() != null)
                    .findFirst()
                    .orElse(null);
            assertNotNull(rowEntry);
            assertNull(rowEntry.getDisplayName());
            assertNull(rowEntry.getTagColor());
        }
    }

    @Nested
    @DisplayName("regression: showOnlyOpen excludes completed")
    class ShowOnlyOpenRegression {

        @Test
        @DisplayName("showOnlyOpen excludes completed assignments after grouping")
        void showOnlyOpen_excludesDoneAssignments() {
            courseRepo.addCourse(new Course("CSE 110", "Software Engineering"));
            Assignment open = makeAssignment("Open", "CSE 110", null);
            Assignment done = makeAssignment("Done", "CSE 110", null);
            done.markDone();
            assignmentRepo.addAssignment(open);
            assignmentRepo.addAssignment(done);

            List<AssignmentListEntry> entries = AssignmentListGrouper.buildGroupedList(
                    assignmentRepo.getAllAssignments(),
                    true,
                    "All Courses",
                    "All Courses",
                    model);

            List<AssignmentListEntry> rows = entries.stream()
                    .filter(e -> !e.isHeader() && e.getAssignment() != null)
                    .collect(Collectors.toList());
            assertEquals(1, rows.size());
            assertEquals("Open", rows.get(0).getAssignment().getName());
        }
    }
}
