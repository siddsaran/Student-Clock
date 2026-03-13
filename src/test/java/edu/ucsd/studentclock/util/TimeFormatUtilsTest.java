package edu.ucsd.studentclock.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TimeFormatUtils")
class TimeFormatUtilsTest {

    @Test
    void formatHoursAsHHMM_integerHours() {
        assertEquals("02:00", TimeFormatUtils.formatHoursAsHHMM(2.0));
        assertEquals("12:00", TimeFormatUtils.formatHoursAsHHMM(12.0));
        assertEquals("00:00", TimeFormatUtils.formatHoursAsHHMM(0.0));
    }

    @Test
    void formatHoursAsHHMM_fractionalHours() {
        assertEquals("02:30", TimeFormatUtils.formatHoursAsHHMM(2.5));
        assertEquals("01:30", TimeFormatUtils.formatHoursAsHHMM(1.5));
        assertEquals("00:45", TimeFormatUtils.formatHoursAsHHMM(0.75));
    }

    @Test
    void formatHoursAsHHMM_roundsCorrectly() {
        assertEquals("02:30", TimeFormatUtils.formatHoursAsHHMM(2.5));
        assertEquals("01:29", TimeFormatUtils.formatHoursAsHHMM(1.49));
        assertEquals("01:30", TimeFormatUtils.formatHoursAsHHMM(1.5));
    }

    @Test
    void formatHoursAsHHMM_negativeClampsToZero() {
        assertEquals("00:00", TimeFormatUtils.formatHoursAsHHMM(-1.5));
        assertEquals("00:00", TimeFormatUtils.formatHoursAsHHMM(-0.1));
    }

    @Test
    void formatHoursAsHHMM_padsWithLeadingZeros() {
        assertEquals("00:05", TimeFormatUtils.formatHoursAsHHMM(5.0 / 60));
        assertEquals("09:00", TimeFormatUtils.formatHoursAsHHMM(9.0));
    }
}
