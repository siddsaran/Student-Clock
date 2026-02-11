package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeriesTest {

    @Test
    void getIdReturnsConstructorId() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals("midterm-1", series.getId());
    }

    @Test
    void getCourseIdReturnsConstructorCourseId() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals("CSE 110", series.getCourseId());
    }

    @Test
    void getNameReturnsConstructorName() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals("Midterm", series.getName());
    }

    @Test
    void getDefaultLateDaysReturnsConstructorValue() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 2);
        assertEquals(2, series.getDefaultLateDays());
    }

    @Test
    void equalsWhenSameIdReturnsTrue() {
        Series a = new Series("midterm-1", "CSE 110", "Midterm", 0);
        Series b = new Series("midterm-1", "CSE 101", "Different", 1);
        assertEquals(a, b);
        assertTrue(a.equals(b));
    }

    @Test
    void equalsWhenDifferentIdReturnsFalse() {
        Series a = new Series("midterm-1", "CSE 110", "Midterm", 0);
        Series b = new Series("midterm-2", "CSE 110", "Midterm", 0);
        assertNotEquals(a, b);
        assertFalse(a.equals(b));
    }

    @Test
    void equalsWhenNullReturnsFalse() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertFalse(series.equals(null));
    }

    @Test
    void equalsWhenNotSeriesReturnsFalse() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertFalse(series.equals("midterm-1"));
    }

    @Test
    void equalsReflexive() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertTrue(series.equals(series));
    }

    @Test
    void hashCodeConsistentWithEquals() {
        Series a = new Series("midterm-1", "CSE 110", "Midterm", 0);
        Series b = new Series("midterm-1", "CSE 101", "Different", 1);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsIdAndName() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        String s = series.toString();
        assertTrue(s.contains("midterm-1"));
        assertTrue(s.contains("Midterm"));
        assertTrue(s.contains("CSE 110"));
    }

    @Test
    void constructorThrowsWhenIdNull() {
        assertThrows(NullPointerException.class, () -> new Series(null, "CSE 110", "Midterm", 0));
    }

    @Test
    void constructorThrowsWhenCourseIdNull() {
        assertThrows(NullPointerException.class, () -> new Series("midterm-1", null, "Midterm", 0));
    }

    @Test
    void constructorThrowsWhenNameNull() {
        assertThrows(NullPointerException.class, () -> new Series("midterm-1", "CSE 110", null, 0));
    }

    @Test
    void constructorThrowsWhenDefaultLateDaysNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Series("midterm-1", "CSE 110", "Midterm", -1));
    }

    @Test
    void constructorAcceptsZeroDefaultLateDays() {
        Series series = new Series("midterm-1", "CSE 110", "Midterm", 0);
        assertEquals(0, series.getDefaultLateDays());
    }
}
