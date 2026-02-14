package edu.ucsd.studentclock.view;

import java.time.LocalDateTime;
import java.util.List;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.AssignmentStatusCalculator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardView extends VBox {

    private final VBox listBox = new VBox(8);
    private final Button backButton = new Button("Back");

    public DashboardView() {
        setPadding(new Insets(20));
        setSpacing(15);

        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        getChildren().addAll(title, listBox, backButton);
    }

    public void showAssignments(List<Assignment> assignments, LocalDateTime now) {
        listBox.getChildren().clear();

        for (Assignment a : assignments) {
            Label row = new Label(a.getName() + " (" + a.getCourseID() + ")");
            row.setPadding(new Insets(10));
            row.setMinWidth(320);
            row.setMaxWidth(Double.MAX_VALUE);

            boolean urgent = AssignmentStatusCalculator.isUrgent(a, now);
            AssignmentStatus status =
                    AssignmentStatusCalculator.behindStatus(a, now);

            if (urgent) {
                row.setStyle("-fx-background-color: #e24747ff; -fx-background-radius: 8; -fx-font-weight: bold;");
            } else {
                switch (status) {
                    case YELLOW:
                        row.setStyle("-fx-background-color: #ffdf74ff; -fx-background-radius: 8; -fx-font-weight: bold;");
                        break;
                    case ORANGE:
                        row.setStyle("-fx-background-color: #f69d20ff; -fx-background-radius: 8; -fx-font-weight: bold;");
                        break;
                    case RED:
                        row.setStyle("-fx-background-color: #e75b67ff; -fx-background-radius: 8; -fx-font-weight: bold;");
                        break;
                    default:
                        break;
                }
            }


            listBox.getChildren().add(row);
        }
    }

    public Button getBackButton() {
        return backButton;
    }
}
