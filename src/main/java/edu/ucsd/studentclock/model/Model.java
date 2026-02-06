package edu.ucsd.studentclock.model;

import edu.ucsd.studentclock.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

public class Model {

    private final CourseRepository repository;

    public Model(CourseRepository repository) {
        if (repository == null) {
            throw new NullPointerException("repository must not be null");
        }
        this.repository = repository;
    }

    public void addCourse(Course course) {
        repository.addCourse(course);
    }

    /**
     * Takes user input (id and name), trims whitespace, validates, creates a Course, and adds it to storage.
     * Rejects null id or name (NPE from Course) and blank-after-trim id or name (IAE).
     *
     * @param id   course id (trimmed; must not be null or blank)
     * @param name course display name (trimmed; must not be null or blank)
     * @throws NullPointerException     if id or name is null (from Course)
     * @throws IllegalArgumentException if id or name is blank after trimming
     */
    public void addCourse(String id, String name) {
        String trimmedId = id != null ? id.trim() : null;
        String trimmedName = name != null ? name.trim() : null;
        if (trimmedId != null && trimmedId.isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (trimmedName != null && trimmedName.isEmpty()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        addCourse(new Course(trimmedId, trimmedName));
    }

    public Optional<Course> getCourse(String id) {
        return repository.getCourse(id);
    }

    public List<Course> getAllCourses() {
        return repository.getAllCourses();
    }
}