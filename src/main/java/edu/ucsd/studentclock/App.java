package edu.ucsd.studentclock;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.datasource.SqlDataSource;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.service.ITimeService;
import edu.ucsd.studentclock.service.TimeService;
import edu.ucsd.studentclock.presenter.AssignmentPresenter;
import edu.ucsd.studentclock.presenter.BigPicturePresenter;
import edu.ucsd.studentclock.presenter.CoursePresenter;
import edu.ucsd.studentclock.presenter.DashboardPresenter;
import edu.ucsd.studentclock.presenter.PresenterManager;
import edu.ucsd.studentclock.presenter.StudyAvailabilityPresenter;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;
import edu.ucsd.studentclock.repository.StudyAvailabilityRepository;
import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.repository.ICourseRepository;
import edu.ucsd.studentclock.repository.ISeriesRepository;
import edu.ucsd.studentclock.repository.IStudyAvailabilityRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import edu.ucsd.studentclock.view.AssignmentView;
import edu.ucsd.studentclock.view.BigPictureView;
import edu.ucsd.studentclock.view.CourseView;
import edu.ucsd.studentclock.view.DashboardView;
import edu.ucsd.studentclock.view.StudyAvailabilityView;
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

        // Repositories (depend on IDataSource abstraction)
        ICourseRepository courseRepository = new CourseRepository(dataSource);
        ISeriesRepository seriesRepository = new SeriesRepository(dataSource);
        IAssignmentRepository assignmentRepository = new AssignmentRepository(dataSource);
        IStudyAvailabilityRepository studyAvailabilityRepository = new StudyAvailabilityRepository(dataSource);

        AssignmentWorkLogRepository assignmentWorkLogRepository = new AssignmentWorkLogRepository(dataSource);
        WorkLogRepository workLogRepository = new WorkLogRepository(dataSource);

        // Shared model (depends on repository and time-service abstractions)
        ITimeService timeService = new TimeService();
        Model sharedModel = new Model(courseRepository, assignmentRepository, seriesRepository, studyAvailabilityRepository, timeService);

        // Views
        CourseView courseView = new CourseView();
        AssignmentView assignmentView = new AssignmentView();
        StudyAvailabilityView studyAvailabilityView = new StudyAvailabilityView();
        DashboardView dashboardView = new DashboardView();
        BigPictureView bigPictureView = new BigPictureView();

        // Presenters
        CoursePresenter coursePresenter =
                new CoursePresenter(sharedModel, courseView);

        AssignmentPresenter assignmentPresenter =
                new AssignmentPresenter(sharedModel, assignmentView, assignmentRepository, workLogRepository, assignmentWorkLogRepository);
        
        StudyAvailabilityPresenter studyAvailabilityPresenter =
                new StudyAvailabilityPresenter(sharedModel, studyAvailabilityView);
        DashboardPresenter dashboardPresenter =
            new DashboardPresenter(sharedModel, dashboardView, assignmentRepository, workLogRepository);
        dashboardView.setPresenter(dashboardPresenter);

        BigPicturePresenter bigPicturePresenter =
            new BigPicturePresenter(sharedModel, bigPictureView, assignmentRepository, assignmentWorkLogRepository);
        bigPictureView.setPresenter(bigPicturePresenter);



        // Navigation manager
        PresenterManager manager = new PresenterManager();
                manager.defineInteractions(
                primaryStage,
                "Student Clock",
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
