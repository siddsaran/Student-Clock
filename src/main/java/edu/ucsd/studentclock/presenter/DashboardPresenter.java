package edu.ucsd.studentclock.presenter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.AssignmentStatusCalculator;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.StudyAvailability;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.service.TimeService;
import edu.ucsd.studentclock.view.DashboardView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class DashboardPresenter extends AbstractPresenter<DashboardView> {

    private final AssignmentRepository assignmentRepo;
    private final TimeService timeService;

    private Runnable onBack;
    private Runnable onBigPicture;

    private final DateTimeFormatter clockFmt =
            DateTimeFormatter.ofPattern("MMMM d, yyyy — h:mm a");

    private final Timeline ticker;

    public DashboardPresenter(Model model,
                              DashboardView view,
                              AssignmentRepository assignmentRepo) {

        super(model, view);
        this.assignmentRepo = assignmentRepo;
        this.timeService = model.getTimeService();

        view.setPresenter(this);

        // toggle real/mock time
        view.syncMockToggleState(timeService.isUsingMock());
        view.getMockToggle().setOnAction(e -> {
            boolean useMock = view.getMockToggle().isSelected();
            if (useMock) {
                timeService.useMockTime();
            } else {
                timeService.useRealTime();
            }
            view.syncMockToggleState(timeService.isUsingMock());
            updateView();
        });

        // set mock date/time
        view.getSetMockButton().setOnAction(e -> {
            try {
                LocalDate d = view.getMockDatePicker().getValue();
                LocalTime t = LocalTime.of(
                        view.getHourSpinner().getValue(),
                        view.getMinuteSpinner().getValue());
                timeService.setMockDateTime(d, t);
                updateView();
            } catch (Exception ex) {
                view.showError(ex.getMessage());
            }
        });

        view.getShowOpenButton().setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        view.getBigPictureButton().setOnAction(e -> {
            if (onBigPicture != null) onBigPicture.run();
        });

        // keep dashboard clock fresh (and also refresh urgency coloring)
        ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateView()));
        ticker.setCycleCount(Timeline.INDEFINITE);
        ticker.play();
    }

    @Override
    public void updateView() {
        LocalDateTime now = timeService.now();

        // show date/time + mode on dashboard
        String mode = timeService.isUsingMock() ? " (MOCK)" : " (REAL)";
        view.setDateTimeText(now.format(clockFmt) + mode);

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
        int workNext7Days = computeRemainingWorkNext7Days(assignmentRepo.getAllAssignments(), now);

        int remainingStudyHours = Math.max(0, availableFromToday - workNext7Days);
        view.setStudyHoursRemaining(remainingStudyHours);

        AssignmentStatus overallStatus = statusFrom(workNext7Days, availableFromToday);
        view.setStudyStatus(overallStatus);

        view.showAssignments(filtered, now);
    }

    private int computeRemainingWorkNext7Days(List<Assignment> all, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDate end = today.plusDays(7);

        int sum = 0;
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

    private int computeAvailableHoursNext7Days(StudyAvailability sa, LocalDateTime now) {
        LocalDate today = now.toLocalDate();

        int sum = 0;
        for (int i = 0; i < 7; i++) {
            DayOfWeek d = today.plusDays(i).getDayOfWeek();
            if (sa.isAvailable(d)) {
                sum += sa.getDailyLimit(d);
            }
        }
        return sum;
    }

    private AssignmentStatus statusFrom(int work, int available) {
        if (available <= 0) {
            return work > 0 ? AssignmentStatus.RED : AssignmentStatus.GREEN;
        }

        double ratio = (double) work / (double) available;

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