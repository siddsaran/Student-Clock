package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNotNull(assignment.getId());
    }

    @Test
    void getIdReturnsDifferentIdsForDifferentAssignments() {
        Assignment a = makeAssignment(3.0);
        Assignment b = makeAssignment(3.0);
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void getNameReturnsConstructorName() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals("Quiz 2 Study", assignment.getName());
    }

    @Test
    void getCourseIdReturnsConstructorCourseId() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals("CSE 110", assignment.getCourseId());
    }

    @Test
    void getSeriesIdReturnsNullWhenCreatedWithoutSeries() {
        Assignment assignment = makeAssignment(3.0);
        assertNull(assignment.getSeriesId());
    }

    @Test
    void getSeriesIdReturnsSeriesIdWhenCreatedWithSeries() {
        Assignment assignment = new Assignment(
                "PA1",
                "CSE 110",
                "pa-series-1",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 5, 23, 59),
                0,
                3.0
        );
        assertEquals("pa-series-1", assignment.getSeriesId());
    }

    @Test
    void getStartReturnsConstructorStart() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(
                LocalDateTime.of(2026, 2, 1, 9, 0),
                assignment.getStart()
        );
    }

    @Test
    void getDeadlineReturnsConstructorDeadline() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(
                LocalDateTime.of(2026, 2, 5, 23, 59),
                assignment.getDeadline()
        );
    }

    @Test
    void remainingHoursInitializesToEstimatedHours() {
        Assignment assignment = makeAssignment(4.5);
        assertEquals(4.5, assignment.getEstimatedHours(), 1e-9);
        assertEquals(4.5, assignment.getRemainingHours(), 1e-9);
    }

    @Test
    void assignmentIsNotDoneInitially() {
        Assignment assignment = makeAssignment(3.0);
        assertFalse(assignment.isDone());
    }

    @Test
    void equalsReflexive() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(assignment, assignment);
    }

    @Test
    void equalsWhenNullReturnsFalse() {
        Assignment assignment = makeAssignment(3.0);
        assertFalse(assignment.equals(null));
    }

    @Test
    void equalsWhenDifferentTypeReturnsFalse() {
        Assignment assignment = makeAssignment(3.0);
        assertFalse(assignment.equals("Quiz 2 Study"));
    }

    @Test
    void equalsWhenDifferentIdsReturnsFalse() {
        Assignment a = makeAssignment(3.0);
        Assignment b = makeAssignment(3.0);
        assertNotEquals(a, b);
    }

    @Test
    void hashCodeConsistentWithEqualsReflexive() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(
                assignment.hashCode(),
                assignment.hashCode()
        );
    }

    @Test
    void toStringContainsEstimateAndRemaining() {
        Assignment assignment = makeAssignment(5.0);
        String s = assignment.toString();
        assertTrue(s.contains("Estimated: 5.0"));
        assertTrue(s.contains("Remaining: 5.0"));
    }

    @Test
    void constructorThrowsWhenNameNull() {
        assertThrows(NullPointerException.class, () -> {
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
        assertThrows(IllegalArgumentException.class, () -> {
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
        assertThrows(IllegalArgumentException.class, () -> {
            makeAssignment(-2.0);
        });
    }

    @Test
    void applyWorkNegativeThrows() {
        Assignment a = makeAssignment(3.0);
        assertThrows(IllegalArgumentException.class, () -> a.applyWork(-0.01));
    }

    @Test
    void applyWorkAccumulatesAndClampsRemainingToZero() {
        Assignment a = makeAssignment(2.0);

        a.applyWork(0.75);
        assertEquals(0.75, a.getCumulativeHours(), 1e-9);
        assertEquals(1.25, a.getRemainingHours(), 1e-9);

        a.applyWork(5.0);
        assertEquals(5.75, a.getCumulativeHours(), 1e-9);
        assertEquals(0.0, a.getRemainingHours(), 1e-9);
        assertFalse(a.isDone());
    }

    @Test
    void setRemainingHoursClampsToZero() {
        Assignment a = makeAssignment(2.0);
        a.setRemainingHours(-123.0);
        assertEquals(0.0, a.getRemainingHours(), 1e-9);
    }

    @Test
    void markDoneSetsDoneAndZerosRemainingButDoesNotChangeCumulative() {
        Assignment a = makeAssignment(4.0);
        a.applyWork(1.5);

        a.markDone();
        assertTrue(a.isDone());
        assertEquals(0.0, a.getRemainingHours(), 1e-9);
        assertEquals(1.5, a.getCumulativeHours(), 1e-9);
    }

    @Test
    void fromDatabaseKeepsProvidedFields() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);

        Assignment a = Assignment.fromDatabase(
                "id-123",
                "PA1",
                "CSE 110",
                "series-1",
                start,
                deadline,
                3,
                10.0,
                4.0,
                6.0,
                true
        );

        assertEquals("id-123", a.getId());
        assertEquals("PA1", a.getName());
        assertEquals("CSE 110", a.getCourseId());
        assertEquals("series-1", a.getSeriesId());
        assertEquals(start, a.getStart());
        assertEquals(deadline, a.getDeadline());
        assertEquals(3, a.getLateDaysAllowed());
        assertEquals(10.0, a.getEstimatedHours(), 1e-9);
        assertEquals(4.0, a.getRemainingHours(), 1e-9);
        assertEquals(6.0, a.getCumulativeHours(), 1e-9);
        assertTrue(a.isDone());
    }
}
