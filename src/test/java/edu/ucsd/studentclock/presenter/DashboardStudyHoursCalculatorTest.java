package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.StudyAvailability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * US5/DS7: Dashboard study-hour calculations, urgency status, edge cases (integer truncation).
 */
@DisplayName("DashboardStudyHoursCalculator")
class DashboardStudyHoursCalculatorTest {

    @Test
    void computeWeeklyHoursLeftFromToday_integerTruncation() {
        StudyAvailability sa = new StudyAvailability();
        sa.setTotalWeeklyHours(7);
        sa.setAvailable(DayOfWeek.MONDAY, true);
        sa.setAvailable(DayOfWeek.TUESDAY, true);
        sa.setAvailable(DayOfWeek.WEDNESDAY, true);
        LocalDateTime monday = LocalDateTime.of(2026, 2, 2, 12, 0);

        int left = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(sa, monday);

        assertEquals(6, left, "7/3=2 per day (truncated), 3 days left = 6");
    }

    @Test
    void computeWeeklyHoursLeftFromToday_noAvailableDays_returnsZero() {
        StudyAvailability sa = new StudyAvailability();
        sa.setTotalWeeklyHours(10);
        int left = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(sa, LocalDateTime.of(2026, 2, 2, 12, 0));
        assertEquals(0, left);
    }

    @Test
    void computeWeeklyHoursLeftFromToday_zeroWeekly_returnsZero() {
        StudyAvailability sa = new StudyAvailability();
        sa.setAvailable(DayOfWeek.MONDAY, true);
        int left = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(sa, LocalDateTime.of(2026, 2, 2, 12, 0));
        assertEquals(0, left);
    }

    @Test
    void computeRemainingWorkNext7Days_sumsOnlyDueInWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);
        Assignment a1 = new AssignmentBuilder()
                .setName("A1")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 12, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(5.0)
                .build();
        a1.setRemainingHours(3.0);

        Assignment a2 = new AssignmentBuilder()
                .setName("A2")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 20, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(4.0)
                .build();
        a2.setRemainingHours(2.0);

        double work = DashboardStudyHoursCalculator.computeRemainingWorkNext7Days(List.of(a1, a2), now);

        assertEquals(3.0, work, 1e-9, "only A1 due in [Feb 10, Feb 17]");
    }

    @Test
    void statusFrom_ratioAt1_returnsRed() {
        assertEquals(AssignmentStatus.RED, DashboardStudyHoursCalculator.statusFrom(5.0, 5));
    }

    @Test
    void statusFrom_ratioAt0_8_returnsOrange() {
        assertEquals(AssignmentStatus.ORANGE, DashboardStudyHoursCalculator.statusFrom(4.0, 5));
    }

    @Test
    void statusFrom_ratioAt0_6_returnsYellow() {
        assertEquals(AssignmentStatus.YELLOW, DashboardStudyHoursCalculator.statusFrom(3.0, 5));
    }

    @Test
    void statusFrom_ratioBelow0_6_returnsGreen() {
        assertEquals(AssignmentStatus.GREEN, DashboardStudyHoursCalculator.statusFrom(2.0, 5));
    }

    @Test
    void statusFrom_zeroAvailable_workPresent_returnsRed() {
        assertEquals(AssignmentStatus.RED, DashboardStudyHoursCalculator.statusFrom(1.0, 0));
    }

    @Test
    void statusFrom_zeroAvailable_noWork_returnsGreen() {
        assertEquals(AssignmentStatus.GREEN, DashboardStudyHoursCalculator.statusFrom(0.0, 0));
    }
}
