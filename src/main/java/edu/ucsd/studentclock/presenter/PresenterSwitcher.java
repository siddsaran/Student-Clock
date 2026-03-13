package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.view.MainLayoutView;
import javafx.stage.Stage;

public class PresenterSwitcher {
    private final Stage stage;
    private final String appName;
    private final MainLayoutView mainLayout;

    public PresenterSwitcher(Stage stage, String appName, MainLayoutView mainLayout) {
        this.stage = stage;
        this.appName = appName;
        this.mainLayout = mainLayout;
    }

    public void switchTo(AbstractPresenter<?> presenter) {
        presenter.updateView();
        stage.setTitle(appName + ": " + presenter.getViewTitle());
        mainLayout.setContent(presenter.getView());
        mainLayout.setActivePage(presenter.getViewTitle());
        stage.show();
    }
}
