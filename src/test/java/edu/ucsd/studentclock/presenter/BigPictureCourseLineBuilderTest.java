package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Big Picture: verifies one series per course and slope after work logged.
 */
@DisplayName("BigPictureCourseLineBuilder")
class BigPictureCourseLineBuilderTest {

    @Test
    void build_oneAssignmentNoWork_returnsPointsWithEstimatedHoursOnStartDay() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 3);
        Assignment a = new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setStart(start.atStartOfDay())
                .setDeadline(end.atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(5.0)
                .build();

        Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));
        Function<LocalDate, Map<String, Double>> noWorkLogged = day -> Map.of();

        List<BigPictureCourseLineBuilder.ChartPoint> points =
                BigPictureCourseLineBuilder.build(List.of(a), ranges, noWorkLogged, start, end);

        assertFalse(points.isEmpty());
        BigPictureCourseLineBuilder.ChartPoint firstWithWorkload = points.stream()
                .filter(p -> p.y > 0)
                .findFirst()
                .orElse(null);
        assertNotNull(firstWithWorkload);
        assertEquals(5.0, firstWithWorkload.y, 1e-9);
    }

    @Test
    void build_workLoggedReducesSlope() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 5);
        Assignment a = new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setStart(start.atStartOfDay())
                .setDeadline(end.atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(10.0)
                .build();
        a.applyWork(3.0);

        Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));
        LocalDate day2 = LocalDate.of(2026, 2, 2);
        Function<LocalDate, Map<String, Double>> workLoggedByDay2 = day ->
                !day.isBefore(day2) ? Map.of(a.getId(), 3.0) : Map.of();

        List<BigPictureCourseLineBuilder.ChartPoint> points =
                BigPictureCourseLineBuilder.build(List.of(a), ranges, workLoggedByDay2, start, end);

        BigPictureCourseLineBuilder.ChartPoint onDay3 = points.stream()
                .filter(p -> p.label.equals("02/03"))
                .findFirst()
                .orElse(null);
        assertNotNull(onDay3);
        assertEquals(7.0, onDay3.y, 1e-9, "remaining = 10 - 3 logged");
    }
}
