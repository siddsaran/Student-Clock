package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.view.BigPictureView;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class BigPicturePresenter extends AbstractPresenter<BigPictureView> implements IBigPictureScreenPresenter {

    private final BigPictureChartCalculator calculator = new BigPictureChartCalculator();
    private final BigPictureTooltipInstaller tooltipInstaller = new BigPictureTooltipInstaller();

    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onAssignments;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;

    public BigPicturePresenter(Model model, BigPictureView view) {
        super(model, view);

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

    @Override
    public void updateView() {
        List<Assignment> assignments = AssignmentFilters.openAssignments(model.getAllAssignments());

        if (assignments.isEmpty()) {
            view.getChart().getData().clear();
            return;
        }

        Map<Assignment, BigPictureEffectiveRanges.DateRange> effectiveRanges =
                BigPictureEffectiveRanges.computeEffectiveRanges(assignments);

        LocalDate chartStart = effectiveRanges.values().stream()
                .map(BigPictureEffectiveRanges.DateRange::start)
                .min(LocalDate::compareTo)
                .orElseThrow();

        LocalDate chartEnd = effectiveRanges.values().stream()
                .map(BigPictureEffectiveRanges.DateRange::end)
                .max(LocalDate::compareTo)
                .orElseThrow();

        CumulativeHoursProvider provider =
                new MemoizedCumulativeHoursProvider(model::getCumulativeHoursByEndOf);

        BigPictureChartModel chartModel = calculator.build(
                assignments,
                effectiveRanges,
                chartStart,
                chartEnd,
                provider
        );

        XYChart.Series<String, Number> workloadSeries = chartModel.getWorkloadSeries();
        XYChart.Series<String, Number> burndownSeries = chartModel.getBurndownSeries();

        view.getChart().getData().setAll(List.of(workloadSeries, burndownSeries));
        applyYAxisBounds(chartModel.getMaxWork());

        view.getChart().applyCss();
        view.getChart().layout();

        applySeriesStyles(workloadSeries, burndownSeries);
        tooltipInstaller.installTooltips(workloadSeries);
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

    public void setOnBack(Runnable onBack) { this.onBack = onBack; }
    public void setOnCourses(Runnable onCourses) { this.onCourses = onCourses; }
    public void setOnAssignments(Runnable onAssignments) { this.onAssignments = onAssignments; }
    public void setOnStudyAvailability(Runnable onStudyAvailability) { this.onStudyAvailability = onStudyAvailability; }
    public void setOnDashboard(Runnable onDashboard) { this.onDashboard = onDashboard; }
}