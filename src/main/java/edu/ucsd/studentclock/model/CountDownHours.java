package edu.ucsd.studentclock.model;

import java.util.Objects;

public class CountDownHours{
    private int currentHour;
    private int totalHours;
    
    public CountDownHours(Integer currentHour, Integer totalHours){
        if((currentHour == null) || (totalHours == null)){
            throw new IllegalArgumentException("Please input current hour and/or total hours.");

        }
        if((currentHour < 0) || (totalHours < 0)){
            throw new IllegalArgumentException("Please provide values greater than 0.");
        }
        if(currentHour > totalHours){
            throw new IllegalArgumentException("The current hour must not exceed the total time requested.");
        }
        this.currentHour = currentHour;
        this.totalHours = totalHours;
    }

    public int hoursLeft(int currentHour, int totalHours){
        int remain = totalHours - currentHour;
        return remain;
    }
}

/*Write logic that subtracts elapsed days/hours from the 
weekly total to compute remaining study time.*/
