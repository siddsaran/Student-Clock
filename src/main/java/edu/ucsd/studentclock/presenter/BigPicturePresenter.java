package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.view.BigPictureView;
import javafx.scene.chart.XYChart;

public class BigPicturePresenter extends AbstractPresenter<BigPictureView> {

    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onAssignments;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;


    public BigPicturePresenter(Model model, BigPictureView view) {
        super(model, view);

        view.getBackButton().setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        view.getCoursesButton().setOnAction(e -> {
            if (onCourses != null) onCourses.run();
        });

        view.getAssignmentsButton().setOnAction(e -> {
            if (onAssignments != null) onAssignments.run();
        });

        view.getStudyAvailabilityButton().setOnAction(e -> {
            if (onStudyAvailability != null) onStudyAvailability.run();
        });

        view.getDashboardButton().setOnAction(e -> {
            if (onDashboard != null) onDashboard.run();
        });


        updateView();
    }

    @Override
    public String getViewTitle() {
        return "Big Picture";
    }

    @Override
    public void updateView() {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(0, 10));
        series.getData().add(new XYChart.Data<>(1, 8));
        series.getData().add(new XYChart.Data<>(2, 6));
        series.getData().add(new XYChart.Data<>(3, 4));

        view.getChart().getData().setAll(series);
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }
    public void setOnCourses(Runnable r) {
        onCourses = r; 
    }
    public void setOnAssignments(Runnable r) { 
        onAssignments = r; 
    }
    public void setOnStudyAvailability(Runnable r) { 
        onStudyAvailability = r; 
    }
    public void setOnDashboard(Runnable r) { 
        onDashboard = r; 
    }

}
