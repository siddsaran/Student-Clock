package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 2: Unit tests for domain and model logic (Series).
 * MS1: US3 (Group related assignments into a series), US3-1 (Define assignment series data structure).
 */
@DisplayName("DS7-2: Series")
class SeriesTest {

    @Test
    @DisplayName("getId returns the ID provided in the constructor")
    void getIdReturnsConstructorId() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals("midterm-1", series.getId());
    }

    @Test
    @DisplayName("getCourseId returns the course ID provided in the constructor")
    void getCourseIdReturnsConstructorCourseId() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals("CSE 110", series.getCourseId());
    }

    @Test
    @DisplayName("getName returns the name provided in the constructor")
    void getNameReturnsConstructorName() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals("Midterm", series.getName());
    }

    @Test
    @DisplayName("getDefaultLateDays returns the configured default late days")
    void getDefaultLateDaysReturnsConstructorValue() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 2);
        assertEquals(2, series.getDefaultLateDays());
    }

    @Test
    @DisplayName("Series with the same ID are equal even if other fields differ")
    void equalsWhenSameIdReturnsTrue() {
        Series a = new Series("midterm-1", "CSE 110", "Midterm", 0);
        Series b = new Series("midterm-1", "CSE 101", "Different", 1);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("Series with different IDs are not equal")
    void equalsWhenDifferentIdReturnsFalse() {
        Series a = new Series("midterm-1", "CSE 110", "Midterm", 0);
        Series b = new Series("midterm-2", "CSE 110", "Midterm", 0);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals returns false when compared with null")
    void equalsWhenNullReturnsFalse() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertFalse(series.equals(null));
    }

    @Test
    @DisplayName("equals returns false when compared with a non-Series object")
    void equalsWhenNotSeriesReturnsFalse() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertFalse(series.equals("midterm-1"));
    }

    @Test
    @DisplayName("equals is reflexive")
    void equalsReflexive() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertTrue(series.equals(series));
    }

    @Test
    @DisplayName("hashCode is consistent for series with the same ID")
    void hashCodeConsistentWithEquals() {
        Series a = new Series("midterm-1", "CSE 110", "Midterm", 0);
        Series b = new Series("midterm-1", "CSE 101", "Different", 1);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("toString includes series ID, name, and course ID")
    void toStringContainsIdAndName() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        String s = series.toString();
        assertTrue(s.contains("midterm-1"));
        assertTrue(s.contains("Midterm"));
        assertTrue(s.contains("CSE 110"));
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when ID is null")
    void constructorThrowsWhenIdNull() {
        assertThrows(NullPointerException.class,
                () -> new Series(null, "CSE 110", "Midterm", 0));
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when course ID is null")
    void constructorThrowsWhenCourseIdNull() {
        assertThrows(NullPointerException.class,
                () -> new Series("midterm-1", null, "Midterm", 0));
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when name is null")
    void constructorThrowsWhenNameNull() {
        assertThrows(NullPointerException.class,
                () -> new Series("midterm-1", "CSE 110", null, 0));
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException when default late days are negative")
    void constructorThrowsWhenDefaultLateDaysNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new Series("midterm-1", "CSE 110", "Midterm", -1));
    }

    @Test
    @DisplayName("Constructor accepts zero default late days")
    void constructorAcceptsZeroDefaultLateDays() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals(0, series.getDefaultLateDays());
    }
}