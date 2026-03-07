package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {

    private Assignment makeAssignment(double estimatedHours) {
        return new AssignmentBuilder()
                .setName("Quiz 2 Study")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(estimatedHours)
                .build();
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
        Assignment assignment = new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setSeriesId("pa-series-1")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(3.0)
                .build();
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
        assertEquals(4.5, assignment.getEstimatedHours());
        assertEquals(4.5, assignment.getRemainingHours());
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
    void constructorThrowsWhenNameNull() {
        assertThrows(NullPointerException.class, () -> {
            new AssignmentBuilder()
                    .setName(null)
                    .setCourseId("CSE 110")
                    .setStart(LocalDateTime.now())
                    .setDeadline(LocalDateTime.now().plusDays(1))
                    .setLateDaysAllowed(0)
                    .setEstimatedHours(3.0)
                    .build();
        });
    }

    @Test
    void constructorThrowsWhenDeadlineBeforeStart() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AssignmentBuilder()
                    .setName("Quiz 2")
                    .setCourseId("CSE 110")
                    .setStart(LocalDateTime.now())
                    .setDeadline(LocalDateTime.now().minusDays(1))
                    .setLateDaysAllowed(0)
                    .setEstimatedHours(3.0)
                    .build();
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
        assertEquals(0.75, a.getCumulativeHours());
        assertEquals(1.25, a.getRemainingHours());

        a.applyWork(5.0);
        assertEquals(5.75, a.getCumulativeHours());
        assertEquals(0.0, a.getRemainingHours());
        assertFalse(a.isDone());
    }

    @Test
    void setRemainingHoursClampsToZero() {
        Assignment a = makeAssignment(2.0);
        a.setRemainingHours(-123.0);
        assertEquals(0.0, a.getRemainingHours());
    }

    @Test
    void markDoneSetsDoneAndZerosRemainingButDoesNotChangeCumulative() {
        Assignment a = makeAssignment(4.0);
        a.applyWork(1.5);

        a.markDone();
        assertTrue(a.isDone());
        assertEquals(0.0, a.getRemainingHours());
        assertEquals(1.5, a.getCumulativeHours());
    }

    @Test
    void fromDatabaseKeepsProvidedFields() {
        LocalDateTime start = LocalDateTime.of(2026, 2, 1, 9, 0);
        LocalDateTime deadline = LocalDateTime.of(2026, 2, 5, 23, 59);
        Assignment a = new AssignmentBuilder()
                        .setId("id-123")
                        .setName("PA1")
                        .setCourseId("CSE 110")
                        .setSeriesId("series-1")
                        .setStart(start)
                        .setDeadline(deadline)
                        .setLateDaysAllowed(3)
                        .setEstimatedHours(10)
                        .setRemainingHours(4)
                        .setCumulativeHours(6)
                        .setDone(true)
                        .build();

        assertEquals("id-123", a.getId());
        assertEquals("PA1", a.getName());
        assertEquals("CSE 110", a.getCourseId());
        assertEquals("series-1", a.getSeriesId());
        assertEquals(start, a.getStart());
        assertEquals(deadline, a.getDeadline());
        assertEquals(3, a.getLateDaysAllowed());
        assertEquals(10.0, a.getEstimatedHours());
        assertEquals(4.0, a.getRemainingHours());
        assertEquals(6.0, a.getCumulativeHours());
        assertTrue(a.isDone());
    }
}