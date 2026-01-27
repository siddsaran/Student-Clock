package edu.ucsd.studentclock.presenter;

import javafx.stage.Stage;

public class PresenterManager {
    public void defineInteractions(Stage stage, String appName,
            ExamplePresenter1 example1,
            ExamplePresenter2 example2) {

        PresenterSwitcher switcher = new PresenterSwitcher(stage, appName);

        example1.setOnSwitch(() -> {
            example2.updateView();
            switcher.switchTo(example2);
        });

        example2.setOnBack(() -> {
            switcher.switchTo(example1);
        });

        switcher.switchTo(example1);
    }
}

