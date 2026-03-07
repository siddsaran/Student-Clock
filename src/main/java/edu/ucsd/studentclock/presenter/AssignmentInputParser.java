package edu.ucsd.studentclock.presenter;

/**
 * Parses raw form input for assignment operations.
 * DS6-2: Parsing lives in presenter layer, not View.
 */
final class AssignmentInputParser {

    private AssignmentInputParser() {}

    static int parseDefaultLateDays(String text) {
        try {
            String s = (text == null || text.trim().isEmpty()) ? "0" : text.trim();
            int value = Integer.parseInt(s);
            if (value < 0) {
                throw new IllegalArgumentException("Default late days cannot be negative");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Default late days must be a valid integer");
        }
    }

    static double parseHours(String text) {
        try {
            double value = Double.parseDouble(text != null ? text.trim() : "");
            if (value < 0) {
                throw new IllegalArgumentException("Hours cannot be negative");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid number like 1.5");
        }
    }
}
