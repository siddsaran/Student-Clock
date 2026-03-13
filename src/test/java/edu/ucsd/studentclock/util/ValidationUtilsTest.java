package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils")
class ValidationUtilsTest {

    @Test
    @DisplayName("requireNonBlank returns trimmed value when input is valid")
    void requireNonBlank_returnsTrimmedValueWhenValid() {
        assertEquals("CSE 110", ValidationUtils.requireNonBlank("CSE 110", "id"));
        assertEquals("CSE 110", ValidationUtils.requireNonBlank("  CSE 110  ", "id"));
    }

    @Test
    @DisplayName("requireNonBlank throws IllegalArgumentException when input is null")
    void requireNonBlank_nullThrowsWithMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonBlank(null, "id must not be blank"));
        assertEquals("id must not be blank", e.getMessage());
    }

    @Test
    @DisplayName("requireNonBlank throws IllegalArgumentException when input is empty")
    void requireNonBlank_emptyThrowsWithMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonBlank("", "name must not be blank"));
        assertEquals("name must not be blank", e.getMessage());
    }

    @Test
    @DisplayName("requireNonBlank throws IllegalArgumentException when input is whitespace only")
    void requireNonBlank_whitespaceOnlyThrowsWithMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonBlank("   ", "value must not be blank"));
        assertEquals("value must not be blank", e.getMessage());
    }

    @Test
    @DisplayName("normalizeNullable returns null when input is null")
    void normalizeNullable_nullReturnsNull() {
        assertNull(ValidationUtils.normalizeNullable(null));
    }

    @Test
    @DisplayName("normalizeNullable returns null when input is empty")
    void normalizeNullable_emptyReturnsNull() {
        assertNull(ValidationUtils.normalizeNullable(""));
    }

    @Test
    @DisplayName("normalizeNullable returns null when input is whitespace only")
    void normalizeNullable_whitespaceOnlyReturnsNull() {
        assertNull(ValidationUtils.normalizeNullable("   "));
    }

    @Test
    @DisplayName("normalizeNullable returns trimmed value when input is valid")
    void normalizeNullable_validReturnsTrimmed() {
        assertEquals("CSE 110", ValidationUtils.normalizeNullable("  CSE 110  "));
    }

    @Test
    @DisplayName("isNullOrBlank returns true for null input")
    void isNullOrBlank_nullReturnsTrue() {
        assertTrue(ValidationUtils.isNullOrBlank(null));
    }

    @Test
    @DisplayName("isNullOrBlank returns true for empty string")
    void isNullOrBlank_emptyReturnsTrue() {
        assertTrue(ValidationUtils.isNullOrBlank(""));
    }

    @Test
    @DisplayName("isNullOrBlank returns true for whitespace-only string")
    void isNullOrBlank_whitespaceOnlyReturnsTrue() {
        assertTrue(ValidationUtils.isNullOrBlank("   "));
    }

    @Test
    @DisplayName("isNullOrBlank returns false for non-blank strings")
    void isNullOrBlank_nonBlankReturnsFalse() {
        assertFalse(ValidationUtils.isNullOrBlank("CSE 110"));
        assertFalse(ValidationUtils.isNullOrBlank("  x  "));
    }
}