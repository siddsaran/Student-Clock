package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 2: Unit tests for domain and model logic (TimeTrackingManager).
 * MS1: US7 (Track time spent working on assignments).
 */
@DisplayName("DS7-2: TimeTrackingManager")
class TimeTrackingManagerTest {

    private static Assignment makeAssignment(double estimatedHours) {
        return new AssignmentBuilder()
                .setName("Quiz 2 Study")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(estimatedHours)
                .build();
    }

    private static LocalDateTime initMockTime(TimeService timeService) {
        timeService.useMockTime();
        return timeService.now();
    }

    private static void setMock(TimeService timeService, LocalDateTime dt) {
        timeService.setMockDateTime(dt.toLocalDate(), dt.toLocalTime());
    }

    @Test
    @DisplayName("clockIn throws NullPointerException when assignment is null")
    void clockInWithNullThrows() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        assertThrows(NullPointerException.class, () -> manager.clockIn(null));
    }

    @Test
    @DisplayName("clockIn throws IllegalStateException when assignment is already marked done")
    void clockInOnDoneAssignmentThrows() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        Assignment a = makeAssignment(1.0);
        a.markDone();

        assertThrows(IllegalStateException.class, () -> manager.clockIn(a));
    }

    @Test
    @DisplayName("clockOut throws IllegalStateException when no active session exists")
    void clockOutWhenNotClockedInThrows() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        assertThrows(IllegalStateException.class, manager::clockOut);
    }

    @Test
    @DisplayName("clockIn followed by clockOut updates cumulative and remaining hours")
    void clockInThenClockOut_updatesCumulativeAndRemaining() {
        TimeService timeService = new TimeService();
        LocalDateTime base = initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        Assignment a = makeAssignment(10.0);

        assertFalse(manager.isTracking());

        manager.clockIn(a);

        assertTrue(manager.isTracking());
        assertEquals(a, manager.getActiveAssignment());

        // Advance mock time by 90 minutes (1.5 hours)
        setMock(timeService, base.plusMinutes(90));

        ClockOutResult result = manager.clockOut();

        assertFalse(manager.isTracking());
        assertNull(manager.getActiveAssignment());

        assertEquals(a.getId(), result.getAssignmentId());
        assertEquals(1.5, result.getSessionHours());

        assertEquals(1.5, a.getCumulativeHours());
        assertEquals(a.getCumulativeHours(), result.getCumulativeHours());

        // Expected remaining = 10.0 - 1.5 = 8.5
        assertEquals(8.5, a.getRemainingHours());
        assertEquals(a.getRemainingHours(), result.getRemainingHours());

        assertFalse(a.isDone());
        assertFalse(result.isDone());
    }

    @Test
    @DisplayName("clockIn throws when another assignment is already being tracked")
    void cannotClockInTwiceWithoutClockOut() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        Assignment a1 = makeAssignment(5.0);
        Assignment a2 = makeAssignment(5.0);

        manager.clockIn(a1);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.clockIn(a2));

        assertTrue(e.getMessage().toLowerCase().contains("already"));
    }

    @Test
    @DisplayName("Assignment is not automatically marked done when remaining hours reach zero")
    void assignmentNotDoneAutomaticallyWhenRemainingHitsZero() {
        TimeService timeService = new TimeService();
        LocalDateTime base = initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);
        Assignment a = makeAssignment(3.0);

        manager.clockIn(a);

        // Advance 4 hours
        setMock(timeService, base.plusHours(4));

        ClockOutResult result = manager.clockOut();

        assertEquals(4.0, a.getCumulativeHours());
        assertEquals(a.getCumulativeHours(), result.getCumulativeHours());

        assertEquals(0.0, a.getRemainingHours());
        assertEquals(a.getRemainingHours(), result.getRemainingHours());

        assertFalse(a.isDone());
        assertFalse(result.isDone());
    }
}