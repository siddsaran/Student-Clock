package edu.ucsd.studentclock.presenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DS6-2: Tests that form input is parsed in presenter layer.
 * Original bug: View did Integer.parseInt / Double.parseDouble before calling
 * Presenter.
 */
@DisplayName("AssignmentInputParser")
class AssignmentInputParserTest {

    @Nested
    @DisplayName("parseDefaultLateDays")
    class ParseDefaultLateDays {

        @Test
        @DisplayName("DS6-2: parses valid integer")
        void parsesValidInteger() {
            assertEquals(2, AssignmentInputParser.parseDefaultLateDays("2"));
            assertEquals(0, AssignmentInputParser.parseDefaultLateDays("0"));
            assertEquals(0, AssignmentInputParser.parseDefaultLateDays(null));
            assertEquals(0, AssignmentInputParser.parseDefaultLateDays(""));
        }

        @Test
        @DisplayName("DS6-2: rejects invalid and throws from presenter layer")
        void invalidThrows() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> AssignmentInputParser.parseDefaultLateDays("not-a-number"));
            assertTrue(ex.getMessage().contains("valid integer"));
        }

        @Test
        @DisplayName("DS6-2: rejects negative")
        void negativeThrows() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> AssignmentInputParser.parseDefaultLateDays("-1"));
            assertTrue(ex.getMessage().toLowerCase().contains("negative"));
        }
    }

    @Nested
    @DisplayName("parseHours")
    class ParseHours {

        @Test
        @DisplayName("DS6-2: parses valid number")
        void parsesValidNumber() {
            assertEquals(1.5, AssignmentInputParser.parseHours("1.5"), 0.001);
            assertEquals(0.0, AssignmentInputParser.parseHours("0"));
        }

        @Test
        @DisplayName("DS6-2: rejects invalid and throws from presenter layer")
        void invalidThrows() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> AssignmentInputParser.parseHours("not-a-number"));
            assertTrue(ex.getMessage().contains("valid number"));
        }

        @Test
        @DisplayName("DS6-2: rejects negative")
        void negativeThrows() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> AssignmentInputParser.parseHours("-1.5"));
            assertTrue(ex.getMessage().toLowerCase().contains("negative"));
        }
    }
}
