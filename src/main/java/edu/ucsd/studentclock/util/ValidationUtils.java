package edu.ucsd.studentclock.util;

/**
 * Shared validation helpers to avoid repeated trim-and-check logic.
 */
public final class ValidationUtils {

    /**
     * Trims the value; if null or blank after trim, throws IllegalArgumentException
     * with the given message.
     *
     * @param value          input string (may be null)
     * @param messageIfBlank message for the exception when value is null or blank
     * @return the trimmed value (never null or blank)
     */
    public static String requireNonBlank(String value, String messageIfBlank) {
        String trimmed = normalizeNullable(value);
        if (trimmed == null) {
            throw new IllegalArgumentException(messageIfBlank);
        }
        return trimmed;
    }

    /**
     * Trims the value and returns null if it is null or blank after trimming.
     *
     * @param value input string (may be null)
     * @return trimmed string, or null when the value is null/blank
     */
    public static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Returns true when the input is null or blank after trimming.
     *
     * @param value input string (may be null)
     * @return whether the input is null/blank
     */
    public static boolean isNullOrBlank(String value) {
        return normalizeNullable(value) == null;
    }
}