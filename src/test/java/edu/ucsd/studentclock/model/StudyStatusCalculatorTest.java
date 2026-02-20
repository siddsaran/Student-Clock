package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StudyStatusCalculatorTest {

    @Test
    public void exceedsAvailableHours_isRed() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 19, 12, 0);

        Assignment a1 = new Assignment("A1", "CSE110", now.minusDays(1), now.plusDays(3), 0, 6);
        Assignment a2 = new Assignment("A2", "MGT107", now.minusDays(2), now.plusDays(6), 0, 7);

        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);

        AssignmentStatus status = StudyStatusCalculator.overallStudyStatus(
                List.of(a1, a2),
                availability,
                now
        );

        assertEquals(AssignmentStatus.RED, status);
    }

    @Test
    public void highButNotExceeding_isOrange() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 19, 12, 0);

        Assignment a1 = new Assignment("A1", "CSE110", now.minusDays(1), now.plusDays(3), 0, 4);
        Assignment a2 = new Assignment("A2", "MGT107", now.minusDays(2), now.plusDays(6), 0, 3.6);

        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);

        AssignmentStatus status = StudyStatusCalculator.overallStudyStatus(
                List.of(a1, a2),
                availability,
                now
        );

        assertEquals(AssignmentStatus.ORANGE, status);
    }

    @Test
    public void mediumWorkload_isYellow() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 19, 12, 0);

        Assignment a1 = new Assignment("A1", "CSE110", now.minusDays(1), now.plusDays(3), 0, 3);
        Assignment a2 = new Assignment("A2", "MGT107", now.minusDays(2), now.plusDays(6), 0, 2.5);

        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);

        AssignmentStatus status = StudyStatusCalculator.overallStudyStatus(
                List.of(a1, a2),
                availability,
                now
        );

        assertEquals(AssignmentStatus.YELLOW, status);
    }

    @Test
    public void dueWithinDay_escalates() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 19, 12, 0);

        Assignment a1 = new Assignment("A1", "CSE110", now.minusDays(1), now.plusHours(12), 0, 6);
        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);

        AssignmentStatus status = StudyStatusCalculator.overallStudyStatus(
                List.of(a1),
                availability,
                now
        );

        assertEquals(AssignmentStatus.ORANGE, status);
    }

    @Test
    public void lowWorkload_isNone() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 19, 12, 0);

        Assignment a1 = new Assignment("A1", "CSE110", now.minusDays(1), now.plusDays(3), 0, 2);
        StudyAvailability availability = new StudyAvailability();
        availability.setTotalWeeklyHours(10);

        AssignmentStatus status = StudyStatusCalculator.overallStudyStatus(
                List.of(a1),
                availability,
                now
        );

        assertEquals(AssignmentStatus.NONE, status);
    }
}
