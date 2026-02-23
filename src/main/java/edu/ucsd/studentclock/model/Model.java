package edu.ucsd.studentclock.model;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.repository.ICourseRepository;
import edu.ucsd.studentclock.repository.ISeriesRepository;
import edu.ucsd.studentclock.repository.IStudyAvailabilityRepository;
import edu.ucsd.studentclock.service.ITimeService;
import edu.ucsd.studentclock.util.ValidationUtils;


public class Model {

    private final ICourseRepository repository;
    private final ISeriesRepository seriesRepository;
    private final StudyAvailability studyAvailability;
    private final IAssignmentRepository aRepository;
    private Assignment selectedAssignment;
    private final IStudyAvailabilityRepository saRepository;
    private final ITimeService timeService;

    /**
     * Creates a Model with the given repositories and time service.
     *
     * @param repository course repository
     * @param aRepository assignment repository
     * @param seriesRepository series repository
     * @param saRepository study availability repository
     * @param timeService time service (must not be null)
     */
    public Model(ICourseRepository repository, IAssignmentRepository aRepository, ISeriesRepository seriesRepository, IStudyAvailabilityRepository saRepository, ITimeService timeService) {
        if (repository == null) {
            throw new NullPointerException("repository must not be null");
        }
        if (seriesRepository == null) {
            throw new NullPointerException("seriesRepository must not be null");
        }
        if (aRepository == null) {
            throw new NullPointerException("assignmentRepository must not be null");
        }
        if (saRepository == null) {
            throw new NullPointerException("studyAvailabilityRepository must not be null");
        }
        if (timeService == null) {
            throw new NullPointerException("timeService must not be null");
        }

        this.repository = repository;
        this.seriesRepository = seriesRepository;
        this.aRepository = aRepository;
        this.saRepository = saRepository;
        this.timeService = timeService;

        this.studyAvailability = saRepository.load().orElseGet(StudyAvailability::new);
    }

    public void addCourse(Course course) {
        repository.addCourse(course);
    }
    
    public ITimeService getTimeService() {
        return timeService;
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
        String trimmedId = ValidationUtils.requireNonBlank(id, "id must not be blank");
        String trimmedName = ValidationUtils.requireNonBlank(name, "name must not be blank");
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
    public void setSelectedAssignment(Assignment assignment) {
        this.selectedAssignment = assignment;
    }
    public Assignment getSelectedAssignment() {
        return selectedAssignment;
    }

    public List<Assignment> getAllAssignments() {
        return aRepository.getAllAssignments();
    }

    /** Persist current in-memory availability to the DB. */
    public void saveStudyAvailability() {
        saRepository.save(studyAvailability);
    }

    /** Updates weekly hours and persists immediately. */
    public void setTotalWeeklyHours(int hours) {
        studyAvailability.setTotalWeeklyHours(hours);
        saveStudyAvailability();
    }

    /** Updates availability for a day and persists immediately. */
    public void setAvailable(DayOfWeek day, boolean available) {
        studyAvailability.setAvailable(day, available);
        saveStudyAvailability();
    }

    /** Updates daily limit and persists immediately. */
    public void setDailyLimit(DayOfWeek day, int hours) {
        studyAvailability.setDailyLimit(day, hours);
        saveStudyAvailability();
    }
}