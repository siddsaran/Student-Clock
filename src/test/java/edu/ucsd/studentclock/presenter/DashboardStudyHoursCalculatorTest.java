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

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("DashboardStudyHoursCalculator")
class DashboardStudyHoursCalculatorTest {

    private Assignment makeAssignment(String name, LocalDateTime deadline, double estimated, double remaining) {
        Assignment a = new AssignmentBuilder()
                .setName(name)
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(deadline)
                .setLateDaysAllowed(0)
                .setEstimatedHours(estimated)
                .build();
        a.setRemainingHours(remaining);
        return a;
    }

    @Test
    @DisplayName("Weekly hours left uses integer truncation when distributing hours across available days")
    void computeWeeklyHoursLeftFromToday_integerTruncation() {
        StudyAvailability sa = new StudyAvailability();
        sa.setTotalWeeklyHours(7);
        sa.setAvailable(DayOfWeek.MONDAY, true);
        sa.setAvailable(DayOfWeek.TUESDAY, true);
        sa.setAvailable(DayOfWeek.WEDNESDAY, true);

        LocalDateTime monday = LocalDateTime.of(2026, 2, 2, 12, 0);

        int left = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(sa, monday);

        assertEquals(6, left);
    }

    @Test
    @DisplayName("Weekly hours left is zero when there are no available study days")
    void computeWeeklyHoursLeftFromToday_noAvailableDays_returnsZero() {
        StudyAvailability sa = new StudyAvailability();
        sa.setTotalWeeklyHours(10);

        int left = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(
                sa, LocalDateTime.of(2026, 2, 2, 12, 0));

        assertEquals(0, left);
    }

    @Test
    @DisplayName("Weekly hours left is zero when total weekly hours is zero")
    void computeWeeklyHoursLeftFromToday_zeroWeekly_returnsZero() {
        StudyAvailability sa = new StudyAvailability();
        sa.setAvailable(DayOfWeek.MONDAY, true);

        int left = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(
                sa, LocalDateTime.of(2026, 2, 2, 12, 0));

        assertEquals(0, left);
    }

    @Test
    @DisplayName("Weekly hours left counts only remaining available days from today onward")
    void computeWeeklyHoursLeftFromToday_skipsPastDays() {
        StudyAvailability sa = new StudyAvailability();
        sa.setTotalWeeklyHours(9);
        sa.setAvailable(DayOfWeek.MONDAY, true);
        sa.setAvailable(DayOfWeek.TUESDAY, true);
        sa.setAvailable(DayOfWeek.WEDNESDAY, true);

        LocalDateTime tuesday = LocalDateTime.of(2026, 2, 3, 12, 0);

        int left = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(sa, tuesday);

        assertEquals(6, left);
    }

    @Test
    @DisplayName("Remaining work in next 7 days sums only assignments due within the 7-day window")
    void computeRemainingWorkNext7Days_sumsOnlyDueInWindow() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

        Assignment a1 = makeAssignment("A1", LocalDateTime.of(2026, 2, 12, 23, 59), 5.0, 3.0);
        Assignment a2 = makeAssignment("A2", LocalDateTime.of(2026, 2, 20, 23, 59), 4.0, 2.0);

        double work = DashboardStudyHoursCalculator.computeRemainingWorkNext7Days(List.of(a1, a2), now);

        assertEquals(3.0, work, 1e-9);
    }

    @Test
    @DisplayName("Remaining work in next 7 days is zero for an empty assignment list")
    void computeRemainingWorkNext7Days_emptyList_returnsZero() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

        double work = DashboardStudyHoursCalculator.computeRemainingWorkNext7Days(List.of(), now);

        assertEquals(0.0, work, 1e-9);
    }

    @Test
    @DisplayName("Remaining work includes assignments due exactly 7 days from now")
    void computeRemainingWorkNext7Days_includesExactBoundary() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 12, 0);

        Assignment a = makeAssignment("A1", now.plusDays(7), 5.0, 2.5);

        double work = DashboardStudyHoursCalculator.computeRemainingWorkNext7Days(List.of(a), now);

        assertEquals(2.5, work, 1e-9);
    }

    @Test
    @DisplayName("statusFrom returns RED when work exactly equals available hours")
    void statusFrom_ratioAt1_returnsRed() {
        assertEquals(AssignmentStatus.RED, DashboardStudyHoursCalculator.statusFrom(5.0, 5));
    }

    @Test
    @DisplayName("statusFrom returns ORANGE when work to available ratio is exactly 0.8")
    void statusFrom_ratioAt0_8_returnsOrange() {
        assertEquals(AssignmentStatus.ORANGE, DashboardStudyHoursCalculator.statusFrom(4.0, 5));
    }

    @Test
    @DisplayName("statusFrom returns YELLOW when work to available ratio is exactly 0.6")
    void statusFrom_ratioAt0_6_returnsYellow() {
        assertEquals(AssignmentStatus.YELLOW, DashboardStudyHoursCalculator.statusFrom(3.0, 5));
    }

    @Test
    @DisplayName("statusFrom returns GREEN when work to available ratio is below 0.6")
    void statusFrom_ratioBelow0_6_returnsGreen() {
        assertEquals(AssignmentStatus.GREEN, DashboardStudyHoursCalculator.statusFrom(2.0, 5));
    }

    @Test
    @DisplayName("statusFrom returns RED when no hours are available and work remains")
    void statusFrom_zeroAvailable_workPresent_returnsRed() {
        assertEquals(AssignmentStatus.RED, DashboardStudyHoursCalculator.statusFrom(1.0, 0));
    }

    @Test
    @DisplayName("statusFrom returns GREEN when no hours are available and no work remains")
    void statusFrom_zeroAvailable_noWork_returnsGreen() {
        assertEquals(AssignmentStatus.GREEN, DashboardStudyHoursCalculator.statusFrom(0.0, 0));
    }
}