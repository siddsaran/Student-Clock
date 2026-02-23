package edu.ucsd.studentclock.util;

/**
 * Formats hours (as decimal) into HH:MM display format for consistent display throughout the app.
 */
public final class TimeFormatUtils {

    private TimeFormatUtils() {}

    /**
     * Converts decimal hours to HH:MM format (e.g., 2.5 -> "02:30", 12 -> "12:00").
     *
     * @param totalHours hours as a decimal (e.g., 2.5 for 2h 30m)
     * @return formatted string "HH:MM"
     */
    public static String formatHoursAsHHMM(double totalHours) {
        if (totalHours < 0) {
            totalHours = 0;
        }
        int totalMinutes = (int) Math.round(totalHours * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
