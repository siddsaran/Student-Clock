package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.util.List;

public class StudyStatusCalculator {

    public static AssignmentStatus overallStudyStatus(List<Assignment> assignments,
                                                      StudyAvailability availability,
                                                      LocalDateTime now) {

        if (assignments == null || availability == null || now == null) {
            throw new NullPointerException();
        }

        double totalRemainingHoursDueSoon = assignments.stream()
                .filter(a -> a != null)
                .filter(a -> !a.isDone())
                .filter(a -> !a.getDeadline().isAfter(now.plusDays(7)))
                .mapToDouble(Assignment::getRemainingHours)
                .sum();

        boolean hasDueWithinDay = assignments.stream()
                .filter(a -> a != null)
                .filter(a -> !a.isDone())
                .anyMatch(a -> a.getDeadline().isBefore(now.plusDays(1)));

        int availableHours = availability.getTotalWeeklyHours();
        if (availableHours <= 0) {
            if (totalRemainingHoursDueSoon <= 0.0) {
                return AssignmentStatus.NONE;
            }
            return AssignmentStatus.RED;
        }

        double ratio = totalRemainingHoursDueSoon / availableHours;

        if (ratio >= 1.0) {
            return AssignmentStatus.RED;
        }

        if (hasDueWithinDay) {
            if (ratio >= 0.75) {
                return AssignmentStatus.RED;
            }
            if (ratio >= 0.50) {
                return AssignmentStatus.ORANGE;
            }
            if (ratio > 0.0) {
                return AssignmentStatus.YELLOW;
            }
            return AssignmentStatus.NONE;
        }

        if (ratio >= 0.75) {
            return AssignmentStatus.ORANGE;
        }
        if (ratio >= 0.50) {
            return AssignmentStatus.YELLOW;
        }

        return AssignmentStatus.NONE;
    }
}
