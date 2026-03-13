package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 3: Presenter tests for core user actions (BigPictureChartCalculator).
 * MS1: US10 (View a Big Picture workload visualization).
 */
@DisplayName("DS7-3: BigPictureChartCalculator")
class BigPictureChartCalculatorTest {

        private Assignment makeAssignment(
                        String name,
                        String courseId,
                        LocalDate start,
                        LocalDate deadline,
                        int lateDays,
                        double estimatedHours) {
                return new AssignmentBuilder()
                                .setName(name)
                                .setCourseId(courseId)
                                .setStart(start.atStartOfDay())
                                .setDeadline(deadline.atTime(23, 59))
                                .setLateDaysAllowed(lateDays)
                                .setEstimatedHours(estimatedHours)
                                .build();
        }

        @Test
        @DisplayName("build creates workload and burndown series for a single assignment")
        void build_createsTwoSeries_andBurndownHasTwoPoints() {
                Assignment a = makeAssignment(
                                "A",
                                "CSE 110",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                0,
                                10.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                LocalDate chartStart = ranges.get(a)[0];
                LocalDate chartEnd = ranges.get(a)[1];

                CumulativeHoursProvider provider = day -> new HashMap<>();

                BigPictureChartCalculator calc = new BigPictureChartCalculator();
                BigPictureChartModel model = calc.build(List.of(a), ranges, chartStart, chartEnd, provider);

                assertNotNull(model.getWorkloadSeries());
                assertNotNull(model.getBurndownSeries());

                assertEquals("Workload", model.getWorkloadSeries().getName());
                assertEquals("IdealBurndown", model.getBurndownSeries().getName());

                assertEquals(2, model.getBurndownSeries().getData().size());
                assertFalse(model.getWorkloadSeries().getData().isEmpty());
        }

        @Test
        @DisplayName("build handles empty assignments without failing")
        void build_emptyAssignments_returnsValidModel() {
                BigPictureChartCalculator calc = new BigPictureChartCalculator();

                BigPictureChartModel model = calc.build(
                                List.of(),
                                Map.of(),
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                day -> Map.of());

                assertNotNull(model);
                assertNotNull(model.getWorkloadSeries());
                assertNotNull(model.getBurndownSeries());
                assertEquals(0.0, model.getMaxWork(), 1e-9);
        }

        @Test
        @DisplayName("build sets maxWork to a meaningful positive value for non-empty workload")
        void build_maxWorkMatchesPeakWorkload() {
                Assignment a = makeAssignment(
                                "A",
                                "CSE 110",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 2),
                                0,
                                5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                BigPictureChartModel model = new BigPictureChartCalculator().build(
                                List.of(a),
                                ranges,
                                ranges.get(a)[0],
                                ranges.get(a)[1],
                                day -> new HashMap<>());

                assertEquals(5.0, model.getMaxWork(), 1e-9);
        }

        @Test
        @DisplayName("build reflects logged work in workload series")
        void build_loggedWorkReducesWorkloadPoints() {
                Assignment a = makeAssignment(
                                "A",
                                "CSE 110",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                0,
                                10.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                LocalDate workDay = LocalDate.of(2026, 2, 2);
                CumulativeHoursProvider provider = day -> !day.isBefore(workDay) ? Map.of(a.getId(), 4.0) : Map.of();

                BigPictureChartModel model = new BigPictureChartCalculator().build(
                                List.of(a),
                                ranges,
                                ranges.get(a)[0],
                                ranges.get(a)[1],
                                provider);

                boolean foundReducedPoint = model.getWorkloadSeries().getData().stream()
                                .anyMatch(p -> Math.abs(p.getYValue().doubleValue() - 6.0) < 1e-9);

                assertTrue(foundReducedPoint);
        }

        @Test
        @DisplayName("build creates burndown endpoints from starting work to zero")
        void build_burndownStartsAtTotalAndEndsAtZero() {
                Assignment a = makeAssignment(
                                "A",
                                "CSE 110",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                0,
                                8.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                BigPictureChartModel model = new BigPictureChartCalculator().build(
                                List.of(a),
                                ranges,
                                ranges.get(a)[0],
                                ranges.get(a)[1],
                                day -> Map.of());

                assertEquals(2, model.getBurndownSeries().getData().size());

                double firstY = model.getBurndownSeries().getData().get(0).getYValue().doubleValue();
                double secondY = model.getBurndownSeries().getData().get(1).getYValue().doubleValue();

                assertEquals(8.0, firstY, 1e-9);
                assertEquals(0.0, secondY, 1e-9);
        }

        @Test
        @DisplayName("workload points store tooltip payload in extraValue")
        void workloadPoint_storesTooltipPayloadInExtraValue() {
                Assignment a = makeAssignment(
                                "A",
                                "CSE 110",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 2),
                                0,
                                5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                BigPictureChartModel model = new BigPictureChartCalculator().build(
                                List.of(a),
                                ranges,
                                ranges.get(a)[0],
                                ranges.get(a)[1],
                                day -> new HashMap<>());

                assertFalse(model.getWorkloadSeries().getData().isEmpty());

                Object extra = model.getWorkloadSeries().getData().get(0).getExtraValue();
                assertTrue(extra instanceof BigPictureTooltipPayload);
        }

        @Test
        @DisplayName("tooltip payload contains assignment data for workload point")
        void workloadPoint_tooltipPayloadContainsAssignmentItem() {
                Assignment a = makeAssignment(
                                "PA1",
                                "CSE 110",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 2),
                                0,
                                5.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

                BigPictureChartModel model = new BigPictureChartCalculator().build(
                                List.of(a),
                                ranges,
                                ranges.get(a)[0],
                                ranges.get(a)[1],
                                day -> Map.of());

                Object extra = model.getWorkloadSeries().getData().stream()
                                .map(point -> point.getExtraValue())
                                .filter(BigPictureTooltipPayload.class::isInstance)
                                .findFirst()
                                .orElse(null);

                assertNotNull(extra);

                BigPictureTooltipPayload payload = (BigPictureTooltipPayload) extra;
                assertFalse(payload.getItems().isEmpty());
                assertEquals("PA1", payload.getItems().get(0).getName());
                assertEquals("CSE 110", payload.getItems().get(0).getCourseId());
        }

        @Test
        @DisplayName("memoized provider calls delegate once for the same day")
        void memoizedProvider_callsDelegateOncePerDay() {
                final int[] calls = { 0 };

                CumulativeHoursProvider delegate = day -> {
                        calls[0]++;
                        return new HashMap<>();
                };

                MemoizedCumulativeHoursProvider memo = new MemoizedCumulativeHoursProvider(delegate);

                LocalDate d = LocalDate.of(2026, 2, 1);
                memo.getCumulativeHoursByEndOf(d);
                memo.getCumulativeHoursByEndOf(d);
                memo.getCumulativeHoursByEndOf(d);

                assertEquals(1, calls[0]);
        }

        @Test
        @DisplayName("memoized provider calls delegate again for a different day")
        void memoizedProvider_callsDelegateForDifferentDays() {
                final int[] calls = { 0 };

                CumulativeHoursProvider delegate = day -> {
                        calls[0]++;
                        return new HashMap<>();
                };

                MemoizedCumulativeHoursProvider memo = new MemoizedCumulativeHoursProvider(delegate);

                memo.getCumulativeHoursByEndOf(LocalDate.of(2026, 2, 1));
                memo.getCumulativeHoursByEndOf(LocalDate.of(2026, 2, 2));
                memo.getCumulativeHoursByEndOf(LocalDate.of(2026, 2, 1));

                assertEquals(2, calls[0]);
        }

        @Test
        @DisplayName("build combines multiple assignments into one workload series")
        void build_multipleAssignmentsProducesHigherPeakWork() {
                Assignment a1 = makeAssignment(
                                "A1",
                                "CSE 110",
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 3),
                                0,
                                4.0);

                Assignment a2 = makeAssignment(
                                "A2",
                                "CSE 110",
                                LocalDate.of(2026, 2, 2),
                                LocalDate.of(2026, 2, 4),
                                0,
                                3.0);

                Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a1, a2));

                BigPictureChartModel model = new BigPictureChartCalculator().build(
                                List.of(a1, a2),
                                ranges,
                                LocalDate.of(2026, 2, 1),
                                LocalDate.of(2026, 2, 4),
                                day -> Map.of());

                assertTrue(model.getMaxWork() >= 7.0 - 1e-9);
        }
}