package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An assignment entry for a class. Each assignment will have an arbitrary id
 * and be linked
 * to a course. Multiple assignment objects can link to a single course. An
 * assignment links
 * to just ONE course.
 *
 */
public class Assignment {

    private static final double MIN_HOURS = 0.0;
    private static final String NEGATIVE_HOURS_MESSAGE = "Hours cannot be negative";

    private final String id;
    private final String name;
    private final String courseId;
    private final String seriesId;
    private final int lateDaysAllowed;

    // Start date and deadline
    private final LocalDateTime start, deadline;
    private final double estimatedHours;
    private double remainingHours;
    private boolean done;
    private double cumulativeHours;

    public Assignment(
            String id,
            String name,
            String courseId,
            String seriesId,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours,
            double remainingHours,
            double cumulativeHours,
            boolean done) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
        this.seriesId = seriesId;
        this.start = start;
        this.deadline = deadline;
        this.lateDaysAllowed = lateDaysAllowed;
        this.estimatedHours = estimatedHours;
        this.remainingHours = clampHoursAtZero(remainingHours);
        this.cumulativeHours = cumulativeHours;
        this.done = done;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public int getLateDaysAllowed() {
        return lateDaysAllowed;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public double getRemainingHours() {
        return remainingHours;
    }

    public boolean isDone() {
        return done;
    }

    public double getCumulativeHours() {
        return cumulativeHours;
    }

    public void setRemainingHours(double remainingHours) {
        this.remainingHours = clampHoursAtZero(remainingHours);
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public void markDone() {
        this.done = true;
        this.remainingHours = MIN_HOURS;
    }

    public void applyWork(double hours) {
        requireNonNegativeHours(hours);
        this.cumulativeHours += hours;
        this.remainingHours = clampHoursAtZero(this.remainingHours - hours);
    }

    private static void requireNonNegativeHours(double hours) {
        if (hours < MIN_HOURS) {
            throw new IllegalArgumentException(NEGATIVE_HOURS_MESSAGE);
        }
    }

    private static double clampHoursAtZero(double hours) {
        return Math.max(MIN_HOURS, hours);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return Objects.equals(id, ((Assignment) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Assignment{"
                + "id='" + id + '\''
                + ", courseId='" + courseId + '\''
                + ", name='" + name + '\''
                + ", start=" + start
                + ", deadline=" + deadline
                + ", lateDaysAllowed=" + lateDaysAllowed
                + ", estimatedHours=" + estimatedHours
                + ", remainingHours=" + remainingHours
                + ", done=" + done
                + '}';
    }
}