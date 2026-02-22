package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.view.BigPictureView;
import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

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
        List<Assignment> assignments = repository.getAllAssignments().stream()
                .filter(a -> !a.isDone())
                .collect(Collectors.toList());

        if (assignments.isEmpty()) {
            view.getChart().getData().clear();
            return;
        }
        // TODO (US10-1 / US10-6):
        // Uses BigPictureEffectiveRanges for late days + series chaining.
        // If this logic changes, update start/end + loop conditions accordingly.

        // NOTE: This currently uses raw start/deadline dates.
        // Should be updated to use BigPictureEffectiveRanges (effectiveStart/effectiveEnd)
        // to support late days and assignment series chaining.
        LocalDate start = assignments.stream()
            .map(a -> a.getStart().toLocalDate())
            .min(LocalDate::compareTo)
            .get();

        LocalDate end = assignments.stream()
            .map(a -> a.getDeadline().toLocalDate())
            .max(LocalDate::compareTo)
            .get();
        //THIS section
        XYChart.Series<String, Number> workload = new XYChart.Series<>();
        workload.setName("Workload");

        XYChart.Series<String, Number> burndown = new XYChart.Series<>();
        burndown.setName("Ideal Burndown");

        long days = ChronoUnit.DAYS.between(start, end);
        double runningWorkload = 0;

        List<Assignment> plateauAssignments = null;

        for (int i = 0; i <= days; i++) {
            LocalDate day = start.plusDays(i);

            List<Assignment> todaysAssignments = assignments.stream()
                .filter(a -> a.getStart().toLocalDate().equals(day))
                .collect(Collectors.toList());

            if (!todaysAssignments.isEmpty()) {
                for (Assignment a : todaysAssignments) {
                    // TODO (US10-2 / US10-5): switch from estimatedHours to remainingHours
                    // so workload reflects actual progress when hours are logged
                    runningWorkload += a.getEstimatedHours();
                }
                plateauAssignments = List.copyOf(todaysAssignments);
            }

            String label = day.getMonthValue() + "/" + day.getDayOfMonth();

            XYChart.Data<String, Number> dataPoint =
                new XYChart.Data<>(label, runningWorkload);

            dataPoint.setExtraValue(plateauAssignments);

            workload.getData().add(dataPoint);
        }

        double maxWork = workload.getData().isEmpty()
            ? 0
            : workload.getData().get(workload.getData().size() - 1).getYValue().doubleValue();

        if (!workload.getData().isEmpty()) {
            String first = workload.getData().get(0).getXValue();
            String last = workload.getData().get(workload.getData().size() - 1).getXValue();
            // TODO (US10-5): burndown currently ideal/placeholder.
            // Replace with real remaining-hours-per-day calculation.
            burndown.getData().add(new XYChart.Data<>(first, maxWork));
            burndown.getData().add(new XYChart.Data<>(last, 0));
        }

        view.getChart().getData().setAll(List.of(workload, burndown));

        NumberAxis yAxis = (NumberAxis) view.getChart().getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(Math.max(1, maxWork));
        yAxis.setTickUnit(Math.max(1, maxWork / 5));

        view.getChart().applyCss();
        view.getChart().layout();

        if (workload.getNode() != null) {
            workload.getNode().setStyle("-fx-stroke-width: 2;");
        }
        if (burndown.getNode() != null) {
            burndown.getNode().setStyle("-fx-stroke-width: 2; -fx-stroke-dash-array: 6 6;");
        }

        // Tooltips
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> point : workload.getData()) {

                if (point.getNode() == null) continue;

                @SuppressWarnings("unchecked")
                List<Assignment> matches =
                    (List<Assignment>) point.getExtraValue();

                if (matches == null || matches.isEmpty()) continue;

                StringBuilder sb = new StringBuilder();
                for (Assignment a : matches) {
                    sb.append(a.getName())
                    .append(" (").append(a.getCourseID()).append(")\n")
                    .append("Due: ").append(a.getDeadline().toLocalDate()).append("\n")
                    .append("Estimated: ").append(a.getEstimatedHours()).append(" hrs\n")
                    .append("Completed: ").append(a.getCumulativeHours()).append(" hrs\n")
                    .append("Remaining: ").append(a.getRemainingHours()).append(" hrs\n");

                    if (a.isDone()) sb.append("Status: DONE\n");
                    sb.append("\n");
                }

                Tooltip tooltip = new Tooltip(sb.toString().trim());
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.ZERO);
                tooltip.setShowDuration(Duration.seconds(60));

                point.getNode().setPickOnBounds(true);
                point.getNode().setMouseTransparent(false);
                point.getNode().setStyle("-fx-cursor: hand;");

                Tooltip.install(point.getNode(), tooltip);
            }
        });
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
