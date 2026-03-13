package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.view.MainLayoutView;
import edu.ucsd.studentclock.view.DashboardView;
import edu.ucsd.studentclock.view.StudyAvailabilityView;
import javafx.stage.Stage;

/**
 * Manages navigation between presenters.
 */
public class PresenterManager {

        public void defineInteractions(Stage stage,
                        String appName,
                        MainLayoutView mainLayout,
                        DashboardPresenter dashboardPresenter,
                        CoursePresenter coursePresenter,
                        AssignmentPresenter assignmentPresenter,
                        StudyAvailabilityPresenter studyAvailabilityPresenter,
                        BigPicturePresenter bigPicturePresenter) {

                PresenterSwitcher switcher = new PresenterSwitcher(stage, appName, mainLayout);

                // Centralized navigation rules
                NavigationRouter router = new NavigationRouter(
                                () -> switcher.switchTo(dashboardPresenter),
                                () -> switcher.switchTo(coursePresenter),
                                () -> switcher.switchTo(studyAvailabilityPresenter),
                                () -> switcher.switchTo(bigPicturePresenter),
                                () -> switcher.switchTo(assignmentPresenter),
                                assignmentPresenter::showAllAssignments,
                                assignmentPresenter::showOpenAssignments);

                // Global nav bar - same on every page
                mainLayout.getDashboardButton().setOnAction(e -> router.toDashboard());
                mainLayout.getCoursesButton().setOnAction(e -> router.toCourses());
                mainLayout.getAssignmentsButton().setOnAction(e -> router.toAssignmentsAll());
                mainLayout.getStudyAvailabilityButton().setOnAction(e -> router.toStudyAvailability());
                mainLayout.getBigPictureButton().setOnAction(e -> router.toBigPicture());

                // Study Availability specific navigation (Back button)
                StudyAvailabilityView studyAvailabilityView = (StudyAvailabilityView) studyAvailabilityPresenter
                                .getView();
                studyAvailabilityView.getBackButton().setOnAction(e -> router.toCourses());

                // Dashboard-specific actions (Show Open, Big Picture buttons on dashboard)
                DashboardView dashboardView = (DashboardView) dashboardPresenter.getView();
                dashboardView.getShowOpenButton().setOnAction(e -> router.toAssignmentsOpen());
                dashboardView.getBigPictureButton().setOnAction(e -> router.toBigPicture());

                // Initial screen
                switcher.switchTo(dashboardPresenter);
        }
}