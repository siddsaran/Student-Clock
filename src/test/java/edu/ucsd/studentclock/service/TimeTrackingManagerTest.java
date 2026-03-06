package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.model.Assignment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeTrackingManagerTest {
    private static Assignment makeAssignment(double estimatedHours) {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);
        return new Assignment("Quiz 2 Study", "CSE 110", start, deadline, 0, estimatedHours);
    }

    private static LocalDateTime initMockTime(TimeService timeService) {
        timeService.useMockTime();
        return timeService.now();
    }

    private static void setMock(TimeService timeService, LocalDateTime dt) {
        timeService.setMockDateTime(dt.toLocalDate(), dt.toLocalTime());
    }

    @Test
    void clockInWithNullThrows() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        assertThrows(NullPointerException.class, () -> manager.clockIn(null));
    }

    @Test
    void clockInOnDoneAssignmentThrows() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        Assignment a = makeAssignment(1.0);
        a.markDone();

        assertThrows(IllegalStateException.class, () -> manager.clockIn(a));
    }

    @Test
    void clockOutWhenNotClockedInThrows() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        assertThrows(IllegalStateException.class, manager::clockOut);
    }

    @Test
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
    void cannotClockInTwiceWithoutClockOut() {
        TimeService timeService = new TimeService();
        initMockTime(timeService);

        TimeTrackingManager manager = new TimeTrackingManager(timeService);

        Assignment a1 = makeAssignment(5.0);
        Assignment a2 = new Assignment(
                "PA1",
                "CSE 110",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 5, 23, 59),
                0,
                5.0
        );

        manager.clockIn(a1);

        IllegalStateException e =
                assertThrows(IllegalStateException.class, () -> manager.clockIn(a2));

        assertTrue(e.getMessage().toLowerCase().contains("already"));
    }

    @Test
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

        // Manager does not auto-mark done (your current behavior)
        assertFalse(a.isDone());
        assertFalse(result.isDone());
    }
}