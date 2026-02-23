package edu.ucsd.studentclock.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.util.TimeFormatUtils;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.presenter.AssignmentPresenter;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AssignmentView extends BorderPane {

    // Dropdown for ALL courses
    public static final String ALL_COURSES = "All Courses";

    private AssignmentPresenter presenter;

    private final TextField nameField = new TextField();
    private final ComboBox<String> courseBox = new ComboBox<>();
    private final DatePicker startPicker = new DatePicker();
    private final DatePicker deadlinePicker = new DatePicker();
    private final TextField estimatedHoursField = new TextField();
    private final TextField seriesIdField = new TextField();
    private final TextField seriesNameField = new TextField();
    private final TextField seriesDefaultLateDaysField = new TextField();

    private static final String SERIES_CHOICE_NONE = "No series";
    private static final String SERIES_CHOICE_NEW = "Create new series";
    private static final String SERIES_CHOICE_EXISTING = "Add to existing series";

    private final ComboBox<String> seriesChoiceBox = new ComboBox<>();
    private final TextField addFormNewSeriesNameField = new TextField();
    private final TextField addFormNewSeriesIdField = new TextField();
    private final TextField addFormNewDefaultLateDaysField = new TextField();
    private final ComboBox<Series> existingSeriesBox = new ComboBox<>();
    private final GridPane addFormNewSeriesPanel = new GridPane();
    private final HBox addFormExistingSeriesRow = new HBox(10);

    private final Button addButton = new Button("Add Assignment");
    private final Button deleteButton = new Button("Delete Assignment");
    private final Button createSeriesButton = new Button("Create Series + Link Selected");
    private final Button backButton = new Button("Back");
    private final Button clockButton = new Button("Clock In");
    private final Button markDoneButton = new Button("Mark Done");

    private final TextField manualHoursField = new TextField();
    private final Button applyHoursButton = new Button("Apply Hours");

    private final Button dashboardButton = new Button("Dashboard");
    private final Button courseButton = new Button("Go to Courses");
    private final Button studyAvailabilityButton = new Button("Go to Study Availability");
    private final Button bigPictureButton = new Button("Big Picture");

    private final ListView<AssignmentListEntry> assignmentList = new ListView<>();

    private boolean suppressCourseListener = false;

    public AssignmentView() {
        setPadding(new Insets(20));

        Label title = new Label("Add Assignment");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label assignmentsTitle = new Label("Assignments");
        assignmentsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label seriesTitle = new Label("Series");
        seriesTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

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

        addFormNewSeriesNameField.setPromptText("Series name");
        addFormNewSeriesIdField.setPromptText("Series ID (optional)");
        addFormNewDefaultLateDaysField.setPromptText("Default late days");
        addFormNewDefaultLateDaysField.setText("0");

        seriesChoiceBox.getItems().setAll(SERIES_CHOICE_NONE, SERIES_CHOICE_NEW, SERIES_CHOICE_EXISTING);
        seriesChoiceBox.getSelectionModel().select(SERIES_CHOICE_NONE);
        seriesChoiceBox.setMaxWidth(Double.MAX_VALUE);

        addFormNewSeriesPanel.setHgap(10);
        addFormNewSeriesPanel.setVgap(10);
        addFormNewSeriesPanel.add(new Label("Series name"), 0, 0);
        addFormNewSeriesPanel.add(addFormNewSeriesNameField, 1, 0);
        addFormNewSeriesPanel.add(new Label("Series ID (optional)"), 0, 1);
        addFormNewSeriesPanel.add(addFormNewSeriesIdField, 1, 1);
        addFormNewSeriesPanel.add(new Label("Default late days"), 0, 2);
        addFormNewSeriesPanel.add(addFormNewDefaultLateDaysField, 1, 2);

        existingSeriesBox.setPromptText("Select series");
        existingSeriesBox.setMaxWidth(Double.MAX_VALUE);
        existingSeriesBox.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Series item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        existingSeriesBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Series item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        addFormNewSeriesPanel.setVisible(false);
        addFormNewSeriesPanel.setManaged(false);
        addFormExistingSeriesRow.getChildren().setAll(new Label("Existing series"), existingSeriesBox);
        addFormExistingSeriesRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(existingSeriesBox, Priority.ALWAYS);
        addFormExistingSeriesRow.setVisible(false);
        addFormExistingSeriesRow.setManaged(false);

        seriesChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean showNew = SERIES_CHOICE_NEW.equals(newVal);
            boolean showExisting = SERIES_CHOICE_EXISTING.equals(newVal);
            addFormNewSeriesPanel.setVisible(showNew);
            addFormNewSeriesPanel.setManaged(showNew);
            addFormExistingSeriesRow.setVisible(showExisting);
            addFormExistingSeriesRow.setManaged(showExisting);
            if (showExisting && presenter != null) {
                String course = courseBox.getValue();
                if (course != null && !course.isBlank()) {
                    refreshExistingSeriesForCourse(course);
                }
            }
        });

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

        form.add(new Label("Series"), 0, 5);
        form.add(seriesChoiceBox, 1, 5);

        VBox seriesOptionsContainer = new VBox(10);
        seriesOptionsContainer.getChildren().addAll(addFormNewSeriesPanel, addFormExistingSeriesRow);
        form.add(seriesOptionsContainer, 1, 6);

        HBox navBar = new HBox(10,
                courseButton,
                studyAvailabilityButton,
                dashboardButton,
                bigPictureButton
        );
        navBar.setAlignment(Pos.CENTER_LEFT);

        VBox topContainer = new VBox(navBar);
        topContainer.setPadding(new Insets(0, 0, 20, 0));
        setTop(topContainer);

        VBox assignmentButtons = new VBox(10, addButton, deleteButton);

        GridPane seriesBox = new GridPane();
        seriesBox.setHgap(10);
        seriesBox.setVgap(10);

        seriesBox.add(new Label("Series ID"), 0, 0);
        seriesBox.add(seriesIdField, 1, 0);

        seriesBox.add(new Label("Series Name"), 0, 1);
        seriesBox.add(seriesNameField, 1, 1);

        seriesBox.add(new Label("Default Late Days"), 0, 2);
        seriesBox.add(seriesDefaultLateDaysField, 1, 2);

        seriesBox.add(createSeriesButton, 1, 3);

        VBox trackingBox = new VBox(
                10,
                clockButton,
                manualHoursField,
                applyHoursButton,
                markDoneButton);

        trackingBox.setVisible(false);
        trackingBox.setManaged(false);
        trackingBox.setMaxWidth(300);

        assignmentList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        assignmentList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(AssignmentListEntry item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // HEADER row
                if (item.isHeader()) {
                    setText(item.getHeaderText());
                    setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                    setGraphic(null);
                    setDisable(true);
                    return;
                }

                // normal assignment row
                setDisable(false);
                setStyle("");

                Assignment a = item.getAssignment();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                String mainText = a.getName() + " (" + a.getCourseID() + ")"        
                        + " | Start: " + a.getStart().format(formatter)
                        + " | Deadline: " + a.getDeadline().format(formatter)
                        + " | Estimated: " + TimeFormatUtils.formatHoursAsHHMM(a.getEstimatedHours())
                        + " | Remaining: " + TimeFormatUtils.formatHoursAsHHMM(a.getRemainingHours())
                        + " | Cumulative: " + TimeFormatUtils.formatHoursAsHHMM(a.getCumulativeHours());
                Label mainLabel = new Label(mainText);

                HBox row;
                if (item.getDisplayName() != null) {
                    Label tag = new Label(item.getDisplayName());
                    String tagColor = tagColorForSeries(item.getDisplayName());
                    tag.setStyle("-fx-background-color: " + tagColor + "; "
                            + "-fx-background-radius: 9999px; "
                            + "-fx-padding: 4 10; -fx-font-size: 11px; "
                            + "-fx-text-fill: white; -fx-font-weight: bold;");
                    row = new HBox(10, mainLabel, tag);
                } else {
                    row = new HBox(10, mainLabel);
                }

                HBox.setHgrow(mainLabel, Priority.ALWAYS);
                setGraphic(row);
                setText(null);
            }
        });

        assignmentList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    boolean hasSelection = getFirstSelectedAssignment() != null;
                    trackingBox.setVisible(hasSelection);
                    trackingBox.setManaged(hasSelection);
                });

        VBox leftPanel = new VBox(20,
                title,
                form,
                assignmentButtons,
                seriesTitle,
                seriesBox
        );

        VBox rightPanel = new VBox(10,
                assignmentsTitle,
                assignmentList,
                trackingBox
        );

        assignmentList.setPrefHeight(400);
        assignmentList.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(assignmentList, Priority.ALWAYS);

        HBox main = new HBox(50, leftPanel, rightPanel);
        setCenter(main);

        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        HBox bottom = new HBox(backButton);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        setBottom(bottom);

        courseBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (SERIES_CHOICE_EXISTING.equals(seriesChoiceBox.getValue()) && presenter != null && newVal != null && !newVal.isBlank()) {
                refreshExistingSeriesForCourse(newVal);
            }
        });

        addButton.setOnAction(e -> handleCreate());
        deleteButton.setOnAction(e -> handleDelete());
        createSeriesButton.setOnAction(e -> handleCreateSeriesAndLink());
        clockButton.setOnAction(e -> handleClockButton());
        applyHoursButton.setOnAction(e -> handleApplyHours());
        markDoneButton.setOnAction(e -> handleMarkDone());
        backButton.setOnAction(e -> {
            if (presenter != null) presenter.back();
        });

        courseBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (suppressCourseListener) return;
            if (presenter != null) presenter.setCourseFilter(newVal);
        });
    }

    public Button getBigPictureButton() { return bigPictureButton; }

    private void handleClockButton() {
        Assignment selected = getFirstSelectedAssignment();
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

    public void setPresenter(AssignmentPresenter presenter) {
        this.presenter = presenter;
    }

    private void handleCreate() {
        try {
            String name = nameField.getText().trim();
            double estimate = Double.parseDouble(estimatedHoursField.getText().trim());
            String course = courseBox.getValue();

            LocalDate startDate = startPicker.getValue();
            LocalDate deadlineDate = deadlinePicker.getValue();

            if (course == null || ALL_COURSES.equals(course) || startDate == null || deadlineDate == null) {
                throw new IllegalArgumentException("All fields required");
            }

            String seriesChoice = seriesChoiceBox.getValue();

            if (seriesChoice == null || SERIES_CHOICE_NONE.equals(seriesChoice)) {
                presenter.createAssignment(
                        name,
                        course,
                        startDate.atStartOfDay(),
                        deadlineDate.atStartOfDay(),
                        0,
                        estimate,
                        null
                );
            } else if (SERIES_CHOICE_NEW.equals(seriesChoice)) {
                String seriesName = addFormNewSeriesNameField.getText().trim();
                if (seriesName.isEmpty()) {
                    throw new IllegalArgumentException("Series name is required when creating a new series");
                }
                String seriesId = addFormNewSeriesIdField.getText().trim();
                if (seriesId.isEmpty()) {
                    seriesId = "series-" + UUID.randomUUID().toString();
                }
                int defaultLateDays = 0;
                String lateDaysStr = addFormNewDefaultLateDaysField.getText().trim();
                if (!lateDaysStr.isEmpty()) {
                    defaultLateDays = Integer.parseInt(lateDaysStr);
                }
                presenter.createSeries(seriesId, course, seriesName, defaultLateDays);
                presenter.createAssignment(
                        name,
                        course,
                        startDate.atStartOfDay(),
                        deadlineDate.atStartOfDay(),
                        defaultLateDays,
                        estimate,
                        seriesId
                );
                clearAddFormSeriesInputs();
            } else if (SERIES_CHOICE_EXISTING.equals(seriesChoice)) {
                Series selected = existingSeriesBox.getValue();
                if (selected == null) {
                    throw new IllegalArgumentException("Select a series to add this assignment to");
                }
                presenter.createAssignment(
                        name,
                        course,
                        startDate.atStartOfDay(),
                        deadlineDate.atStartOfDay(),
                        selected.getDefaultLateDays(),
                        estimate,
                        selected.getId()
                );
            }

            clearInputs();

        } catch (NumberFormatException ex) {
            new Alert(Alert.AlertType.ERROR, "Enter valid numbers for estimated hours and late days").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void refreshExistingSeriesForCourse(String courseId) {
        if (presenter == null) return;
        List<Series> seriesList = presenter.getSeriesForCourse(courseId);
        Series current = existingSeriesBox.getValue();
        existingSeriesBox.getItems().setAll(seriesList);
        existingSeriesBox.getSelectionModel().clearSelection();
        if (current != null && seriesList.contains(current)) {
            existingSeriesBox.getSelectionModel().select(current);
        }
    }

    private void clearAddFormSeriesInputs() {
        addFormNewSeriesNameField.clear();
        addFormNewSeriesIdField.clear();
        addFormNewDefaultLateDaysField.setText("0");
    }

    private void handleDelete() {
        Assignment selected = getFirstSelectedAssignment();
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

            if (presenter == null) throw new IllegalStateException("Presenter is not attached");

            presenter.createSeriesAndLinkSelected(seriesId, seriesName, defaultLateDays, selectedAssignmentIds);
            clearSeriesInputs();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void clearInputs() {
        nameField.clear();
        estimatedHoursField.clear();
        startPicker.setValue(null);
        deadlinePicker.setValue(null);
        clearAddFormSeriesInputs();
        seriesChoiceBox.getSelectionModel().select(SERIES_CHOICE_NONE);
        existingSeriesBox.getSelectionModel().clearSelection();
    }

    private void clearSeriesInputs() {
        seriesIdField.clear();
        seriesNameField.clear();
        seriesDefaultLateDaysField.clear();
    }

    private void handleApplyHours() {
        Assignment selected = getFirstSelectedAssignment();
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
        Assignment selected = getFirstSelectedAssignment();
        if (selected == null) return;

        try {
            presenter.markDone(selected.getID());
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    public void showGroupedAssignments(List<AssignmentListEntry> entries) {
        assignmentList.getItems().setAll(entries);
    }

    public void setCourses(List<String> courses) {
        String currentSelection = courseBox.getValue();

        List<String> items = new ArrayList<>();
        items.add(ALL_COURSES);
        if (courses != null) items.addAll(courses);

        suppressCourseListener = true;
        courseBox.getItems().setAll(items);

        if (currentSelection != null && items.contains(currentSelection)) {
            courseBox.setValue(currentSelection);
        } else {
            courseBox.setValue(ALL_COURSES);
        }
        suppressCourseListener = false;
    }

    public void setSelectedCourse(String courseIdOrAllCourses) {
        suppressCourseListener = true;
        courseBox.setValue(courseIdOrAllCourses);
        suppressCourseListener = false;
    }

    public List<String> getSelectedAssignmentIds() {
        List<String> selectedIds = new ArrayList<>();
        for (AssignmentListEntry entry : assignmentList.getSelectionModel().getSelectedItems()) {
            if (entry.getAssignment() != null) selectedIds.add(entry.getAssignment().getID());
        }
        return selectedIds;
    }

    private Assignment getFirstSelectedAssignment() {
        for (AssignmentListEntry entry : assignmentList.getSelectionModel().getSelectedItems()) {
            if (entry.getAssignment() != null) return entry.getAssignment();
        }
        return null;
    }

    public void selectAssignment(Assignment assignment) {
        if (assignment == null) return;
        List<AssignmentListEntry> items = assignmentList.getItems();
        for (int i = 0; i < items.size(); i++) {
            AssignmentListEntry entry = items.get(i);
            if (assignment.equals(entry.getAssignment())) {
                assignmentList.getSelectionModel().clearAndSelect(i);
                return;
            }
        }
    }

    private static final String[] SERIES_TAG_COLORS = {
            "#4A90D9", "#7B68A6", "#50A060", "#C07850", "#B85450",
            "#5B9AA0", "#E8A838", "#6B8E6B", "#9B6B8E", "#4A7C9E"
    };

    private static String tagColorForSeries(String seriesName) {
        int index = Math.abs(seriesName.hashCode()) % SERIES_TAG_COLORS.length;
        return SERIES_TAG_COLORS[index];
    }

    public Button getCoursesButton() { return courseButton; }
    public Button getStudyAvailabilityButton() { return studyAvailabilityButton; }
    public Button getDashboardButton() { return dashboardButton; }

    public Button getBackButton() { return backButton; }
}