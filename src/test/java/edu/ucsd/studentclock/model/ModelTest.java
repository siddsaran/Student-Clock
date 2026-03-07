package edu.ucsd.studentclock.model;

import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.StudyAvailabilityRepository;
import edu.ucsd.studentclock.service.TimeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private CourseRepository repository;
    private SeriesRepository seriesRepository;
    private AssignmentRepository assignmentRepository;
    private StudyAvailabilityRepository saRepository;
    private Model model;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        edu.ucsd.studentclock.datasource.IDataSource dataSource = () -> connection;
        repository = new CourseRepository(dataSource);
        seriesRepository = new SeriesRepository(dataSource);
        saRepository = new StudyAvailabilityRepository(dataSource);
        assignmentRepository = new AssignmentRepository(dataSource);
        model = new Model(repository, assignmentRepository, seriesRepository, saRepository, new TimeService());
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
        assertThrows(NullPointerException.class, () -> new Model(null, null, seriesRepository, saRepository, new TimeService()));
    }

    @Test
    void modelWithNullSeriesRepositoryThrows() {
        assertThrows(NullPointerException.class, () -> new Model(repository, null, null, saRepository, new TimeService()));
    }

    @Test
    void modelWithNullRepositoryThrows() {
        assertThrows(NullPointerException.class, () -> new Model(null, null, null, saRepository, new TimeService()));
    }

    @Test
    void modelWithNullTimeServiceThrows() {
        assertThrows(NullPointerException.class, () -> new Model(repository, assignmentRepository, seriesRepository, saRepository, null));
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
        assertThrows(IllegalArgumentException.class, () -> model.addCourse(null, "Name"));
    }

    @Test
    void addCourseWithNullNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> model.addCourse("CSE 110", null));
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

        assignmentRepository.addAssignment(new AssignmentBuilder()
                .setName("Quiz 2 Study")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(0)
                .build());
        assignmentRepository.addAssignment(new AssignmentBuilder()
                .setName("MVP")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 6, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(0)
                .build());

        assertEquals(2, assignmentRepository.getAssignmentsForCourse("CSE 110").size());

        model.deleteCourse("CSE 110");

        assertTrue(model.getCourse("CSE 110").isEmpty());
        assertTrue(assignmentRepository.getAssignmentsForCourse("CSE 110").isEmpty());
    }

    @Test
    void createSeriesAndLinkAssignmentsCreatesSeriesAndLinksSelectedAssignments() {
        model.addCourse("CSE 110", "Software Engineering");

        Assignment a1 = new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(0)
                .build();
        Assignment a2 = new AssignmentBuilder()
                .setName("PA2")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 6, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 12, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(0)
                .build();
        assignmentRepository.addAssignment(a1);
        assignmentRepository.addAssignment(a2);

        Series series = new Series("pa-series", "CSE 110", "PAs", 2);
        model.createSeriesAndLinkAssignments(series, List.of(a1.getId()));

        Optional<Series> storedSeries = model.getSeries("pa-series");
        assertTrue(storedSeries.isPresent());
        assertEquals("PAs", storedSeries.get().getName());

        List<Assignment> linked = assignmentRepository.getAssignmentsBySeries("pa-series");
        assertEquals(1, linked.size());
        assertEquals(a1.getId(), linked.get(0).getId());
        assertEquals(2, linked.get(0).getLateDaysAllowed());

        Assignment stillUnlinked = assignmentRepository.getAssignmentsForCourse("CSE 110").stream()
                .filter(a -> a2.getId().equals(a.getId()))
                .findFirst()
                .orElseThrow();
        assertNull(stillUnlinked.getSeriesId());
    }

    @Test
    void getSeriesByCourseReturnsSeriesForThatCourse() {
        model.addCourse("CSE 110", "Software Engineering");
        model.addCourse("CSE 101", "Other");
        model.addSeries(new Series("pa-110", "CSE 110", "PAs", 2));
        model.addSeries(new Series("pa-101", "CSE 101", "PAs", 1));

        List<Series> cse110 = model.getSeriesByCourse("CSE 110");
        assertEquals(1, cse110.size());
        assertEquals("pa-110", cse110.get(0).getId());
        assertEquals("PAs", cse110.get(0).getName());

        List<Series> cse101 = model.getSeriesByCourse("CSE 101");
        assertEquals(1, cse101.size());
        assertEquals("pa-101", cse101.get(0).getId());
    }

    @Test
    void addSeriesThenAddAssignmentWithSeriesIdAndDefaultLateDaysStoresCorrectly() {
        model.addCourse("CSE 110", "Software Engineering");
        Series series = new Series("pa-series", "CSE 110", "PAs", 2);
        model.addSeries(series);

        Assignment a = new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setSeriesId("pa-series")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(series.getDefaultLateDays())
                .setEstimatedHours(2.0)
                .build();
        assignmentRepository.addAssignment(a);

        assertTrue(model.getSeries("pa-series").isPresent());
        List<Assignment> inSeries = assignmentRepository.getAssignmentsBySeries("pa-series");
        assertEquals(1, inSeries.size());
        assertEquals("PA1", inSeries.get(0).getName());
        assertEquals(2, inSeries.get(0).getLateDaysAllowed());
        assertEquals("pa-series", inSeries.get(0).getSeriesId());
    }

    @Test
    void deleteCourseNoOpsOnNullOrBlank() {
        model.addCourse("CSE 110", "Software Engineering");
        model.deleteCourse(null);
        assertTrue(model.getCourse("CSE 110").isPresent());

        model.deleteCourse("   ");
        assertTrue(model.getCourse("CSE 110").isPresent());
    }

    @Test
    void selectedAssignmentRoundTrips() {
        Assignment a = new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(2.0)
                .build();

        assertNull(model.getSelectedAssignment());
        model.setSelectedAssignment(a);
        assertEquals(a, model.getSelectedAssignment());
    }

    @Test
    void setTotalWeeklyHoursPersistsToRepository() {
        model.setTotalWeeklyHours(12);
        StudyAvailability loaded = saRepository.load().orElseThrow();
        assertEquals(12, loaded.getTotalWeeklyHours());
    }

    @Test
    void setAvailableAndDailyLimitPersistToRepository() {
        model.setTotalWeeklyHours(10);

        model.setAvailable(DayOfWeek.MONDAY, true);
        model.setDailyLimit(DayOfWeek.MONDAY, 3);

        StudyAvailability loaded = saRepository.load().orElseThrow();
        assertEquals(10, loaded.getTotalWeeklyHours());
        assertTrue(loaded.isAvailable(DayOfWeek.MONDAY));
        assertEquals(3, loaded.getDailyLimit(DayOfWeek.MONDAY));
    }

    @Test
    void modelConstructorLoadsExistingAvailabilityWhenPresent() throws SQLException {
        StudyAvailability preset = new StudyAvailability();
        preset.setTotalWeeklyHours(7);
        preset.setAvailable(DayOfWeek.WEDNESDAY, true);
        preset.setDailyLimit(DayOfWeek.WEDNESDAY, 2);
        saRepository.save(preset);

        Model reloaded = new Model(repository, assignmentRepository, seriesRepository, saRepository, new TimeService());
        StudyAvailability a = reloaded.getStudyAvailability();
        assertEquals(7, a.getTotalWeeklyHours());
        assertTrue(a.isAvailable(DayOfWeek.WEDNESDAY));
        assertEquals(2, a.getDailyLimit(DayOfWeek.WEDNESDAY));
    }
}