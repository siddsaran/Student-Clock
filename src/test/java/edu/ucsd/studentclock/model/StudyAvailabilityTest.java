package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 2: Unit tests for domain and model logic (StudyAvailability).
 * MS1: US5 (Define weekly study availability).
 */
@DisplayName("DS7-2: StudyAvailability")
class StudyAvailabilityTest {

    private StudyAvailability availability;

    @BeforeEach
    void setUp() {
        availability = new StudyAvailability();
    }

    @Test
    @DisplayName("Default constructor initializes zero weekly hours with no available days")
    void defaultConstructorInitializesToZeroAndNoAvailability() {
        assertEquals(0, availability.getTotalWeeklyHours());
        for (DayOfWeek d : DayOfWeek.values()) {
            assertFalse(availability.isAvailable(d));
            assertEquals(0, availability.getDailyLimit(d));
        }
        assertEquals(0, availability.getTotalAllocatedHours());
        assertEquals(0, availability.getUnallocatedHours());
        assertNull(availability.validate());
    }

    @Test
    @DisplayName("Setting total weekly hours stores the value and updates unallocated hours")
    void setTotalWeeklyHoursStoresValue() {
        availability.setTotalWeeklyHours(8);
        assertEquals(8, availability.getTotalWeeklyHours());
        assertEquals(8, availability.getUnallocatedHours());
        assertNull(availability.validate());
    }

    @Test
    @DisplayName("Setting negative total weekly hours throws IllegalArgumentException")
    void setTotalWeeklyHoursNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> availability.setTotalWeeklyHours(-1));
    }

    @Test
    @DisplayName("Marking a day available sets availability without changing its daily limit")
    void setAvailableTrueMarksDayAvailable() {
        availability.setAvailable(DayOfWeek.MONDAY, true);
        assertTrue(availability.isAvailable(DayOfWeek.MONDAY));
        assertEquals(0, availability.getDailyLimit(DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("Marking a day unavailable clears its daily limit")
    void setAvailableFalseClearsDailyLimit() {
        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setDailyLimit(DayOfWeek.MONDAY, 3);
        assertEquals(3, availability.getDailyLimit(DayOfWeek.MONDAY));

        availability.setAvailable(DayOfWeek.MONDAY, false);
        assertFalse(availability.isAvailable(DayOfWeek.MONDAY));
        assertEquals(0, availability.getDailyLimit(DayOfWeek.MONDAY));
    }

    @Test
    @DisplayName("Setting a daily limit on an available day stores the limit")
    void setDailyLimitOnAvailableDayStoresHours() {
        availability.setAvailable(DayOfWeek.TUESDAY, true);
        availability.setDailyLimit(DayOfWeek.TUESDAY, 2);
        assertEquals(2, availability.getDailyLimit(DayOfWeek.TUESDAY));
    }

    @Test
    @DisplayName("Setting a negative daily limit throws IllegalArgumentException")
    void setDailyLimitNegativeThrows() {
        availability.setAvailable(DayOfWeek.WEDNESDAY, true);
        assertThrows(IllegalArgumentException.class,
                () -> availability.setDailyLimit(DayOfWeek.WEDNESDAY, -5));
    }

    @Test
    @DisplayName("Setting a positive daily limit on an unavailable day throws IllegalStateException")
    void setDailyLimitOnUnavailableDayWithPositiveHoursThrows() {
        assertFalse(availability.isAvailable(DayOfWeek.THURSDAY));
        assertThrows(IllegalStateException.class,
                () -> availability.setDailyLimit(DayOfWeek.THURSDAY, 1));
    }

    @Test
    @DisplayName("Total allocated hours sums limits across available days")
    void totalAllocatedHoursSumsAcrossDays() {
        availability.setTotalWeeklyHours(10);

        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setAvailable(DayOfWeek.WEDNESDAY, true);

        availability.setDailyLimit(DayOfWeek.MONDAY, 3);
        availability.setDailyLimit(DayOfWeek.WEDNESDAY, 4);

        assertEquals(7, availability.getTotalAllocatedHours());
        assertEquals(3, availability.getUnallocatedHours());
        assertNull(availability.validate());
    }

    @Test
    @DisplayName("Validation returns an error when allocated hours exceed total weekly hours")
    void validateReturnsErrorWhenAllocatedExceedsWeekly() {
        availability.setTotalWeeklyHours(5);

        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setAvailable(DayOfWeek.TUESDAY, true);

        availability.setDailyLimit(DayOfWeek.MONDAY, 3);
        availability.setDailyLimit(DayOfWeek.TUESDAY, 3);

        assertEquals(6, availability.getTotalAllocatedHours());
        assertNotNull(availability.validate());
        assertEquals("Allocated hours exceed total weekly hours.", availability.validate());
    }

    @Test
    @DisplayName("Remaining weekly hours is zero when consumed hours equal total hours")
    void remainingHoursForWeek_consumedEqualsTotal_returnsZero() {
        availability.setTotalWeeklyHours(5);
        assertEquals(0, availability.getRemainingHoursForWeek(5));
    }

    @Test
    @DisplayName("Remaining weekly hours is zero when consumed hours exceed total hours")
    void remainingHoursForWeek_consumedExceedsTotal_returnsZero() {
        availability.setTotalWeeklyHours(5);
        assertEquals(0, availability.getRemainingHoursForWeek(7));
    }

    @Test
    @DisplayName("Remaining weekly hours returns the difference when consumed is below total")
    void remainingHoursForWeek_consumedLessThanTotal_returnsRemaining() {
        availability.setTotalWeeklyHours(5);
        assertEquals(2, availability.getRemainingHoursForWeek(3));
    }
}