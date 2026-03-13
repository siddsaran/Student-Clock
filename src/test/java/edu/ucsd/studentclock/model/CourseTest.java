package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Course")
class CourseTest {

    @Test
    @DisplayName("getId returns the ID provided in the constructor")
    void getIdReturnsConstructorId() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertEquals("CSE 110", course.getId());
    }

    @Test
    @DisplayName("getName returns the name provided in the constructor")
    void getNameReturnsConstructorName() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertEquals("Software Engineering", course.getName());
    }

    @Test
    @DisplayName("Courses with the same ID are equal even if names differ")
    void equalsWhenSameIdReturnsTrue() {
        Course a = new Course("CSE 110", "Software Engineering");
        Course b = new Course("CSE 110", "Different Name");
        assertEquals(a, b);
        assertTrue(a.equals(b));
    }

    @Test
    @DisplayName("Courses with different IDs are not equal")
    void equalsWhenDifferentIdReturnsFalse() {
        Course a = new Course("CSE 110", "Software Engineering");
        Course b = new Course("CSE 101", "Software Engineering");
        assertNotEquals(a, b);
        assertFalse(a.equals(b));
    }

    @Test
    @DisplayName("equals returns false when compared with null")
    void equalsWhenNullReturnsFalse() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertFalse(course.equals(null));
    }

    @Test
    @DisplayName("equals returns false when compared with a non-Course object")
    void equalsWhenNotCourseReturnsFalse() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertFalse(course.equals("CSE 110"));
    }

    @Test
    @DisplayName("equals is reflexive")
    void equalsReflexive() {
        Course course = new Course("CSE 110", "Software Engineering");
        assertTrue(course.equals(course));
    }

    @Test
    @DisplayName("hashCode is consistent for courses with the same ID")
    void hashCodeConsistentWithEquals() {
        Course a = new Course("CSE 110", "Software Engineering");
        Course b = new Course("CSE 110", "Different Name");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("toString includes both course ID and name")
    void toStringContainsIdAndName() {
        Course course = new Course("CSE 110", "Software Engineering");
        String s = course.toString();
        assertTrue(s.contains("CSE 110"));
        assertTrue(s.contains("Software Engineering"));
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when ID is null")
    void constructorThrowsWhenIdNull() {
        assertThrows(NullPointerException.class,
                () -> new Course(null, "Software Engineering"));
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when name is null")
    void constructorThrowsWhenNameNull() {
        assertThrows(NullPointerException.class,
                () -> new Course("CSE 110", null));
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when both ID and name are null")
    void constructorThrowsWhenBothNull() {
        assertThrows(NullPointerException.class,
                () -> new Course(null, null));
    }
}