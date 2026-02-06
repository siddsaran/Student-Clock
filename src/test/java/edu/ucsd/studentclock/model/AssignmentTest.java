package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class AssignmentTest {
    //Sample assignment for testing
    private Assignment createTestAssignment(double hours) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime deadline = start.plusDays(2);

        return new Assignment("Hw 1", "CSE 110", 
                              start, deadline, 0, hours);
    }   
    
    @Test
    void getEstimatedHoursReturnsConstructorValue() {
        Assignment assignment = createTestAssignment(5.0);
        assertEquals(5.0, assignment.getEstimatedHours());
    }

    @Test
    void remainingHoursInitializedFromEstimate() {
        Assignment assignment = createTestAssignment(3.5);
        assertEquals(3.5, assignment.getRemainingHours());
    }

    @Test
    void toStringContainsEstimateAndRemaining() {
        Assignment assignment = createTestAssignment(5.0);
        String s = assignment.toString();
        assertTrue(s.contains("estimatedHours=5.0"));
        assertTrue(s.contains("remainingHours=5.0"));
    }

    @Test
    void isDoneInitiallyFalse() {
        Assignment assignment = createTestAssignment(2.0);
        assertFalse(assignment.isDone());
    }

    @Test
    void constructorThrowsWhenEstimatedHoursNegative() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime deadline = start.plusDays(1);

        assertThrows(IllegalArgumentException.class, () ->
            new Assignment("HW1","CSE 110",
                        start,deadline,0, -1.0));
    }
}
