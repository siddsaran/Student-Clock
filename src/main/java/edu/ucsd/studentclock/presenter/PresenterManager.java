package edu.ucsd.studentclock.presenter;

import javafx.stage.Stage;

public class PresenterManager {
    public void defineInteractions(Stage stage, String appName,
            AbstractPresenter coursePresenter,
            AssignmentPresenter assignmentPresenter) {

        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName);

        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName);

        // When assignment screen presses back → go to courses
        assignmentPresenter.setOnBack(() -> switcher.switchTo(coursePresenter));

        // Initial screen
        switcher.switchTo(coursePresenter);
    }
}

