package edu.ucsd.studentclock.view;

import java.time.LocalDate;

import edu.ucsd.studentclock.model.Series;

/**
 * Raw user inputs from the AssignmentView add form.
 * View responsibility: collect raw values; Presenter responsibility: validate/parse/apply.
 */
public final class AssignmentCreateRequest {

    public enum SeriesChoice {
        NONE,
        NEW_SERIES,
        EXISTING_SERIES
    }

    private final String nameText;
    private final String courseId;
    private final LocalDate startDate;
    private final LocalDate deadlineDate;
    private final String estimatedHoursText;

    private final SeriesChoice seriesChoice;

    // NEW series inputs
    private final String newSeriesNameText;
    private final String newSeriesIdText;
    private final String newSeriesDefaultLateDaysText;

    // EXISTING series selection
    private final Series existingSeries;

    public AssignmentCreateRequest(
            String nameText,
            String courseId,
            LocalDate startDate,
            LocalDate deadlineDate,
            String estimatedHoursText,
            SeriesChoice seriesChoice,
            String newSeriesNameText,
            String newSeriesIdText,
            String newSeriesDefaultLateDaysText,
            Series existingSeries
    ) {
        this.nameText = nameText;
        this.courseId = courseId;
        this.startDate = startDate;
        this.deadlineDate = deadlineDate;
        this.estimatedHoursText = estimatedHoursText;
        this.seriesChoice = seriesChoice;
        this.newSeriesNameText = newSeriesNameText;
        this.newSeriesIdText = newSeriesIdText;
        this.newSeriesDefaultLateDaysText = newSeriesDefaultLateDaysText;
        this.existingSeries = existingSeries;
    }

    public String getNameText() { return nameText; }
    public String getCourseId() { return courseId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDeadlineDate() { return deadlineDate; }
    public String getEstimatedHoursText() { return estimatedHoursText; }

    public SeriesChoice getSeriesChoice() { return seriesChoice; }

    public String getNewSeriesNameText() { return newSeriesNameText; }
    public String getNewSeriesIdText() { return newSeriesIdText; }
    public String getNewSeriesDefaultLateDaysText() { return newSeriesDefaultLateDaysText; }

    public Series getExistingSeries() { return existingSeries; }
}