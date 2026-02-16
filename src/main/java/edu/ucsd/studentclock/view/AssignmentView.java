package edu.ucsd.studentclock.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.presenter.AssignmentPresenter;
import edu.ucsd.studentclock.service.ClockOutResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AssignmentView extends VBox {

    private AssignmentPresenter presenter;

    private final TextField nameField = new TextField();
    private final ComboBox<String> courseBox = new ComboBox<>();
    private final DatePicker startPicker = new DatePicker();
    private final DatePicker deadlinePicker = new DatePicker();
    private final TextField estimatedHoursField = new TextField();
    private final TextField seriesIdField = new TextField();
    private final TextField seriesNameField = new TextField();
    private final TextField seriesDefaultLateDaysField = new TextField();

    private final Button addButton = new Button("Add Assignment");
    private final Button deleteButton = new Button("Delete Assignment");
    private final Button createSeriesButton = new Button("Create Series + Link Selected");
    private final Button backButton = new Button("Back");
    private final Button clockButton = new Button("Clock In");
    private final Button markDoneButton = new Button("Mark Done");

    private final TextField manualHoursField = new TextField();
    private final Button applyHoursButton = new Button("Apply Hours");
    

    private final ListView<Assignment> assignmentList = new ListView<>();

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

        estimatedHoursField.setPromptText("Estimated hours");
        seriesIdField.setPromptText("Series ID");
        seriesNameField.setPromptText("Series name");
        seriesDefaultLateDaysField.setPromptText("Default late days");
        manualHoursField.setPromptText("Hours worked (e.g., 1.5)");

        form.add(new Label("Assignment Name"), 0, 0);
        form.add(nameField, 1, 0);

        form.add(new Label("Course"), 0, 1);
        form.add(courseBox, 1, 1);

        form.add(new Label("Start Date"), 0, 2);
        form.add(startPicker, 1, 2);

        form.add(new Label("Due Date"), 0, 3);
        form.add(deadlinePicker, 1, 3);

        form.add(new Label("Estimated Hours"), 0, 4);
        form.add(estimatedHoursField, 1, 4);
        form.add(new Label("Series ID"), 0, 5);
        form.add(seriesIdField, 1, 5);
        form.add(new Label("Series Name"), 0, 6);
        form.add(seriesNameField, 1, 6);
        form.add(new Label("Series Default Late Days"), 0, 7);
        form.add(seriesDefaultLateDaysField, 1, 7);

        VBox trackingBox = new VBox(
                10,
                clockButton,
                manualHoursField,
                applyHoursButton,
                markDoneButton
        );

        VBox buttonBox = new VBox(
                10,
                addButton,
                deleteButton,
                createSeriesButton,
                trackingBox
        );

        buttonBox.setAlignment(Pos.CENTER);
        trackingBox.setVisible(false);
        trackingBox.setManaged(false);
        assignmentList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        assignmentList.getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> {

                boolean hasSelection = newVal != null;

                trackingBox.setVisible(hasSelection);
                trackingBox.setManaged(hasSelection);
            });

        getChildren().addAll(
                title,
                form,
                buttonBox,
                new Label("Assignments"),
                assignmentList,
                backButton
        );

        addButton.setOnAction(e -> handleCreate());
        deleteButton.setOnAction(e -> handleDelete());
        createSeriesButton.setOnAction(e -> handleCreateSeriesAndLink());
        clockButton.setOnAction(e -> handleClockButton());
        applyHoursButton.setOnAction(e -> handleApplyHours());
        markDoneButton.setOnAction(e -> handleMarkDone());
        backButton.setOnAction(e -> {
            if (presenter != null) {
                presenter.back();
            }
        });

    }

    private void handleClockButton() {
        Assignment selected = assignmentList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            if (!presenter.isTracking()) {
                presenter.clockIn(selected.getID());
                clockButton.setText("Clock Out");
            } else {
                presenter.clockOut(selected.getID());
                clockButton.setText("Clock In");
            }
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
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
            double estimate = Double.parseDouble(estimatedHoursField.getText());
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
                    0,
                    estimate
            );

            clearInputs();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void handleDelete() {
        Assignment selected = assignmentList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.ERROR, "Select an assignment to delete.").showAndWait();
            return;
        }
        presenter.deleteAssignment(selected);
    }

    private void handleCreateSeriesAndLink() {
        try {
            String seriesId = seriesIdField.getText();
            String seriesName = seriesNameField.getText();
            int defaultLateDays = Integer.parseInt(seriesDefaultLateDaysField.getText());
            List<String> selectedAssignmentIds = getSelectedAssignmentIds();

            if (presenter == null) {
                throw new IllegalStateException("Presenter is not attached");
            }
            presenter.createSeriesAndLinkSelected(seriesId, seriesName, defaultLateDays, selectedAssignmentIds);
            clearSeriesInputs();
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

    private void clearSeriesInputs() {
        seriesIdField.clear();
        seriesNameField.clear();
        seriesDefaultLateDaysField.clear();
    }

    private void handleApplyHours() {
        Assignment selected = assignmentList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            double hours = Double.parseDouble(manualHoursField.getText().trim());
            presenter.applyManualHours(selected.getID(), hours);
            manualHoursField.clear();
        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Enter a valid number like 1.5").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void handleMarkDone() {
        Assignment selected = assignmentList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            presenter.markDone(selected.getID());
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    /**
     * Displays all assignments for the currently selected course.
     *
     * @param assignments list of assignments
     */
    public void showAssignments(List<Assignment> assignments) {
        assignmentList.getItems().setAll(assignments);
    }


    /**
     * Populates the course dropdown.
    *
    * @param courses list of course IDs
    */
    public void setCourses(List<String> courses) {
        courseBox.getItems().setAll(courses);
    }

    public List<String> getSelectedAssignmentIds() {
        List<String> selectedIds = new ArrayList<>();
        for (Assignment assignment : assignmentList.getSelectionModel().getSelectedItems()) {
            selectedIds.add(assignment.getID());
        }
        return selectedIds;
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
