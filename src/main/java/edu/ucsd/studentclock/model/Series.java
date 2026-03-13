package edu.ucsd.studentclock.model;

import java.util.Objects;

/**
 * A named group of related assignments under a course (e.g. "Midterm", "PAs").
 * Assignments reference a series by seriesId; Series does not hold the list of
 * assignments.
 */
public final class Series {

    private final String id;
    private final String courseId;
    private final String name;
    private final int defaultLateDays;

    public Series(String id, String courseId, String name, int defaultLateDays) {
        if (id == null) {
            throw new NullPointerException("id must not be null");
        }
        if (courseId == null) {
            throw new NullPointerException("courseId must not be null");
        }
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (defaultLateDays < 0) {
            throw new IllegalArgumentException("defaultLateDays must be >= 0");
        }

        this.id = id;
        this.courseId = courseId;
        this.name = name;
        this.defaultLateDays = defaultLateDays;
    }

    public String getId() {
        return id;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public int getDefaultLateDays() {
        return defaultLateDays;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Series series = (Series) object;
        return Objects.equals(id, series.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Series{id='" + id
                + "', courseId='" + courseId
                + "', name='" + name
                + "', defaultLateDays=" + defaultLateDays
                + "}";
    }
}