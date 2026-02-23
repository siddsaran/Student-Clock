package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import edu.ucsd.studentclock.service.ClockOutResult;
import edu.ucsd.studentclock.service.TimeTrackingManager;
import edu.ucsd.studentclock.util.ValidationUtils;
import edu.ucsd.studentclock.view.AssignmentListEntry;
import edu.ucsd.studentclock.view.AssignmentView;
import edu.ucsd.studentclock.service.ITimeService;

/**
 * Presenter for the Assignment screen.
 * Handles user actions and coordinates between AssignmentView and the assignment repository.
 */
public class AssignmentPresenter extends AbstractPresenter<AssignmentView> implements IAssignmentScreenPresenter {

    private final IAssignmentRepository repository;
    private final WorkLogRepository workLogRepository;
    private final AssignmentWorkLogRepository assignmentWorkLogRepository;
    private final TimeTrackingManager timeTrackingManager;
    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;
    private Runnable onBigPicture;
    private String courseFilter = AssignmentView.ALL_COURSES;
    private final ITimeService timeService;
    private boolean showOnlyOpen = false;
    

    /**
     * Creates an AssignmentPresenter.
     *
     * @param model shared application model
     * @param view assignment view
     * @param repository assignment repository
     */
    public AssignmentPresenter(Model model,
                               AssignmentView view,
                               IAssignmentRepository repository,
                               WorkLogRepository workLogRepository,
                               AssignmentWorkLogRepository assignmentWorkLogRepository) {
        super(model, view);
        this.repository = repository;
        this.workLogRepository = workLogRepository;
        this.assignmentWorkLogRepository = assignmentWorkLogRepository;
        this.timeService = model.getTimeService();
        this.timeTrackingManager = new TimeTrackingManager(this.timeService);
        view.setPresenter(this);
        view.getCoursesButton().setOnAction(e -> runIfSet(onCourses));
        view.getStudyAvailabilityButton().setOnAction(e -> runIfSet(onStudyAvailability));
        view.getDashboardButton().setOnAction(e -> runIfSet(onDashboard));
        view.getBigPictureButton().setOnAction(e -> runIfSet(onBigPicture));

        updateView();
    }

    /**
     * Returns the title displayed in the window.
     *
     * @return view title
     */
    @Override
    public String getViewTitle() {
        return "Assignments";
    }

    /**
     * Refreshes the view.
     * (Later will reload assignments from database.)
     */
    @Override
    public void updateView() {
        List<String> courseIds = model.getAllCourses().stream()
                .map(Course::getId)
                .collect(Collectors.toList());
        view.setCourses(courseIds);
        view.setSelectedCourse(courseFilter);
        List<AssignmentListEntry> grouped = AssignmentListGrouper.buildGroupedList(
                repository.getAllAssignments(), showOnlyOpen, courseFilter, AssignmentView.ALL_COURSES, model);
        view.showGroupedAssignments(grouped);

        Assignment selected = model.getSelectedAssignment();
        if (selected != null) {
            view.selectAssignment(selected);
        }
    }

    /**
     * Creates a new assignment and stores it in the database.
     *
     * @param name assignment name
     * @param course course id
     * @param start start date/time
     * @param deadline deadline date/time
     * @param lateDays allowed late days
     * @param estimate estimated hours
     */
    public void createAssignment(String name,
                                 String course,
                                 LocalDateTime start,
                                 LocalDateTime deadline,
                                 int lateDays,
                                 double estimate) {
        createAssignment(name, course, start, deadline, lateDays, estimate, null);
    }

    /**
     * Creates a new assignment and stores it in the database, optionally in a series.
     * When seriesId is non-null, the series must exist and its default late days are used.
     *
     * @param name assignment name
     * @param course course id
     * @param start start date/time
     * @param deadline deadline date/time
     * @param lateDays allowed late days (used only when seriesId is null)
     * @param estimate estimated hours
     * @param seriesId optional series id; if non-null, assignment is linked and series default late days are used
     */
    public void createAssignment(String name,
                                 String course,
                                 LocalDateTime start,
                                 LocalDateTime deadline,
                                 int lateDays,
                                 double estimate,
                                 String seriesId) {
        int effectiveLateDays = lateDays;
        String effectiveSeriesId = seriesId == null || seriesId.isBlank() ? null : seriesId.trim();

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
        repository.addAssignment(assignment);
        updateView();
    }
    

    /**
     * Creates a new series and stores it. Used when adding the first assignment of a new series.
     *
     * @param seriesId series id
     * @param courseId course id
     * @param seriesName display name
     * @param defaultLateDays default late days for assignments in this series
     */
    public void createSeries(String seriesId,
                             String courseId,
                             String seriesName,
                             int defaultLateDays) {
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

    /**
     * Returns all series for the given course, for use in "Add to existing series" dropdown.
     *
     * @param courseId course id
     * @return list of series for that course (never null)
     */
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
        if (assignment == null) return;
        repository.deleteAssignment(assignment.getID());
        updateView();
    }

    /**
     * Creates a series and links selected existing assignments to it.
     *
     * @param seriesId series id
     * @param seriesName series display name
     * @param defaultLateDays default late days for assignments in this series
     * @param assignmentIds selected assignment ids to link
     */
    public void createSeriesAndLinkSelected(String seriesId,
                                            String seriesName,
                                            int defaultLateDays,
                                            List<String> assignmentIds) {
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
                .map(Assignment::getCourseID)
                .orElseThrow(() -> new IllegalArgumentException("Could not determine course for selected assignments"));
        boolean sameCourse = selectedAssignments.stream().allMatch(a -> courseId.equals(a.getCourseID()));
        if (!sameCourse) {
            throw new IllegalArgumentException("Selected assignments must be from the same course");
        }

        Series series = new Series(trimmedSeriesId, courseId, trimmedSeriesName, defaultLateDays);
        model.createSeriesAndLinkAssignments(series, assignmentIds);
        updateView();
    }

    private Assignment findAssignmentById(String assignmentId) {
        return repository.getAllAssignments().stream()
                .filter(a -> a.getID().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
    }

    public void clockIn(String assignmentId) {
        Assignment a = findAssignmentById(assignmentId);
        timeTrackingManager.clockIn(a);
        updateView();
    }

    public ClockOutResult clockOut(String assignmentId) {
        Assignment active = timeTrackingManager.getActiveAssignment();
        if (active == null) {
            throw new IllegalStateException("Not currently clocked in");
        }
        if (!active.getID().equals(assignmentId)) {
            throw new IllegalArgumentException("Selected assignment is not the active clocked-in assignment");
        }

        ClockOutResult result = timeTrackingManager.clockOut();
        LocalDateTime now = timeService.now();
        workLogRepository.addWorkLog(result.getSessionHours(), now);
        assignmentWorkLogRepository.addWorkLog(active.getID(), result.getSessionHours(), now);

        repository.addAssignment(active);

        updateView();
        return result;
    }

    /**
     * Registers callback for back navigation.
     *
     * @param onBack runnable executed when back is pressed
     */
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }
    public void setOnCourses(Runnable r) {
        onCourses = r;
    }

    public void setOnStudyAvailability(Runnable r) {
        onStudyAvailability = r;
    }

    public void setOnDashboard(Runnable r) {
        onDashboard = r;
    }
    public void setOnBigPicture(Runnable r) {
        onBigPicture = r;
    }
    public void setShowOnlyOpen(boolean value) {
        this.showOnlyOpen = value;
        updateView();
    }


    public boolean isTracking() {
        return timeTrackingManager.isTracking();
    }  

    

    public void applyManualHours(String assignmentId, double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }

        Assignment a = findAssignmentById(assignmentId);
        a.applyWork(hours);
        LocalDateTime now = timeService.now();
        workLogRepository.addWorkLog(hours, now);
        assignmentWorkLogRepository.addWorkLog(a.getID(), hours, now);
        repository.addAssignment(a);
        updateView();
    }

    public void markDone(String assignmentId) {
        Assignment a = findAssignmentById(assignmentId);
        a.markDone();
        repository.addAssignment(a);
        updateView();
    }

    /**
     * Invoked by view when back button is pressed.
     */
    public void back() {
        runIfSet(onBack);
    }


}
