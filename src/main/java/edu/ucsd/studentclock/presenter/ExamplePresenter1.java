package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.view.ExampleView1;

public class ExamplePresenter1 extends AbstractPresenter<ExampleView1> {
    private Runnable onSwitch;

    public ExamplePresenter1(Model model, ExampleView1 view) {
        super(model, view);
        this.view.getSwitchButton().setOnAction(e -> {
            if (onSwitch != null)
                onSwitch.run();
        });

        updateView();
    }

    public void setOnSwitch(Runnable action) {
        this.onSwitch = action;
    }

    @Override
    public String getViewTitle() {
        return "Example1";
    }

    public void updateView() {
        //get data from model
    }
}

