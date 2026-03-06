package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * An assignment entry for a class. Each assignment will have an arbitrary id and be linked
 * to a course. Multiple assignment objects can link to a single course. An assignment links
 * to just ONE course
 */
public class Assignment {

    // unique id to an assignment, name of the assignment, and course
    private final String id, name, courseID;

    // number of late days
    private final int lateDaysAllowed;

    // Start date and deadline
    private final LocalDateTime start, deadline;
    private final String id;
    private final String name;
    private final String courseId;
    private final String seriesId;
    private final int lateDaysAllowed;

    private final LocalDateTime start;
    private final LocalDateTime deadline;

    private final double estimatedHours;
    private double remainingHours;
    private boolean done;
    private double cumulativeHours;

    public Assignment(
            String name,
            String courseId,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours
    ) {
        this(name, courseId, null, start, deadline, lateDaysAllowed, estimatedHours);
    }

    public Assignment(
            String name,
            String courseId,
            String seriesId,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours
    ) {
        if (name == null) {
            throw new NullPointerException("assignment must have a name");
        }
        if (courseId == null) {
            throw new NullPointerException("must have a course");
        }
        if (start == null) {
            throw new NullPointerException("must have a start date");
        }
        if (deadline == null) {
            throw new NullPointerException("must have a deadline");
        }
        if (deadline.isBefore(start)) {
            throw new IllegalArgumentException("deadline must not be before start");
        }
        if (lateDaysAllowed < 0) {
            throw new IllegalArgumentException("late days allowed must be >= 0");
        }
        if (estimatedHours < 0) {
            throw new IllegalArgumentException("estimated hours must be >= 0");
        }

        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.courseId = courseId;
        this.seriesId = seriesId;
        this.start = start;
        this.deadline = deadline;
        this.lateDaysAllowed = lateDaysAllowed;
        this.estimatedHours = estimatedHours;
        this.remainingHours = estimatedHours;
        this.cumulativeHours = 0.0;
        this.done = false;
    }

    private Assignment(
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
            boolean done
    ) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
        this.seriesId = seriesId;
        this.start = start;
        this.deadline = deadline;
        this.lateDaysAllowed = lateDaysAllowed;
        this.estimatedHours = estimatedHours;
        this.remainingHours = remainingHours;
        this.cumulativeHours = cumulativeHours;
        this.done = done;
    }

    public static Assignment fromDatabase(
            String id,
            String name,
            String courseId,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours,
            double remainingHours,
            double cumulativeHours,
            boolean done
    ) {
        return fromDatabase(
                id,
                name,
                courseId,
                null,
                start,
                deadline,
                lateDaysAllowed,
                estimatedHours,
                remainingHours,
                cumulativeHours,
                done
        );
    }

    public static Assignment fromDatabase(
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
            boolean done
    ) {
        return new Assignment(
                id,
                name,
                courseId,
                seriesId,
                start,
                deadline,
                lateDaysAllowed,
                estimatedHours,
                remainingHours,
                cumulativeHours,
                done
        );
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

    public void setRemainingHours(double remainingHours) {
        this.remainingHours = Math.max(0.0, remainingHours);
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public double getCumulativeHours() {
        return cumulativeHours;
    }

    public void markDone() {
        this.done = true;
        this.remainingHours = 0.0;
    }

    public void applyWork(double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }
        this.cumulativeHours += hours;
        this.remainingHours = Math.max(0.0, this.remainingHours - hours);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Assignment that = (Assignment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + courseId + ")"
                + " | Estimated: " + estimatedHours
                + " | Remaining: " + remainingHours
                + " | Hours Worked: " + cumulativeHours;
    }

    public String toFullString() {
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