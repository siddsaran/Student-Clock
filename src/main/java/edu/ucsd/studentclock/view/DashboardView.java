package edu.ucsd.studentclock.view;

import java.time.LocalDateTime;
import java.util.List;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.AssignmentStatusCalculator;
import edu.ucsd.studentclock.presenter.DashboardPresenter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class DashboardView extends BorderPane {

    private DashboardPresenter presenter;

    private final TableView<Assignment> table = new TableView<>();
    private final Label studyHoursLabel = new Label("Study Hours Remaining 0");
    private final Region studyStatusIndicator = new Region();
    private final Button showOpenButton = new Button("Show Open");
    private final Button bigPictureButton = new Button("Big Picture");

    public DashboardView() {

        setPadding(new Insets(20));

        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        studyStatusIndicator.setPrefSize(25, 25);
        studyStatusIndicator.setStyle("-fx-background-radius: 6;");

        HBox statusRow = new HBox(15, studyStatusIndicator);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        HBox topBar = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(title, spacer, studyHoursLabel);

        VBox topContainer = new VBox(10, topBar, statusRow);
        setTop(topContainer);

        TableColumn<Assignment, String> nameCol =
                new TableColumn<>("Assignment");
        nameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getName()));

        TableColumn<Assignment, String> courseCol =
                new TableColumn<>("Course");
        courseCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCourseID()));

        TableColumn<Assignment, String> dueCol =
                new TableColumn<>("Due Date");
        dueCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDeadline().toLocalDate().toString()));

        TableColumn<Assignment, String> remainingCol =
                new TableColumn<>("Hours Remaining");
        remainingCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(c.getValue().getRemainingHours())));

        table.getColumns().addAll(nameCol, courseCol, dueCol, remainingCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setRowFactory(tv -> new TableRow<Assignment>() {
            @Override
            protected void updateItem(Assignment a, boolean empty) {
                super.updateItem(a, empty);

                if (a == null || empty) {
                    setStyle("");
                    return;
                }

                AssignmentStatus status =
                        AssignmentStatusCalculator.behindStatus(
                                a, LocalDateTime.now());

                switch (status) {
                    case RED:
                        setStyle("-fx-background-color:#e75b67;");
                        break;
                    case ORANGE:
                        setStyle("-fx-background-color:#f69d20;");
                        break;
                    case YELLOW:
                        setStyle("-fx-background-color:#ffdf74;");
                        break;
                    default:
                        setStyle("");
                        break;
                }
            }
        });

        table.setOnMouseClicked(e -> {
            Assignment selected =
                    table.getSelectionModel().getSelectedItem();
            if (selected != null && presenter != null) {
                presenter.openAssignment(selected);
            }
        });

        setCenter(table);

        HBox bottom = new HBox(15, showOpenButton, bigPictureButton);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(15));
        setBottom(bottom);
    }

    public Button getBigPictureButton() {
        return bigPictureButton;
    }

    public void setPresenter(DashboardPresenter presenter) {
        this.presenter = presenter;
    }

    public void showAssignments(List<Assignment> assignments,
                                LocalDateTime now) {
        table.setItems(FXCollections.observableArrayList(assignments));
    }

    public void setStudyHoursRemaining(int hours) {
        studyHoursLabel.setText("Study Hours Remaining  " + hours);
    }

    public void setStudyStatus(AssignmentStatus status) {

        Color color = Color.LIMEGREEN;

        switch (status) {
            case YELLOW:
                color = Color.GOLD;
                break;
            case ORANGE:
                color = Color.ORANGE;
                break;
            case RED:
                color = Color.RED;
                break;
            case URGENT:
                color = Color.DARKRED;
                break;
            default:
                break;
        }

        studyStatusIndicator.setStyle(
                "-fx-background-color:" + toRgb(color) +
                        "; -fx-background-radius:6;");
    }

    private String toRgb(Color c) {
        return String.format(
                "rgb(%d,%d,%d)",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }

    public Button getShowOpenButton() {
        return showOpenButton;
    }
}
