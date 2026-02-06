package edu.ucsd.studentclock;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.datasource.SqlDataSource;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.presenter.AssignmentPresenter;
import edu.ucsd.studentclock.presenter.ExamplePresenter1;
import edu.ucsd.studentclock.presenter.PresenterManager;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.view.AssignmentView;
import edu.ucsd.studentclock.view.ExampleView1;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

        // DataSource abstraction (your design)
        IDataSource dataSource = new SqlDataSource(JDBC_URL);

        // Repositories
        CourseRepository courseRepository = new CourseRepository(connection);
        AssignmentRepository assignmentRepository = new AssignmentRepository(dataSource);

        // Shared model
        Model sharedModel = new Model(courseRepository);

        // Views
        ExampleView1 courseView = new ExampleView1();
        AssignmentView assignmentView = new AssignmentView();

        // Presenters
        ExamplePresenter1 coursePresenter =
                new ExamplePresenter1(sharedModel, courseView);

        AssignmentPresenter assignmentPresenter =
                new AssignmentPresenter(sharedModel, assignmentView, assignmentRepository);

        // Navigation manager
        PresenterManager manager = new PresenterManager();
        manager.defineInteractions(
                primaryStage,
                "Student Clock",
                coursePresenter,
                assignmentPresenter
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
