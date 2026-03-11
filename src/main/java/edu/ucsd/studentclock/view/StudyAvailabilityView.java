package edu.ucsd.studentclock.view;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;

import edu.ucsd.studentclock.presenter.IStudyAvailabilityScreenPresenter;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class StudyAvailabilityView extends VBox {

    private IStudyAvailabilityScreenPresenter presenter;
    

    private final Label titleLabel = new Label("Study Availability");
    private final Label promptLabel = new Label("Total study hours this week (integer):");
    private final TextField weeklyHoursField = new TextField();

    private final Map<DayOfWeek, CheckBox> dayCheckboxes = new EnumMap<>(DayOfWeek.class);
    private final Label daysLabel = new Label("Available days:");

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

        getChildren().addAll(
                titleLabel,
                promptLabel,
                weeklyHoursField,
                saveButton,
                backButton,
                statusLabel,
                daysLabel
        );

        addDayCheckbox(DayOfWeek.MONDAY, "Mon");
        addDayCheckbox(DayOfWeek.TUESDAY, "Tues");
        addDayCheckbox(DayOfWeek.WEDNESDAY, "Wed");
        addDayCheckbox(DayOfWeek.THURSDAY, "Thurs");
        addDayCheckbox(DayOfWeek.FRIDAY, "Fri");
        addDayCheckbox(DayOfWeek.SATURDAY, "Sat");
        addDayCheckbox(DayOfWeek.SUNDAY, "Sun");
    }

    private void addDayCheckbox(DayOfWeek day, String label) {
        CheckBox cb = new CheckBox(label);
        dayCheckboxes.put(day, cb);
        getChildren().add(cb);
    }

    /** Called by the presenter constructor */
    public void setPresenter(IStudyAvailabilityScreenPresenter presenter) {
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

    public void setDaySelected(DayOfWeek day, boolean selected) {
        CheckBox cb = dayCheckboxes.get(day);
        if (cb != null) {
            cb.setSelected(selected);
        }
    }

    public boolean isDaySelected(DayOfWeek day) {
        CheckBox cb = dayCheckboxes.get(day);
        return (cb != null && cb.isSelected());
    }

    public void setOnDayToggled(DayOfWeek day, Runnable action) {
        CheckBox cb = dayCheckboxes.get(day);
        if (cb != null) cb.setOnAction(e -> action.run());
    }

    public Button getBackButton() {
        return backButton;
    }
}
