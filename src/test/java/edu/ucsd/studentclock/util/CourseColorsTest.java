package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CourseColors")
class CourseColorsTest {

    @Test
    void getColor_sameCourseIdReturnsSameColor() {
        CourseColors colors = new CourseColors();
        String a = colors.getColor("CSE 110");
        String b = colors.getColor("CSE 110");
        assertEquals(a, b);
    }

    @Test
    void getColor_differentCoursesReturnDifferentColors() {
        CourseColors colors = new CourseColors();
        String a = colors.getColor("CSE 110");
        String b = colors.getColor("CSE 101");
        assertNotEquals(a, b);
    }

    @Test
    void getColor_returnsHexFormat() {
        CourseColors colors = new CourseColors();
        String color = colors.getColor("CSE 110");
        assertNotNull(color);
        assertTrue(color.startsWith("#"));
        assertEquals(7, color.length());
    }

    @Test
    void getColor_wrapsPaletteWhenExceedingCount() {
        CourseColors colors = new CourseColors();
        for (int i = 0; i < 12; i++) {
            String color = colors.getColor("COURSE-" + i);
            assertNotNull(color);
            assertTrue(color.startsWith("#"));
        }
    }

    @Test
    void getColor_nullCourseIdAllowed() {
        CourseColors colors = new CourseColors();
        String color = colors.getColor(null);
        assertNotNull(color);
        assertTrue(color.startsWith("#"));
    }
}
