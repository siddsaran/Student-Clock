package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.model.Assignment;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Manages time tracking
 */
public class TimeTrackingManager {

    private final TimeService timeService;
    private Assignment activeAssignment;
    private Instant clockInInstant;

    public TimeTrackingManager() {
        this(new TimeService());
    }

    public TimeTrackingManager(TimeService timeService) {
        if (timeService == null) {
            throw new NullPointerException("clock must not be null");
        }
        this.timeService = timeService;
    }

    /**
     * Starts tracking time for an assignment.
     * Throws if another assignment is already active.
     */
    public void clockIn(Assignment assignment) {
        if (assignment == null) {
            throw new NullPointerException("assignment must not be null");
        }

        if (activeAssignment != null) {
            throw new IllegalStateException(
                "Already clocked into assignment: " + activeAssignment.getName()
            );
        }

        if (assignment.isDone()) {
            throw new IllegalStateException("Cannot clock into completed assignment");
        }

        this.activeAssignment = assignment;
        this.clockInInstant = timeService.nowInstant();
    }

    /**
     * Stops tracking time for the current assignment.
     * Applies work to the assignment.
     */
    public ClockOutResult clockOut() {
        if (activeAssignment == null || clockInInstant == null) {
            throw new IllegalStateException("Not currently clocked into any assignment");
        }

        Instant out = timeService.nowInstant();

        double hoursWorked = Duration.between(clockInInstant, out).toMinutes() / 60.0;

        activeAssignment.applyWork(hoursWorked);

        ClockOutResult result = new ClockOutResult(
                activeAssignment.getID(),
                hoursWorked,
                activeAssignment.getCumulativeHours(),
                activeAssignment.getRemainingHours(),
                activeAssignment.isDone()
        );

        // clear active session
        activeAssignment = null;
        clockInInstant = null;

        return result;
    }

    /**
     * Returns true if an assignment is currently being tracked.
     */
    public boolean isTracking() {
        return activeAssignment != null;
    }

    /**
     * Returns the currently active assignment (null if none).
     */
    public Assignment getActiveAssignment() {
        return activeAssignment;
    }
}
