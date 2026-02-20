package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.AssignmentStatusCalculator;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.StudyStatusCalculator;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.view.DashboardView;

public class DashboardPresenter extends AbstractPresenter<DashboardView> {

    private final AssignmentRepository assignmentRepo;
    private Runnable onBack;
    private Runnable onBigPicture;

    public DashboardPresenter(Model model,
                              DashboardView view,
                              AssignmentRepository assignmentRepo) {

        super(model, view);
        this.assignmentRepo = assignmentRepo;

        view.setPresenter(this);

        view.getShowOpenButton().setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        view.getBigPictureButton().setOnAction(e -> {
            if (onBigPicture != null) onBigPicture.run();
        });

    }

    @Override
    public void updateView() {
        LocalDateTime now = LocalDateTime.now();

        List<Assignment> filtered = assignmentRepo.getAllAssignments().stream()
                .filter(a -> !a.isDone())
                .filter(a -> {
                    boolean urgent = AssignmentStatusCalculator.isUrgent(a, now);
                    AssignmentStatus status =
                            AssignmentStatusCalculator.behindStatus(a, now);

                    return urgent
                            || status == AssignmentStatus.RED
                            || status == AssignmentStatus.ORANGE
                            || status == AssignmentStatus.YELLOW;
                })
                .sorted((a, b) -> {
                    int sa = severityScore(a, now);
                    int sb = severityScore(b, now);

                    if (sa != sb) return Integer.compare(sb, sa);

                    return a.getDeadline().compareTo(b.getDeadline());
                })
                .collect(Collectors.toList());

        int remainingStudyHours = model.getStudyAvailability().getUnallocatedHours();

        view.setStudyHoursRemaining(remainingStudyHours);

        AssignmentStatus overallStatus = StudyStatusCalculator.overallStudyStatus(
                assignmentRepo.getAllAssignments(),
                model.getStudyAvailability(),
                now
        );
        view.setStudyStatus(overallStatus);

        view.showAssignments(filtered, now);
    }

    private int severityScore(Assignment a, LocalDateTime now) {
        if (AssignmentStatusCalculator.isUrgent(a, now)) return 4;

        AssignmentStatus status =
                AssignmentStatusCalculator.behindStatus(a, now);

        if (status == AssignmentStatus.RED) return 3;
        if (status == AssignmentStatus.ORANGE) return 2;
        if (status == AssignmentStatus.YELLOW) return 1;

        return 0;
    }

    public void openAssignment(Assignment assignment) {
        model.setSelectedAssignment(assignment);
        if (onBack != null) onBack.run();
    }

    public void setOnBack(Runnable action) {
        this.onBack = action;
    }
    public void setOnBigPicture(Runnable r) {
        onBigPicture = r;
    }

    @Override
    public String getViewTitle() {
        return "Dashboard";
    }
}
