package edu.ucsd.studentclock.util;

/**
 * Shared validation helpers to avoid repeated trim-and-check logic.
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    /**
     * Trims the value; if null or blank after trim, throws IllegalArgumentException with the given message.
     *
     * @param value input string (may be null)
     * @param messageIfBlank message for the exception when value is null or blank
     * @return the trimmed value (never null or blank)
     */
    public static String requireNonBlank(String value, String messageIfBlank) {
        String trimmed = value == null ? null : value.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            throw new IllegalArgumentException(messageIfBlank);
        }
        return trimmed;
    }
}
