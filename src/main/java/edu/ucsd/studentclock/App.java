package edu.ucsd.studentclock;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.presenter.ExamplePresenter1;
import edu.ucsd.studentclock.presenter.ExamplePresenter2;
import edu.ucsd.studentclock.presenter.PresenterManager;
import edu.ucsd.studentclock.repository.ExampleRepository;
import edu.ucsd.studentclock.view.ExampleView1;
import edu.ucsd.studentclock.view.ExampleView2;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        ExampleRepository repository = new ExampleRepository();

        Model sharedModel = new Model();

        ExampleView1 view1 = new ExampleView1();
        ExampleView2 view2 = new ExampleView2();

        ExamplePresenter1 presenter1 = new ExamplePresenter1(sharedModel, view1);
        ExamplePresenter2 presenter2 = new ExamplePresenter2(sharedModel, view2);

        PresenterManager manager = new PresenterManager();
        manager.defineInteractions(primaryStage, "Student Clock", presenter1, presenter2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

