package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.service.ClockOutResult;
import edu.ucsd.studentclock.util.ValidationUtils;
import edu.ucsd.studentclock.view.AssignmentCreateRequest;
import edu.ucsd.studentclock.view.AssignmentCreateRequest.SeriesChoice;
import edu.ucsd.studentclock.view.AssignmentListEntry;
import edu.ucsd.studentclock.view.AssignmentView;

/**
 * Presenter for the Assignment screen.
 * Handles user actions and coordinates between AssignmentView and the assignment repository.
 */
public class AssignmentPresenter extends AbstractPresenter<AssignmentView> implements IAssignmentScreenPresenter {

    private static final String SERIES_ID_REQUIRED_MESSAGE = "Series ID is required";
    private static final String SERIES_NAME_REQUIRED_MESSAGE = "Series name is required";
    private static final String COURSE_REQUIRED_MESSAGE = "Course is required";
    private static final String ASSIGNMENT_NAME_REQUIRED_MESSAGE = "Assignment name is required";
    private static final String ASSIGNMENT_NOT_FOUND_PREFIX = "Assignment not found: ";
    private static final String AUTO_SERIES_ID_PREFIX = "series-";

    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;
    private Runnable onBigPicture;

    private String courseFilter = AssignmentView.ALL_COURSES;
    private boolean showOnlyOpen = false;

    public AssignmentPresenter(Model model, AssignmentView view) {
        super(model, view);

        view.setPresenter(this);

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
                model.getAllAssignments(),
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
        String effectiveSeriesId = ValidationUtils.normalizeNullable(seriesId);

        if (effectiveSeriesId != null) {
            Series series = model.getSeries(effectiveSeriesId)
                    .orElseThrow(() -> new IllegalArgumentException("Series not found: " + effectiveSeriesId));
            effectiveLateDays = series.getDefaultLateDays();
        }

        model.createAssignment(name, course, effectiveSeriesId, start, deadline, effectiveLateDays, estimate);
        updateView();
    }

    public void createSeries(String seriesId, String courseId, String seriesName, int defaultLateDays) {
        String trimmedId = ValidationUtils.requireNonBlank(seriesId, SERIES_ID_REQUIRED_MESSAGE);
        String trimmedCourseId = ValidationUtils.requireNonBlank(courseId, COURSE_REQUIRED_MESSAGE);
        String trimmedName = ValidationUtils.requireNonBlank(seriesName, SERIES_NAME_REQUIRED_MESSAGE);

        if (defaultLateDays < 0) {
            throw new IllegalArgumentException("Default late days must be >= 0");
        }

        model.createSeries(trimmedId, trimmedCourseId, trimmedName, defaultLateDays);
    }

    public List<Series> getSeriesForCourse(String courseId) {
        String trimmedCourseId = ValidationUtils.normalizeNullable(courseId);
        if (trimmedCourseId == null) {
            return List.of();
        }
        return model.getSeriesByCourse(trimmedCourseId);
    }

    public void setCourseFilter(String courseIdOrAllCourses) {
        courseFilter = (courseIdOrAllCourses == null) ? AssignmentView.ALL_COURSES : courseIdOrAllCourses;
        updateView();
    }

    public void deleteAssignment(Assignment assignment) {
        if (assignment == null) {
            return;
        }
        model.deleteAssignment(assignment.getId());
        updateView();
    }

    public void createSeriesAndLinkSelected(
            String seriesId,
            String seriesName,
            String defaultLateDaysText,
            List<String> assignmentIds
    ) {
        String trimmedSeriesId = ValidationUtils.requireNonBlank(seriesId, SERIES_ID_REQUIRED_MESSAGE);
        String trimmedSeriesName = ValidationUtils.requireNonBlank(seriesName, SERIES_NAME_REQUIRED_MESSAGE);
        int defaultLateDays = AssignmentInputParser.parseDefaultLateDays(defaultLateDaysText);

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

        model.createSeriesAndLinkAssignments(trimmedSeriesId, courseId, trimmedSeriesName, defaultLateDays, assignmentIds);
        updateView();
    }

    private Assignment findAssignmentById(String assignmentId) {
        String trimmedId = ValidationUtils.requireNonBlank(assignmentId, "Assignment ID is required");
        return model.getAllAssignments().stream()
                .filter(assignment -> assignment.getId().equals(trimmedId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ASSIGNMENT_NOT_FOUND_PREFIX + trimmedId));
    }

    public void clockIn(String assignmentId) {
        model.clockIn(assignmentId);
        updateView();
    }

    public ClockOutResult clockOut(String assignmentId) {
        ClockOutResult result = model.clockOut(assignmentId);
        updateView();
        return result;
    }

    public void applyManualHours(String assignmentId, String hoursText) {
        double hours = AssignmentInputParser.parseHours(hoursText);
        model.applyManualHours(assignmentId, hours);
        updateView();
    }

    public void markDone(String assignmentId) {
        model.markDone(assignmentId);
        updateView();
    }

    public boolean isTracking() {
        return model.isTracking();
    }

    @Override
    public void back() {
        // Navigation handled by global nav bar
    }

    public void setShowOnlyOpen(boolean showOnlyOpen) {
        this.showOnlyOpen = showOnlyOpen;
        updateView();
    }

    public void showOpenAssignments() {
        setShowOnlyOpen(true);
        setCourseFilter(AssignmentView.ALL_COURSES);
    }

    public void showAllAssignments() {
        setShowOnlyOpen(false);
    }

    public void onCreateAssignment(AssignmentCreateRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Missing form data");
        }

        String name = ValidationUtils.requireNonBlank(req.getNameText(), ASSIGNMENT_NAME_REQUIRED_MESSAGE);
        String course = ValidationUtils.normalizeNullable(req.getCourseId());
        if (course == null || AssignmentView.ALL_COURSES.equals(course)) {
            throw new IllegalArgumentException(COURSE_REQUIRED_MESSAGE);
        }
        if (req.getStartDate() == null || req.getDeadlineDate() == null) {
            throw new IllegalArgumentException("Start date and due date are required");
        }

        double estimate = AssignmentInputParser.parseEstimatedHours(req.getEstimatedHoursText());
        LocalDateTime start = req.getStartDate().atStartOfDay();
        LocalDateTime deadline = req.getDeadlineDate().atStartOfDay();

        SeriesChoice choice = req.getSeriesChoice();
        if (choice == null) {
            choice = SeriesChoice.NONE;
        }

        if (choice == SeriesChoice.NONE) {
            createAssignment(name, course, start, deadline, 0, estimate, null);
            return;
        }

        if (choice == SeriesChoice.EXISTING_SERIES) {
            Series selected = req.getExistingSeries();
            if (selected == null) {
                throw new IllegalArgumentException("Select a series to add this assignment to");
            }
            createAssignment(
                    name,
                    course,
                    start,
                    deadline,
                    selected.getDefaultLateDays(),
                    estimate,
                    selected.getId()
            );
            return;
        }

        String seriesName = ValidationUtils.requireNonBlank(
                req.getNewSeriesNameText(),
                "Series name is required when creating a new series"
        );
        String seriesId = ValidationUtils.normalizeNullable(req.getNewSeriesIdText());
        if (seriesId == null) {
            seriesId = AUTO_SERIES_ID_PREFIX + UUID.randomUUID();
        }

        int defaultLateDays = AssignmentInputParser.parseOptionalNonNegativeInt(
                req.getNewSeriesDefaultLateDaysText(),
                "Enter a valid integer for default late days",
                "Default late days must be >= 0"
        );

        createSeries(seriesId, course, seriesName, defaultLateDays);
        createAssignment(name, course, start, deadline, defaultLateDays, estimate, seriesId);
    }
}