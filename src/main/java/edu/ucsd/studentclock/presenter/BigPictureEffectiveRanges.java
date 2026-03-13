package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucsd.studentclock.model.Assignment;

/**
 * Pure logic for computing effective (start, end) dates for Big Picture chart.
 *
 * Chart behavior: line changes only on (1) assignment start → vertical jump,
 * (2) work logged → diagonal down, (3) assignment marked finished → vertical
 * drop.
 * Reaching the due date does NOT change the line unless late is not allowed.
 *
 * Series: next assignment starts day after previous deadline; previous can
 * overlap
 * (late period), so remaining hours carry over and stack with new assignment.
 */
final class BigPictureEffectiveRanges {

    private BigPictureEffectiveRanges() {
    }

    static final class DateRange {
        private final LocalDate start;
        private final LocalDate end;

        DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        LocalDate start() {
            return start;
        }

        LocalDate end() {
            return end;
        }
    }

    /**
     * Computes effective (start, end) date for each assignment for chart display.
     * Effective end = deadline + lateDaysAllowed. For series assignments, effective
     * start
     * is the previous assignment's effective end so they start automatically after
     * it (order by deadline).
     *
     * @param assignments active assignments (non-done)
     * @return map of assignment to effective date range (start, end)
     */
    static Map<Assignment, LocalDate[]> computeEffectiveRanges(List<Assignment> assignments) {
        Map<Assignment, LocalDate[]> out = new HashMap<>();
        LocalDate chartEnd = assignments.stream()
                .map(a -> a.getDeadline().toLocalDate().plusDays(a.getLateDaysAllowed()))
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        Map<String, List<Assignment>> bySeries = new HashMap<>();
        for (Assignment a : assignments) {
            String key = a.getCourseId() + "|" + (a.getSeriesId() != null ? a.getSeriesId() : "");
            bySeries.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
        }

        for (Map.Entry<String, List<Assignment>> e : bySeries.entrySet()) {
            List<Assignment> list = e.getValue();
            int pipe = e.getKey().indexOf('|');
            boolean noSeries = pipe >= 0 && e.getKey().substring(pipe + 1).isEmpty();
            if (noSeries) {
                for (Assignment a : list) {
                    LocalDate effStart = a.getStart().toLocalDate();
                    LocalDate baseEnd = a.getDeadline().toLocalDate().plusDays(a.getLateDaysAllowed());
                    LocalDate effEnd = a.getLateDaysAllowed() > 0 ? chartEnd : baseEnd;
                    out.put(a, new LocalDate[] { effStart, effEnd });
                }
            } else {
                list.sort(Comparator.comparing(Assignment::getDeadline));
                LocalDate prevDeadline = null;
                for (Assignment a : list) {
                    LocalDate storedStart = a.getStart().toLocalDate();
                    LocalDate baseEnd = a.getDeadline().toLocalDate().plusDays(a.getLateDaysAllowed());
                    LocalDate effEnd = a.getLateDaysAllowed() > 0 ? chartEnd : baseEnd;
                    LocalDate effStart;
                    if (prevDeadline == null) {
                        effStart = storedStart;
                    } else {
                        LocalDate nextStartsAfterPrevDue = prevDeadline.plusDays(1);
                        effStart = storedStart.isBefore(nextStartsAfterPrevDue)
                                ? nextStartsAfterPrevDue
                                : storedStart;
                    }
                    out.put(a, new LocalDate[] { effStart, effEnd });
                    prevDeadline = a.getDeadline().toLocalDate();
                }
            }
        }

        return out;
    }
}