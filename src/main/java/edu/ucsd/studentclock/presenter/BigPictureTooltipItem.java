package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;

public final class BigPictureTooltipItem {

    private final String name;
    private final String courseId;
    private final LocalDate dueDate;
    private final double estimatedHours;
    private final double completedHours;
    private final double remainingHours;
    private final boolean done;

    public BigPictureTooltipItem(
            String name,
            String courseId,
            LocalDate dueDate,
            double estimatedHours,
            double completedHours,
            double remainingHours,
            boolean done
    ) {
        this.name = name;
        this.courseId = courseId;
        this.dueDate = dueDate;
        this.estimatedHours = estimatedHours;
        this.completedHours = completedHours;
        this.remainingHours = remainingHours;
        this.done = done;
    }

    public String getName() { return name; }
    public String getCourseId() { return courseId; }
    public LocalDate getDueDate() { return dueDate; }
    public double getEstimatedHours() { return estimatedHours; }
    public double getCompletedHours() { return completedHours; }
    public double getRemainingHours() { return remainingHours; }
    public boolean isDone() { return done; }
}