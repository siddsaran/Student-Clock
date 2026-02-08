package edu.ucsd.studentclock.presenter;

import javafx.stage.Stage;

/**
 * Manages navigation between presenters.
 */
public class PresenterManager {

    /**
     * Defines navigation interactions between Course and Assignment presenters.
     *
     * @param stage application stage
     * @param appName application name
     * @param coursePresenter presenter for course screen
     * @param assignmentPresenter presenter for assignment screen
     */
    public void defineInteractions(Stage stage,
                                   String appName,
                                   CoursePresenter coursePresenter,
                                   AssignmentPresenter assignmentPresenter) {

        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName);

        // Navigate from courses to assignments
        coursePresenter.setOnNavigateToAssignments(() -> switcher.switchTo(assignmentPresenter));

        // Navigate from assignment back to courses
        assignmentPresenter.setOnBack(() -> switcher.switchTo(coursePresenter));

        // Initial screen
        switcher.switchTo(coursePresenter);
    }
}