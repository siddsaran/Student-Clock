package edu.ucsd.studentclock.view;

public final class ViewFactory {

    public CourseView createCourseView() {
        return new CourseView();
    }

    public AssignmentView createAssignmentView() {
        return new AssignmentView();
    }

    public StudyAvailabilityView createStudyAvailabilityView() {
        return new StudyAvailabilityView();
    }

    public DashboardView createDashboardView() {
        return new DashboardView();
    }

    public BigPictureView createBigPictureView() {
        return new BigPictureView();
    }
}