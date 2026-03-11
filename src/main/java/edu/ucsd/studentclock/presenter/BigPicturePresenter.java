package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.util.CourseColors;
import edu.ucsd.studentclock.util.TimeFormatUtils;
import edu.ucsd.studentclock.view.BigPictureView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class BigPicturePresenter extends AbstractPresenter<BigPictureView> implements IBigPictureScreenPresenter {

    private final CourseColors courseColors = new CourseColors();

    public BigPicturePresenter(
            Model model,
            BigPictureView view
    ) {
        super(model, view);

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

    @Override
    public void updateView() {
        List<Assignment> assignments = model.getAllAssignments().stream()
                .filter(a -> !a.isDone())
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
                .orElseThrow();

        LocalDate chartEndFromRanges = effectiveRanges.values().stream()
                .map(range -> range[1])
                .max(LocalDate::compareTo)
                .orElseThrow();

        LocalDate today = model.getTimeService().now().toLocalDate();
        LocalDate chartEnd = chartEndFromRanges.isBefore(today) ? today : chartEndFromRanges;

        Map<String, List<Assignment>> byCourse = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getCourseId));

        XYChart.Series<String, Number> burndownSeries = new XYChart.Series<>();
        burndownSeries.setName("Ideal Burndown");

        List<XYChart.Series<String, Number>> courseSeriesList = new ArrayList<>();
        double maxWork = 0.0;
        final double courseNudge = 0.5;
        int courseIndex = 0;

        for (String courseId : byCourse.keySet().stream().sorted().collect(Collectors.toList())) {
            List<Assignment> courseAssignments = byCourse.get(courseId);
            XYChart.Series<String, Number> courseSeries = new XYChart.Series<>();
            courseSeries.setName(courseId);

            List<BigPictureCourseLineBuilder.ChartPoint> coursePoints = BigPictureCourseLineBuilder.build(
                    courseAssignments,
                    effectiveRanges,
                    model::getCumulativeHoursByEndOf,
                    chartStart,
                    chartEnd
            );

            double yNudge = courseIndex * courseNudge;
            double courseMax = 0.0;
            for (BigPictureCourseLineBuilder.ChartPoint cp : coursePoints) {
                double y = cp.y > 0 ? cp.y + yNudge : 0.0;
                courseSeries.getData().add(createDataPoint(cp.label, y, cp.activeAssignments));
                courseMax = Math.max(courseMax, y);
            }
            maxWork = Math.max(maxWork, courseMax);
            courseSeriesList.add(courseSeries);
            courseIndex++;
        }

        maxWork = buildBurndownSeries(assignments, chartStart, chartEnd, maxWork, burndownSeries);

        setXAxisCategoriesInOrder(chartStart, chartEnd);

        List<XYChart.Series<String, Number>> allSeries = new ArrayList<>();
        allSeries.add(burndownSeries);
        allSeries.addAll(courseSeriesList);

        view.getChart().getData().setAll(allSeries);
        applyYAxisBounds(maxWork);

        view.getChart().applyCss();
        view.getChart().layout();

        applySeriesStyles(burndownSeries, courseSeriesList);
        installDataPointTooltips(courseSeriesList);
    }


    private double buildBurndownSeries(
            List<Assignment> assignments,
            LocalDate chartStart,
            LocalDate chartEnd,
            double maxWorkSoFar,
            XYChart.Series<String, Number> burndownSeries
    ) {
        long totalDays = ChronoUnit.DAYS.between(chartStart, chartEnd);
        double totalWorkAtStart = assignments.stream()
                .mapToDouble(Assignment::getEstimatedHours)
                .sum();

        double maxWork = Math.max(maxWorkSoFar, totalWorkAtStart);

        if (totalDays >= 0) {
            String first = String.format("%02d/%02d", chartStart.getMonthValue(), chartStart.getDayOfMonth());
            String last = String.format("%02d/%02d", chartEnd.getMonthValue(), chartEnd.getDayOfMonth());
            burndownSeries.getData().add(new XYChart.Data<>(first, maxWork));
            burndownSeries.getData().add(new XYChart.Data<>(last, 0.0));
        }

        return maxWork;
    }

    private void setXAxisCategoriesInOrder(LocalDate chartStart, LocalDate chartEnd) {
        CategoryAxis xAxis = (CategoryAxis) view.getChart().getXAxis();
        List<String> labels = new ArrayList<>();
        long totalDays = ChronoUnit.DAYS.between(chartStart, chartEnd);
        for (long d = 0; d <= totalDays; d++) {
            LocalDate day = chartStart.plusDays(d);
            labels.add(String.format("%02d/%02d", day.getMonthValue(), day.getDayOfMonth()));
        }
        xAxis.setCategories(FXCollections.observableArrayList(labels));
    }

    private void applyYAxisBounds(double maxWork) {
        NumberAxis yAxis = (NumberAxis) view.getChart().getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0.0);
        yAxis.setUpperBound(Math.max(1.0, maxWork));
        yAxis.setTickUnit(Math.max(1.0, maxWork / 5.0));
    }

    private void applySeriesStyles(
            XYChart.Series<String, Number> burndownSeries,
            List<XYChart.Series<String, Number>> courseSeriesList
    ) {
        if (burndownSeries.getNode() != null) {
            burndownSeries.getNode().setStyle("-fx-stroke-width: 2; -fx-stroke-dash-array: 6 6;");
        }
        burndownSeries.nodeProperty().addListener((obs, o, n) -> {
            if (n != null) n.setStyle("-fx-stroke-width: 2; -fx-stroke-dash-array: 6 6;");
        });

        for (XYChart.Series<String, Number> series : courseSeriesList) {
            String color = courseColors.getColor(series.getName());
            String style = "-fx-stroke: " + color + "; -fx-stroke-width: 2;";
            if (series.getNode() != null) {
                series.getNode().setStyle(style);
            }
            series.nodeProperty().addListener((obs, o, n) -> {
                if (n != null) n.setStyle(style);
            });
        }
    }

    private void installDataPointTooltips(List<XYChart.Series<String, Number>> courseSeriesList) {
        Platform.runLater(() -> {
            for (XYChart.Series<String, Number> series : courseSeriesList) {
                for (XYChart.Data<String, Number> point : series.getData()) {
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
            }
        });
    }
}