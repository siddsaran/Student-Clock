package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.presenter.StudyAvailabilityPresenter;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class StudyAvailabilityView extends VBox {

    private StudyAvailabilityPresenter presenter;

    private final Label titleLabel = new Label("Study Availability");
    private final Label promptLabel = new Label("Total study hours this week (integer):");
    private final TextField weeklyHoursField = new TextField();

    private final Button saveButton = new Button("Save");
    private final Button backButton = new Button("Back");

    private final Label statusLabel = new Label();

    public StudyAvailabilityView() {
        super(10);
        setPadding(new Insets(16));

        weeklyHoursField.setPromptText("e.g. 8");
        statusLabel.setWrapText(true);

        saveButton.setOnAction(e -> {
            if (presenter != null) presenter.onSave();
        });

        weeklyHoursField.setOnAction(e -> {
            if (presenter != null) presenter.onSave();
        });

        backButton.setOnAction(e -> {
            if (presenter != null) presenter.onBack();
        });

        getChildren().addAll(
                titleLabel,
                promptLabel,
                weeklyHoursField,
                saveButton,
                backButton,
                statusLabel
        );
    }

    /** Called by the presenter constructor */
    public void setPresenter(StudyAvailabilityPresenter presenter) {
        this.presenter = presenter;
    }

    // ---- View API used by presenter ----

    public String getWeeklyHoursText() {
        return weeklyHoursField.getText();
    }

    public void setWeeklyHoursText(String text) {
        weeklyHoursField.setText(text);
    }

    public void showMessage(String message) {
        statusLabel.setText(message);
    }

    public void showError(String message) {
        statusLabel.setText(message);
    }

    public void clearStatus() {
        statusLabel.setText("");
    }
}
