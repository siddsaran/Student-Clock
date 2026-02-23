package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.Course;

import java.util.List;
import java.util.Optional;

/**
 * Abstraction for course persistence.
 */
public interface ICourseRepository {

    void addCourse(Course course);

    Optional<Course> getCourse(String id);

    List<Course> getAllCourses();

    void deleteCourse(String id);
}
