package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;
import edu.ucsd.studentclock.view.AssignmentView;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentPresenterTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private CourseRepository courseRepository;
    private AssignmentRepository assignmentRepository;
    private SeriesRepository seriesRepository;
    private Model model;
    private AssignmentView view;
    private AssignmentPresenter presenter;

    @BeforeAll
    static void initJavaFx() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit should start");
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        courseRepository = new CourseRepository(connection);
        assignmentRepository = new AssignmentRepository(() -> connection);
        seriesRepository = new SeriesRepository(connection);
        model = new Model(courseRepository, assignmentRepository, seriesRepository);
        view = new AssignmentView();
        presenter = new AssignmentPresenter(model, view, assignmentRepository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void createAssignmentWithNullSeriesIdStoresAssignmentWithoutSeries() {
        model.addCourse(new Course("CSE 110", "Software Engineering"));
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        presenter.createAssignment("PA1", "CSE 110", start, deadline, 0, 2.0, null);

        List<Assignment> assignments = assignmentRepository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, assignments.size());
        Assignment a = assignments.get(0);
        assertEquals("PA1", a.getName());
        assertNull(a.getSeriesId());
        assertEquals(0, a.getLateDaysAllowed());
    }

    @Test
    void createAssignmentWithExistingSeriesIdUsesSeriesDefaultLateDaysAndLinks() {
        model.addCourse(new Course("CSE 110", "Software Engineering"));
        Series series = new Series("pa-series", "CSE 110", "PAs", 2);
        model.addSeries(series);
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        presenter.createAssignment("PA1", "CSE 110", start, deadline, 0, 2.0, "pa-series");

        List<Assignment> assignments = assignmentRepository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, assignments.size());
        Assignment a = assignments.get(0);
        assertEquals("PA1", a.getName());
        assertEquals("pa-series", a.getSeriesId());
        assertEquals(2, a.getLateDaysAllowed());
    }

    @Test
    void createAssignmentWithUnknownSeriesIdThrows() {
        model.addCourse(new Course("CSE 110", "Software Engineering"));
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        assertThrows(IllegalArgumentException.class, () ->
                presenter.createAssignment("PA1", "CSE 110", start, deadline, 0, 2.0, "unknown-series"));
    }

    @Test
    void createSeriesThenCreateAssignmentStoresBothAndLinksAssignment() {
        model.addCourse(new Course("CSE 110", "Software Engineering"));
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        presenter.createSeries("pa-series", "CSE 110", "PAs", 2);
        presenter.createAssignment("PA1", "CSE 110", start, deadline, 2, 2.0, "pa-series");

        assertTrue(model.getSeries("pa-series").isPresent());
        assertEquals("PAs", model.getSeries("pa-series").get().getName());

        List<Assignment> inSeries = assignmentRepository.getAssignmentsBySeries("pa-series");
        assertEquals(1, inSeries.size());
        assertEquals("PA1", inSeries.get(0).getName());
        assertEquals(2, inSeries.get(0).getLateDaysAllowed());
    }

    @Test
    void getSeriesForCourseReturnsSeriesForThatCourse() {
        model.addCourse(new Course("CSE 110", "Software Engineering"));
        model.addCourse(new Course("CSE 101", "Other"));
        model.addSeries(new Series("pa-110", "CSE 110", "PAs", 2));
        model.addSeries(new Series("pa-101", "CSE 101", "PAs", 1));

        List<Series> cse110 = presenter.getSeriesForCourse("CSE 110");
        assertEquals(1, cse110.size());
        assertEquals("pa-110", cse110.get(0).getId());
        assertEquals("PAs", cse110.get(0).getName());

        List<Series> cse101 = presenter.getSeriesForCourse("CSE 101");
        assertEquals(1, cse101.size());
        assertEquals("pa-101", cse101.get(0).getId());
    }

    @Test
    void getSeriesForCourseWithBlankCourseIdReturnsEmptyList() {
        assertTrue(presenter.getSeriesForCourse("").isEmpty());
        assertTrue(presenter.getSeriesForCourse(null).isEmpty());
    }

    @Test
    void createAssignmentSixArgOverloadDelegatesToSevenArgWithNullSeriesId() {
        model.addCourse(new Course("CSE 110", "Software Engineering"));
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        presenter.createAssignment("PA1", "CSE 110", start, deadline, 0, 2.0);

        List<Assignment> assignments = assignmentRepository.getAssignmentsForCourse("CSE 110");
        assertEquals(1, assignments.size());
        assertNull(assignments.get(0).getSeriesId());
    }
}
