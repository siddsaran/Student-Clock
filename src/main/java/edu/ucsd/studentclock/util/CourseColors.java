package edu.ucsd.studentclock.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Assigns distinct colors to courses for Big Picture chart.
 */
public final class CourseColors {

    private static final String[] PALETTE = {
            "#87CEEB",
            "#9370DB", 
            "#32CD32", 
            "#FF6347", 
            "#FFD700", 
            "#20B2AA", 
            "#FF69B4", 
            "#4169E1", 
            "#FF8C00", 
            "#6B8E23", 
    };

    private final Map<String, String> courseToColor = new LinkedHashMap<>();
    private int nextIndex = 0;

    public String getColor(String courseId) {
        return courseToColor.computeIfAbsent(courseId, k -> PALETTE[nextIndex++ % PALETTE.length]);
    }
}
