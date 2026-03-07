package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BigPictureEffectiveRangesTest {

    @Test
    void seriesRespectsDeadlineOrderingNotInputOrdering() {
        Assignment later = new Assignment(
                "Later",
                "CSE 110",
                "series-x",
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 20, 23, 59),
                0,
                1.0
        );

        Assignment earlier = new Assignment(
                "Earlier",
                "CSE 110",
                "series-x",
                LocalDateTime.of(2026, 2, 10, 0, 0),
                LocalDateTime.of(2026, 2, 15, 23, 59),
                0,
                1.0
        );

        Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(later, earlier));

        LocalDate[] rEarlier = ranges.get(earlier);
        LocalDate[] rLater = ranges.get(later);

        assertEquals(LocalDate.of(2026, 2, 10), rEarlier[0]);
        assertEquals(LocalDate.of(2026, 2, 15), rEarlier[1]);

        assertEquals(LocalDate.of(2026, 2, 16), rLater[0]);
        assertEquals(LocalDate.of(2026, 2, 20), rLater[1]);
    }

    @Test
    void seriesDoesNotMoveStartForwardWhenStoredStartAfterPrevEnd() {
        Assignment first = new Assignment(
                "A",
                "CSE 110",
                "series-y",
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 10, 23, 59),
                2,
                1.0
        );

        Assignment second = new Assignment(
                "B",
                "CSE 110",
                "series-y",
                LocalDateTime.of(2026, 2, 20, 0, 0),
                LocalDateTime.of(2026, 2, 25, 23, 59),
                0,
                1.0
        );

        Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(first, second));

        LocalDate[] rFirst = ranges.get(first);
        LocalDate[] rSecond = ranges.get(second);

        assertEquals(LocalDate.of(2026, 2, 1), rFirst[0]);
        assertEquals(LocalDate.of(2026, 2, 25), rFirst[1]);

        assertEquals(LocalDate.of(2026, 2, 20), rSecond[0]);
        assertEquals(LocalDate.of(2026, 2, 25), rSecond[1]);
    }

    @Test
    void nullSeriesDoesNotChainAcrossCourses() {
        Assignment a = new Assignment(
                "A",
                "CSE 110",
                LocalDateTime.of(2026, 2, 1, 0, 0),
                LocalDateTime.of(2026, 2, 5, 23, 59),
                0,
                1.0
        );

        Assignment b = new Assignment(
                "B",
                "CSE 101",
                LocalDateTime.of(2026, 2, 2, 0, 0),
                LocalDateTime.of(2026, 2, 6, 23, 59),
                3,
                1.0
        );

        Map<Assignment, LocalDate[]> ranges = BigPictureEffectiveRanges.computeEffectiveRanges(List.of(a, b));

        assertEquals(LocalDate.of(2026, 2, 1), ranges.get(a)[0]);
        assertEquals(LocalDate.of(2026, 2, 5), ranges.get(a)[1]);

        assertEquals(LocalDate.of(2026, 2, 2), ranges.get(b)[0]);
        assertEquals(LocalDate.of(2026, 2, 9), ranges.get(b)[1]);
    }
}