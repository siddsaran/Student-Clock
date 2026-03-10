package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.repository.IAssignmentRepository;
import edu.ucsd.studentclock.repository.ICourseRepository;
import edu.ucsd.studentclock.repository.IStudyAvailabilityRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import edu.ucsd.studentclock.view.AssignmentView;
import edu.ucsd.studentclock.view.BigPictureView;
import edu.ucsd.studentclock.view.CourseView;
import edu.ucsd.studentclock.view.DashboardView;
import edu.ucsd.studentclock.view.StudyAvailabilityView;

public final class PresenterFactory {

    private final Model model;
    private final IAssignmentRepository assignmentRepository;
    private final AssignmentWorkLogRepository assignmentWorkLogRepository;

    public PresenterFactory(
            Model model,
            ICourseRepository courseRepository,
            IAssignmentRepository assignmentRepository,
            IStudyAvailabilityRepository studyAvailabilityRepository,
            WorkLogRepository workLogRepository,
            AssignmentWorkLogRepository assignmentWorkLogRepository
    ) {
        this.model = model;
        this.assignmentRepository = assignmentRepository;
        this.assignmentWorkLogRepository = assignmentWorkLogRepository;
    }

    public CoursePresenter createCoursePresenter(CourseView view) {
        return new CoursePresenter(model, view);
    }

    public AssignmentPresenter createAssignmentPresenter(AssignmentView view) {
        return new AssignmentPresenter(model, view);
    }

    public StudyAvailabilityPresenter createStudyAvailabilityPresenter(StudyAvailabilityView view) {
        return new StudyAvailabilityPresenter(model, view);
    }

    public DashboardPresenter createDashboardPresenter(DashboardView view) {
        DashboardPresenter presenter = new DashboardPresenter(model, view);
        return presenter;
    }

    public BigPicturePresenter createBigPicturePresenter(BigPictureView view) {
        return new BigPicturePresenter(model, view, assignmentRepository, assignmentWorkLogRepository);
    }
}