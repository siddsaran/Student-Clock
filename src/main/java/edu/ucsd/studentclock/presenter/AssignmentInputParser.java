package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.util.ValidationUtils;

/**
 * Parses raw form input for assignment operations.
 * DS6-2: Parsing lives in presenter layer, not View.
 */
final class AssignmentInputParser {

    private static final String DEFAULT_ZERO = "0";

    private AssignmentInputParser() {
    }

    static int parseDefaultLateDays(String text) {
        return parseNonNegativeIntOrDefault(
                text,
                DEFAULT_ZERO,
                "Default late days cannot be negative",
                "Default late days must be a valid integer");
    }

    static int parseOptionalNonNegativeInt(String text, String invalidMessage, String negativeMessage) {
        return parseNonNegativeIntOrDefault(text, DEFAULT_ZERO, negativeMessage, invalidMessage);
    }

    static double parseHours(String text) {
        return parseNonNegativeDouble(text, "Enter a valid number like 1.5", "Hours cannot be negative");
    }

    static double parseEstimatedHours(String text) {
        return parseNonNegativeDouble(
                text,
                "Enter a valid number for estimated hours",
                "Estimated hours must be >= 0");
    }

    private static int parseNonNegativeIntOrDefault(
            String text,
            String defaultValue,
            String negativeMessage,
            String invalidMessage) {
        try {
            String normalized = normalizeOrDefault(text, defaultValue);
            int value = Integer.parseInt(normalized);
            if (value < 0) {
                throw new IllegalArgumentException(negativeMessage);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMessage);
        }
    }

    private static double parseNonNegativeDouble(String text, String invalidMessage, String negativeMessage) {
        try {
            String normalized = ValidationUtils.requireNonBlank(text, invalidMessage);
            double value = Double.parseDouble(normalized);
            if (value < 0) {
                throw new IllegalArgumentException(negativeMessage);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidMessage);
        }
    }

    private static String normalizeOrDefault(String text, String defaultValue) {
        String normalized = ValidationUtils.normalizeNullable(text);
        return normalized == null ? defaultValue : normalized;
    }
}