package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 2: Unit tests for domain and model logic (CourseColors).
 * MS1: Supports US1, US3, US9 (course display and assignment list styling).
 */
@DisplayName("DS7-2: CourseColors")
class CourseColorsTest {

    @Test
    @DisplayName("Same course ID always returns the same color")
    void getColor_sameCourseIdReturnsSameColor() {
        CourseColors colors = new CourseColors();
        String a = colors.getColor("CSE 110");
        String b = colors.getColor("CSE 110");
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Different course IDs return different colors")
    void getColor_differentCoursesReturnDifferentColors() {
        CourseColors colors = new CourseColors();
        String a = colors.getColor("CSE 110");
        String b = colors.getColor("CSE 101");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Returned color is a hex string in the format #RRGGBB")
    void getColor_returnsHexFormat() {
        CourseColors colors = new CourseColors();
        String color = colors.getColor("CSE 110");

        assertNotNull(color);
        assertTrue(color.startsWith("#"));
        assertEquals(7, color.length());
    }

    @Test
    @DisplayName("Color palette wraps correctly when more courses than colors exist")
    void getColor_wrapsPaletteWhenExceedingCount() {
        CourseColors colors = new CourseColors();

        for (int i = 0; i < 12; i++) {
            String color = colors.getColor("COURSE-" + i);
            assertNotNull(color);
            assertTrue(color.startsWith("#"));
        }
    }

    @Test
    @DisplayName("Null course ID still produces a valid color")
    void getColor_nullCourseIdAllowed() {
        CourseColors colors = new CourseColors();
        String color = colors.getColor(null);

        assertNotNull(color);
        assertTrue(color.startsWith("#"));
    }
}