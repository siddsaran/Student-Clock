package edu.ucsd.studentclock.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Assigns distinct colors to courses for Big Picture chart.
 */
public final class CourseColors {

    private static final String[] PALETTE = {
            "#87CEEB",  // Sky Blue
            "#9370DB",  // Purple
            "#32CD32",  // Green
            "#FF6347",  // Tomato
            "#FFD700",  // Gold
            "#20B2AA",  // Light Sea Green
            "#FF69B4",  // Hot Pink
            "#4169E1",  // Royal Blue
            "#FF8C00",  // Dark Orange
            "#6B8E23",  // Olive Drab
    };

    private final Map<String, String> courseToColor = new LinkedHashMap<>();
    private int nextIndex = 0;

    public String getColor(String courseId) {
        return courseToColor.computeIfAbsent(courseId, k -> PALETTE[nextIndex++ % PALETTE.length]);
    }
}
