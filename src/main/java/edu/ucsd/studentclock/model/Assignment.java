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

    // unique id to an assignment
    private final String id;

    // name of assignment
    private final String name;

    // course
    private final String courseID;

    // optional series (null = not in any series)
    private final String seriesId;

    // number of late days
    private final int lateDaysAllowed;

    // Start date and deadline
    private final LocalDateTime start;
    private final LocalDateTime deadline;

    // planning hours and progress
    private final double estimatedHours;
    private double remainingHours;
    private boolean done;

    public Assignment(
            String name,
            String courseID,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours
    ) {
        this(name, courseID, null, start, deadline, lateDaysAllowed, estimatedHours);
    }

    public Assignment(
            String name,
            String courseID,
            String seriesId,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours
    ) {
        if (name == null) {
            throw new NullPointerException("assignment must have a name");
        }
        if (courseID == null) {
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

        // random unique id for each assignment
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.courseID = courseID;
        this.seriesId = seriesId;
        this.start = start;
        this.deadline = deadline;
        this.lateDaysAllowed = lateDaysAllowed;
        this.estimatedHours = estimatedHours;
        this.remainingHours = estimatedHours;
        this.done = false;
    }

    Assignment(
            String id,
            String name,
            String courseID,
            String seriesId,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours,
            double remainingHours,
            boolean done
    ) {
        this.id = id;
        this.name = name;
        this.courseID = courseID;
        this.seriesId = seriesId;
        this.start = start;
        this.deadline = deadline;
        this.lateDaysAllowed = lateDaysAllowed;
        this.estimatedHours = estimatedHours;
        this.remainingHours = remainingHours;
        this.done = done;
    }

    public static Assignment fromDatabase(
        String id,
        String name,
        String courseID,
        LocalDateTime start,
        LocalDateTime deadline,
        int lateDaysAllowed,
        double estimatedHours,
        double remainingHours,
        boolean done
    ) {
        return fromDatabase(id, name, courseID, null, start, deadline, lateDaysAllowed, estimatedHours, remainingHours, done);
    }

    public static Assignment fromDatabase(
            String id,
            String name,
            String courseID,
            String seriesId,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDaysAllowed,
            double estimatedHours,
            double remainingHours,
            boolean done
    ) {
        return new Assignment(
                id,
                name,
                courseID,
                seriesId,
                start,
                deadline,
                lateDaysAllowed,
                estimatedHours,
                remainingHours,
                done
        );
    }


    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCourseID() {
        return courseID;
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
        this.remainingHours = Math.max(0, remainingHours);
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null || getClass() != o.getClass())) {
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
        return name + " (" + courseID + ")"
                + " | Estimated: " + estimatedHours
                + " | Remaining: " + remainingHours;
    }

    public String toFullString() {
        return "Assignment{" +
                "id='" + id + '\'' +
                ", courseId=" + courseID + '\'' +
                ", name='" + name + '\'' +
                ", start=" + start +
                ", deadline=" + deadline +
                ", lateDaysAllowed=" + lateDaysAllowed +
                ", estimatedHours=" + estimatedHours +
                ", remainingHours=" + remainingHours +
                ", done=" + done +
                '}';
    }


}
