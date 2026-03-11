package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("AssignmentFilters")
class AssignmentFiltersTest {

    private Assignment makeAssignment(
            String name,
            String courseId,
            LocalDateTime start,
            LocalDateTime deadline,
            double estimatedHours,
            double remainingHours,
            boolean done
    ) {
        return new AssignmentBuilder()
                .setName(name)
                .setCourseId(courseId)
                .setStart(start)
                .setDeadline(deadline)
                .setLateDaysAllowed(0)
                .setEstimatedHours(estimatedHours)
                .setRemainingHours(remainingHours)
                .setDone(done)
                .build();
    }

    @Test
    @DisplayName("openAssignments excludes done items and preserves order")
    void openAssignments_excludesDoneAndPreservesOrder() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 10, 12, 0);

        Assignment openOne = makeAssignment(
                "Open 1",
                "CSE 110",
                now.minusDays(2),
                now.plusDays(5),
                4.0,
                4.0,
                false
        );
        Assignment done = makeAssignment(
                "Done",
                "CSE 110",
                now.minusDays(2),
                now.plusDays(5),
                4.0,
                0.0,
                true
        );
        Assignment openTwo = makeAssignment(
                "Open 2",
                "CSE 120",
                now.minusDays(2),
                now.plusDays(5),
                4.0,
                4.0,
                false
        );

        List<Assignment> filtered = AssignmentFilters.openAssignments(List.of(openOne, done, openTwo));

        assertEquals(List.of(openOne, openTwo), filtered);
    }

    @Test
    @DisplayName("filterByCourse returns only matching course rows")
    void filterByCourse_returnsOnlyMatchingCourseRows() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 10, 12, 0);

        Assignment cse110 = makeAssignment(
                "PA1",
                "CSE 110",
                now.minusDays(1),
                now.plusDays(2),
                5.0,
                5.0,
                false
        );
        Assignment cse120 = makeAssignment(
                "HW1",
                "CSE 120",
                now.minusDays(1),
                now.plusDays(2),
                5.0,
                5.0,
                false
        );

        List<Assignment> filtered = AssignmentFilters.filterByCourse(
                List.of(cse110, cse120),
                "CSE 110",
                "All Courses"
        );

        assertEquals(List.of(cse110), filtered);
    }

    @Test
    @DisplayName("dashboardAssignments keeps the previous severity ordering")
    void dashboardAssignments_keepsSeverityOrdering() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 10, 12, 0);

        Assignment urgent = makeAssignment(
                "Urgent",
                "CSE 110",
                now.minusDays(2),
                now.plusHours(10),
                5.0,
                5.0,
                false
        );
        Assignment red = makeAssignment(
                "Red",
                "CSE 110",
                now.minusDays(4),
                now.plusDays(1),
                10.0,
                10.0,
                false
        );
        Assignment yellow = makeAssignment(
                "Yellow",
                "CSE 110",
                now.minusDays(1),
                now.plusDays(3),
                4.0,
                4.0,
                false
        );
        Assignment none = makeAssignment(
                "None",
                "CSE 110",
                now,
                now.plusDays(10),
                4.0,
                4.0,
                false
        );
        Assignment done = makeAssignment(
                "Done",
                "CSE 110",
                now.minusDays(2),
                now.plusHours(5),
                4.0,
                0.0,
                true
        );

        List<Assignment> filtered = AssignmentFilters.dashboardAssignments(
                List.of(yellow, none, red, done, urgent),
                now
        );

        assertEquals(List.of(urgent, red, yellow), filtered);
    }
}