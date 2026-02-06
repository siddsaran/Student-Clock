package edu.ucsd.studentclock.model;
import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores a student's weekly study availability.
 *
 * - All study hours are whole numbers.
 * - totalWeeklyHours >= 0.
 * - Daily limits are only allowed on available days.
 * - Sum of daily limits should not exceed totalWeeklyHours.
 */
public final class StudyAvailability {

    private int totalWeeklyHours;
    private final Set<DayOfWeek> availableDays;
    private final Map<DayOfWeek, Integer> dailyLimits;

    public StudyAvailability() {
        this.totalWeeklyHours = 0;
        this.availableDays = EnumSet.noneOf(DayOfWeek.class);
        this.dailyLimits = new EnumMap<>(DayOfWeek.class);

        for (DayOfWeek d : DayOfWeek.values()) {
            dailyLimits.put(d, 0);
        }
    }

    public int getTotalWeeklyHours() {
        return totalWeeklyHours;
    }

    public void setTotalWeeklyHours(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Total weekly hours must be >= 0");
        }
        this.totalWeeklyHours = hours;
    }

    public boolean isAvailable(DayOfWeek day) {
        return availableDays.contains(day);
    }

    public void setAvailable(DayOfWeek day, boolean available) {
        if (available) {
            availableDays.add(day);
        } else {
            availableDays.remove(day);
            dailyLimits.put(day, 0); 
        }
    }

    public int getDailyLimit(DayOfWeek day) {
        return dailyLimits.getOrDefault(day, 0);
    }

    public void setDailyLimit(DayOfWeek day, int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Daily limit must be >= 0");
        }
        if (!isAvailable(day) && hours > 0) {
            throw new IllegalStateException(
                "Cannot set hours for an unavailable day: " + day
            );
        }
        dailyLimits.put(day, hours);
    }

    public int getTotalAllocatedHours() {
        int sum = 0;
        for (int hrs : dailyLimits.values()) {
            sum += hrs;
        }
        return sum;
    }

    public int getUnallocatedHours() {
        return totalWeeklyHours - getTotalAllocatedHours();
    }

    public String validate() {
        if (totalWeeklyHours < 0) {
            return "Total weekly hours must be >= 0.";
        }
        if (getTotalAllocatedHours() > totalWeeklyHours) {
            return "Allocated hours exceed total weekly hours.";
        }
        return null;
    }
}