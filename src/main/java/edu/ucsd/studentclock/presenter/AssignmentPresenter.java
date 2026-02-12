package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Course;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.view.AssignmentView;

/**
 * Presenter for the Assignment screen.
 * Handles user actions and coordinates between AssignmentView and AssignmentRepository.
 */
public class AssignmentPresenter extends AbstractPresenter<AssignmentView> {

    private final AssignmentRepository repository;
    private Runnable onBack;
    /**
     * Creates an AssignmentPresenter.
     *
     * @param model shared application model
     * @param view assignment view
     * @param repository assignment repository
     */
    public AssignmentPresenter(Model model,
                               AssignmentView view,
                               AssignmentRepository repository) {
        super(model, view);
        this.repository = repository;
        view.setPresenter(this);
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
        view.showAssignments(repository.getAllAssignments());
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

    /**
     * Registers callback for back navigation.
     *
     * @param onBack runnable executed when back is pressed
     */
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
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
