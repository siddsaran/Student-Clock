package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.util.TimeFormatUtils;
import edu.ucsd.studentclock.view.BigPictureView;
import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class BigPicturePresenter extends AbstractPresenter<BigPictureView> implements IBigPictureScreenPresenter {

    private final IAssignmentRepository assignmentRepository;
    private final AssignmentWorkLogRepository assignmentWorkLogRepository;

    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onAssignments;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;

    public BigPicturePresenter(
            Model model,
            BigPictureView view,
            IAssignmentRepository assignmentRepository,
            AssignmentWorkLogRepository assignmentWorkLogRepository
    ) {
        super(model, view);
        this.assignmentRepository = assignmentRepository;
        this.assignmentWorkLogRepository = assignmentWorkLogRepository;

        view.getBackButton().setOnAction(event -> runIfSet(onBack));
        view.getCoursesButton().setOnAction(event -> runIfSet(onCourses));
        view.getAssignmentsButton().setOnAction(event -> runIfSet(onAssignments));
        view.getStudyAvailabilityButton().setOnAction(event -> runIfSet(onStudyAvailability));
        view.getDashboardButton().setOnAction(event -> runIfSet(onDashboard));

        updateView();
    }

    @Override
    public String getViewTitle() {
        return "Big Picture";
    }

    private static XYChart.Data<String, Number> createDataPoint(
            String label,
            double y,
            List<Assignment> activeAssignments
    ) {
        XYChart.Data<String, Number> point = new XYChart.Data<>(label, y);
        point.setExtraValue(activeAssignments);
        return point;
    }

    private double remainingHoursAt(
            Assignment assignment,
            LocalDate day,
            Map<String, Double> cumulativeByEndOfDay
    ) {
        Double loggedHours = cumulativeByEndOfDay.get(assignment.getId());
        if (loggedHours != null) {
            return Math.max(0.0, assignment.getEstimatedHours() - loggedHours);
        }
        return assignment.getRemainingHours();
    }

    @Override
    public void updateView() {
        List<Assignment> assignments = assignmentRepository.getAllAssignments().stream()
                .filter(assignment -> !assignment.isDone())
                .collect(Collectors.toList());

        if (assignments.isEmpty()) {
            view.getChart().getData().clear();
            return;
        }

        Map<Assignment, LocalDate[]> effectiveRanges =
                BigPictureEffectiveRanges.computeEffectiveRanges(assignments);

        LocalDate chartStart = effectiveRanges.values().stream()
                .map(range -> range[0])
                .min(LocalDate::compareTo)
                .get();

        LocalDate chartEnd = effectiveRanges.values().stream()
                .map(range -> range[1])
                .max(LocalDate::compareTo)
                .get();

        XYChart.Series<String, Number> workloadSeries = new XYChart.Series<>();
        workloadSeries.setName("Workload");

        XYChart.Series<String, Number> burndownSeries = new XYChart.Series<>();
        burndownSeries.setName("IdealBurndown");

        double maxWork = buildWorkloadAndBurndownSeries(
                assignments,
                effectiveRanges,
                chartStart,
                chartEnd,
                workloadSeries,
                burndownSeries
        );

        view.getChart().getData().setAll(List.of(workloadSeries, burndownSeries));
        applyYAxisBounds(maxWork);

        view.getChart().applyCss();
        view.getChart().layout();

        applySeriesStyles(workloadSeries, burndownSeries);
        installDataPointTooltips(workloadSeries);
    }

    private double buildWorkloadAndBurndownSeries(
            List<Assignment> assignments,
            Map<Assignment, LocalDate[]> effectiveRanges,
            LocalDate chartStart,
            LocalDate chartEnd,
            XYChart.Series<String, Number> workloadSeries,
            XYChart.Series<String, Number> burndownSeries
    ) {
        long totalDays = ChronoUnit.DAYS.between(chartStart, chartEnd);
        double runningWorkload = 0.0;
        double maxWorkload = 0.0;

        Map<String, Double> previousCumulative = Map.of();

        for (int dayIndex = 0; dayIndex <= totalDays; dayIndex++) {
            LocalDate day = chartStart.plusDays(dayIndex);
            LocalDate previousDay = dayIndex > 0 ? chartStart.plusDays(dayIndex - 1) : null;

            Map<String, Double> cumulativeWork = assignmentWorkLogRepository.getCumulativeHoursByEndOf(day);

            List<Assignment> endsToday = assignments.stream()
                    .filter(assignment -> effectiveRanges.get(assignment)[1].equals(day))
                    .collect(Collectors.toList());

            List<Assignment> startsToday = assignments.stream()
                    .filter(assignment -> effectiveRanges.get(assignment)[0].equals(day))
                    .collect(Collectors.toList());

            List<Assignment> activeOnDay = assignments.stream()
                    .filter(assignment -> {
                        LocalDate[] range = effectiveRanges.get(assignment);
                        return !day.isBefore(range[0]) && !day.isAfter(range[1]);
                    })
                    .collect(Collectors.toList());

            String label = day.getMonthValue() + "/" + day.getDayOfMonth();

            List<Assignment> activeAtStart = assignments.stream()
                    .filter(assignment -> {
                        LocalDate[] range = effectiveRanges.get(assignment);
                        return day.compareTo(range[0]) > 0 && !day.isAfter(range[1]);
                    })
                    .collect(Collectors.toList());

            boolean skipStartPoint = runningWorkload == 0.0 && !startsToday.isEmpty();
            if (!skipStartPoint) {
                workloadSeries.getData().add(createDataPoint(label, runningWorkload, activeOnDay));
            }

            if (previousDay != null) {
                boolean workLoggedToday = false;

                for (Assignment assignment : activeAtStart) {
                    double remainingPrevious = remainingHoursAt(assignment, previousDay, previousCumulative);
                    double remainingToday = remainingHoursAt(assignment, day, cumulativeWork);

                    if (remainingToday != remainingPrevious) {
                        runningWorkload += (remainingToday - remainingPrevious);
                        workLoggedToday = true;
                    }
                }

                if (workLoggedToday) {
                    workloadSeries.getData().add(createDataPoint(label, runningWorkload, activeOnDay));
                }
            }

            previousCumulative = cumulativeWork;

            if (!endsToday.isEmpty()) {
                for (Assignment assignment : endsToday) {
                    runningWorkload -= remainingHoursAt(assignment, day, cumulativeWork);
                }

                List<Assignment> activeAfterEnds = activeOnDay.stream()
                        .filter(assignment -> !endsToday.contains(assignment))
                        .collect(Collectors.toList());

                workloadSeries.getData().add(createDataPoint(label, runningWorkload, activeAfterEnds));
            }

            if (!startsToday.isEmpty()) {
                for (Assignment assignment : startsToday) {
                    runningWorkload += remainingHoursAt(assignment, day, cumulativeWork);
                }
                workloadSeries.getData().add(createDataPoint(label, runningWorkload, activeOnDay));
            }

            maxWorkload = Math.max(maxWorkload, runningWorkload);
        }

        double firstHeight = workloadSeries.getData().isEmpty()
                ? 0.0
                : workloadSeries.getData().get(0).getYValue().doubleValue();
        double maxWork = Math.max(firstHeight, maxWorkload);

        if (!workloadSeries.getData().isEmpty()) {
            String first = workloadSeries.getData().get(0).getXValue();
            String last = workloadSeries.getData().get(workloadSeries.getData().size() - 1).getXValue();
            double finalWorkload = workloadSeries.getData()
                    .get(workloadSeries.getData().size() - 1)
                    .getYValue()
                    .doubleValue();

            burndownSeries.getData().add(new XYChart.Data<>(first, maxWork));
            burndownSeries.getData().add(new XYChart.Data<>(last, finalWorkload));
        }

        return maxWork;
    }

    private void applyYAxisBounds(double maxWork) {
        NumberAxis yAxis = (NumberAxis) view.getChart().getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0.0);
        yAxis.setUpperBound(Math.max(1.0, maxWork));
        yAxis.setTickUnit(Math.max(1.0, maxWork / 5.0));
    }

    private void applySeriesStyles(
            XYChart.Series<String, Number> workloadSeries,
            XYChart.Series<String, Number> burndownSeries
    ) {
        if (workloadSeries.getNode() != null) {
            workloadSeries.getNode().setStyle("-fx-stroke-width: 2;");
        }
        if (burndownSeries.getNode() != null) {
            burndownSeries.getNode().setStyle("-fx-stroke-width: 2; -fx-stroke-dash-array: 6 6;");
        }
    }

    private void installDataPointTooltips(XYChart.Series<String, Number> workloadSeries) {
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> point : workloadSeries.getData()) {
                if (point.getNode() == null) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                List<Assignment> matches = (List<Assignment>) point.getExtraValue();

                if (matches == null || matches.isEmpty()) {
                    continue;
                }

                StringBuilder tooltipText = new StringBuilder();
                for (Assignment assignment : matches) {
                    tooltipText.append(assignment.getName())
                            .append(" (").append(assignment.getCourseId()).append(")\n")
                            .append("Due: ").append(assignment.getDeadline().toLocalDate()).append("\n")
                            .append("Estimated: ")
                            .append(TimeFormatUtils.formatHoursAsHHMM(assignment.getEstimatedHours()))
                            .append("\n")
                            .append("Completed: ")
                            .append(TimeFormatUtils.formatHoursAsHHMM(assignment.getCumulativeHours()))
                            .append("\n")
                            .append("Remaining: ")
                            .append(TimeFormatUtils.formatHoursAsHHMM(assignment.getRemainingHours()))
                            .append("\n");

                    if (assignment.isDone()) {
                        tooltipText.append("Status: DONE\n");
                    }
                    tooltipText.append("\n");
                }

                Tooltip tooltip = new Tooltip(tooltipText.toString().trim());
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

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setOnCourses(Runnable onCourses) {
        this.onCourses = onCourses;
    }

    public void setOnAssignments(Runnable onAssignments) {
        this.onAssignments = onAssignments;
    }

    public void setOnStudyAvailability(Runnable onStudyAvailability) {
        this.onStudyAvailability = onStudyAvailability;
    }

    public void setOnDashboard(Runnable onDashboard) {
        this.onDashboard = onDashboard;
    }
}