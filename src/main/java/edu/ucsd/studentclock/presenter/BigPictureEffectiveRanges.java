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
 * No JavaFX or I/O so it can be unit tested without a toolkit.
 */
final class BigPictureEffectiveRanges {

    private BigPictureEffectiveRanges() {}

    static final class DateRange {
        private final LocalDate start;
        private final LocalDate end;

        DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        LocalDate start() { return start; }
        LocalDate end() { return end; }
    }

    /**
     * Computes effective (start, end) date for each assignment for chart display.
     * Effective end = deadline + lateDaysAllowed. For series assignments, effective start
     * is the previous assignment's effective end so they start automatically after it (order by deadline).
     *
     * @param assignments active assignments (non-done)
     * @return map of assignment to effective date range (start, end)
     */
    static Map<Assignment, DateRange> computeEffectiveRanges(List<Assignment> assignments) {
        Map<Assignment, DateRange> out = new HashMap<>();

        Map<String, List<Assignment>> bySeries = new HashMap<>();
        for (Assignment a : assignments) {
            String key = a.getSeriesId() != null ? a.getSeriesId() : null;
            bySeries.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
        }

        for (Map.Entry<String, List<Assignment>> e : bySeries.entrySet()) {
            List<Assignment> list = e.getValue();
            if (e.getKey() == null) {
                for (Assignment a : list) {
                    LocalDate effStart = a.getStart().toLocalDate();
                    LocalDate effEnd = a.getDeadline().toLocalDate().plusDays(a.getLateDaysAllowed());
                    out.put(a, new DateRange(effStart, effEnd));
                }
            } else {
                list.sort(Comparator.comparing(Assignment::getDeadline));
                LocalDate prevEffectiveEnd = null;
                for (Assignment a : list) {
                    LocalDate storedStart = a.getStart().toLocalDate();
                    LocalDate effEnd = a.getDeadline().toLocalDate().plusDays(a.getLateDaysAllowed());
                    LocalDate effStart = prevEffectiveEnd == null
                            ? storedStart
                            : storedStart.isBefore(prevEffectiveEnd) ? prevEffectiveEnd : storedStart;
                    out.put(a, new DateRange(effStart, effEnd));
                    prevEffectiveEnd = effEnd;
                }
            }
        }

        return out;
    }
}