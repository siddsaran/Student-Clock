package edu.ucsd.studentclock.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssignmentBuilder {

    private String name;
    private String courseId;
    private LocalDateTime start;
    private LocalDateTime deadline;
    private int lateDaysAllowed;
    private double estimatedHours;
    private String id = null;
    private String seriesId = null;
    private Double remainingHours = null;
    private double cumulativeHours = 0.0;
    private boolean done = false;

    public AssignmentBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public AssignmentBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public AssignmentBuilder setCourseId(String courseId) {
        this.courseId = courseId;
        return this;
    }

    public AssignmentBuilder setSeriesId(String seriesId) {
        this.seriesId = seriesId;
        return this;
    }

    public AssignmentBuilder setStart(LocalDateTime start) {
        this.start = start;
        return this;
    }

    public AssignmentBuilder setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        return this;
    }

    public AssignmentBuilder setLateDaysAllowed(int lateDaysAllowed) {
        this.lateDaysAllowed = lateDaysAllowed;
        return this;
    }

    public AssignmentBuilder setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
        return this;
    }

    public AssignmentBuilder setRemainingHours(double remainingHours) {
        this.remainingHours = remainingHours;
        return this;
    }

    public AssignmentBuilder setCumulativeHours(double cumulativeHours) {
        this.cumulativeHours = cumulativeHours;
        return this;
    }

    public AssignmentBuilder setDone(boolean done) {
        this.done = done;
        return this;
    }

    /**
     * Validates all required fields and returns a new Assignment
     *
     * @throws NullPointerException     if any required field is null
     * @throws IllegalArgumentException if any field value is invalid
     */
    public Assignment build() {
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

        String resolvedId = (id != null) ? id : UUID.randomUUID().toString();
        double resolvedRemainingHours = (remainingHours != null) ? remainingHours : estimatedHours;

        return new Assignment(
                resolvedId,
                name,
                courseId,
                seriesId,
                start,
                deadline,
                lateDaysAllowed,
                estimatedHours,
                resolvedRemainingHours,
                cumulativeHours,
                done);
    }
}