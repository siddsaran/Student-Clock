package edu.ucsd.studentclock.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Abstraction for current time and mock-time support.
 */
public interface ITimeService {

    boolean isUsingMock();

    LocalDateTime now();

    Instant nowInstant();

    void useRealTime();

    void useMockTime();

    void setMockDateTime(LocalDate date, LocalTime time);

    ZoneId getZone();
}
