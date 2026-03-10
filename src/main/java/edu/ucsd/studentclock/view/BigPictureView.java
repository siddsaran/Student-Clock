package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.presenter.IBigPictureScreenPresenter;
import edu.ucsd.studentclock.util.TimeFormatUtils;
import javafx.util.StringConverter;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class BigPictureView extends BorderPane {

    private IBigPictureScreenPresenter presenter;
    private final LineChart<String, Number> chart;

    public BigPictureView() {
        setPadding(new Insets(20));

        Label title = new Label("Big Picture");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        BorderPane.setMargin(title, new Insets(0, 0, 10, 0));
        setTop(title);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setGapStartAndEnd(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        xAxis.setLabel("Days");
        yAxis.setLabel("Remaining Hours (HH:MM)");
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number n) {
                return TimeFormatUtils.formatHoursAsHHMM(n.doubleValue());
            }
            @Override
            public Number fromString(String s) {
                return 0;
            }
        });

        chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);

        setCenter(chart);
        chart.setPrefHeight(500);
        BorderPane.setMargin(chart, new Insets(10));
    }

    public LineChart<String, Number> getChart() {
        return chart;
    }

    public void setPresenter(IBigPictureScreenPresenter presenter) {
        this.presenter = presenter;
    }
}
