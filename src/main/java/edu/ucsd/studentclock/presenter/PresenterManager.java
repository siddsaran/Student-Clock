package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.view.AssignmentView;
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
                                   StudyAvailabilityPresenter studyAvailabilityPresenter,
                                   BigPicturePresenter bigPicturePresenter) {

        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName);

        // Navigate from courses to assignments
        coursePresenter.setOnNavigateToAssignments(() -> {
            assignmentPresenter.setShowOnlyOpen(false);
            switcher.switchTo(assignmentPresenter);
        });
        
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
        assignmentPresenter.setOnBigPicture(() -> switcher.switchTo(bigPicturePresenter));

        // Navigate from study availability back to courses
        studyAvailabilityPresenter.setOnBack(() -> switcher.switchTo(coursePresenter));

        // Navigate from dashboard to courses
        dashboardPresenter.setOnShowOpenAssignments(() -> {
            assignmentPresenter.setShowOnlyOpen(true);
            assignmentPresenter.setCourseFilter(AssignmentView.ALL_COURSES);
            switcher.switchTo(assignmentPresenter);
        });

        dashboardPresenter.setOnAllAssignments(() -> {
            assignmentPresenter.setShowOnlyOpen(false);
            switcher.switchTo(assignmentPresenter);
        });

        // Navigate from dashboard to BigPictureView
        dashboardPresenter.setOnBigPicture(() -> switcher.switchTo(bigPicturePresenter));

        // Big Picture global nav
        bigPicturePresenter.setOnBack(() -> switcher.switchTo(dashboardPresenter));
        bigPicturePresenter.setOnCourses(() -> switcher.switchTo(coursePresenter));
        bigPicturePresenter.setOnAssignments(() -> switcher.switchTo(assignmentPresenter));
        bigPicturePresenter.setOnStudyAvailability(() -> switcher.switchTo(studyAvailabilityPresenter));
        bigPicturePresenter.setOnDashboard(() -> switcher.switchTo(dashboardPresenter));




        // Initial screen
        switcher.switchTo(dashboardPresenter);
    }
}