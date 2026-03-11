package edu.ucsd.studentclock.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Shared layout with a persistent navigation bar across all pages.
 * The content area (center) is swapped when switching between views.
 */
public class MainLayoutView extends BorderPane {

    private static final String ACTIVE_STYLE =
            "-fx-background-color: #e0e0e0; -fx-font-weight: bold;";
    private static final String INACTIVE_STYLE = "";

    private final Button dashboardButton = new Button("Dashboard");
    private final Button coursesButton = new Button("Courses");
    private final Button assignmentsButton = new Button("Assignments");
    private final Button studyAvailabilityButton = new Button("Study Availability");
    private final Button bigPictureButton = new Button("Big Picture");

    private final Button[] navButtons = {
            dashboardButton, coursesButton, assignmentsButton,
            studyAvailabilityButton, bigPictureButton
    };

    private final StackPane contentArea = new StackPane();

    public MainLayoutView() {
        setPadding(new Insets(0));

        HBox navBar = new HBox(12,
                dashboardButton,
                coursesButton,
                assignmentsButton,
                studyAvailabilityButton,
                bigPictureButton
        );
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(12, 20, 12, 20));
        navBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        navBar.getStyleClass().add("nav-bar");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        navBar.getChildren().add(spacer);

        contentArea.setPadding(new Insets(0));
        BorderPane.setMargin(contentArea, new Insets(0));

        setTop(navBar);
        setCenter(contentArea);
    }

    public void setContent(Region content) {
        contentArea.getChildren().clear();
        if (content != null) {
            contentArea.getChildren().add(content);
            content.setMaxWidth(Double.MAX_VALUE);
            content.setMaxHeight(Double.MAX_VALUE);
        }
    }

    /**
     * Highlights the nav button for the current page.
     *
     * @param viewTitle the title of the current view (e.g. "Dashboard", "Courses")
     */
    public void setActivePage(String viewTitle) {
        for (Button b : navButtons) {
            b.setStyle(b.getText().equals(viewTitle) ? ACTIVE_STYLE : INACTIVE_STYLE);
        }
    }

    public Button getDashboardButton() {
        return dashboardButton;
    }

    public Button getCoursesButton() {
        return coursesButton;
    }

    public Button getAssignmentsButton() {
        return assignmentsButton;
    }

    public Button getStudyAvailabilityButton() {
        return studyAvailabilityButton;
    }

    public Button getBigPictureButton() {
        return bigPictureButton;
    }
}
