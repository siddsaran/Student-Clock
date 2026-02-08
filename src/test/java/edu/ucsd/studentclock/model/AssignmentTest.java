package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AssignmentTest {

    private Assignment makeAssignment(double estimatedHours) {
        return new Assignment(
                "Quiz 2 Study",
                "CSE 110",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 5, 23, 59),
                0,
                estimatedHours
        );
    }

    @Test
    void getIdReturnsNonNullId() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertNotNull(assignment.getID());
    }

    @Test
    void getIdReturnsDifferentIdsForDifferentAssignments() {
        Assignment a = makeAssignment(3.0);
        Assignment b = makeAssignment(3.0);
        Assertions.assertNotEquals(a.getID(), b.getID());
    }

    @Test
    void getNameReturnsConstructorName() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertEquals("Quiz 2 Study", assignment.getName());
    }

    @Test
    void getCourseIdReturnsConstructorCourseId() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertEquals("CSE 110", assignment.getCourseID());
    }

    @Test
    void getStartReturnsConstructorStart() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertEquals(
                LocalDateTime.of(2026, 2, 1, 9, 0),
                assignment.getStart()
        );
    }

    @Test
    void getDeadlineReturnsConstructorDeadline() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertEquals(
                LocalDateTime.of(2026, 2, 5, 23, 59),
                assignment.getDeadline()
        );
    }

    @Test
    void remainingHoursInitializesToEstimatedHours() {
        Assignment assignment = makeAssignment(4.5);
        Assertions.assertEquals(4.5, assignment.getEstimatedHours(), 1e-9);
        Assertions.assertEquals(4.5, assignment.getRemainingHours(), 1e-9);
    }

    @Test
    void assignmentIsNotDoneInitially() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertFalse(assignment.isDone());
    }

    @Test
    void equalsReflexive() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertEquals(assignment, assignment);
    }

    @Test
    void equalsWhenNullReturnsFalse() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertFalse(assignment.equals(null));
    }

    @Test
    void equalsWhenDifferentTypeReturnsFalse() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertFalse(assignment.equals("Quiz 2 Study"));
    }

    @Test
    void equalsWhenDifferentIdsReturnsFalse() {
        Assignment a = makeAssignment(3.0);
        Assignment b = makeAssignment(3.0);
        Assertions.assertNotEquals(a, b);
    }

    @Test
    void hashCodeConsistentWithEqualsReflexive() {
        Assignment assignment = makeAssignment(3.0);
        Assertions.assertEquals(
                assignment.hashCode(),
                assignment.hashCode()
        );
    }

    @Test
    void toStringContainsNameAndCourseId() {
        Assignment assignment = makeAssignment(3.0);
        String s = assignment.toString();
        Assertions.assertTrue(s.contains("Quiz 2 Study"));
        Assertions.assertTrue(s.contains("CSE 110"));
    }

    // US4-specific coverage
    @Test
    void toStringContainsEstimateAndRemaining() {
        Assignment assignment = makeAssignment(5.0);
        String s = assignment.toString();
        Assertions.assertTrue(s.contains("estimatedHours=5.0"));
        Assertions.assertTrue(s.contains("remainingHours=5.0"));
    }

    @Test
    void constructorThrowsWhenNameNull() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new Assignment(
                    null,
                    "CSE 110",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1),
                    0,
                    3.0
            );
        });
    }

    @Test
    void constructorThrowsWhenDeadlineBeforeStart() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Assignment(
                    "Quiz 2",
                    "CSE 110",
                    LocalDateTime.now(),
                    LocalDateTime.now().minusDays(1),
                    0,
                    3.0
            );
        });
    }

    @Test
    void constructorThrowsWhenEstimatedHoursNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            makeAssignment(-2.0);
        });
    }
}
