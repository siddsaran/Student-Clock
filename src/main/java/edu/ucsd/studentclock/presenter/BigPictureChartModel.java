package edu.ucsd.studentclock.presenter;

import javafx.scene.chart.XYChart;

public final class BigPictureChartModel {

    private final XYChart.Series<String, Number> workloadSeries;
    private final XYChart.Series<String, Number> burndownSeries;
    private final double maxWork;

    public BigPictureChartModel(
            XYChart.Series<String, Number> workloadSeries,
            XYChart.Series<String, Number> burndownSeries,
            double maxWork) {
        this.workloadSeries = workloadSeries;
        this.burndownSeries = burndownSeries;
        this.maxWork = maxWork;
    }

    public XYChart.Series<String, Number> getWorkloadSeries() {
        return workloadSeries;
    }

    public XYChart.Series<String, Number> getBurndownSeries() {
        return burndownSeries;
    }

    public double getMaxWork() {
        return maxWork;
    }
}