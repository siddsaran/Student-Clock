package edu.ucsd.studentclock.presenter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.StudyAvailability;

public final class DashboardStudyHoursCalculator {

    private DashboardStudyHoursCalculator() {
    }

    public static int computeWeeklyHoursLeftFromToday(StudyAvailability sa, LocalDateTime now) {
        int weekly = sa.getTotalWeeklyHours();
        if (weekly <= 0)
            return 0;
        int availableDaysInWeek = 0;
        for (DayOfWeek d : DayOfWeek.values()) {
            if (sa.isAvailable(d))
                availableDaysInWeek++;
        }
        if (availableDaysInWeek == 0)
            return 0;
        int perAvailableDay = weekly / availableDaysInWeek;
        DayOfWeek today = now.getDayOfWeek();
        int remainingAvailableDays = 0;
        for (DayOfWeek d : DayOfWeek.values()) {
            if (d.getValue() >= today.getValue() && sa.isAvailable(d)) {
                remainingAvailableDays++;
            }
        }
        return perAvailableDay * remainingAvailableDays;
    }

    public static double computeRemainingWorkNext7Days(List<Assignment> all, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate end = today.plusDays(7);
        double sum = 0;
        for (Assignment a : all) {
            if (a.isDone())
                continue;
            if (a.getDeadline() == null)
                continue;
            LocalDate due = a.getDeadline().toLocalDate();
            if (!due.isBefore(today) && !due.isAfter(end)) {
                sum += a.getRemainingHours();
            }
        }
        return sum;
    }

    public static AssignmentStatus statusFrom(double work, int available) {
        if (available <= 0) {
            return work > 0 ? AssignmentStatus.RED : AssignmentStatus.GREEN;
        }
        double ratio = work / (double) available;
        if (ratio >= 1.0)
            return AssignmentStatus.RED;
        if (ratio >= 0.80)
            return AssignmentStatus.ORANGE;
        if (ratio >= 0.60)
            return AssignmentStatus.YELLOW;
        return AssignmentStatus.GREEN;
    }
}
