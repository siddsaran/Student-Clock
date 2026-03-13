package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils")
class ValidationUtilsTest {

    @Test
    void requireNonBlank_returnsTrimmedValueWhenValid() {
        assertEquals("CSE 110", ValidationUtils.requireNonBlank("CSE 110", "id"));
        assertEquals("CSE 110", ValidationUtils.requireNonBlank("  CSE 110  ", "id"));
    }

    @Test
    void requireNonBlank_nullThrowsWithMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonBlank(null, "id must not be blank"));
        assertEquals("id must not be blank", e.getMessage());
    }

    @Test
    void requireNonBlank_emptyThrowsWithMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonBlank("", "name must not be blank"));
        assertEquals("name must not be blank", e.getMessage());
    }

    @Test
    void requireNonBlank_whitespaceOnlyThrowsWithMessage() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonBlank("   ", "value must not be blank"));
        assertEquals("value must not be blank", e.getMessage());
    }

    @Test
    void normalizeNullable_nullReturnsNull() {
        assertNull(ValidationUtils.normalizeNullable(null));
    }

    @Test
    void normalizeNullable_emptyReturnsNull() {
        assertNull(ValidationUtils.normalizeNullable(""));
    }

    @Test
    void normalizeNullable_whitespaceOnlyReturnsNull() {
        assertNull(ValidationUtils.normalizeNullable("   "));
    }

    @Test
    void normalizeNullable_validReturnsTrimmed() {
        assertEquals("CSE 110", ValidationUtils.normalizeNullable("  CSE 110  "));
    }

    @Test
    void isNullOrBlank_nullReturnsTrue() {
        assertTrue(ValidationUtils.isNullOrBlank(null));
    }

    @Test
    void isNullOrBlank_emptyReturnsTrue() {
        assertTrue(ValidationUtils.isNullOrBlank(""));
    }

    @Test
    void isNullOrBlank_whitespaceOnlyReturnsTrue() {
        assertTrue(ValidationUtils.isNullOrBlank("   "));
    }

    @Test
    void isNullOrBlank_nonBlankReturnsFalse() {
        assertFalse(ValidationUtils.isNullOrBlank("CSE 110"));
        assertFalse(ValidationUtils.isNullOrBlank("  x  "));
    }
}
