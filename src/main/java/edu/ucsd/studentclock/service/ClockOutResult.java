package edu.ucsd.studentclock.service;

/**
 * Result returned when clocking out of an assignment.
 */
public class ClockOutResult {

    private final String assignmentId;
    private final double sessionHours;
    private final double cumulativeHours;
    private final double remainingHours;
    private final boolean done;

    public ClockOutResult(String assignmentId,
            double sessionHours,
            double cumulativeHours,
            double remainingHours,
            boolean done) {
        this.assignmentId = assignmentId;
        this.sessionHours = sessionHours;
        this.cumulativeHours = cumulativeHours;
        this.remainingHours = remainingHours;
        this.done = done;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public double getSessionHours() {
        return sessionHours;
    }

    public double getCumulativeHours() {
        return cumulativeHours;
    }

    public double getRemainingHours() {
        return remainingHours;
    }

    public boolean isDone() {
        return done;
    }
}
