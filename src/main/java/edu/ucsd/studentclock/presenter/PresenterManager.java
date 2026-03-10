package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.view.AssignmentView;
import edu.ucsd.studentclock.view.MainLayoutView;
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
     * @param mainLayout shared layout with nav bar
     * @param dashboardPresenter presenter for dashboard
     * @param coursePresenter presenter for course screen
     * @param assignmentPresenter presenter for assignment screen
     * @param studyAvailabilityPresenter presenter for study availability
     * @param bigPicturePresenter presenter for big picture
     */
    public void defineInteractions(Stage stage,
                                   String appName,
                                   MainLayoutView mainLayout,
                                   DashboardPresenter dashboardPresenter,
                                   CoursePresenter coursePresenter,
                                   AssignmentPresenter assignmentPresenter,
                                   StudyAvailabilityPresenter studyAvailabilityPresenter,
                                   BigPicturePresenter bigPicturePresenter) {

        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName, mainLayout);

        // Global nav bar - same on every page
        mainLayout.getDashboardButton().setOnAction(e -> switcher.switchTo(dashboardPresenter));
        mainLayout.getCoursesButton().setOnAction(e -> switcher.switchTo(coursePresenter));
        mainLayout.getAssignmentsButton().setOnAction(e -> {
            assignmentPresenter.setShowOnlyOpen(false);
            switcher.switchTo(assignmentPresenter);
        });
        mainLayout.getStudyAvailabilityButton().setOnAction(e -> switcher.switchTo(studyAvailabilityPresenter));
        mainLayout.getBigPictureButton().setOnAction(e -> switcher.switchTo(bigPicturePresenter));

        studyAvailabilityPresenter.setOnBack(() -> switcher.switchTo(coursePresenter));

        // Dashboard-specific actions (Show Open, Big Picture buttons on dashboard)
        dashboardPresenter.setOnShowOpenAssignments(() -> {
            assignmentPresenter.setShowOnlyOpen(true);
            assignmentPresenter.setCourseFilter(AssignmentView.ALL_COURSES);
            switcher.switchTo(assignmentPresenter);
        });

        dashboardPresenter.setOnAllAssignments(() -> {
            assignmentPresenter.setShowOnlyOpen(false);
            switcher.switchTo(assignmentPresenter);
        });

        dashboardPresenter.setOnBigPicture(() -> switcher.switchTo(bigPicturePresenter));


        // Initial screen
        switcher.switchTo(dashboardPresenter);
    }
}