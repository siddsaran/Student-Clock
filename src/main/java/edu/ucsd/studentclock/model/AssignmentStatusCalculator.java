package edu.ucsd.studentclock.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Calculates urgency and behind-schedule status for assignments.
 */
public class AssignmentStatusCalculator {

    // Thresholds from project description
    public static final double YELLOW_THRESHOLD = 0.25;
    public static final double ORANGE_THRESHOLD = 0.50;
    public static final double RED_THRESHOLD = 0.75;

    /**
     * Returns true if assignment is due within the next 24 hours.
     */
    public static boolean isUrgent(Assignment a, LocalDateTime now) {
        return a.getDeadline().isBefore(now.plusDays(1));
    }

    /**
     * Returns how far behind the assignment is (0.0–1.0).
     */
    public static double behindFraction(Assignment a, LocalDateTime now) {

        if (a.isDone()) return 0.0;
        if (now.isBefore(a.getStart())) return 0.0;

        double estimated = a.getEstimatedHours();
        if (estimated <= 0) return 0.0;

        double remaining = a.getRemainingHours();
        double actualDone = (estimated - remaining) / estimated;

        Duration total = Duration.between(a.getStart(), a.getDeadline());
        Duration elapsed = Duration.between(a.getStart(), now);

        if (total.isZero() || total.isNegative()) return 0.0;

        double expectedDone = Math.min(
                1.0,
                (double) elapsed.toMinutes() / total.toMinutes()
        );

        return Math.max(0.0, expectedDone - actualDone);
    }

    /**
     * Returns severity status based on how far behind the assignment is.
     */
    public static AssignmentStatus behindStatus(Assignment a, LocalDateTime now) {

        if (a.isDone()) return AssignmentStatus.NONE;

        double behind = behindFraction(a, now);

        if (behind >= RED_THRESHOLD) return AssignmentStatus.RED;
        if (behind >= ORANGE_THRESHOLD) return AssignmentStatus.ORANGE;
        if (behind >= YELLOW_THRESHOLD) return AssignmentStatus.YELLOW;

        return AssignmentStatus.NONE;
    }
}
