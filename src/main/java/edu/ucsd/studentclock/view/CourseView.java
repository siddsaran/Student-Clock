package edu.ucsd.studentclock.view;

import java.util.List;

import edu.ucsd.studentclock.model.Course;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the course management screen: list courses, add, delete, and navigate to Assignments.
 */
public class CourseView extends BorderPane {

    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final Button dashboardButton = new Button("Dashboard");
    private final Button addButton = new Button("Add Course");
    private final Button deleteButton = new Button("Delete Selected");
    private final Button assignmentsButton = new Button("Go to Assignments");
    private final Button studyAvailabilityButton = new Button("Go to Study Availability");
    private final ListView<String> courseList = new ListView<>();

    public CourseView() {
        setPadding(new Insets(20));

        Label title = new Label("Courses");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        idField.setPromptText("Course ID (e.g. CSE 110)");
        nameField.setPromptText("Course name");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Course ID"), 0, 0);
        form.add(idField, 1, 0);
        form.add(new Label("Course Name"), 0, 1);
        form.add(nameField, 1, 1);

        HBox navBar = new HBox(10,
                assignmentsButton,
                studyAvailabilityButton,
                dashboardButton
        );
        navBar.setAlignment(Pos.CENTER_LEFT);

        VBox topContainer = new VBox(navBar);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        setTop(topContainer);

        VBox courseButtons = new VBox(8, addButton, deleteButton);
        VBox leftPanel = new VBox(30,
                title,
                form,
                courseButtons
        );

        VBox rightPanel = new VBox(
                new Label("Existing courses"),
                courseList
        );

        VBox.setVgrow(courseList, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        HBox main = new HBox(40, leftPanel, rightPanel);
        setCenter(main);

        VBox.setVgrow(courseList, Priority.ALWAYS);
    }



    /**
     * Displays the given courses in the list (format: "id - name").
     */
    public void showCourses(List<Course> courses) {
        courseList.getItems().clear();
        for (Course c : courses) {
            courseList.getItems().add(c.getId() + " - " + c.getName());
        }
    }

    public TextField getIdField() {
        return idField;
    }

    public TextField getNameField() {
        return nameField;
    }

    /**
     * Returns the course id of the selected list item, or null if none selected.
     * List items are in the form "id - name"; the part before " - " is the id.
     */
    public String getSelectedCourseId() {
        String selected = courseList.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.contains(" - ")) {
            return null;
        }
        return selected.substring(0, selected.indexOf(" - ")).trim();
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public Button getAssignmentsButton() {
        return assignmentsButton;
    }

    public Button getStudyAvailabilityButton() {
        return studyAvailabilityButton;
    }
    public Button getDashboardButton() {
        return dashboardButton;
    }

    public void clearForm() {
        idField.clear();
        nameField.clear();
    }
}
