package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.service.ClockOutResult;
import edu.ucsd.studentclock.service.WorkSessionService;
import edu.ucsd.studentclock.util.ValidationUtils;
import edu.ucsd.studentclock.view.AssignmentListEntry;
import edu.ucsd.studentclock.view.AssignmentView;
import edu.ucsd.studentclock.view.AssignmentCreateRequest;
import edu.ucsd.studentclock.view.AssignmentCreateRequest.SeriesChoice;

/**
 * Presenter for the Assignment screen.
 * Handles user actions and coordinates between AssignmentView and the assignment repository.
 * Work-session concerns (clock in/out, manual hours) are delegated to {@link WorkSessionService}.
 */
public class AssignmentPresenter extends AbstractPresenter<AssignmentView> implements IAssignmentScreenPresenter {

    private String courseFilter = AssignmentView.ALL_COURSES;
    private boolean showOnlyOpen = false;

    /**
     * Creates an AssignmentPresenter.
     *
     * @param model                       shared application model
     * @param view                        assignment view
     */
    public AssignmentPresenter(
            Model model,
            AssignmentView view
    ) {
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
        String effectiveSeriesId = (seriesId == null || seriesId.isBlank()) ? null : seriesId.trim();

        if (effectiveSeriesId != null) {
            Series series = model.getSeries(effectiveSeriesId)
                    .orElseThrow(() -> new IllegalArgumentException("Series not found: " + effectiveSeriesId));
            effectiveLateDays = series.getDefaultLateDays();
        }

        model.createAssignment(name, course, effectiveSeriesId, start, deadline, effectiveLateDays, estimate);
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

        model.createSeries(trimmedId, courseId, trimmedName, defaultLateDays);
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
        model.deleteAssignment(assignment.getId());
        updateView();
    }

    public void createSeriesAndLinkSelected(
            String seriesId,
            String seriesName,
            String defaultLateDaysText,
            List<String> assignmentIds
    ) {
        String trimmedSeriesId = ValidationUtils.requireNonBlank(seriesId, "Series ID is required");
        String trimmedSeriesName = ValidationUtils.requireNonBlank(seriesName, "Series name is required");

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
                .allMatch(a -> courseId.equals(a.getCourseId()));
        if (!sameCourse) {
            throw new IllegalArgumentException("Selected assignments must be from the same course");
        }

        model.createSeriesAndLinkAssignments(trimmedSeriesId, courseId, trimmedSeriesName, defaultLateDays, assignmentIds);
        updateView();
    }

    private Assignment findAssignmentById(String assignmentId) {
        return model.getAllAssignments().stream()
                .filter(a -> a.getId().equals(assignmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found: " + assignmentId));
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

        if (req == null) throw new IllegalArgumentException("Missing form data");

        String name = (req.getNameText() == null) ? "" : req.getNameText().trim();
        String course = req.getCourseId();
        if (course == null || course.isBlank() || AssignmentView.ALL_COURSES.equals(course)) {
            throw new IllegalArgumentException("Course is required");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Assignment name is required");
        }
        if (req.getStartDate() == null || req.getDeadlineDate() == null) {
            throw new IllegalArgumentException("Start date and due date are required");
        }

        double estimate;
        try {
            estimate = Double.parseDouble((req.getEstimatedHoursText() == null) ? "" : req.getEstimatedHoursText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid number for estimated hours");
        }
        if (estimate < 0) {
            throw new IllegalArgumentException("Estimated hours must be >= 0");
        }

        LocalDateTime start = req.getStartDate().atStartOfDay();
        LocalDateTime deadline = req.getDeadlineDate().atStartOfDay();

        SeriesChoice choice = req.getSeriesChoice();
        if (choice == null) choice = SeriesChoice.NONE;

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

        String seriesName = (req.getNewSeriesNameText() == null) ? "" : req.getNewSeriesNameText().trim();
        if (seriesName.isEmpty()) {
            throw new IllegalArgumentException("Series name is required when creating a new series");
        }

        String seriesId = (req.getNewSeriesIdText() == null) ? "" : req.getNewSeriesIdText().trim();
        if (seriesId.isEmpty()) {
            seriesId = "series-" + UUID.randomUUID();
        }

        int defaultLateDays = 0;
        String lateDaysText = (req.getNewSeriesDefaultLateDaysText() == null) ? "" : req.getNewSeriesDefaultLateDaysText().trim();
        if (!lateDaysText.isEmpty()) {
            try {
                defaultLateDays = Integer.parseInt(lateDaysText);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Enter a valid integer for default late days");
            }
        }
        if (defaultLateDays < 0) {
            throw new IllegalArgumentException("Default late days must be >= 0");
        }

        createSeries(seriesId, course, seriesName, defaultLateDays);
        createAssignment(name, course, start, deadline, defaultLateDays, estimate, seriesId);
    }
}