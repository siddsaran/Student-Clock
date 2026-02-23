package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import java.time.LocalDateTime;

import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import edu.ucsd.studentclock.service.ClockOutResult;
import edu.ucsd.studentclock.service.TimeTrackingManager;
import edu.ucsd.studentclock.view.AssignmentListEntry;
import edu.ucsd.studentclock.view.AssignmentView;

/**
 * Presenter for the Assignment screen.
 * Handles user actions and coordinates between AssignmentView and AssignmentRepository.
 */
public class AssignmentPresenter extends AbstractPresenter<AssignmentView> {

    private final AssignmentRepository repository;
    private final WorkLogRepository workLogRepository;
    private final TimeTrackingManager timeTrackingManager;
    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;
    private Runnable onBigPicture;

    /**
     * Creates an AssignmentPresenter.
     *
     * @param model shared application model
     * @param view assignment view
     * @param repository assignment repository
     */
    public AssignmentPresenter(Model model,
                               AssignmentView view,
                               AssignmentRepository repository,
                               WorkLogRepository workLogRepository) {
        super(model, view);
        this.repository = repository;
        this.workLogRepository = workLogRepository;
        this.timeTrackingManager = new TimeTrackingManager();
        view.setPresenter(this);
        view.getCoursesButton().setOnAction(e -> {
            if (onCourses != null) onCourses.run();
        });

        view.getStudyAvailabilityButton().setOnAction(e -> {
            if (onStudyAvailability != null) onStudyAvailability.run();
        });

        view.getDashboardButton().setOnAction(e -> {
            if (onDashboard != null) onDashboard.run();
        });

        view.getBigPictureButton().setOnAction(e -> {
            if (onBigPicture != null) onBigPicture.run();
        });

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
        List<AssignmentListEntry> grouped = buildGroupedAssignmentList();
        view.showGroupedAssignments(grouped);

        Assignment selected = model.getSelectedAssignment();
        if (selected != null) {
            view.selectAssignment(selected);
        }
    }

    /**
     * Builds a list of assignment rows (no headers). No-series assignments have no tag;
     * series assignments have a tag. Order: no series first, then each series by name.
     */
    private List<AssignmentListEntry> buildGroupedAssignmentList() {
        List<Assignment> assignments = repository.getAllAssignments();
        Map<String, String> seriesIdToName = new HashMap<>();
        for (Assignment a : assignments) {
            String sid = a.getSeriesId();
            if (sid != null && !seriesIdToName.containsKey(sid)) {
                String name = model.getSeries(sid)
                        .map(Series::getName)
                        .orElse(sid);
                seriesIdToName.put(sid, name);
            }
        }

        Map<String, List<Assignment>> bySeries = new HashMap<>();
        for (Assignment a : assignments) {
            String key = a.getSeriesId() != null ? a.getSeriesId() : null;
            bySeries.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
        }

        List<AssignmentListEntry> result = new ArrayList<>();
        List<Assignment> noSeriesList = bySeries.get(null);
        if (noSeriesList != null) {
            for (Assignment a : noSeriesList) {
                result.add(AssignmentListEntry.forRowWithoutTag(a));
            }
        }

        List<String> seriesIds = assignments.stream()
                .map(Assignment::getSeriesId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator
                        .comparing((String id) -> seriesIdToName.getOrDefault(id, id))
                        .thenComparing(id -> id))
                .collect(Collectors.toList());

        for (String seriesId : seriesIds) {
            String displayName = seriesIdToName.getOrDefault(seriesId, seriesId);
            for (Assignment a : bySeries.get(seriesId)) {
                result.add(AssignmentListEntry.forRow(a, displayName));
            }
        }

        return result;
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

        Assignment assignment = new Assignment(
                name,
                course,
                start,
                deadline,
                lateDays,
                estimate
        );
        repository.addAssignment(assignment);
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
        String trimmedSeriesId = seriesId == null ? null : seriesId.trim();
        String trimmedSeriesName = seriesName == null ? null : seriesName.trim();
        if (trimmedSeriesId == null || trimmedSeriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is required");
        }
        if (trimmedSeriesName == null || trimmedSeriesName.isEmpty()) {
            throw new IllegalArgumentException("Series name is required");
        }
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
        workLogRepository.addWorkLog(result.getSessionHours(), LocalDateTime.now());

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


    public boolean isTracking() {
        return timeTrackingManager.isTracking();
    }  

    public void applyManualHours(String assignmentId, double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }

        Assignment a = findAssignmentById(assignmentId);
        a.applyWork(hours);
        workLogRepository.addWorkLog(hours, LocalDateTime.now());
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
        if (onBack != null) {
            onBack.run();
        }
    }


}
