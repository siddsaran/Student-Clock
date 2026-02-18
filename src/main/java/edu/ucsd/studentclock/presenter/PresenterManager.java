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
                                   DashboardPresenter dashboardPresenter,
                                   CoursePresenter coursePresenter,
                                   AssignmentPresenter assignmentPresenter,
                                   StudyAvailabilityPresenter studyAvailabilityPresenter) {

        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName);

        // Navigate from courses to assignments
        coursePresenter.setOnNavigateToAssignments(() -> switcher.switchTo(assignmentPresenter));
        
        // Navigate from courses to study availability
        coursePresenter.setOnNavigateToStudyAvailability(() -> switcher.switchTo(studyAvailabilityPresenter));

        // Navigate from courses to dashboard
        coursePresenter.setOnNavigateToDashboard(() -> switcher.switchTo(dashboardPresenter));

        // Navigate from assignment back to courses
        assignmentPresenter.setOnBack(() -> switcher.switchTo(coursePresenter));

        // Topbar assignments page
        assignmentPresenter.setOnCourses(() -> switcher.switchTo(coursePresenter));
        assignmentPresenter.setOnStudyAvailability(() -> switcher.switchTo(studyAvailabilityPresenter));
        assignmentPresenter.setOnDashboard(() -> switcher.switchTo(dashboardPresenter));

        // Navigate from study availability back to courses
        studyAvailabilityPresenter.setOnBack(() -> switcher.switchTo(coursePresenter));

        // Navigate from dashboard back to courses
        dashboardPresenter.setOnBack(() -> switcher.switchTo(assignmentPresenter));



        // Initial screen
        switcher.switchTo(dashboardPresenter);
    }
}