package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkSessionServiceTest {

    private static final String JDBC_MEMORY_URL = "jdbc:sqlite::memory:";

    private Connection connection;
    private AssignmentRepository assignmentRepository;
    private WorkLogRepository workLogRepository;
    private AssignmentWorkLogRepository assignmentWorkLogRepository;
    private TimeService timeService;
    private WorkSessionService service;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(JDBC_MEMORY_URL);
        IDataSource dataSource = () -> connection;

        assignmentRepository = new AssignmentRepository(dataSource);
        workLogRepository = new WorkLogRepository(dataSource);
        assignmentWorkLogRepository = new AssignmentWorkLogRepository(dataSource);

        timeService = new TimeService();
        timeService.useMockTime();

        service = new WorkSessionService(
                timeService,
                workLogRepository,
                assignmentWorkLogRepository,
                assignmentRepository
        );
    }

    private Assignment makeAssignment(double estimatedHours) {
        Assignment a = new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(estimatedHours)
                .build();
        assignmentRepository.addAssignment(a);
        return a;
    }

    private void advanceMinutes(long minutes) {
        LocalDateTime now = timeService.now();
        timeService.setMockDateTime(
                now.plusMinutes(minutes).toLocalDate(),
                now.plusMinutes(minutes).toLocalTime()
        );
    }

    @Test
    void constructorThrowsWhenTimeServiceNull() {
        assertThrows(NullPointerException.class, () ->
                new WorkSessionService(null, workLogRepository, assignmentWorkLogRepository, assignmentRepository));
    }

    @Test
    void constructorThrowsWhenWorkLogRepositoryNull() {
        assertThrows(NullPointerException.class, () ->
                new WorkSessionService(timeService, null, assignmentWorkLogRepository, assignmentRepository));
    }

    @Test
    void constructorThrowsWhenAssignmentWorkLogRepositoryNull() {
        assertThrows(NullPointerException.class, () ->
                new WorkSessionService(timeService, workLogRepository, null, assignmentRepository));
    }

    @Test
    void constructorThrowsWhenAssignmentRepositoryNull() {
        assertThrows(NullPointerException.class, () ->
                new WorkSessionService(timeService, workLogRepository, assignmentWorkLogRepository, null));
    }

    @Test
    void isTrackingFalseBeforeClockIn() {
        assertFalse(service.isTracking());
    }

    @Test
    void isTrackingTrueAfterClockIn() {
        service.clockIn(makeAssignment(5.0));
        assertTrue(service.isTracking());
    }

    @Test
    void isTrackingFalseAfterClockOut() {
        Assignment a = makeAssignment(5.0);
        service.clockIn(a);
        service.clockOut(a.getId());
        assertFalse(service.isTracking());
    }

    @Test
    void clockInNullAssignmentThrows() {
        assertThrows(NullPointerException.class, () -> service.clockIn(null));
    }

    @Test
    void clockInDoneAssignmentThrows() {
        Assignment a = makeAssignment(1.0);
        a.markDone();
        assertThrows(IllegalStateException.class, () -> service.clockIn(a));
    }

    @Test
    void clockInWhileAlreadyTrackingThrows() {
        Assignment a1 = makeAssignment(5.0);
        Assignment a2 = makeAssignment(5.0);
        service.clockIn(a1);
        assertThrows(IllegalStateException.class, () -> service.clockIn(a2));
    }

    @Test
    void clockOutWhenNotTrackingThrows() {
        assertThrows(IllegalStateException.class, () -> service.clockOut("any-id"));
    }

    @Test
    void clockOutWithWrongIdThrows() {
        Assignment a = makeAssignment(5.0);
        service.clockIn(a);
        assertThrows(IllegalArgumentException.class, () -> service.clockOut("wrong-id"));
    }

    @Test
    void clockOutReturnsCorrectResult() {
        Assignment a = makeAssignment(10.0);
        service.clockIn(a);
        advanceMinutes(90);

        ClockOutResult result = service.clockOut(a.getId());

        assertEquals(a.getId(), result.getAssignmentId());
        assertEquals(1.5, result.getSessionHours());
        assertEquals(1.5, result.getCumulativeHours());
        assertEquals(8.5, result.getRemainingHours());
        assertFalse(result.isDone());
    }

    @Test
    void clockOutPersistsHoursToAssignmentWorkLogRepository() {
        Assignment a = makeAssignment(10.0);
        service.clockIn(a);
        advanceMinutes(120);
        service.clockOut(a.getId());

        var cumulative = assignmentWorkLogRepository.getCumulativeHoursByEndOf(
                timeService.now().toLocalDate());
        assertTrue(cumulative.containsKey(a.getId()));
        assertEquals(2.0, cumulative.get(a.getId()));
    }

    @Test
    void clockOutPersistsHoursToWorkLogRepository() {
        Assignment a = makeAssignment(10.0);
        service.clockIn(a);
        advanceMinutes(60);
        service.clockOut(a.getId());
        assertEquals(1.0, workLogRepository.getTotalHoursLoggedThisWeek());
    }

    @Test
    void clockOutFlushesUpdatedAssignmentToRepository() {
        Assignment a = makeAssignment(10.0);
        service.clockIn(a);
        advanceMinutes(90);
        service.clockOut(a.getId());

        Assignment reloaded = assignmentRepository.getAllAssignments().stream()
                .filter(x -> x.getId().equals(a.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(1.5, reloaded.getCumulativeHours());
        assertEquals(8.5, reloaded.getRemainingHours());
    }
    
    @Test
    void applyManualHoursNegativeThrows() {
        Assignment a = makeAssignment(5.0);
        assertThrows(IllegalArgumentException.class, () -> service.applyManualHours(a, -1.0));
    }

    @Test
    void applyManualHoursUpdatesAssignmentHours() {
        Assignment a = makeAssignment(5.0);
        service.applyManualHours(a, 2.0);

        assertEquals(2.0, a.getCumulativeHours());
        assertEquals(3.0, a.getRemainingHours());
    }

    @Test
    void applyManualHoursPersistsToWorkLogRepository() {
        Assignment a = makeAssignment(5.0);
        service.applyManualHours(a, 3.0);

        assertEquals(3.0, workLogRepository.getTotalHoursLoggedThisWeek());
    }

    @Test
    void applyManualHoursPersistsToAssignmentWorkLogRepository() {
        Assignment a = makeAssignment(5.0);
        service.applyManualHours(a, 3.0);

        var cumulative = assignmentWorkLogRepository.getCumulativeHoursByEndOf(
                timeService.now().toLocalDate());
        assertTrue(cumulative.containsKey(a.getId()));
        assertEquals(3.0, cumulative.get(a.getId()));
    }

    @Test
    void applyManualHoursFlushesUpdatedAssignmentToRepository() {
        Assignment a = makeAssignment(5.0);
        service.applyManualHours(a, 3.0);

        Assignment reloaded = assignmentRepository.getAllAssignments().stream()
                .filter(x -> x.getId().equals(a.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(3.0, reloaded.getCumulativeHours());
        assertEquals(2.0, reloaded.getRemainingHours());
    }

    @Test
    void applyManualHoursExceedingEstimateClampsRemainingToZeroButDoesNotMarkDone() {
        Assignment a = makeAssignment(2.0);
        service.applyManualHours(a, 5.0);

        assertEquals(5.0, a.getCumulativeHours());
        assertEquals(0.0, a.getRemainingHours());
        assertFalse(a.isDone());
    }

    @Test
    void applyManualHoursZeroIsAllowed() {
        Assignment a = makeAssignment(5.0);
        assertDoesNotThrow(() -> service.applyManualHours(a, 0.0));
        assertEquals(0.0, a.getCumulativeHours());
        assertEquals(5.0, a.getRemainingHours());
    }
}