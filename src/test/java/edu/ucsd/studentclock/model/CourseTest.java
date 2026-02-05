package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void getIdReturnsConstructorId() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertEquals("CSE 110", course.getId());
    }

    @Test
    void getNameReturnsConstructorName() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertEquals("Software Engineering", course.getName());
    }

    @Test
    void equalsWhenSameIdReturnsTrue() {
        Course a = new Course("CSE 110", "Software Engineering");
        Course b = new Course("CSE 110", "Different Name");
        assertEquals(a, b);
        assertTrue(a.equals(b));
    }

    @Test
    void equalsWhenDifferentIdReturnsFalse() {
        Course a = new Course("CSE 110", "Software Engineering");
        Course b = new Course("CSE 101", "Software Engineering");
        assertNotEquals(a, b);
        assertFalse(a.equals(b));
    }

    @Test
    void equalsWhenNullReturnsFalse() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertFalse(course.equals(null));
    }

    @Test
    void equalsWhenNotCourseReturnsFalse() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertFalse(course.equals("CSE 110"));
    }

    @Test
    void equalsReflexive() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertTrue(course.equals(course));
    }

    @Test
    void hashCodeConsistentWithEquals() {
        Course a = new Course("CSE 110", "Software Engineering");
        Course b = new Course("CSE 110", "Different Name");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toStringContainsIdAndName() {
        Course course = new Course("CSE 110", "Software Engineering");
        String s = course.toString();
        assertTrue(s.contains("CSE 110"));
        assertTrue(s.contains("Software Engineering"));
    }

    @Test
    void constructorThrowsWhenIdNull() {
        assertThrows(NullPointerException.class, () -> new Course(null, "Software Engineering"));
    }

    @Test
    void constructorThrowsWhenNameNull() {
        assertThrows(NullPointerException.class, () -> new Course("CSE 110", null));
    }

    @Test
    void constructorThrowsWhenBothNull() {
        assertThrows(NullPointerException.class, () -> new Course(null, null));
    }
}
