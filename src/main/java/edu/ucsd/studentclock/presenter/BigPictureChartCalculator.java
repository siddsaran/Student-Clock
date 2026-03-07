package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import javafx.scene.chart.XYChart;

public final class BigPictureChartCalculator {

    private final BigPictureTooltipPayloadFactory tooltipFactory;

    public BigPictureChartCalculator() {
        this(new BigPictureTooltipPayloadFactory());
    }

    public BigPictureChartCalculator(BigPictureTooltipPayloadFactory tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
    }

    public BigPictureChartModel build(
            List<Assignment> assignments,
            Map<Assignment, BigPictureEffectiveRanges.DateRange> effectiveRanges,
            LocalDate chartStart,
            LocalDate chartEnd,
            CumulativeHoursProvider cumulativeHoursProvider
    ) {
        XYChart.Series<String, Number> workloadSeries = new XYChart.Series<>();
        workloadSeries.setName("Workload");

        XYChart.Series<String, Number> burndownSeries = new XYChart.Series<>();
        burndownSeries.setName("IdealBurndown");

        double maxWork = buildWorkloadAndBurndown(
                assignments,
                effectiveRanges,
                chartStart,
                chartEnd,
                cumulativeHoursProvider,
                workloadSeries,
                burndownSeries
        );

        return new BigPictureChartModel(workloadSeries, burndownSeries, maxWork);
    }

    private double buildWorkloadAndBurndown(
            List<Assignment> assignments,
            Map<Assignment, BigPictureEffectiveRanges.DateRange> effectiveRanges,
            LocalDate chartStart,
            LocalDate chartEnd,
            CumulativeHoursProvider cumulativeHoursProvider,
            XYChart.Series<String, Number> workloadSeries,
            XYChart.Series<String, Number> burndownSeries
    ) {
        long totalDays = ChronoUnit.DAYS.between(chartStart, chartEnd);
        double runningWorkload = 0.0;
        double maxWorkload = 0.0;

        Map<String, Double> previousCumulative = Collections.emptyMap();

        for (int dayIndex = 0; dayIndex <= totalDays; dayIndex++) {
            LocalDate day = chartStart.plusDays(dayIndex);
            LocalDate previousDay = dayIndex > 0 ? chartStart.plusDays(dayIndex - 1) : null;

            Map<String, Double> cumulativeWork = cumulativeHoursProvider.getCumulativeHoursByEndOf(day);

            List<Assignment> endsToday = assignments.stream()
                    .filter(a -> effectiveRanges.get(a).end().equals(day))
                    .collect(Collectors.toList());

            List<Assignment> startsToday = assignments.stream()
                    .filter(a -> effectiveRanges.get(a).start().equals(day))
                    .collect(Collectors.toList());

            List<Assignment> activeOnDay = assignments.stream()
                    .filter(a -> isActiveOnDay(effectiveRanges.get(a), day))
                    .collect(Collectors.toList());

            List<Assignment> activeAtStart = assignments.stream()
                    .filter(a -> isActiveAtStart(effectiveRanges.get(a), day))
                    .collect(Collectors.toList());

            String label = formatLabel(day);

            boolean skipStartPoint = runningWorkload == 0.0 && !startsToday.isEmpty();
            if (!skipStartPoint) {
                workloadSeries.getData().add(createPoint(label, runningWorkload, activeOnDay));
            }

            if (previousDay != null) {
                boolean workLoggedToday = false;

                for (Assignment assignment : activeAtStart) {
                    double remainingPrevious = remainingHoursAt(assignment, previousCumulative);
                    double remainingToday = remainingHoursAt(assignment, cumulativeWork);

                    if (remainingToday != remainingPrevious) {
                        runningWorkload += (remainingToday - remainingPrevious);
                        workLoggedToday = true;
                    }
                }

                if (workLoggedToday) {
                    workloadSeries.getData().add(createPoint(label, runningWorkload, activeOnDay));
                }
            }

            previousCumulative = cumulativeWork;

            if (!endsToday.isEmpty()) {
                for (Assignment assignment : endsToday) {
                    runningWorkload -= remainingHoursAt(assignment, cumulativeWork);
                }

                List<Assignment> activeAfterEnds = activeOnDay.stream()
                        .filter(a -> !endsToday.contains(a))
                        .collect(Collectors.toList());

                workloadSeries.getData().add(createPoint(label, runningWorkload, activeAfterEnds));
            }

            if (!startsToday.isEmpty()) {
                for (Assignment assignment : startsToday) {
                    runningWorkload += remainingHoursAt(assignment, cumulativeWork);
                }
                workloadSeries.getData().add(createPoint(label, runningWorkload, activeOnDay));
            }

            maxWorkload = Math.max(maxWorkload, runningWorkload);
        }

        double firstHeight = workloadSeries.getData().isEmpty()
                ? 0.0
                : workloadSeries.getData().get(0).getYValue().doubleValue();
        double maxWork = Math.max(firstHeight, maxWorkload);

        if (!workloadSeries.getData().isEmpty()) {
            String first = workloadSeries.getData().get(0).getXValue();
            String last = workloadSeries.getData().get(workloadSeries.getData().size() - 1).getXValue();
            double finalWorkload = workloadSeries.getData()
                    .get(workloadSeries.getData().size() - 1)
                    .getYValue()
                    .doubleValue();

            burndownSeries.getData().add(new XYChart.Data<>(first, maxWork));
            burndownSeries.getData().add(new XYChart.Data<>(last, finalWorkload));
        }

        return maxWork;
    }

    private static boolean isActiveOnDay(BigPictureEffectiveRanges.DateRange r, LocalDate day) {
        return !day.isBefore(r.start()) && !day.isAfter(r.end());
    }

    private static boolean isActiveAtStart(BigPictureEffectiveRanges.DateRange r, LocalDate day) {
        return day.compareTo(r.start()) > 0 && !day.isAfter(r.end());
    }

    private static String formatLabel(LocalDate day) {
        return day.getMonthValue() + "/" + day.getDayOfMonth();
    }

    private XYChart.Data<String, Number> createPoint(String label, double y, List<Assignment> activeAssignments) {
        BigPictureTooltipPayload payload = tooltipFactory.fromAssignments(activeAssignments);
        XYChart.Data<String, Number> point = new XYChart.Data<>(label, y);
        point.setExtraValue(payload);
        return point;
    }

    private static double remainingHoursAt(Assignment assignment, Map<String, Double> cumulativeByEndOfDay) {
        Double loggedHours = cumulativeByEndOfDay.get(assignment.getId());
        if (loggedHours != null) {
            return Math.max(0.0, assignment.getEstimatedHours() - loggedHours);
        }
        return assignment.getRemainingHours();
    }
}