package edu.ucsd.studentclock.presenter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;
import edu.ucsd.studentclock.repository.StudyAvailabilityRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import edu.ucsd.studentclock.service.TimeService;
import edu.ucsd.studentclock.view.AssignmentListEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("AssignmentListGrouper regression")
class AssignmentListGrouperRegressionTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private Model model;
    private CourseRepository courseRepo;
    private AssignmentRepository assignmentRepo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        IDataSource dataSource = () -> connection;
        courseRepo = new CourseRepository(dataSource);
        SeriesRepository seriesRepo = new SeriesRepository(dataSource);
        StudyAvailabilityRepository saRepo = new StudyAvailabilityRepository(dataSource);
        assignmentRepo = new AssignmentRepository(dataSource);
        model = new Model(
                courseRepo,
                assignmentRepo,
                seriesRepo,
                saRepo,
                new WorkLogRepository(dataSource),
                new AssignmentWorkLogRepository(dataSource),
                new TimeService()
        );
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private Assignment makeAssignment(
            String name,
            String courseId,
            boolean done
    ) {
        return new AssignmentBuilder()
                .setName(name)
                .setCourseId(courseId)
                .setStart(LocalDateTime.of(2026, 3, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 3, 20, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(2.0)
                .setDone(done)
                .build();
    }

    @Test
    @DisplayName("showOnlyOpen still excludes completed assignments after consolidation")
    void showOnlyOpen_stillExcludesCompletedAssignments() {
        courseRepo.addCourse(new Course("CSE 110", "Software Engineering"));

        assignmentRepo.addAssignment(makeAssignment("Open", "CSE 110", false));
        assignmentRepo.addAssignment(makeAssignment("Done", "CSE 110", true));

        List<AssignmentListEntry> rows = AssignmentListGrouper.buildGroupedList(
                        assignmentRepo.getAllAssignments(),
                        true,
                        "All Courses",
                        "All Courses",
                        model
                ).stream()
                .filter(entry -> !entry.isHeader())
                .collect(Collectors.toList());

        assertEquals(1, rows.size());
        assertEquals("Open", rows.get(0).getAssignment().getName());
    }

    @Test
    @DisplayName("course filter still limits rows to the selected course after consolidation")
    void courseFilter_stillLimitsRowsToSelectedCourse() {
        courseRepo.addCourse(new Course("CSE 110", "Software Engineering"));
        courseRepo.addCourse(new Course("CSE 120", "Operating Systems"));

        assignmentRepo.addAssignment(makeAssignment("PA1", "CSE 110", false));
        assignmentRepo.addAssignment(makeAssignment("HW1", "CSE 120", false));

        List<AssignmentListEntry> rows = AssignmentListGrouper.buildGroupedList(
                        assignmentRepo.getAllAssignments(),
                        false,
                        "CSE 110",
                        "All Courses",
                        model
                ).stream()
                .filter(entry -> !entry.isHeader())
                .collect(Collectors.toList());

        assertEquals(1, rows.size());
        assertEquals("CSE 110", rows.get(0).getAssignment().getCourseId());
        assertEquals("PA1", rows.get(0).getAssignment().getName());
    }
}