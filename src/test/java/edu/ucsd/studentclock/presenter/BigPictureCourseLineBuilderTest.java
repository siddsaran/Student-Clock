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
 * Story DS7, Task 3: Presenter tests for core user actions (BigPictureCourseLineBuilder).
 * MS1: US10 (View a Big Picture workload visualization).
 */
@DisplayName("DS7-3: BigPictureCourseLineBuilder")
class BigPictureCourseLineBuilderTest {

        private Assignment makeAssignment(
                        String name,
                        String courseId,
                        String seriesId,
                        LocalDate start,
                        LocalDate deadline,
                        int lateDays,
                        double estimated) {
                return new AssignmentBuilder()
                                .setName(name)
                                .setCourseId(courseId)
                                .setSeriesId(seriesId)
                                .setStart(start.atStartOfDay())
                                .setDeadline(deadline.atTime(23, 59))
                                .setLateDaysAllowed(lateDays)
                                .setEstimatedHours(estimated)
                                .build();
        }

        private Assignment makeAssignment(
                        String name,
                        LocalDate start,
                        LocalDate deadline,
                        double estimated) {
                return makeAssignment(name, "CSE 110", null, start, deadline, 0, estimated);
        }

        private static Function<LocalDate, Map<String, Double>> noLoggedWork() {
                return day -> Map.of();
        }

        private static BigPictureCourseLineBuilder.ChartPoint findFirstPoint(
                        List<BigPictureCourseLineBuilder.ChartPoint> points,
                        String label) {
                return points.stream()
                                .filter(p -> p.label.equals(label))
                                .findFirst()
                                .orElse(null);
        }

        private static List<BigPictureCourseLineBuilder.ChartPoint> findPoints(
                        List<BigPictureCourseLineBuilder.ChartPoint> points, String label) {
                return points.stream()
                                .filter(p -> p.label.equals(label))
                                .collect(java.util.stream.Collectors.toList());
        }

        @Test
        @DisplayName("build returns empty list when no effective ranges exist")
        void build_noRanges_returnsEmpty() {
                LocalDate chartStart = LocalDate.of(2026, 2, 1);
                LocalDate chartEnd = LocalDate.of(2026, 2, 5);

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(),
                                Map.of(),
                                noLoggedWork(),
                                chartStart,
                                chartEnd);

                assertTrue(points.isEmpty());
        }

        @Test
        @DisplayName("build returns empty list when chart start is after chart end")
        void build_invalidChartWindow_returnsEmpty() {
                Assignment a = makeAssignment(
                                "PA1",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                noLoggedWork(),
                                LocalDate.of(2026, 2, 5),
                                LocalDate.of(2026, 2, 1));

                assertTrue(points.isEmpty());
        }

        @Test
        @DisplayName("build does not create points before the first effective day")
        void build_skipsDaysBeforeFirstEffectiveDay() {
                Assignment a = makeAssignment(
                                "PA1",
                                LocalDate.of(2026, 2, 3),
                                LocalDate.of(2026, 2, 5),
                                5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                noLoggedWork(),
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 5));

                assertNull(findFirstPoint(points, "02/01"));
                assertNull(findFirstPoint(points, "02/02"));
                assertNotNull(findFirstPoint(points, "02/03"));
        }

        @Test
        @DisplayName("build stops creating points after the last effective day")
        void build_stopsAfterLastEffectiveDay() {
                Assignment a = makeAssignment(
                                "PA1",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                noLoggedWork(),
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 6));

                assertNotNull(findFirstPoint(points, "02/03"));
                assertNull(findFirstPoint(points, "02/04"));
                assertNull(findFirstPoint(points, "02/05"));
                assertNull(findFirstPoint(points, "02/06"));
        }

        @Test
        @DisplayName("build creates a vertical jump on the assignment start day")
        void build_startDayCreatesVerticalJump() {
                LocalDate start = LocalDate.of(2026, 2, 1);
                LocalDate end = LocalDate.of(2026, 2, 3);

                Assignment a = makeAssignment("PA1", start, end, 5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                noLoggedWork(),
                                start,
                                end);

                List<BigPictureCourseLineBuilder.ChartPoint> startDayPoints = findPoints(points, "02/01");
                assertFalse(startDayPoints.isEmpty());

                assertTrue(startDayPoints.stream().anyMatch(p -> Math.abs(p.y - 0.0) < 1e-9));
                assertTrue(startDayPoints.stream().anyMatch(p -> Math.abs(p.y - 5.0) < 1e-9));
        }

        @Test
        @DisplayName("build creates a downward change on a work-logged day with no starts or ends")
        void build_workLoggedOnMiddleDayReducesRemaining() {
                LocalDate start = LocalDate.of(2026, 2, 1);
                LocalDate end = LocalDate.of(2026, 2, 5);

                Assignment a = makeAssignment("PA1", start, end, 10.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                LocalDate workDay = LocalDate.of(2026, 2, 2);
                Function<LocalDate, Map<String, Double>> logged = day -> !day.isBefore(workDay) ? Map.of(a.getId(), 3.0)
                                : Map.of();

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                logged,
                                start,
                                end);

                BigPictureCourseLineBuilder.ChartPoint day3 = findFirstPoint(points, "02/03");
                assertNotNull(day3);
                assertEquals(7.0, day3.y, 1e-9);
        }

        @Test
        @DisplayName("build creates a vertical drop on the assignment end day")
        void build_endDayCreatesVerticalDrop() {
                LocalDate start = LocalDate.of(2026, 2, 1);
                LocalDate end = LocalDate.of(2026, 2, 3);

                Assignment a = makeAssignment("PA1", start, end, 5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                noLoggedWork(),
                                start,
                                end);

                List<BigPictureCourseLineBuilder.ChartPoint> endDayPoints = findPoints(points, "02/03");
                assertFalse(endDayPoints.isEmpty());

                assertTrue(endDayPoints.stream().anyMatch(p -> Math.abs(p.y - 5.0) < 1e-9));
                assertTrue(endDayPoints.stream().anyMatch(p -> Math.abs(p.y - 0.0) < 1e-9));
        }

        @Test
        @DisplayName("build stacks multiple active assignments on the same day")
        void build_multipleActiveAssignments_stackTotals() {
                LocalDate chartStart = LocalDate.of(2026, 2, 1);
                LocalDate chartEnd = LocalDate.of(2026, 2, 5);

                Assignment a1 = makeAssignment("PA1", chartStart, LocalDate.of(2026, 2, 5), 5.0);
                Assignment a2 = makeAssignment("PA2", LocalDate.of(2026, 2, 2), LocalDate.of(2026, 2, 4), 3.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a1, a2));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a1, a2),
                                ranges,
                                noLoggedWork(),
                                chartStart,
                                chartEnd);

                List<BigPictureCourseLineBuilder.ChartPoint> day2Points = findPoints(points, "02/02");
                assertTrue(day2Points.stream().anyMatch(p -> Math.abs(p.y - 8.0) < 1e-9));
        }

        @Test
        @DisplayName("build clamps remaining hours at zero when cumulative work exceeds estimate")
        void build_overLoggedWork_clampsToZero() {
                LocalDate start = LocalDate.of(2026, 2, 1);
                LocalDate end = LocalDate.of(2026, 2, 5);

                Assignment a = makeAssignment("PA1", start, end, 4.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                Function<LocalDate, Map<String, Double>> logged = day -> Map.of(a.getId(), 10.0);

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                logged,
                                start,
                                end);

                assertTrue(points.stream().allMatch(p -> p.y >= -1e-9));
                assertTrue(points.stream().anyMatch(p -> Math.abs(p.y - 0.0) < 1e-9));
        }

        @Test
        @DisplayName("build includes next assignment workload on same-day series transition")
        void build_sameDayEndAndStart_includesNextAssignmentWorkload() {
                Assignment first = makeAssignment(
                                "A",
                                "CSE 110",
                                "series-1",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                0,
                                5.0);

                Assignment second = makeAssignment(
                                "B",
                                "CSE 110",
                                "series-1",
                                LocalDate.of(2026, 2, 2),
                                LocalDate.of(2026, 2, 5),
                                0,
                                4.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges
                                .computeEffectiveRanges(List.of(first, second));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(first, second),
                                ranges,
                                noLoggedWork(),
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 5));

                List<BigPictureCourseLineBuilder.ChartPoint> day4Points = findPoints(points, "02/04");
                assertFalse(day4Points.isEmpty());
                assertTrue(day4Points.stream().anyMatch(p -> Math.abs(p.y - 4.0) < 1e-9));
        }

        @Test
        @DisplayName("build includes active assignments in chart point metadata")
        void build_chartPointContainsActiveAssignments() {
                LocalDate start = LocalDate.of(2026, 2, 1);
                LocalDate end = LocalDate.of(2026, 2, 3);

                Assignment a = makeAssignment("PA1", start, end, 5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                List<BigPictureCourseLineBuilder.ChartPoint> points = BigPictureCourseLineBuilder.build(
                                List.of(a),
                                ranges,
                                noLoggedWork(),
                                start,
                                end);

                BigPictureCourseLineBuilder.ChartPoint pointWithAssignment = points.stream()
                                .filter(p -> !p.activeAssignments.isEmpty())
                                .findFirst()
                                .orElse(null);

                assertNotNull(pointWithAssignment);
                assertEquals(1, pointWithAssignment.activeAssignments.size());
                assertEquals(a, pointWithAssignment.activeAssignments.get(0));
        }
}