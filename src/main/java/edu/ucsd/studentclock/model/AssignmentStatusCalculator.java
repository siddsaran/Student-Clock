package edu.ucsd.studentclock.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Calculates urgency and behind-schedule status for assignments.
 */
public class AssignmentStatusCalculator {

    public static final double YELLOW_THRESHOLD = 0.25;
    public static final double ORANGE_THRESHOLD = 0.50;
    public static final double RED_THRESHOLD = 0.75;

    /**
     * Returns true if assignment is due within the next 24 hours.
     */
    public static boolean isUrgent(Assignment assignment, LocalDateTime now) {
        return assignment.getDeadline().isBefore(now.plusDays(1));
    }

    /**
     * Returns how far behind the assignment is (0.0–1.0).
     */
    public static double behindFraction(Assignment assignment, LocalDateTime now) {
        if (assignment.isDone()) {
            return 0.0;
        }
        if (now.isBefore(assignment.getStart())) {
            return 0.0;
        }

        double estimatedHours = assignment.getEstimatedHours();
        if (estimatedHours <= 0.0) {
            return 0.0;
        }

        double remainingHours = assignment.getRemainingHours();
        double actualDone = (estimatedHours - remainingHours) / estimatedHours;

        Duration total = Duration.between(assignment.getStart(), assignment.getDeadline());
        Duration elapsed = Duration.between(assignment.getStart(), now);

        if (total.isZero() || total.isNegative()) {
            return 0.0;
        }

        double expectedDone = Math.min(
                1.0,
                (double) elapsed.toMinutes() / total.toMinutes());

        return Math.max(0.0, expectedDone - actualDone);
    }

    /**
     * Returns severity status based on how far behind the assignment is.
     */
    public static AssignmentStatus behindStatus(Assignment assignment, LocalDateTime now) {
        if (assignment.isDone()) {
            return AssignmentStatus.NONE;
        }

        double behindFraction = behindFraction(assignment, now);

        if (behindFraction >= RED_THRESHOLD) {
            return AssignmentStatus.RED;
        }
        if (behindFraction >= ORANGE_THRESHOLD) {
            return AssignmentStatus.ORANGE;
        }
        if (behindFraction >= YELLOW_THRESHOLD) {
            return AssignmentStatus.YELLOW;
        }

        return AssignmentStatus.NONE;
    }
}