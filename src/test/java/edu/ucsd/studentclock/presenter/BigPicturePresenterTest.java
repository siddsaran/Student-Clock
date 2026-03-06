package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import edu.ucsd.studentclock.model.Assignment;

class BigPicturePresenterTest {

    @Test
    void effectiveEndExtendsByLateDays() {
        LocalDate d = LocalDate.of(2026, 2, 10);
        LocalDateTime start = d.minusDays(2).atStartOfDay();
        LocalDateTime deadline = d.atTime(23, 59);
        Assignment a = new Assignment("A", "CSE 110", start, deadline, 2, 5.0);

        Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

        LocalDate[] range = ranges.get(a);
        assertEquals(d.minusDays(2), range[0], "effective start is stored start");
        assertEquals(d.plusDays(2), range[1], "effective end is deadline + lateDaysAllowed (D+2)");
    }

    @Test
    void seriesSecondAssignmentStartsAfterFirstEffectiveEnd() {
        LocalDate e1 = LocalDate.of(2026, 2, 15);
        LocalDate s2 = LocalDate.of(2026, 2, 10);
        LocalDate e2 = LocalDate.of(2026, 2, 20);
        Assignment a = new Assignment("A", "CSE 110", "series-1",
                e1.minusDays(5).atStartOfDay(), e1.atTime(23, 59), 0, 3.0);
        Assignment b = new Assignment("B", "CSE 110", "series-1",
                s2.atStartOfDay(), e2.atTime(23, 59), 1, 4.0);

        Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a, b));

        LocalDate[] rangeA = ranges.get(a);
        LocalDate[] rangeB = ranges.get(b);
        assertEquals(e1.minusDays(5), rangeA[0]);
        assertEquals(e1, rangeA[1]);

        // B's effective start = max(S2, E1) = max(Feb 10, Feb 15) = Feb 15
        assertEquals(e1, rangeB[0], "B's effective start is first assignment's effective end when S2 < E1");
        assertEquals(e2.plusDays(1), rangeB[1], "B's effective end is deadline + 1 late day");
    }
    @Test
    void nonSeriesAssignmentsDoNotChain() {
        LocalDate d1 = LocalDate.of(2026, 2, 10);
        LocalDate d2 = LocalDate.of(2026, 3, 10);

        Assignment a = new Assignment("A", "CSE 110",
                d1.minusDays(2).atStartOfDay(),
                d1.atTime(23,59),
                0,
                3.0);

        Assignment b = new Assignment("B", "CSE 110",
                d2.minusDays(2).atStartOfDay(),
                d2.atTime(23,59),
                0,
                4.0);

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a,b));

        assertEquals(d1.minusDays(2), ranges.get(a)[0]);
        assertEquals(d2.minusDays(2), ranges.get(b)[0]);
    }
}