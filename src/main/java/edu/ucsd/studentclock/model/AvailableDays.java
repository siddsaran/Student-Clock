package edu.ucsd.studentclock.model;

import java.util.Objects;

public final class AvailableDays {

    // number of study days
    final String dayofWeek;

    public AvailableDays(String dayofWeek){
        if(dayofWeek == null){
            throw new NullPointerException("Please put the day of the week that you're available.");
        }
        String[] day = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        for(int i = 0; i <= day.length; i++){
            if(dayofWeek != day[i]){
                throw new IllegalArgumentException("Invalid day of the week.");
            }
        }
        this.dayofWeek = dayofWeek;
    }

    public String[] daysSelect(String dayofWeek){
       String[] daysAvailable = new String[7];
       for(int j = 0; j <= daysAvailable.length; j++){
        if(daysAvailable[j] == null){
            daysAvailable[j] = dayofWeek;
            break;
        }
       }
       return daysAvailable;
    }

    
}


/* Allow the user to select which days of the week they can study.
 */