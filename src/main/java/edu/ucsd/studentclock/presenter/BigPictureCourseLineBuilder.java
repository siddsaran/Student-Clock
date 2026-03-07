package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import edu.ucsd.studentclock.model.Assignment;

/**
 * Builds chart data points for a single course line in the Big Picture chart.
 *
 * Line changes only on: (1) assignment start → vertical jump up,
 * (2) work logged → diagonal down, (3) assignment marked finished → vertical drop to zero.
 * Series: next starts day after previous deadline; previous can overlap (late period),
 * so remaining hours carry over and stack when new assignment is added.
 */
final class BigPictureCourseLineBuilder {

    static final class ChartPoint {
        final String label;
        final double y;
        final List<Assignment> activeAssignments;

        ChartPoint(String label, double y, List<Assignment> activeAssignments) {
            this.label = label;
            this.y = y;
            this.activeAssignments = activeAssignments != null ? activeAssignments : List.of();
        }
    }

    private BigPictureCourseLineBuilder() {}

    /**
     * Builds event-driven points for one course. Processes by day: start-of-day total,
     * then starts (vertical jump up), then ends (vertical drop to zero), then
     * end-of-day total. When both end and start same day, collapses to avoid drop-to-zero.
     */
    static List<ChartPoint> build(
            List<Assignment> courseAssignments,
            Map<Assignment, LocalDate[]> effectiveRanges,
            Function<LocalDate, Map<String, Double>> cumulativeByEndOfDay,
            LocalDate chartStart,
            LocalDate chartEnd
    ) {
        List<ChartPoint> points = new ArrayList<>();

        LocalDate firstDay = null;
        LocalDate lastDay = null;
        for (Assignment a : courseAssignments) {
            LocalDate[] r = effectiveRanges.get(a);
            if (r == null) continue;
            if (firstDay == null || r[0].isBefore(firstDay)) firstDay = r[0];
            if (lastDay == null || r[1].isAfter(lastDay)) lastDay = r[1];
        }
        if (firstDay == null || lastDay == null) return points;

        long totalDays = ChronoUnit.DAYS.between(chartStart, chartEnd);
        if (totalDays < 0) return points;

        double courseTotal = 0.0;
        Map<String, Double> prevCumulative = Map.of();

        for (long d = 0; d <= totalDays; d++) {
            LocalDate day = chartStart.plusDays(d);
            if (day.isBefore(firstDay)) continue;
            if (day.isAfter(lastDay)) break;
            String label = String.format("%02d/%02d", day.getMonthValue(), day.getDayOfMonth());
            Map<String, Double> cumulative = cumulativeByEndOfDay.apply(day);

            List<Assignment> startsToday = new ArrayList<>();
            List<Assignment> endsToday = new ArrayList<>();
            List<Assignment> activeOnDay = new ArrayList<>();

            for (Assignment a : courseAssignments) {
                LocalDate[] r = effectiveRanges.get(a);
                if (r[0].equals(day)) startsToday.add(a);
                if (r[1].equals(day)) endsToday.add(a);
                if (!day.isBefore(r[0]) && !day.isAfter(r[1])) activeOnDay.add(a);
            }

            double startOfDayRemaining = 0.0;
            for (Assignment a : courseAssignments) {
                LocalDate[] r = effectiveRanges.get(a);
                if (r[0].isBefore(day) && !r[1].isBefore(day)) {
                    startOfDayRemaining += remainingAt(a, prevCumulative);
                }
            }

            courseTotal = startOfDayRemaining;

            double endOfDayRemaining = 0.0;
            for (Assignment a : courseAssignments) {
                LocalDate[] r = effectiveRanges.get(a);
                if (!day.isBefore(r[0]) && !day.isAfter(r[1])) {
                    endOfDayRemaining += remainingAt(a, cumulative);
                }
            }
            boolean workLoggedToday = endsToday.isEmpty() && startsToday.isEmpty()
                    && Math.abs(endOfDayRemaining - startOfDayRemaining) > 1e-9;

            if (!workLoggedToday) {
                addPoint(points, label, courseTotal, activeOnDay);
            }

            if (!endsToday.isEmpty() && !startsToday.isEmpty()) {
                for (Assignment a : endsToday) courseTotal -= remainingAt(a, prevCumulative);
                for (Assignment a : startsToday) courseTotal += a.getEstimatedHours();
                addPoint(points, label, courseTotal, activeOnDay);
            } else {
                for (Assignment a : endsToday) {
                    double rem = remainingAt(a, prevCumulative);
                    addPoint(points, label, courseTotal, activeOnDay);
                    courseTotal -= rem;
                    addPoint(points, label, courseTotal, activeOnDay);
                }
                for (Assignment a : startsToday) {
                    addPoint(points, label, courseTotal, activeOnDay);
                    courseTotal += a.getEstimatedHours();
                    addPoint(points, label, courseTotal, activeOnDay);
                }
            }

            if (!endsToday.isEmpty() || !startsToday.isEmpty()) {
                addPoint(points, label, courseTotal, activeOnDay);
            } else if (workLoggedToday) {
                addPoint(points, label, endOfDayRemaining, activeOnDay);
            }
            prevCumulative = cumulative;
        }

        return points;
    }

    private static double remainingAt(Assignment a, Map<String, Double> cumulative) {
        Double logged = cumulative.get(a.getId());
        if (logged != null) {
            return Math.max(0.0, a.getEstimatedHours() - logged);
        }
        return a.getRemainingHours();
    }

    private static void addPoint(List<ChartPoint> points, String label, double y,
                                 List<Assignment> active) {
        points.add(new ChartPoint(label, y, new ArrayList<>(active)));
    }
}
