package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

class StudyAvailabilityTest {

    private StudyAvailability availability;

    @BeforeEach
    void setUp() {
        availability = new StudyAvailability();
    }

    @Test
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
    void setTotalWeeklyHoursStoresValue() {
        availability.setTotalWeeklyHours(8);
        assertEquals(8, availability.getTotalWeeklyHours());
        assertEquals(8, availability.getUnallocatedHours());
        assertNull(availability.validate());
    }

    @Test
    void setTotalWeeklyHoursNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> availability.setTotalWeeklyHours(-1));
    }

    @Test
    void setAvailableTrueMarksDayAvailable() {
        availability.setAvailable(DayOfWeek.MONDAY, true);
        assertTrue(availability.isAvailable(DayOfWeek.MONDAY));
        assertEquals(0, availability.getDailyLimit(DayOfWeek.MONDAY));
    }

    @Test
    void setAvailableFalseClearsDailyLimit() {
        availability.setAvailable(DayOfWeek.MONDAY, true);
        availability.setDailyLimit(DayOfWeek.MONDAY, 3);
        assertEquals(3, availability.getDailyLimit(DayOfWeek.MONDAY));

        availability.setAvailable(DayOfWeek.MONDAY, false);
        assertFalse(availability.isAvailable(DayOfWeek.MONDAY));
        assertEquals(0, availability.getDailyLimit(DayOfWeek.MONDAY));
    }

    @Test
    void setDailyLimitOnAvailableDayStoresHours() {
        availability.setAvailable(DayOfWeek.TUESDAY, true);
        availability.setDailyLimit(DayOfWeek.TUESDAY, 2);
        assertEquals(2, availability.getDailyLimit(DayOfWeek.TUESDAY));
    }

    @Test
    void setDailyLimitNegativeThrows() {
        availability.setAvailable(DayOfWeek.WEDNESDAY, true);
        assertThrows(IllegalArgumentException.class,
                () -> availability.setDailyLimit(DayOfWeek.WEDNESDAY, -5));
    }

    @Test
    void setDailyLimitOnUnavailableDayWithPositiveHoursThrows() {
        assertFalse(availability.isAvailable(DayOfWeek.THURSDAY));
        assertThrows(IllegalStateException.class,
                () -> availability.setDailyLimit(DayOfWeek.THURSDAY, 1));
    }

    @Test
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

    //Test for countdown hours go here.
    
    @Test
    void CountDownSameVal(){
        availability.setTotalWeeklyHours(5);
        int remains = availability.hoursLeft(5);
        assertEquals(0, availability.hoursLeft(5));
    }

    @Test
    void CountDownCurrentLarger(){
        availability.setTotalWeeklyHours(5);
        try{
            int remains = availability.hoursLeft(7);
        }
        catch(Exception e){
                assertEquals("Current hour must not exceed the total weekly hours.", e.getMessage());

        }
    }

    @Test
    void CountDownNormal(){
        availability.setTotalWeeklyHours(5);
        int remains = availability.hoursLeft(3);
        assertEquals(2, availability.hoursLeft(3));
    }

}