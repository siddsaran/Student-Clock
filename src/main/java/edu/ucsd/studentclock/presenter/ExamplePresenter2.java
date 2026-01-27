package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.view.ExampleView2;

public class ExamplePresenter2 extends AbstractPresenter<ExampleView2> {
    private Runnable onBack;

    public ExamplePresenter2(Model model, ExampleView2 view) {
        super(model, view);

        this.view.getBackButton().setOnAction(e -> {
            if (onBack != null)
                onBack.run();
        });
    }

    public void setOnBack(Runnable action) {
        this.onBack = action;
    }

    @Override
    public void updateView() {
        //get data from model
    }

    @Override
    public String getViewTitle() {
        return "Example2";
    }
}
