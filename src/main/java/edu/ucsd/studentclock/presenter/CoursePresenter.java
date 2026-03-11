package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.view.CourseView;

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
