package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.model.Assignment;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class TimeTrackingManagerTest {

    static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant start) {
            this.instant = start;
            this.zone = ZoneId.of("UTC");
        }

        void plusSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }

        @Override public ZoneId getZone() { return zone; }

        @Override public Clock withZone(ZoneId zone) {
            return new MutableClock(instant);
        }

        @Override public Instant instant() { return instant; }
    }

    private static Assignment makeAssignment(double estimatedHours) {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);
        return new Assignment("Quiz 2 Study", "CSE 110", start, deadline, 0, estimatedHours);
    }

    @Test
    void clockInWithNullThrows() {
        MutableClock clock = new MutableClock(Instant.parse("2026-02-01T10:00:00Z"));
        TimeTrackingManager manager = new TimeTrackingManager(clock);

        assertThrows(NullPointerException.class, () -> manager.clockIn(null));
    }

    @Test
    void clockInOnDoneAssignmentThrows() {
        MutableClock clock = new MutableClock(Instant.parse("2026-02-01T10:00:00Z"));
        TimeTrackingManager manager = new TimeTrackingManager(clock);

        Assignment a = makeAssignment(1.0);
        a.markDone();

        assertThrows(IllegalStateException.class, () -> manager.clockIn(a));
    }

    @Test
    void clockOutWhenNotClockedInThrows() {
        MutableClock clock = new MutableClock(Instant.parse("2026-02-01T10:00:00Z"));
        TimeTrackingManager manager = new TimeTrackingManager(clock);

        assertThrows(IllegalStateException.class, manager::clockOut);
    }

    @Test
    void clockInThenClockOut_updatesCumulativeAndRemaining() {
        MutableClock clock = new MutableClock(Instant.parse("2026-02-01T10:00:00Z"));
        TimeTrackingManager manager = new TimeTrackingManager(clock);

        Assignment a = makeAssignment(10.0);

        assertFalse(manager.isTracking());

        manager.clockIn(a);

        assertTrue(manager.isTracking());
        assertEquals(a, manager.getActiveAssignment());

        clock.plusSeconds(90 * 60);

        ClockOutResult result = manager.clockOut();

        assertFalse(manager.isTracking());
        assertNull(manager.getActiveAssignment());

        assertEquals(a.getID(), result.getAssignmentId());
        assertEquals(1.5, result.getSessionHours());

        assertEquals(1.5, a.getCumulativeHours());
        assertEquals(a.getCumulativeHours(), result.getCumulativeHours());

        assertEquals(8.5, a.getRemainingHours());
        assertEquals(a.getRemainingHours(), result.getRemainingHours());

        assertFalse(a.isDone());
        assertFalse(result.isDone());
    }

    @Test
    void cannotClockInTwiceWithoutClockOut() {
        MutableClock clock = new MutableClock(Instant.parse("2026-02-01T10:00:00Z"));
        TimeTrackingManager manager = new TimeTrackingManager(clock);

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
    void assignmentNotDoneAutomatically() {
        MutableClock clock = new MutableClock(Instant.parse("2026-02-01T10:00:00Z"));
        TimeTrackingManager manager = new TimeTrackingManager(clock);
        Assignment a = makeAssignment(3.0);

        manager.clockIn(a);
        clock.plusSeconds(60 * 60 * 4);
        ClockOutResult result = manager.clockOut();

        assertEquals(4.0, a.getCumulativeHours());
        assertEquals(a.getCumulativeHours(), result.getCumulativeHours());
        assertEquals(0.0, a.getRemainingHours());
        assertEquals(a.getRemainingHours(), result.getRemainingHours());
        assertFalse(a.isDone());
        assertFalse(result.isDone());
    }
}
