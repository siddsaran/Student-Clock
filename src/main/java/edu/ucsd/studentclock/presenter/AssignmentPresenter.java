package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import edu.ucsd.studentclock.service.ClockOutResult;
import edu.ucsd.studentclock.service.ITimeService;
import edu.ucsd.studentclock.service.TimeTrackingManager;
import edu.ucsd.studentclock.util.ValidationUtils;
import edu.ucsd.studentclock.view.AssignmentListEntry;
import edu.ucsd.studentclock.view.AssignmentView;

/**
 * Presenter for the Assignment screen.
 * Handles user actions and coordinates between AssignmentView and the assignment repository.
 */
public class AssignmentPresenter extends AbstractPresenter<AssignmentView> implements IAssignmentScreenPresenter {

    private final IAssignmentRepository assignmentRepository;
    private final WorkLogRepository workLogRepository;
    private final AssignmentWorkLogRepository assignmentWorkLogRepository;
    private final TimeTrackingManager timeTrackingManager;
    private final ITimeService timeService;

    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;
    private Runnable onBigPicture;

    private String courseFilter = AssignmentView.ALL_COURSES;
    private boolean showOnlyOpen = false;

    /**
     * Creates an AssignmentPresenter.
     *
     * @param model shared application model
     * @param view assignment view
     * @param assignmentRepository assignment repository
     * @param workLogRepository work log repository
     * @param assignmentWorkLogRepository assignment work log repository
     */
    public AssignmentPresenter(
            Model model,
            AssignmentView view,
            IAssignmentRepository assignmentRepository,
            WorkLogRepository workLogRepository,
            AssignmentWorkLogRepository assignmentWorkLogRepository
    ) {
        super(model, view);
        this.assignmentRepository = assignmentRepository;
        this.workLogRepository = workLogRepository;
        this.assignmentWorkLogRepository = assignmentWorkLogRepository;

        this.timeService = model.getTimeService();
        this.timeTrackingManager = new TimeTrackingManager(this.timeService);

        view.setPresenter(this);
        view.getCoursesButton().setOnAction(event -> runIfSet(onCourses));
        view.getStudyAvailabilityButton().setOnAction(event -> runIfSet(onStudyAvailability));
        view.getDashboardButton().setOnAction(event -> runIfSet(onDashboard));
        view.getBigPictureButton().setOnAction(event -> runIfSet(onBigPicture));

        updateView();
    }

    @Override
    public String getViewTitle() {
        return "Assignments";
    }

    @Override
    public void updateView() {
        List<String> courseIds = model.getAllCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        view.setCourses(courseIds);
        view.setSelectedCourse(courseFilter);

        List<AssignmentListEntry> groupedAssignments = AssignmentListGrouper.buildGroupedList(
                assignmentRepository.getAllAssignments(),
                showOnlyOpen,
                courseFilter,
                AssignmentView.ALL_COURSES,
                model
        );
        view.showGroupedAssignments(groupedAssignments);

        Assignment selectedAssignment = model.getSelectedAssignment();
        if (selectedAssignment != null) {
            view.selectAssignment(selectedAssignment);
        }
    }

    public void createAssignment(
            String name,
            String course,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDays,
            double estimate
    ) {
        createAssignment(name, course, start, deadline, lateDays, estimate, null);
    }

    public void createAssignment(
            String name,
            String course,
            LocalDateTime start,
            LocalDateTime deadline,
            int lateDays,
            double estimate,
            String seriesId
    ) {
        int effectiveLateDays = lateDays;
        String effectiveSeriesId = (seriesId == null || seriesId.isBlank()) ? null : seriesId.trim();

        if (effectiveSeriesId != null) {
            Series series = model.getSeries(effectiveSeriesId)
                    .orElseThrow(() -> new IllegalArgumentException("Series not found: " + effectiveSeriesId));
            effectiveLateDays = series.getDefaultLateDays();
        }

        Assignment assignment = new Assignment(
                name,
                course,
                effectiveSeriesId,
                start,
                deadline,
                effectiveLateDays,
                estimate
        );
        assignmentRepository.addAssignment(assignment);
        updateView();
    }

    public void createSeries(
            String seriesId,
            String courseId,
            String seriesName,
            int defaultLateDays
    ) {
        String trimmedId = ValidationUtils.requireNonBlank(seriesId, "Series ID is required");
        String trimmedName = ValidationUtils.requireNonBlank(seriesName, "Series name is required");

        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course is required");
        }
        if (defaultLateDays < 0) {
            throw new IllegalArgumentException("Default late days must be >= 0");
        }

        Series series = new Series(trimmedId, courseId, trimmedName, defaultLateDays);
        model.addSeries(series);
    }

    public List<Series> getSeriesForCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            return List.of();
        }
        return model.getSeriesByCourse(courseId);
    }

    public void setCourseFilter(String courseIdOrAllCourses) {
        courseFilter = (courseIdOrAllCourses == null) ? AssignmentView.ALL_COURSES : courseIdOrAllCourses;
        updateView();
    }

    public void deleteAssignment(Assignment assignment) {
        if (assignment == null) {
            return;
        }
        assignmentRepository.deleteAssignment(assignment.getId());
        updateView();
    }

    public void createSeriesAndLinkSelected(
            String seriesId,
            String seriesName,
            int defaultLateDays,
            List<String> assignmentIds
    ) {
        String trimmedSeriesId = ValidationUtils.requireNonBlank(seriesId, "Series ID is required");
        String trimmedSeriesName = ValidationUtils.requireNonBlank(seriesName, "Series name is required");

        if (assignmentIds == null || assignmentIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one assignment to link");
        }

        List<Assignment> selectedAssignments = assignmentIds.stream()
                .map(this::findAssignmentById)
                .collect(Collectors.toList());

        String courseId = selectedAssignments.stream()
                .findFirst()
                .map(Assignment::getCourseId)
                .orElseThrow(() -> new IllegalArgumentException("Could not determine course for selected assignments"));

        boolean sameCourse = selectedAssignments.stream()
                .allMatch(assignment -> courseId.equals(assignment.getCourseId()));
        if (!sameCourse) {
            throw new IllegalArgumentException("Selected assignments must be from the same course");
        }

        Series series = new Series(trimmedSeriesId, courseId, trimmedSeriesName, defaultLateDays);
        model.createSeriesAndLinkAssignments(series, assignmentIds);
        updateView();
    }

    private Assignment findAssignmentById(String assignmentId) {
        return assignmentRepository.getAllAssignments().stream()
                .filter(assignment -> assignment.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
    }

    public void clockIn(String assignmentId) {
        Assignment assignment = findAssignmentById(assignmentId);
        timeTrackingManager.clockIn(assignment);
        updateView();
    }

    public ClockOutResult clockOut(String assignmentId) {
        Assignment activeAssignment = timeTrackingManager.getActiveAssignment();
        if (activeAssignment == null) {
            throw new IllegalStateException("Not currently clocked in");
        }
        if (!activeAssignment.getId().equals(assignmentId)) {
            throw new IllegalArgumentException("Selected assignment is not the active clocked-in assignment");
        }

        ClockOutResult result = timeTrackingManager.clockOut();
        LocalDateTime now = timeService.now();

        workLogRepository.addWorkLog(result.getSessionHours(), now);
        assignmentWorkLogRepository.addWorkLog(activeAssignment.getId(), result.getSessionHours(), now);

        assignmentRepository.addAssignment(activeAssignment);
        updateView();

        return result;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setOnCourses(Runnable onCourses) {
        this.onCourses = onCourses;
    }

    public void setOnStudyAvailability(Runnable onStudyAvailability) {
        this.onStudyAvailability = onStudyAvailability;
    }

    public void setOnDashboard(Runnable onDashboard) {
        this.onDashboard = onDashboard;
    }

    public void setOnBigPicture(Runnable onBigPicture) {
        this.onBigPicture = onBigPicture;
    }

    public void setShowOnlyOpen(boolean showOnlyOpen) {
        this.showOnlyOpen = showOnlyOpen;
        updateView();
    }

    public boolean isTracking() {
        return timeTrackingManager.isTracking();
    }

    public void applyManualHours(String assignmentId, double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }

        Assignment assignment = findAssignmentById(assignmentId);
        assignment.applyWork(hours);

        LocalDateTime now = timeService.now();
        workLogRepository.addWorkLog(hours, now);
        assignmentWorkLogRepository.addWorkLog(assignment.getId(), hours, now);

        assignmentRepository.addAssignment(assignment);
        updateView();
    }

    public void markDone(String assignmentId) {
        Assignment assignment = findAssignmentById(assignmentId);
        assignment.markDone();
        assignmentRepository.addAssignment(assignment);
        updateView();
    }

    public void back() {
        runIfSet(onBack);
    }
}