package edu.ucsd.studentclock.presenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 3: Presenter tests for core user actions (CourseSelectionParser).
 * MS1: US1 (Create and view courses).
 */
@DisplayName("DS7-3: CourseSelectionParser")
class CourseSelectionParserTest {

    @Test
    @DisplayName("parses id from 'id - name' format")
    void parsesIdFromDisplayFormat() {
        assertEquals("CSE 110",
                CourseSelectionParser.parseCourseId("CSE 110 - Software Engineering"));
    }

    @Test
    @DisplayName("trims whitespace")
    void trimsWhitespace() {
        assertEquals("CSE 110",
                CourseSelectionParser.parseCourseId("  CSE 110  -  Software Engineering  "));
    }

    @Test
    @DisplayName("returns null for null or invalid input")
    void returnsNullForInvalid() {
        assertNull(CourseSelectionParser.parseCourseId(null));
        assertNull(CourseSelectionParser.parseCourseId("no separator"));
        assertNull(CourseSelectionParser.parseCourseId(""));
    }
}