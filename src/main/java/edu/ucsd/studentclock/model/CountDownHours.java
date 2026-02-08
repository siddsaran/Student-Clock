package edu.ucsd.studentclock.model;

/*Write a logic that subtracts elapsed days/hours form the
weekly total to compute remaining study time.*/

import java.util.Objects;

public final class CountDownHours { //note that we can't tell if this is automatic or manual input
    final int totalDays;
    final int currentDay;

    public CountDownHours(int totalDays, int currentDay){
        if(totalDays < 0){
            throw new IllegalArgumentException("Please put the total number of days that's zero or larger.");
        }
        if(currentDay > totalDays){
            throw new IllegalArgumentException("Current day must not exceed the total days of studying.");
        }
        this.totalDays = totalDays;
        this.currentDay = currentDay;
    }
    
    public int findChange(int totalDays, int currentDay){
        int daysRemain = totalDays - currentDay;
        return daysRemain;
    }
}
