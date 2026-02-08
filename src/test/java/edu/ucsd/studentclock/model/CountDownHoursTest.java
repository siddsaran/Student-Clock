package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CountDownHoursTest {
    
    @Test
    void sameness(){
        CountDownHours sameHours = new CountDownHours(6, 6);
        assertTrue(sameHours.equals(0));

    }

    @Test
    void totalDaysLarger(){
        CountDownHours totalLarger = new CountDownHours(1, 0);
        assertTrue(totalLarger.equals(1));

    }

    @Test
    void currentDaysLarger(){
        CountDownHours currentLarge = new CountDownHours(0,16);
        assertTrue(currentLarge.equals("Current day must not exceed the total days of studying."));
    }

    @Test
    void totalDaysNegative(){
        CountDownHours totalNegative = new CountDownHours(2, -2);
        assertTrue(totalNegative.equals("Please put the total number of days that's zero or larger."));
    }


}
