package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.util.Map;

public interface CumulativeHoursProvider {
    Map<String, Double> getCumulativeHoursByEndOf(LocalDate day);
}