package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Assignment")
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
    @DisplayName("New assignment generates a non-null unique ID")
    void getIdReturnsNonNullId() {
        Assignment assignment = makeAssignment(3.0);
        assertNotNull(assignment.getId());
    }

    @Test
    @DisplayName("Different assignments generate different IDs")
    void getIdReturnsDifferentIdsForDifferentAssignments() {
        Assignment a = makeAssignment(3.0);
        Assignment b = makeAssignment(3.0);
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    @DisplayName("getName returns the name provided during construction")
    void getNameReturnsConstructorName() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals("Quiz 2 Study", assignment.getName());
    }

    @Test
    @DisplayName("getCourseId returns the course ID provided during construction")
    void getCourseIdReturnsConstructorCourseId() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals("CSE 110", assignment.getCourseId());
    }

    @Test
    @DisplayName("Series ID is null when assignment is created without a series")
    void getSeriesIdReturnsNullWhenCreatedWithoutSeries() {
        Assignment assignment = makeAssignment(3.0);
        assertNull(assignment.getSeriesId());
    }

    @Test
    @DisplayName("Series ID is preserved when assignment is created with a series")
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
    @DisplayName("Start time is stored correctly")
    void getStartReturnsConstructorStart() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(
                LocalDateTime.of(2026, 2, 1, 9, 0),
                assignment.getStart());
    }

    @Test
    @DisplayName("Deadline time is stored correctly")
    void getDeadlineReturnsConstructorDeadline() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(
                LocalDateTime.of(2026, 2, 5, 23, 59),
                assignment.getDeadline());
    }

    @Test
    @DisplayName("Remaining hours initially equal estimated hours")
    void remainingHoursInitializesToEstimatedHours() {
        Assignment assignment = makeAssignment(4.5);
        assertEquals(4.5, assignment.getEstimatedHours());
        assertEquals(4.5, assignment.getRemainingHours());
    }

    @Test
    @DisplayName("Assignment is not marked done when first created")
    void assignmentIsNotDoneInitially() {
        Assignment assignment = makeAssignment(3.0);
        assertFalse(assignment.isDone());
    }

    @Test
    @DisplayName("equals is reflexive")
    void equalsReflexive() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(assignment, assignment);
    }

    @Test
    @DisplayName("equals returns false when compared with null")
    void equalsWhenNullReturnsFalse() {
        Assignment assignment = makeAssignment(3.0);
        assertFalse(assignment.equals(null));
    }

    @Test
    @DisplayName("equals returns false when compared with different type")
    void equalsWhenDifferentTypeReturnsFalse() {
        Assignment assignment = makeAssignment(3.0);
        assertFalse(assignment.equals("Quiz 2 Study"));
    }

    @Test
    @DisplayName("Assignments with different IDs are not equal")
    void equalsWhenDifferentIdsReturnsFalse() {
        Assignment a = makeAssignment(3.0);
        Assignment b = makeAssignment(3.0);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("hashCode is consistent for the same object")
    void hashCodeConsistentWithEqualsReflexive() {
        Assignment assignment = makeAssignment(3.0);
        assertEquals(
                assignment.hashCode(),
                assignment.hashCode());
    }

    @Test
    @DisplayName("Constructor throws NullPointerException when name is null")
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
    @DisplayName("Constructor throws IllegalArgumentException when deadline is before start")
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
    @DisplayName("Constructor throws IllegalArgumentException when estimated hours are negative")
    void constructorThrowsWhenEstimatedHoursNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            makeAssignment(-2.0);
        });
    }

    @Test
    @DisplayName("applyWork throws exception when hours are negative")
    void applyWorkNegativeThrows() {
        Assignment a = makeAssignment(3.0);
        assertThrows(IllegalArgumentException.class, () -> a.applyWork(-0.01));
    }

    @Test
    @DisplayName("applyWork accumulates work and clamps remaining hours to zero")
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
    @DisplayName("setRemainingHours clamps negative values to zero")
    void setRemainingHoursClampsToZero() {
        Assignment a = makeAssignment(2.0);
        a.setRemainingHours(-123.0);
        assertEquals(0.0, a.getRemainingHours());
    }

    @Test
    @DisplayName("markDone sets assignment as done and clears remaining hours")
    void markDoneSetsDoneAndZerosRemainingButDoesNotChangeCumulative() {
        Assignment a = makeAssignment(4.0);
        a.applyWork(1.5);

        a.markDone();
        assertTrue(a.isDone());
        assertEquals(0.0, a.getRemainingHours());
        assertEquals(1.5, a.getCumulativeHours());
    }

    @Test
    @DisplayName("Assignments loaded from database preserve all stored fields")
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