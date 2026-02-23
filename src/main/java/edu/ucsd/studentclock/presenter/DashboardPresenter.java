package edu.ucsd.studentclock.presenter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.AssignmentStatusCalculator;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.StudyAvailability;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.WorkLogRepository;
import edu.ucsd.studentclock.view.DashboardView;

public class DashboardPresenter extends AbstractPresenter<DashboardView> {

    private final AssignmentRepository assignmentRepo;
    private final WorkLogRepository workLogRepo;
    private Runnable onBack;
    private Runnable onBigPicture;

    public DashboardPresenter(Model model,
                              DashboardView view,
                              AssignmentRepository assignmentRepo,
                              WorkLogRepository workLogRepo) {

        super(model, view);
        this.assignmentRepo = assignmentRepo;
        this.workLogRepo = workLogRepo;

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

        StudyAvailability sa = model.getStudyAvailability();

        int availableFromToday = computeWeeklyHoursLeftFromToday(sa, now);
        double totalLoggedThisWeek = workLogRepo.getTotalHoursLoggedThisWeek();
        int remainingStudyHours = Math.max(0, availableFromToday - (int) Math.round(totalLoggedThisWeek));
        view.setStudyHoursRemaining(remainingStudyHours);

        double workNext7Days = computeRemainingWorkNext7Days(assignmentRepo.getAllAssignments(), now);
        AssignmentStatus overallStatus = statusFrom(workNext7Days, remainingStudyHours);
        view.setStudyStatus(overallStatus);

        view.showAssignments(filtered, now);
    }

    private double computeRemainingWorkNext7Days(List<Assignment> all, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate end = today.plusDays(7);

        double sum = 0;
        for (Assignment a : all) {
            if (a.isDone()) continue;
            if (a.getDeadline() == null) continue;

            LocalDate due = a.getDeadline().toLocalDate();
            // due in [today, today+7]
            if (!due.isBefore(today) && !due.isAfter(end)) {
                sum += a.getRemainingHours();
            }
        }
        return sum;
    }

    private int computeWeeklyHoursLeftFromToday(StudyAvailability sa, LocalDateTime now) {
        int weekly = sa.getTotalWeeklyHours();
        if (weekly <= 0) return 0;

        int availableDaysInWeek = 0;
        for (DayOfWeek d : DayOfWeek.values()) {
            if (sa.isAvailable(d)) availableDaysInWeek++;
        }
        if (availableDaysInWeek == 0) return 0;

        int perAvailableDay = weekly / availableDaysInWeek;

        DayOfWeek today = now.getDayOfWeek();

        int remainingAvailableDays = 0;
        for (DayOfWeek d : DayOfWeek.values()) {
            if (d.getValue() >= today.getValue() && sa.isAvailable(d)) {
                remainingAvailableDays++;
            }
        }

        return perAvailableDay * remainingAvailableDays;
    }


    private AssignmentStatus statusFrom(double work, int available) {
        if (available <= 0) {
            return work > 0 ? AssignmentStatus.RED : AssignmentStatus.GREEN;
        }

        double ratio = work / (double) available;

        if (ratio >= 1.0) return AssignmentStatus.RED;
        if (ratio >= 0.80) return AssignmentStatus.ORANGE;
        if (ratio >= 0.60) return AssignmentStatus.YELLOW;
        return AssignmentStatus.GREEN;
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
