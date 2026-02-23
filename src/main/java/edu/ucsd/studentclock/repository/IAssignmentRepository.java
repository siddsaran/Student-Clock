package edu.ucsd.studentclock.repository;

import edu.ucsd.studentclock.model.Assignment;

import java.util.List;

/**
 * Abstraction for assignment persistence.
 */
public interface IAssignmentRepository {

    void addAssignment(Assignment assignment);

    void deleteAssignment(String id);

    List<Assignment> getAssignmentsForCourse(String courseID);

    void deleteAssignmentsForCourse(String courseID);

    void setSeriesForAssignments(String seriesId, int defaultLateDays, List<String> assignmentIds);

    List<Assignment> getAllAssignments();

    List<Assignment> getAssignmentsBySeries(String seriesId);
}
