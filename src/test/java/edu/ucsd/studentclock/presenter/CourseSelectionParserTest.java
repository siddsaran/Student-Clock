package edu.ucsd.studentclock.presenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DS6-2: Tests that course id is parsed from display format in presenter layer.
 * Original bug: View parsed in getSelectedCourseId().
 */
@DisplayName("CourseSelectionParser")
class CourseSelectionParserTest {

    @Nested
    @DisplayName("parseCourseId")
    class ParseCourseId {

        @Test
        @DisplayName("DS6-2: parses id from 'id - name' format")
        void parsesIdFromDisplayFormat() {
            assertEquals("CSE 110", CourseSelectionParser.parseCourseId("CSE 110 - Software Engineering"));
        }

        @Test
        @DisplayName("DS6-2: trims whitespace")
        void trimsWhitespace() {
            assertEquals("CSE 110", CourseSelectionParser.parseCourseId("  CSE 110  -  Software Engineering  "));
        }

        @Test
        @DisplayName("DS6-2: returns null for null or invalid")
        void returnsNullForInvalid() {
            assertNull(CourseSelectionParser.parseCourseId(null));
            assertNull(CourseSelectionParser.parseCourseId("no separator"));
            assertNull(CourseSelectionParser.parseCourseId(""));
        }
    }
}
