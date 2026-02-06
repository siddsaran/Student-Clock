package edu.ucsd.studentclock.view;

import java.time.LocalDate;
import java.util.List;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.presenter.AssignmentPresenter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AssignmentView extends VBox {

    private AssignmentPresenter presenter;

    private final TextField nameField = new TextField();
    private final ComboBox<String> courseBox = new ComboBox<>();
    private final DatePicker startPicker = new DatePicker();
    private final DatePicker deadlinePicker = new DatePicker();

    private final Button addButton = new Button("Add Assignment");
    private final Button backButton = new Button("Back");

    private final ListView<String> assignmentList = new ListView<>();

    /**
     * Creates the assignment entry screen.
     * Displays input fields for creating assignments and a list of existing assignments.
     */
    public AssignmentView() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Add Assignment");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        nameField.setPromptText("Enter assignment name");

        courseBox.setPromptText("Select course");

        form.add(new Label("Assignment Name"), 0, 0);
        form.add(nameField, 1, 0);

        form.add(new Label("Course"), 0, 1);
        form.add(courseBox, 1, 1);

        form.add(new Label("Start Date"), 0, 2);
        form.add(startPicker, 1, 2);

        form.add(new Label("Due Date"), 0, 3);
        form.add(deadlinePicker, 1, 3);

        VBox buttonBox = new VBox(addButton);
        buttonBox.setAlignment(Pos.CENTER);

        getChildren().addAll(
                title,
                form,
                buttonBox,
                new Label("Assignments"),
                assignmentList,
                backButton
        );

        addButton.setOnAction(e -> handleCreate());
    }

    /**
     * Attaches the presenter responsible for handling assignment actions.
     *
     * @param presenter assignment presenter
     */
    public void setPresenter(AssignmentPresenter presenter) {
        this.presenter = presenter;
    }
    /**
     * Reads user input and delegates assignment creation to presenter.
     */
    private void handleCreate() {
        try {
            String name = nameField.getText();
            String course = courseBox.getValue();

            LocalDate startDate = startPicker.getValue();
            LocalDate deadlineDate = deadlinePicker.getValue();

            if (course == null || startDate == null || deadlineDate == null) {
                throw new IllegalArgumentException("All fields required");
            }

            presenter.createAssignment(
                    name,
                    course,
                    startDate.atStartOfDay(),
                    deadlineDate.atStartOfDay(),
                    0
            );

            clearInputs();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    /**
     * Clears all input fields after successful submission.
     */
    private void clearInputs() {
        nameField.clear();
        startPicker.setValue(null);
        deadlinePicker.setValue(null);
    }

    /**
     * Displays all assignments for the currently selected course.
     *
     * @param assignments list of assignments
     */
    public void showAssignments(List<Assignment> assignments) {
        assignmentList.getItems().clear();
        for (Assignment a : assignments) {
            assignmentList.getItems().add(
                    a.getName() + " (" + a.getCourseID() + ")"
            );
        }
    }

    /**
     * Populates the course dropdown.
    *
    * @param courses list of course IDs
    */
    public void setCourses(List<String> courses) {
        courseBox.getItems().setAll(courses);
    }

    /**
     * Returns the back navigation button.
     *
     * @return back button
     */
    public Button getBackButton() {
        return backButton;
    }
}
