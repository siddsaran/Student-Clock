package edu.ucsd.studentclock;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.presenter.ExamplePresenter1;
import edu.ucsd.studentclock.presenter.ExamplePresenter2;
import edu.ucsd.studentclock.presenter.PresenterManager;
import edu.ucsd.studentclock.repository.CourseRepository;
import edu.ucsd.studentclock.view.ExampleView1;
import edu.ucsd.studentclock.view.ExampleView2;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App extends Application {

    private static final String JDBC_URL = "jdbc:sqlite:studentclock.db";

    private Connection connection;

    @Override
    public void start(Stage primaryStage) {
        try {
            connection = DriverManager.getConnection(JDBC_URL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
        CourseRepository repository = new CourseRepository(connection);
        Model sharedModel = new Model(repository);

        ExampleView1 view1 = new ExampleView1();
        ExampleView2 view2 = new ExampleView2();

        ExamplePresenter1 presenter1 = new ExamplePresenter1(sharedModel, view1);
        ExamplePresenter2 presenter2 = new ExamplePresenter2(sharedModel, view2);

        PresenterManager manager = new PresenterManager();
        manager.defineInteractions(primaryStage, "Student Clock", presenter1, presenter2);
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

