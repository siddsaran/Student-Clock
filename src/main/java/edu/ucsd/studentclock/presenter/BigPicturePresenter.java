package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.view.BigPictureView;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class BigPicturePresenter extends AbstractPresenter<BigPictureView> {

    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onAssignments;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;
    private final AssignmentRepository repository;

    


    public BigPicturePresenter(Model model, BigPictureView view, AssignmentRepository repository) {
        super(model, view);
        this.repository = repository;


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
        // Active assignments only (all courses). View updates when assignments are added,
        // worked on, or marked done (callers trigger refresh / navigation).
        List<Assignment> assignments = repository.getAllAssignments().stream()
                .filter(a -> !a.isDone())
                .collect(Collectors.toList());

        if (assignments.isEmpty()) {
            view.getChart().getData().clear();
            return;
        }

        // Date range: earliest active start date to latest due date
        LocalDate start = assignments.stream()
                .map(a -> a.getStart().toLocalDate())
                .min(LocalDate::compareTo)
                .get();
        LocalDate end = assignments.stream()
                .map(a -> a.getDeadline().toLocalDate())
                .max(LocalDate::compareTo)
                .get();

        // Combined remaining hours from every active assignment (all courses), per day
        XYChart.Series<String, Number> workload = new XYChart.Series<>();
        workload.setName("Remaining Workload");

        XYChart.Series<String, Number> burndown = new XYChart.Series<>();
        burndown.setName("Ideal Burndown");

        long days = ChronoUnit.DAYS.between(start, end);
        for (int i = 0; i <= days; i++) {
            LocalDate day = start.plusDays(i);
            double remaining = 0;
            for (Assignment a : assignments) {
                LocalDate aStart = a.getStart().toLocalDate();
                LocalDate aEnd = a.getDeadline().toLocalDate();
                if (!day.isBefore(aStart) && !day.isAfter(aEnd)) {
                    remaining += a.getRemainingHours();
                }
            }
            String label = (day.getMonthValue()) + "/" + day.getDayOfMonth();
            workload.getData().add(new XYChart.Data<>(label, remaining));
        }

        double maxWork = 0;
        for (XYChart.Data<String, Number> d : workload.getData()) {
            double v = d.getYValue().doubleValue();
            if (v > maxWork) maxWork = v;
        }

        String firstDay = workload.getData().get(0).getXValue();
        String lastDay = workload.getData().get(workload.getData().size() - 1).getXValue();
        double firstDayRemaining = workload.getData().get(0).getYValue().doubleValue();

        burndown.getData().add(new XYChart.Data<>(firstDay, firstDayRemaining));
        burndown.getData().add(new XYChart.Data<>(lastDay, 0));

        view.getChart().getData().setAll(workload, burndown);

        NumberAxis yAxis = (NumberAxis) view.getChart().getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(Math.max(1, maxWork));
        yAxis.setTickUnit(Math.max(1, maxWork / 5));

        view.getChart().applyCss();
        view.getChart().layout();

        workload.getNode().setStyle("-fx-stroke: #1f77b4;");
        burndown.getNode().setStyle("-fx-stroke: #ff7f0e;");
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
