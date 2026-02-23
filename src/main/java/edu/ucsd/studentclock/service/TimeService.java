package edu.ucsd.studentclock.service;

import java.time.*;
import java.util.Objects;

/**
 * Single source of truth for "current time" across the app.
 * Supports switching between real time and mock time for demos/tests.
 */
public final class TimeService {

    private Clock clock;            // the active clock (real or mock)
    private boolean usingMock;      // mode flag
    private Instant lastInstant;    // monotonic guard (never go backwards)

    public TimeService() {
        this.clock = Clock.systemDefaultZone();
        this.usingMock = false;
        this.lastInstant = clock.instant();
    }

    public boolean isUsingMock() {
        return usingMock;
    }

    /** Returns "now" based on the current mode. */
    public LocalDateTime now() {
        Instant current = clock.instant();
        // guard monotonic time
        if (current.isBefore(lastInstant)) {
            // refuse to go backwards
            current = lastInstant;
        } else {
            lastInstant = current;
        }
        return LocalDateTime.ofInstant(current, clock.getZone());
    }

    public Instant nowInstant() {
        Instant current = clock.instant();

        if (current.isBefore(lastInstant)) {
            current = lastInstant;
        } else {
            lastInstant = current;
        }
        return current;
    }

    /** Switch to real time (system clock). */
    public void useRealTime() {
        this.clock = Clock.systemDefaultZone();
        this.usingMock = false;
        this.lastInstant = clock.instant();
    }

    /** Switch to mock time. Starts mock at the current real time. */
    public void useMockTime() {
        Instant start = Clock.systemDefaultZone().instant();
        ZoneId zone = ZoneId.systemDefault();
        this.clock = new MutableClock(start, zone);
        this.usingMock = true;
        this.lastInstant = start;
    }

    /** Set mock time (only allowed when in mock mode). Never moves backwards. */
    public void setMockDateTime(LocalDate date, LocalTime time) {
        if (!usingMock) {
            throw new IllegalStateException("Not using mock time");
        }
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(time, "time must not be null");

        MutableClock mc = (MutableClock) clock;

        ZonedDateTime zdt = ZonedDateTime.of(date, time, mc.getZone());
        Instant target = zdt.toInstant();

        // Enforce "never backwards"
        if (target.isBefore(lastInstant)) {
            // do nothing or clamp to lastInstant (clamp is less annoying in demos)
            target = lastInstant;
        }

        mc.setInstant(target);
        lastInstant = target;
    }

    /** Expose the zone if needed by UI formatting. */
    public ZoneId getZone() {
        return clock.getZone();
    }
}