package edu.ucsd.studentclock.presenter;

/**
 * Parses course id from the display format "id - name".
 * DS6-2: Parsing lives in presenter layer, not View.
 */
final class CourseSelectionParser {

    private CourseSelectionParser() {}

    /**
     * Extracts course id from display string "id - name", or null if invalid.
     */
    static String parseCourseId(String selected) {
        if (selected == null || !selected.contains(" - ")) {
            return null;
        }
        return selected.substring(0, selected.indexOf(" - ")).trim();
    }
}
