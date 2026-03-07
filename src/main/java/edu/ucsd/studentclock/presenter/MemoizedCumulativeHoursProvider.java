package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class MemoizedCumulativeHoursProvider implements CumulativeHoursProvider {

    private final CumulativeHoursProvider delegate;
    private final Map<LocalDate, Map<String, Double>> cache = new HashMap<>();

    public MemoizedCumulativeHoursProvider(CumulativeHoursProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Map<String, Double> getCumulativeHoursByEndOf(LocalDate day) {
        return cache.computeIfAbsent(day, delegate::getCumulativeHoursByEndOf);
    }
}