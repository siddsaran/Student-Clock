package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.view.CourseView;
import javafx.scene.control.Alert;

/**
 * Presenter for the course management screen.
 */
public class CoursePresenter extends AbstractPresenter<CourseView> {

    public CoursePresenter(Model model, CourseView view) {
        super(model, view);
        view.getAddButton().setOnAction(e -> handleAdd());
        view.getDeleteButton().setOnAction(e -> handleDelete());
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
        String id = CourseSelectionParser.parseCourseId(view.getSelectedItem());
        if (id == null) {
            new Alert(Alert.AlertType.WARNING, "Select a course to delete.").showAndWait();
            return;
        }
        model.deleteCourse(id);
        updateView();
    }
}
