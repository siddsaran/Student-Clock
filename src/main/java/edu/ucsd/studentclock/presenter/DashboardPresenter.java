package edu.ucsd.studentclock.presenter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentStatus;
import edu.ucsd.studentclock.model.AssignmentStatusCalculator;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.StudyAvailability;
import edu.ucsd.studentclock.service.ITimeService;
import edu.ucsd.studentclock.view.DashboardView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class DashboardPresenter extends AbstractPresenter<DashboardView> implements IDashboardScreenPresenter {

    private final ITimeService timeService;

    private final DateTimeFormatter clockFmt =
            DateTimeFormatter.ofPattern("MMMM d, yyyy — h:mm a");

    private final Timeline ticker;

    public DashboardPresenter(Model model,
                              DashboardView view) {

        super(model, view);
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

        List<Assignment> filtered = model.getAllAssignments().stream()
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

        int availableFromToday = DashboardStudyHoursCalculator.computeWeeklyHoursLeftFromToday(sa, now);
        double totalLoggedThisWeek = model.getTotalHoursLoggedInWeek(now.toLocalDate());
        int remainingStudyHours = Math.max(0, availableFromToday - (int) Math.round(totalLoggedThisWeek));
        view.setStudyHoursRemaining(remainingStudyHours);

        double workNext7Days = DashboardStudyHoursCalculator.computeRemainingWorkNext7Days(model.getAllAssignments(), now);
        AssignmentStatus overallStatus = DashboardStudyHoursCalculator.statusFrom(workNext7Days, remainingStudyHours);
        view.setStudyStatus(overallStatus);

        Map<Assignment, String> rowStyles = new HashMap<>();
        for (Assignment a : filtered) {
            AssignmentStatus status = AssignmentStatusCalculator.behindStatus(a, now);
            String style;
            switch (status) {
                case RED:
                    style = "-fx-background-color:#e75b67;";
                    break;
                case ORANGE:
                    style = "-fx-background-color:#f69d20;";
                    break;
                case YELLOW:
                    style = "-fx-background-color:#ffdf74;";
                    break;
                default:
                    style = "";
                    break;
            }
            rowStyles.put(a, style);
        }
        view.showAssignments(filtered, rowStyles);
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
    }

    @Override
    public String getViewTitle() {
        return "Dashboard";
    }
}