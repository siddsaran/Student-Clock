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

    private final ICourseRepository courseRepository;
    private final ISeriesRepository seriesRepository;
    private final IAssignmentRepository assignmentRepository;
    private final IStudyAvailabilityRepository studyAvailabilityRepository;
    private final edu.ucsd.studentclock.repository.WorkLogRepository workLogRepository;
    private final edu.ucsd.studentclock.repository.AssignmentWorkLogRepository assignmentWorkLogRepository;
    private final ITimeService timeService;
    private final edu.ucsd.studentclock.service.WorkSessionService workSessionService;

    private final StudyAvailability studyAvailability;
    private Assignment selectedAssignment;

    /**
     * Creates a Model with the given repositories and time service.
     *
     * @param courseRepository course repository
     * @param assignmentRepository assignment repository
     * @param seriesRepository series repository
     * @param studyAvailabilityRepository study availability repository
     * @param workLogRepository work log repository
     * @param assignmentWorkLogRepository assignment work log repository
     * @param timeService time service (must not be null)
     */
    public Model(
            ICourseRepository courseRepository,
            IAssignmentRepository assignmentRepository,
            ISeriesRepository seriesRepository,
            IStudyAvailabilityRepository studyAvailabilityRepository,
            edu.ucsd.studentclock.repository.WorkLogRepository workLogRepository,
            edu.ucsd.studentclock.repository.AssignmentWorkLogRepository assignmentWorkLogRepository,
            ITimeService timeService
    ) {
        if (courseRepository == null) {
            throw new NullPointerException("courseRepository must not be null");
        }
        if (assignmentRepository == null) {
            throw new NullPointerException("assignmentRepository must not be null");
        }
        if (seriesRepository == null) {
            throw new NullPointerException("seriesRepository must not be null");
        }
        if (studyAvailabilityRepository == null) {
            throw new NullPointerException("studyAvailabilityRepository must not be null");
        }
        if (workLogRepository == null) {
            throw new NullPointerException("workLogRepository must not be null");
        }
        if (assignmentWorkLogRepository == null) {
            throw new NullPointerException("assignmentWorkLogRepository must not be null");
        }
        if (timeService == null) {
            throw new NullPointerException("timeService must not be null");
        }

        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.seriesRepository = seriesRepository;
        this.studyAvailabilityRepository = studyAvailabilityRepository;
        this.workLogRepository = workLogRepository;
        this.assignmentWorkLogRepository = assignmentWorkLogRepository;
        this.timeService = timeService;
        this.workSessionService = new edu.ucsd.studentclock.service.WorkSessionService(
                timeService, workLogRepository, assignmentWorkLogRepository, assignmentRepository
        );

        this.studyAvailability = studyAvailabilityRepository.load().orElseGet(StudyAvailability::new);
    }

    public ITimeService getTimeService() {
        return timeService;
    }

    public void addCourse(Course course) {
        courseRepository.addCourse(course);
    }

    /**
     * Takes user input (id and name), trims whitespace, validates, creates a Course, and adds it to storage.
     * Rejects null id or name (NPE from Course) and blank-after-trim id or name (IAE).
     *
     * @param id course id (trimmed; must not be null or blank)
     * @param name course display name (trimmed; must not be null or blank)
     * @throws NullPointerException if id or name is null (from Course)
     * @throws IllegalArgumentException if id or name is blank after trimming
     */
    public void addCourse(String id, String name) {
        String trimmedId = ValidationUtils.requireNonBlank(id, "id must not be blank");
        String trimmedName = ValidationUtils.requireNonBlank(name, "name must not be blank");
        addCourse(new Course(trimmedId, trimmedName));
    }

    public Optional<Course> getCourse(String id) {
        return courseRepository.getCourse(id);
    }

    public List<Course> getAllCourses() {
        return courseRepository.getAllCourses();
    }

    public void createSeries(String id, String courseId, String name, int defaultLateDays) {
        Series series = new Series(id, courseId, name, defaultLateDays);
        seriesRepository.addSeries(series);
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
     * @param seriesId series id
     * @param courseId course id
     * @param name series name
     * @param defaultLateDays default late days
     * @param assignmentIds selected assignment ids to link
     */
    public void createSeriesAndLinkAssignments(String seriesId, String courseId, String name, int defaultLateDays, List<String> assignmentIds) {
        Series series = new Series(seriesId, courseId, name, defaultLateDays);
        seriesRepository.addSeries(series);
        assignmentRepository.setSeriesForAssignments(
                series.getId(),
                series.getDefaultLateDays(),
                assignmentIds
        );
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
        String trimmedId = ValidationUtils.normalizeNullable(id);
        if (trimmedId == null) {
            return;
        }

        assignmentRepository.deleteAssignmentsForCourse(trimmedId);
        courseRepository.deleteCourse(trimmedId);
    }

    public void setSelectedAssignment(Assignment assignment) {
        this.selectedAssignment = assignment;
    }

    public Assignment getSelectedAssignment() {
        return selectedAssignment;
    }

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.getAllAssignments();
    }

    public void createAssignment(String name, String courseId, String seriesId, java.time.LocalDateTime start, java.time.LocalDateTime deadline, int lateDaysAllowed, double estimatedHours) {
        Assignment assignment = new AssignmentBuilder()
                .setName(name)
                .setCourseId(courseId)
                .setSeriesId(seriesId)
                .setStart(start)
                .setDeadline(deadline)
                .setLateDaysAllowed(lateDaysAllowed)
                .setEstimatedHours(estimatedHours)
                .build();
        assignmentRepository.addAssignment(assignment);
    }

    public void saveStudyAvailability() {
        studyAvailabilityRepository.save(studyAvailability);
    }

    public void setTotalWeeklyHours(int hours) {
        studyAvailability.setTotalWeeklyHours(hours);
        saveStudyAvailability();
    }

    public void setAvailable(DayOfWeek day, boolean available) {
        studyAvailability.setAvailable(day, available);
        saveStudyAvailability();
    }

    public void setDailyLimit(DayOfWeek day, int hours) {
        studyAvailability.setDailyLimit(day, hours);
        saveStudyAvailability();
    }

    public void deleteAssignment(String id) {
        assignmentRepository.deleteAssignment(id);
    }

    public void clockIn(String id) {
        workSessionService.clockIn(findAssignmentById(id));
    }

    public edu.ucsd.studentclock.service.ClockOutResult clockOut(String id) {
        return workSessionService.clockOut(id);
    }

    public void applyManualHours(String id, double hours) {
        workSessionService.applyManualHours(findAssignmentById(id), hours);
    }

    public void markDone(String id) {
        Assignment assignment = findAssignmentById(id);
        assignment.markDone();
        assignmentRepository.addAssignment(assignment);
    }

    public double getTotalHoursLoggedInWeek(java.time.LocalDate date) {
        return workLogRepository.getTotalHoursLoggedInWeek(date);
    }

    public java.util.Map<String, Double> getCumulativeHoursByEndOf(java.time.LocalDate date) {
        return assignmentWorkLogRepository.getCumulativeHoursByEndOf(date);
    }

    public boolean isTracking() {
        return workSessionService.isTracking();
    }

    private Assignment findAssignmentById(String id) {
        String trimmedId = ValidationUtils.requireNonBlank(id, "Assignment ID is required");
        return assignmentRepository.getAllAssignments().stream()
                .filter(assignment -> assignment.getId().equals(trimmedId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + trimmedId));
    }
}