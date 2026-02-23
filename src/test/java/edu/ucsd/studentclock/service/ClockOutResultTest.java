package edu.ucsd.studentclock.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClockOutResultTest {

    @Test
    void gettersReturnConstructorValues() {
        ClockOutResult r = new ClockOutResult(
                "a-123",
                1.5,
                4.0,
                2.5,
                false
        );

        assertEquals("a-123", r.getAssignmentId());
        assertEquals(1.5, r.getSessionHours(), 1e-9);
        assertEquals(4.0, r.getCumulativeHours(), 1e-9);
        assertEquals(2.5, r.getRemainingHours(), 1e-9);
        assertFalse(r.isDone());
    }

    @Test
    void doneTrueReturnedWhenSet() {
        ClockOutResult r = new ClockOutResult(
                "a-999",
                0.25,
                10.0,
                0.0,
                true
        );

        assertTrue(r.isDone());
        assertEquals(0.0, r.getRemainingHours(), 1e-9);
    }
}