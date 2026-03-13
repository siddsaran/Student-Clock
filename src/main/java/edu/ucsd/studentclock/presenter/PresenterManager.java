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

                // Facade for centralized navigation rules
                NavigationFacade navFacade = new NavigationFacade(
                                () -> switcher.switchTo(dashboardPresenter),
                                () -> switcher.switchTo(coursePresenter),
                                () -> switcher.switchTo(studyAvailabilityPresenter),
                                () -> switcher.switchTo(bigPicturePresenter),
                                () -> switcher.switchTo(assignmentPresenter),
                                assignmentPresenter::showAllAssignments,
                                assignmentPresenter::showOpenAssignments);

                // Global nav bar - same on every page
                mainLayout.getDashboardButton().setOnAction(e -> navFacade.toDashboard());
                mainLayout.getCoursesButton().setOnAction(e -> navFacade.toCourses());
                mainLayout.getAssignmentsButton().setOnAction(e -> navFacade.toAssignmentsAll());
                mainLayout.getStudyAvailabilityButton().setOnAction(e -> navFacade.toStudyAvailability());
                mainLayout.getBigPictureButton().setOnAction(e -> navFacade.toBigPicture());

                // Study Availability specific navigation (Back button)
                StudyAvailabilityView studyAvailabilityView = (StudyAvailabilityView) studyAvailabilityPresenter
                                .getView();
                studyAvailabilityView.getBackButton().setOnAction(e -> navFacade.toCourses());

                // Dashboard-specific actions (Show Open, Big Picture buttons on dashboard)
                DashboardView dashboardView = (DashboardView) dashboardPresenter.getView();
                dashboardView.getShowOpenButton().setOnAction(e -> navFacade.toAssignmentsOpen());
                dashboardView.getBigPictureButton().setOnAction(e -> navFacade.toBigPicture());

                // Initial screen
                switcher.switchTo(dashboardPresenter);
        }
}