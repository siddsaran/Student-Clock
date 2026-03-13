package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Story DS7, Task 2: Unit tests for domain and model logic (TimeFormatUtils).
 * MS1: Supports US4, US7 (work estimates and time tracking display).
 */
@DisplayName("DS7-2: TimeFormatUtils")
class TimeFormatUtilsTest {

    @Test
    @DisplayName("Integer hour values format as HH:MM with zero minutes")
    void formatHoursAsHHMM_integerHours() {
        assertEquals("02:00", TimeFormatUtils.formatHoursAsHHMM(2.0));
        assertEquals("12:00", TimeFormatUtils.formatHoursAsHHMM(12.0));
        assertEquals("00:00", TimeFormatUtils.formatHoursAsHHMM(0.0));
    }

    @Test
    @DisplayName("Fractional hours convert correctly to minutes")
    void formatHoursAsHHMM_fractionalHours() {
        assertEquals("02:30", TimeFormatUtils.formatHoursAsHHMM(2.5));
        assertEquals("01:30", TimeFormatUtils.formatHoursAsHHMM(1.5));
        assertEquals("00:45", TimeFormatUtils.formatHoursAsHHMM(0.75));
    }

    @Test
    @DisplayName("Hour values are rounded to the nearest minute")
    void formatHoursAsHHMM_roundsCorrectly() {
        assertEquals("02:30", TimeFormatUtils.formatHoursAsHHMM(2.5));
        assertEquals("01:29", TimeFormatUtils.formatHoursAsHHMM(1.49));
        assertEquals("01:30", TimeFormatUtils.formatHoursAsHHMM(1.5));
    }

    @Test
    @DisplayName("Negative hour values clamp to 00:00")
    void formatHoursAsHHMM_negativeClampsToZero() {
        assertEquals("00:00", TimeFormatUtils.formatHoursAsHHMM(-1.5));
        assertEquals("00:00", TimeFormatUtils.formatHoursAsHHMM(-0.1));
    }

    @Test
    @DisplayName("Formatted time pads hours and minutes with leading zeros")
    void formatHoursAsHHMM_padsWithLeadingZeros() {
        assertEquals("00:05", TimeFormatUtils.formatHoursAsHHMM(5.0 / 60));
        assertEquals("09:00", TimeFormatUtils.formatHoursAsHHMM(9.0));
    }
}