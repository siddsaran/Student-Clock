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
 * (2) work logged → diagonal down, (3) assignment marked finished → vertical drop.
 * Reaching the due date does NOT change the line unless late is not allowed.
 *
 * Series: next assignment starts day after previous deadline; previous can overlap
 * (late period), so remaining hours carry over and stack with new assignment.
 */
final class BigPictureEffectiveRanges {

    private BigPictureEffectiveRanges() {}

    /**
     * Effective start/end for chart. End = deadline + lateDays; when lateAllowed > 0,
     * we extend end to a far date so we never auto-end (only "marked finished" ends it).
     * For series: next starts day after previous deadline.
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
                    out.put(a, new LocalDate[]{effStart, effEnd});
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
                    out.put(a, new LocalDate[]{effStart, effEnd});
                    prevDeadline = a.getDeadline().toLocalDate();
                }
            }
        }

        return out;
    }
}