package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.view.AssignmentView;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentPresenterCreateTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private CourseRepository courseRepository;
    private AssignmentRepository assignmentRepository;
    private Model model;

    private FakeAssignmentView view;
    private AssignmentPresenter presenter;

    private static class TestDataSource implements IDataSource {
        private final Connection connection;
        TestDataSource(Connection connection) { this.connection = connection; }
        @Override public Connection getConnection() { return connection; }
    }

    // Fake view to satisfy presenter without real UI
    private static class FakeAssignmentView extends AssignmentView {
        private AssignmentPresenter presenter;
        private List<String> courses = new ArrayList<>();
        private List<Assignment> shownAssignments = new ArrayList<>();

        @Override
        public void setPresenter(AssignmentPresenter presenter) {
            this.presenter = presenter;
        }

        @Override
        public void setCourses(List<String> courseIds) {
            this.courses = new ArrayList<>(courseIds);
        }

        @Override
        public void showAssignments(List<Assignment> assignments) {
            this.shownAssignments = new ArrayList<>(assignments);
        }

        // helpers for assertions
        public List<String> getCourses() { return courses; }
        public List<Assignment> getShownAssignments() { return shownAssignments; }
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);

        courseRepository = new CourseRepository(connection);
        assignmentRepository = new AssignmentRepository(new TestDataSource(connection));
        model = new Model(courseRepository);

        // add courses so presenter can populate dropdown
        model.addCourse("CSE 110", "Software Engineering");
        model.addCourse("CSE 101", "Intro to CS");

        view = new FakeAssignmentView();
        presenter = new AssignmentPresenter(model, view, assignmentRepository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    
    @Test
    void createMultipleAssignmentsForDifferentCoursesStoredSeparately() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime d1 = LocalDateTime.of(2026, 2, 3, 23, 59);
        LocalDateTime d2 = LocalDateTime.of(2026, 2, 5, 23, 59);
        LocalDateTime d3 = LocalDateTime.of(2026, 2, 6, 23, 59);

        // 2 assignments in CSE 110
        presenter.createAssignment("Quiz 2 Study", "CSE 110", start, d1, 0);
        presenter.createAssignment("MVP", "CSE 110", start, d2, 2);

        // 1 assignment in CSE 101
        presenter.createAssignment("PA1", "CSE 101", start, d3, 0);

        // verify repository results per course
        List<Assignment> cse110 = assignmentRepository.getAssignmentsForCourse("CSE 110");
        List<Assignment> cse101 = assignmentRepository.getAssignmentsForCourse("CSE 101");

        assertEquals(2, cse110.size());
        assertTrue(cse110.stream().allMatch(a -> "CSE 110".equals(a.getCourseID())));
        assertTrue(cse110.stream().anyMatch(a -> "Quiz 2 Study".equals(a.getName())));
        assertTrue(cse110.stream().anyMatch(a -> "MVP".equals(a.getName())));

        assertEquals(1, cse101.size());
        assertTrue(cse101.stream().allMatch(a -> "CSE 101".equals(a.getCourseID())));
        assertTrue(cse101.stream().anyMatch(a -> "PA1".equals(a.getName())));

        // (optional) verify view updated (presenter calls updateView after creation)
        assertEquals(3, view.getShownAssignments().size());
        assertTrue(view.getShownAssignments().stream().anyMatch(a -> "PA1".equals(a.getName())));
    }
}
