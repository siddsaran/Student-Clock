package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.AssignmentStatusCalculator;

public final class AssignmentFilters {

    private AssignmentFilters() {}

    public static List<Assignment> openAssignments(List<Assignment> assignments) {
        return assignments.stream()
                .filter(Objects::nonNull)
                .filter(assignment -> !assignment.isDone())
                .collect(Collectors.toList());
    }

    public static List<Assignment> filterByCourse(
            List<Assignment> assignments,
            String courseFilter,
            String allCoursesLabel
    ) {
        if (courseFilter == null || courseFilter.isBlank() || allCoursesLabel.equals(courseFilter)) {
            return List.copyOf(assignments);
        }

        return assignments.stream()
                .filter(assignment -> courseFilter.equals(assignment.getCourseId()))
                .collect(Collectors.toList());
    }

    public static List<Assignment> dashboardAssignments(List<Assignment> assignments, LocalDateTime now) {
        return openAssignments(assignments).stream()
                .filter(assignment -> {
                    boolean urgent = AssignmentStatusCalculator.isUrgent(assignment, now);
                    AssignmentStatus status = AssignmentStatusCalculator.behindStatus(assignment, now);
                    return urgent
                            || status == AssignmentStatus.RED
                            || status == AssignmentStatus.ORANGE
                            || status == AssignmentStatus.YELLOW;
                })
                .sorted(Comparator
                        .comparingInt((Assignment assignment) -> severityScore(assignment, now))
                        .reversed()
                        .thenComparing(Assignment::getDeadline))
                .collect(Collectors.toList());
    }

    private static int severityScore(Assignment assignment, LocalDateTime now) {
        if (AssignmentStatusCalculator.isUrgent(assignment, now)) {
            return 4;
        }

        AssignmentStatus status = AssignmentStatusCalculator.behindStatus(assignment, now);
        if (status == AssignmentStatus.RED) {
            return 3;
        }
        if (status == AssignmentStatus.ORANGE) {
            return 2;
        }
        if (status == AssignmentStatus.YELLOW) {
            return 1;
        }

        return 0;
    }
}