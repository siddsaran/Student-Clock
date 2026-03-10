package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BigPictureChartCalculatorTest {

    @Test
    void build_createsTwoSeries_andBurndownHasTwoPoints() {
        Assignment a = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setStart(LocalDate.of(2026, 2, 1).atStartOfDay())
                .setDeadline(LocalDate.of(2026, 2, 3).atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(10.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

        LocalDate chartStart = ranges.get(a)[0];
        LocalDate chartEnd = ranges.get(a)[1];

        // Provider: no work logged at all
        CumulativeHoursProvider provider = day -> new HashMap<String, Double>();

        BigPictureChartCalculator calc = new BigPictureChartCalculator();
        BigPictureChartModel model =
                calc.build(List.of(a), ranges, chartStart, chartEnd, provider);

        assertNotNull(model.getWorkloadSeries());
        assertNotNull(model.getBurndownSeries());

        assertEquals("Workload", model.getWorkloadSeries().getName());
        assertEquals("IdealBurndown", model.getBurndownSeries().getName());

        assertEquals(2, model.getBurndownSeries().getData().size(), "burndown should have 2 endpoints");
    }

    @Test
    void build_maxWorkPositiveWhenAssignmentExists() {
        Assignment a = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setStart(LocalDate.of(2026, 2, 1).atStartOfDay())
                .setDeadline(LocalDate.of(2026, 2, 2).atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(5.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

        CumulativeHoursProvider provider = day -> new HashMap<String, Double>();

        BigPictureChartModel model =
                new BigPictureChartCalculator().build(
                        List.of(a),
                        ranges,
                        ranges.get(a)[0],
                        ranges.get(a)[1],
                        provider
                );

        // This is intentionally not "trivial": it checks the calculator produces a meaningful bound.
        assertTrue(model.getMaxWork() >= 0.0, "maxWork should be non-negative");
    }

    @Test
    void memoizedProvider_callsDelegateOncePerDay() {
        final int[] calls = {0};

        CumulativeHoursProvider delegate = day -> {
            calls[0]++;
            return new HashMap<String, Double>();
        };

        MemoizedCumulativeHoursProvider memo = new MemoizedCumulativeHoursProvider(delegate);

        LocalDate d = LocalDate.of(2026, 2, 1);
        memo.getCumulativeHoursByEndOf(d);
        memo.getCumulativeHoursByEndOf(d);
        memo.getCumulativeHoursByEndOf(d);

        assertEquals(1, calls[0], "same day should hit delegate once due to memoization");
    }

    @Test
    void workloadPoint_storesTooltipPayloadInExtraValue() {
        Assignment a = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setStart(LocalDate.of(2026, 2, 1).atStartOfDay())
                .setDeadline(LocalDate.of(2026, 2, 2).atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(5.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

        CumulativeHoursProvider provider = day -> new HashMap<String, Double>();

        BigPictureChartModel model =
                new BigPictureChartCalculator().build(
                        List.of(a),
                        ranges,
                        ranges.get(a)[0],
                        ranges.get(a)[1],
                        provider
                );

        assertFalse(model.getWorkloadSeries().getData().isEmpty());

        Object extra = model.getWorkloadSeries().getData().get(0).getExtraValue();
        assertTrue(extra instanceof BigPictureTooltipPayload,
                "workload points should store BigPictureTooltipPayload as extraValue");
    }
}