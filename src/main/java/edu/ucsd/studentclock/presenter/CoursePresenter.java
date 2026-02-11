package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.view.CourseView;
import javafx.scene.control.Alert;

/**
 * Presenter for the course management screen.
 */
public class CoursePresenter extends AbstractPresenter<CourseView> {

    private Runnable onNavigateToAssignments;
    private Runnable onNavigateToStudyAvailability; 

    public CoursePresenter(Model model, CourseView view) {
        super(model, view);
        view.getAddButton().setOnAction(e -> handleAdd());
        view.getDeleteButton().setOnAction(e -> handleDelete());
        view.getAssignmentsButton().setOnAction(e -> {
            if (onNavigateToAssignments != null) {
                onNavigateToAssignments.run();
            }
        });

        view.getStudyAvailabilityButton().setOnAction(e -> {
            if (onNavigateToStudyAvailability != null) {
                onNavigateToStudyAvailability.run();
            }
        });
        

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

    private void handleAdd() {
        try {
            String id = view.getIdField().getText();
            String name = view.getNameField().getText();
            model.addCourse(id, name);
            view.clearForm();
            updateView();
        } catch (IllegalArgumentException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void handleDelete() {
        String id = view.getSelectedCourseId();
        if (id == null) {
            new Alert(Alert.AlertType.WARNING, "Select a course to delete.").showAndWait();
            return;
        }
        model.deleteCourse(id);
        updateView();
    }
}
