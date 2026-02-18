package edu.ucsd.studentclock.model;

import java.util.List;
import java.util.Optional;

import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;

public class Model {

    private final CourseRepository repository;
    private final SeriesRepository seriesRepository;
    private final StudyAvailability studyAvailability = new StudyAvailability();
    private final AssignmentRepository aRepository;
  
    public Model(CourseRepository repository, AssignmentRepository aRepository, SeriesRepository seriesRepository) {
        if (repository == null) {
            throw new NullPointerException("repository must not be null");
        }
        if (seriesRepository == null) {
            throw new NullPointerException("seriesRepository must not be null");
        }
        if (aRepository == null) {
            throw new NullPointerException("assignmentRepository must not be null");
        }
        this.repository = repository;
        this.seriesRepository = seriesRepository;
        this.aRepository = aRepository;
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
        String trimmedId = null;
        String trimmedName = null;
        if (id != null) {
            trimmedId = id.trim();
        }
        if (name != null) {
            trimmedName = name.trim();
        }
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

    public void addSeries(Series series) {
        seriesRepository.addSeries(series);
    }

    public Optional<Series> getSeries(String id) {
        return seriesRepository.getSeries(id);
    }

    public List<Series> getSeriesByCourse(String courseId) {
        return seriesRepository.getSeriesByCourse(courseId);
    }

    /**
     * Creates a series and links selected existing assignments to it.
     *
     * @param series series to create
     * @param assignmentIds selected assignment ids to link
     */
    public void createSeriesAndLinkAssignments(Series series, List<String> assignmentIds) {
        seriesRepository.addSeries(series);
        aRepository.setSeriesForAssignments(series.getId(), series.getDefaultLateDays(), assignmentIds);
    }

    public StudyAvailability getStudyAvailability() {
        return studyAvailability;
    }

    /**
     * Removes the course with the given id. No-op if id is null or blank after trim.
     *
     * @param id the course id to delete
     */
    public void deleteCourse(String id) {
        if (id == null) {
            return;
        }
        String trimmedId = id.trim();
        if (trimmedId.isEmpty()) {
            return;
        }
        aRepository.deleteAssignmentsForCourse(trimmedId);
        repository.deleteCourse(trimmedId);
    }
}