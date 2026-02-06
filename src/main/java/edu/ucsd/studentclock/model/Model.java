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

    public Optional<Course> getCourse(String id) {
        return repository.getCourse(id);
    }

    public List<Course> getAllCourses() {
        return repository.getAllCourses();
    }
}