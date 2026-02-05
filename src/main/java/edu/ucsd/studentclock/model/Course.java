package edu.ucsd.studentclock.model;

import java.util.Objects;

/**
 * An enrolled course (e.g. CSE 110). Assignments and series reference a course by its id;
 * the Model/Repository hold assignments that reference this Course. Dashboard and Big Picture
 * use Course for grouping and display.
 */
public final class Course {

    // unique identifier
    private final String id;

    // display label
    private final String name;

    public Course(String id, String name) {
        if (id == null) {
            throw new NullPointerException("id must not be null");
        }
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Course{id='" + id + "', name='" + name + "'}";
    }
}
