package edu.ucsd.studentclock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.datasource.SqlDataSource;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.presenter.AssignmentPresenter;
import edu.ucsd.studentclock.presenter.BigPicturePresenter;
import edu.ucsd.studentclock.presenter.CoursePresenter;
import edu.ucsd.studentclock.presenter.DashboardPresenter;
import edu.ucsd.studentclock.presenter.PresenterManager;
import edu.ucsd.studentclock.presenter.StudyAvailabilityPresenter;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.repository.SeriesRepository;
import edu.ucsd.studentclock.repository.StudyAvailabilityRepository;
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

    private static final String JDBC_URL = "jdbc:sqlite:studentclock.db";

    private Connection connection;

    @Override
    public void start(Stage primaryStage) {

        // SQLite connection
        try {
            connection = DriverManager.getConnection(JDBC_URL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }

        // DataSource abstraction (same DB file as connection above)
        IDataSource dataSource = new SqlDataSource("studentclock.db");

        // Repositories
        CourseRepository courseRepository = new CourseRepository(connection);
        SeriesRepository seriesRepository = new SeriesRepository(connection);
        AssignmentRepository assignmentRepository = new AssignmentRepository(dataSource);
        StudyAvailabilityRepository studyAvailabilityRepository = new StudyAvailabilityRepository(connection);

        // Shared model
        Model sharedModel = new Model(courseRepository, assignmentRepository, seriesRepository, studyAvailabilityRepository);

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
                new AssignmentPresenter(sharedModel, assignmentView, assignmentRepository);
        
        StudyAvailabilityPresenter studyAvailabilityPresenter =
                new StudyAvailabilityPresenter(sharedModel, studyAvailabilityView);
        DashboardPresenter dashboardPresenter =
            new DashboardPresenter(sharedModel, dashboardView, assignmentRepository);
        dashboardView.setPresenter(dashboardPresenter);

        BigPicturePresenter bigPicturePresenter =
            new BigPicturePresenter(sharedModel, bigPictureView, assignmentRepository);
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

    @Override
    public void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore on shutdown
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
