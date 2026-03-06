package edu.ucsd.studentclock.service;

import java.time.LocalDateTime;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;

/**
 * Coordinates all work-session activity for a single assignment at a time:
 * clock-in, clock-out, and manual hour logging.
 *
 * Responsibility: use-case orchestration (not persistence fan-out).
 */
public class WorkSessionService {

    private final TimeTrackingManager timeTrackingManager;
    private final ITimeService timeService;
    private final WorkSessionRecorder recorder;

    public WorkSessionService(
            ITimeService timeService,
            WorkLogRepository workLogRepository,
            AssignmentWorkLogRepository assignmentWorkLogRepository,
            IAssignmentRepository assignmentRepository
    ) {
        if (timeService == null) {
            throw new NullPointerException("timeService must not be null");
        }
        if (workLogRepository == null) {
            throw new NullPointerException("workLogRepository must not be null");
        }
        if (assignmentWorkLogRepository == null) {
            throw new NullPointerException("assignmentWorkLogRepository must not be null");
        }
        if (assignmentRepository == null) {
            throw new NullPointerException("assignmentRepository must not be null");
        }

        this.timeService = timeService;
        this.timeTrackingManager = new TimeTrackingManager(timeService);
        this.recorder = new WorkSessionRecorder(
                workLogRepository,
                assignmentWorkLogRepository,
                assignmentRepository
        );
    }

    public void clockIn(Assignment assignment) {
        timeTrackingManager.clockIn(assignment);
    }

    public ClockOutResult clockOut(String assignmentId) {
        Assignment activeAssignment = timeTrackingManager.getActiveAssignment();
        if (activeAssignment == null) {
            throw new IllegalStateException("Not currently clocked in");
        }
        if (!activeAssignment.getId().equals(assignmentId)) {
            throw new IllegalArgumentException("Selected assignment is not the active clocked-in assignment");
        }

        ClockOutResult result = timeTrackingManager.clockOut();
        LocalDateTime now = timeService.now();

        recorder.recordWork(activeAssignment, result.getSessionHours(), now);

        return result;
    }

    public void applyManualHours(Assignment assignment, double hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("hours must be >= 0");
        }

        assignment.applyWork(hours);

        LocalDateTime now = timeService.now();
        recorder.recordWork(assignment, hours, now);
    }

    public boolean isTracking() {
        return timeTrackingManager.isTracking();
    }
}