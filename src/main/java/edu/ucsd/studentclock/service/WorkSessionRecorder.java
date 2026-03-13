package edu.ucsd.studentclock.service;

import java.time.LocalDateTime;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;

/**
 * Persist work-session effects to repositories.
 */
public final class WorkSessionRecorder {

    private final WorkLogRepository workLogRepository;
    private final AssignmentWorkLogRepository assignmentWorkLogRepository;
    private final IAssignmentRepository assignmentRepository;

    public WorkSessionRecorder(
            WorkLogRepository workLogRepository,
            AssignmentWorkLogRepository assignmentWorkLogRepository,
            IAssignmentRepository assignmentRepository) {
        if (workLogRepository == null) {
            throw new NullPointerException("workLogRepository must not be null");
        }
        if (assignmentWorkLogRepository == null) {
            throw new NullPointerException("assignmentWorkLogRepository must not be null");
        }
        if (assignmentRepository == null) {
            throw new NullPointerException("assignmentRepository must not be null");
        }

        this.workLogRepository = workLogRepository;
        this.assignmentWorkLogRepository = assignmentWorkLogRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public void recordWork(Assignment assignment, double hours, LocalDateTime loggedAt) {
        if (assignment == null) {
            throw new NullPointerException("assignment must not be null");
        }
        if (hours < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }
        if (loggedAt == null) {
            throw new NullPointerException("loggedAt must not be null");
        }

        workLogRepository.addWorkLog(hours, loggedAt);
        assignmentWorkLogRepository.addWorkLog(assignment.getId(), hours, loggedAt);

        assignmentRepository.addAssignment(assignment);
    }
}