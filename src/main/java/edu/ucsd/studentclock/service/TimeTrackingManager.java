package edu.ucsd.studentclock.service;

import edu.ucsd.studentclock.model.Assignment;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Manages time tracking
 */
public class TimeTrackingManager {

    private Assignment activeAssignment;
    private LocalDateTime clockInTime;

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
        this.clockInTime = LocalDateTime.now();
    }

    /**
     * Stops tracking time for the current assignment.
     * Applies work to the assignment.
     */
    public ClockOutResult clockOut() {
        if (activeAssignment == null || clockInTime == null) {
            throw new IllegalStateException("Not currently clocked into any assignment");
        }

        LocalDateTime now = LocalDateTime.now();

        double hoursWorked = Duration.between(clockInTime, now).toMinutes() / 60.0;

        activeAssignment.applyWork(hoursWorked);

        // auto mark done if remaining reaches zero
        if (activeAssignment.getRemainingHours() <= 0.0) {
            activeAssignment.markDone();
        }

        ClockOutResult result = new ClockOutResult(
                activeAssignment.getID(),
                hoursWorked,
                activeAssignment.getCumulativeHours(),
                activeAssignment.getRemainingHours(),
                activeAssignment.isDone()
        );

        // clear active session
        activeAssignment = null;
        clockInTime = null;

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
