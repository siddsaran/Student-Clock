package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.view.CourseView;

/**
 * Presenter for the course management screen.
 */
public class CoursePresenter extends AbstractPresenter<CourseView> {

    private Runnable onNavigateToAssignments;
    private Runnable onNavigateToStudyAvailability; 
    private Runnable onNavigateToDashboard;
    private Runnable onBigPicture;


    public CoursePresenter(Model model, CourseView view) {
        super(model, view);
        view.getAddButton().setOnAction(e -> handleAdd());
        view.getDeleteButton().setOnAction(e -> handleDelete());
        view.getAssignmentsButton().setOnAction(e -> runIfSet(onNavigateToAssignments));
        view.getStudyAvailabilityButton().setOnAction(e -> runIfSet(onNavigateToStudyAvailability));
        view.getDashboardButton().setOnAction(e -> runIfSet(onNavigateToDashboard));
        view.getBigPictureButton().setOnAction(e -> runIfSet(onBigPicture));
        updateView();
    }

    @Override
    public String getViewTitle() {
        return "Courses";
    }

    @Override
    public void updateView() {
        view.showCourses(model.getAllCourses());
    }

    /**
     * Registers the callback for navigating to the Assignments screen.
     */
    public void setOnNavigateToAssignments(Runnable runnable) {
        this.onNavigateToAssignments = runnable;
    }

    public void setOnNavigateToStudyAvailability(Runnable runnable) {
        this.onNavigateToStudyAvailability = runnable;
    }
    public void setOnNavigateToDashboard(Runnable action) {
        this.onNavigateToDashboard = action;
    }
    public void setOnBigPicture(Runnable r) {
        onBigPicture = r;
    }


    private void handleAdd() {
        try {
            String id = view.getIdField().getText();
            String name = view.getNameField().getText();
            model.addCourse(id, name);
            view.clearForm();
            updateView();
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }

    private void handleDelete() {
        String id = CourseSelectionParser.parseCourseId(view.getSelectedItem());
        if (id == null) {
            view.showWarning("Select a course to delete.");
            return;
        }
        model.deleteCourse(id);
        updateView();
    }
}
