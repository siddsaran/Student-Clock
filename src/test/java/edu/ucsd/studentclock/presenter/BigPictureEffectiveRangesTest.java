package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BigPictureEffectiveRanges")
class BigPictureEffectiveRangesTest {

    @Test
    void seriesRespectsDeadlineOrderingNotInputOrdering() {
        Assignment later = new AssignmentBuilder()
                .setName("Later")
                .setCourseId("CSE 110")
                .setSeriesId("series-x")
                .setStart(LocalDateTime.of(2026, 2, 1, 0, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 20, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(1.0)
                .build();

        Assignment earlier = new AssignmentBuilder()
                .setName("Earlier")
                .setCourseId("CSE 110")
                .setSeriesId("series-x")
                .setStart(LocalDateTime.of(2026, 2, 10, 0, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 15, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(1.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(later, earlier));

        LocalDate[] rEarlier = ranges.get(earlier);
        LocalDate[] rLater = ranges.get(later);

        assertNotNull(rEarlier);
        assertNotNull(rLater);

        assertEquals(LocalDate.of(2026, 2, 10), rEarlier[0]);
        assertEquals(LocalDate.of(2026, 2, 15), rEarlier[1]);

        assertEquals(LocalDate.of(2026, 2, 16), rLater[0]);
        assertEquals(LocalDate.of(2026, 2, 20), rLater[1]);
    }

    @Test
    void seriesDoesNotMoveStartForwardWhenStoredStartAfterPrevEnd() {
        Assignment first = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setSeriesId("series-y")
                .setStart(LocalDateTime.of(2026, 2, 1, 0, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 10, 23, 59))
                .setLateDaysAllowed(2)
                .setEstimatedHours(1.0)
                .build();

        Assignment second = new AssignmentBuilder()
                .setName("B")
                .setCourseId("CSE 110")
                .setSeriesId("series-y")
                .setStart(LocalDateTime.of(2026, 2, 20, 0, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 25, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(1.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(first, second));

        LocalDate[] rFirst = ranges.get(first);
        LocalDate[] rSecond = ranges.get(second);

        assertEquals(LocalDate.of(2026, 2, 1), rFirst[0]);
        assertEquals(LocalDate.of(2026, 2, 25), rFirst[1]);

        assertEquals(LocalDate.of(2026, 2, 20), rSecond[0]);
        assertEquals(LocalDate.of(2026, 2, 25), rSecond[1]);
    }

    @Test
    void nullSeriesDoesNotChainAcrossCourses() {
        Assignment a = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 0, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(1.0)
                .build();

        Assignment b = new AssignmentBuilder()
                .setName("B")
                .setCourseId("CSE 101")
                .setStart(LocalDateTime.of(2026, 2, 2, 0, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 6, 23, 59))
                .setLateDaysAllowed(3)
                .setEstimatedHours(1.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a, b));

        assertEquals(LocalDate.of(2026, 2, 1), ranges.get(a)[0]);
        assertEquals(LocalDate.of(2026, 2, 5), ranges.get(a)[1]);

        assertEquals(LocalDate.of(2026, 2, 2), ranges.get(b)[0]);
        assertEquals(LocalDate.of(2026, 2, 9), ranges.get(b)[1]);
    }

    @Test
    void effectiveEndExtendsByLateDays() {
        LocalDate d = LocalDate.of(2026, 2, 10);
        Assignment a = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setStart(d.minusDays(2).atStartOfDay())
                .setDeadline(d.atTime(23, 59))
                .setLateDaysAllowed(2)
                .setEstimatedHours(5.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a));

        LocalDate[] range = ranges.get(a);
        assertEquals(d.minusDays(2), range[0], "effective start is stored start");
        assertEquals(d.plusDays(2), range[1], "effective end is deadline + lateDaysAllowed (D+2)");
    }

    @Test
    void seriesSecondAssignmentStartsAfterFirstEffectiveEnd() {
        LocalDate e1 = LocalDate.of(2026, 2, 15);
        LocalDate s2 = LocalDate.of(2026, 2, 10);
        LocalDate e2 = LocalDate.of(2026, 2, 20);

        Assignment a = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setSeriesId("series-1")
                .setStart(e1.minusDays(5).atStartOfDay())
                .setDeadline(e1.atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(3.0)
                .build();

        Assignment b = new AssignmentBuilder()
                .setName("B")
                .setCourseId("CSE 110")
                .setSeriesId("series-1")
                .setStart(s2.atStartOfDay())
                .setDeadline(e2.atTime(23, 59))
                .setLateDaysAllowed(1)
                .setEstimatedHours(4.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a, b));

        LocalDate[] rangeA = ranges.get(a);
        LocalDate[] rangeB = ranges.get(b);

        assertEquals(e1.minusDays(5), rangeA[0]);
        assertEquals(e1, rangeA[1]);

        // B's effective start = day after A's deadline = Feb 15 + 1 = Feb 16
        assertEquals(e1.plusDays(1), rangeB[0], "B's effective start is day after first assignment's deadline when S2 < E1");
        assertEquals(e2.plusDays(1), rangeB[1], "B's effective end is deadline + 1 late day");
    }

    @Test
    void nonSeriesAssignmentsDoNotChain() {
        LocalDate d1 = LocalDate.of(2026, 2, 10);
        LocalDate d2 = LocalDate.of(2026, 3, 10);

        Assignment a = new AssignmentBuilder()
                .setName("A")
                .setCourseId("CSE 110")
                .setStart(d1.minusDays(2).atStartOfDay())
                .setDeadline(d1.atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(3.0)
                .build();

        Assignment b = new AssignmentBuilder()
                .setName("B")
                .setCourseId("CSE 110")
                .setStart(d2.minusDays(2).atStartOfDay())
                .setDeadline(d2.atTime(23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(4.0)
                .build();

        Map<Assignment, LocalDate[]> ranges =
                BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a, b));

        assertEquals(d1.minusDays(2), ranges.get(a)[0]);
        assertEquals(d2.minusDays(2), ranges.get(b)[0]);
    }
}