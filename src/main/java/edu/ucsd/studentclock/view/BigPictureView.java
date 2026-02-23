package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.presenter.BigPicturePresenter;
import edu.ucsd.studentclock.util.TimeFormatUtils;
import javafx.util.StringConverter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BigPictureView extends BorderPane {

    private final Button coursesButton = new Button("Courses");
    private final Button assignmentsButton = new Button("Assignments");
    private final Button studyAvailabilityButton = new Button("Study Availability");
    private final Button dashboardButton = new Button("Dashboard");
    private final Button backButton = new Button("Back");
    private BigPicturePresenter presenter;


    private final LineChart<String, Number> chart;

    public BigPictureView() {
        setPadding(new Insets(20));

        // Top nav bar
        HBox navBar = new HBox(10,
                coursesButton,
                assignmentsButton,
                studyAvailabilityButton,
                dashboardButton
        );
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(0,0,10,0));
        setTop(navBar);

        Label title = new Label("Big Picture");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        BorderPane.setMargin(title, new Insets(10, 0, 10, 0));
        VBox top = new VBox(10, navBar, title);
        setTop(top);


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
        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);

        setCenter(chart);
        chart.setPrefHeight(500);
        BorderPane.setMargin(chart, new Insets(10));

        HBox bottom = new HBox(backButton);
        bottom.setPadding(new Insets(10, 0, 0, 0));
        bottom.setAlignment(Pos.CENTER_LEFT);
        setBottom(bottom);
    }

    public LineChart<String, Number> getChart() {
        return chart;
    }

    public Button getCoursesButton() { return coursesButton; }
    public Button getAssignmentsButton() { return assignmentsButton; }
    public Button getStudyAvailabilityButton() { return studyAvailabilityButton; }
    public Button getDashboardButton() { return dashboardButton; }
    public Button getBackButton() { return backButton; }
    public void setPresenter(BigPicturePresenter presenter) {
        this.presenter = presenter;
    }
}
