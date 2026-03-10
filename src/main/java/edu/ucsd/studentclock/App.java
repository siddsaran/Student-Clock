package edu.ucsd.studentclock;

import edu.ucsd.studentclock.datasource.*;
import edu.ucsd.studentclock.model.*;
import edu.ucsd.studentclock.service.*;
import edu.ucsd.studentclock.repository.*;
import edu.ucsd.studentclock.view.*;
import edu.ucsd.studentclock.presenter.*;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for Student Clock.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        // Single data source for all repositories
        IDataSource dataSource = new SqlDataSource("studentclock.db");

        // Repositories
        RepositoryFactory repoFactory = new RepositoryFactory(dataSource);

        ICourseRepository courseRepository = repoFactory.createCourseRepository();
        ISeriesRepository seriesRepository = repoFactory.createSeriesRepository();
        IAssignmentRepository assignmentRepository = repoFactory.createAssignmentRepository();
        IStudyAvailabilityRepository studyAvailabilityRepository = repoFactory.createStudyAvailabilityRepository();
        WorkLogRepository workLogRepository = repoFactory.createWorkLogRepository();
        AssignmentWorkLogRepository assignmentWorkLogRepository = repoFactory.createAssignmentWorkLogRepository();

        // Shared model
        ITimeService timeService = new TimeService();
        Model sharedModel = new Model(courseRepository, assignmentRepository, seriesRepository, studyAvailabilityRepository, workLogRepository, assignmentWorkLogRepository, timeService);

        // Views
        ViewFactory viewFactory = new ViewFactory();

        CourseView courseView = viewFactory.createCourseView();
        AssignmentView assignmentView = viewFactory.createAssignmentView();
        StudyAvailabilityView studyAvailabilityView = viewFactory.createStudyAvailabilityView();
        DashboardView dashboardView = viewFactory.createDashboardView();
        BigPictureView bigPictureView = viewFactory.createBigPictureView();

        MainLayoutView mainLayout = new MainLayoutView();

        // Presenters
        PresenterFactory factory = new PresenterFactory(
                        sharedModel,
                        courseRepository,
                        assignmentRepository,
                        studyAvailabilityRepository,
                        workLogRepository,
                        assignmentWorkLogRepository
                );

        CoursePresenter coursePresenter = factory.createCoursePresenter(courseView);
        AssignmentPresenter assignmentPresenter = factory.createAssignmentPresenter(assignmentView);
        StudyAvailabilityPresenter studyAvailabilityPresenter =
                factory.createStudyAvailabilityPresenter(studyAvailabilityView);
        DashboardPresenter dashboardPresenter = factory.createDashboardPresenter(dashboardView);
        BigPicturePresenter bigPicturePresenter = factory.createBigPicturePresenter(bigPictureView);



        // Single scene with shared nav bar
        javafx.scene.Scene scene = new javafx.scene.Scene(mainLayout, 1200, 800);
        primaryStage.setScene(scene);

        // Navigation manager
        PresenterManager manager = new PresenterManager();
        manager.defineInteractions(
                primaryStage,
                "Student Clock",
                mainLayout,
                dashboardPresenter,
                coursePresenter,
                assignmentPresenter,
                studyAvailabilityPresenter,
                bigPicturePresenter
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
