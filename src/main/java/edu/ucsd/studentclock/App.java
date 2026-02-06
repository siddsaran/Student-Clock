package edu.ucsd.studentclock;

import edu.ucsd.studentclock.datasource.IDataSource;
import edu.ucsd.studentclock.datasource.SqlDataSource;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.presenter.AssignmentPresenter;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.view.AssignmentView;
import javafx.application.Application;
import javafx.stage.Stage;
/**
 * Entry point for Student Clock.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        // SQLite Data Source
        IDataSource dataSource =
                new SqlDataSource("jdbc:sqlite:studentclock.db");

        // Repositories
        AssignmentRepository assignmentRepository =
                new AssignmentRepository(dataSource);

        // Shared Model
        Model sharedModel = new Model();

        // Views 
        AssignmentView assignmentView = new AssignmentView();

        // Presenters
        AssignmentPresenter assignmentPresenter =
                new AssignmentPresenter(sharedModel, assignmentView, assignmentRepository);

        primaryStage.setTitle("Student Clock: Assignments");
        primaryStage.setScene(assignmentPresenter.getView());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
