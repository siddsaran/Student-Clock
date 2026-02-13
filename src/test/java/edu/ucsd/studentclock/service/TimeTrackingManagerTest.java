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

    @Test
    void clockInWithNullThrows() {
        TimeTrackingManager manager = new TimeTrackingManager();
        assertThrows(NullPointerException.class, () -> manager.clockIn(null));
    }

    @Test
    void clockInOnDoneAssignmentThrows() {
        TimeTrackingManager manager = new TimeTrackingManager();
        Assignment a = makeAssignment(1.0);
        a.markDone();
        assertThrows(IllegalStateException.class, () -> manager.clockIn(a));
    }

    @Test
    void clockOutWhenNotClockedInThrows() {
        TimeTrackingManager manager = new TimeTrackingManager();
        assertThrows(IllegalStateException.class, manager::clockOut);
    }

    @Test
    void clockInThenClockOutUpdatesAssignmentAndReturnsResult() throws InterruptedException {
        TimeTrackingManager manager = new TimeTrackingManager();
        Assignment a = makeAssignment(10.0);

        assertFalse(manager.isTracking());
        manager.clockIn(a);
        assertTrue(manager.isTracking());
        assertEquals(a, manager.getActiveAssignment());

        // sleep a bit so elapsed time > 0
        Thread.sleep(50);

        ClockOutResult result = manager.clockOut();

        assertFalse(manager.isTracking());
        assertNull(manager.getActiveAssignment());

        assertEquals(a.getID(), result.getAssignmentId());

        // sessionHours should be >= 0 (small but non-negative)
        assertTrue(result.getSessionHours() >= 0.0);

        // cumulative should match assignment cumulative
        assertEquals(a.getCumulativeHours(), result.getCumulativeHours());

        // remaining should match assignment remaining
        assertEquals(a.getRemainingHours(), result.getRemainingHours());

        // done should match
        assertEquals(a.isDone(), result.isDone());
    }

    @Test
    void cannotClockInTwiceWithoutClockOut() {
        TimeTrackingManager manager = new TimeTrackingManager();
        Assignment a1 = makeAssignment(5.0);
        Assignment a2 = makeAssignment(5.0);

        manager.clockIn(a1);

        IllegalStateException e =
                assertThrows(IllegalStateException.class, () -> manager.clockIn(a2));

        assertTrue(e.getMessage().toLowerCase().contains("already"));
    }
}
