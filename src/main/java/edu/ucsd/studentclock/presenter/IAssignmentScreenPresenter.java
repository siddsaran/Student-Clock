package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.service.ClockOutResult;
import edu.ucsd.studentclock.view.AssignmentCreateRequest;

/**
 * Contract for the assignment screen presenter as used by AssignmentView.
 * Allows the view to depend on an abstraction rather than the concrete presenter.
 */
public interface IAssignmentScreenPresenter {

    void back();

    void setCourseFilter(String courseIdOrAllCourses);

    boolean isTracking();

    void clockIn(String assignmentId);

    ClockOutResult clockOut(String assignmentId);

    void createAssignment(String name,
                          String course,
                          LocalDateTime start,
                          LocalDateTime deadline,
                          int lateDays,
                          double estimate);

    void createAssignment(String name,
                          String course,
                          LocalDateTime start,
                          LocalDateTime deadline,
                          int lateDays,
                          double estimate,
                          String seriesId);

    void createSeries(String seriesId, String courseId, String seriesName, int defaultLateDays);

    List<Series> getSeriesForCourse(String courseId);

    void deleteAssignment(Assignment assignment);

    void createSeriesAndLinkSelected(String seriesId,
                                     String seriesName,
                                     String defaultLateDaysText,
                                     List<String> assignmentIds);

    void applyManualHours(String assignmentId, String hoursText);

    void markDone(String assignmentId);

    void onCreateAssignment(AssignmentCreateRequest request);
}
