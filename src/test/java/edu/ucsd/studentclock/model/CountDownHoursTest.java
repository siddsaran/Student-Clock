package edu.ucsd.studentclock.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CountDownHoursTest {
    
    @Test
    void sameVal(){
        CountDownHours countDownHours = new CountDownHours(3, 3);
        assertEquals(0, countDownHours.hoursLeft(3,3));
    }

    @Test
    void currentLarger(){
        try{
            CountDownHours countDownHours = new CountDownHours(3, 0);
        }
        catch(Exception e){
                    assertEquals("The current hour must not exceed the total time requested.", e.getMessage());

        }
    }

    @Test
    void normal(){
        CountDownHours countDownHours = new CountDownHours(3, 6);
        assertEquals(3, countDownHours.hoursLeft(3,6));
    }

    @Test
    void lessThanZer0(){
        try{
            CountDownHours countDownHours = new CountDownHours(-1, -1);
        }
        catch(Exception e){
                    assertEquals("Please provide values greater than 0.", e.getMessage());

        }
    }
}
