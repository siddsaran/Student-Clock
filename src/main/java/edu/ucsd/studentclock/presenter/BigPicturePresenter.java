package edu.ucsd.studentclock.presenter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.util.TimeFormatUtils;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.repository.AssignmentWorkLogRepository;
import edu.ucsd.studentclock.view.BigPictureView;
import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class BigPicturePresenter extends AbstractPresenter<BigPictureView> {

    private Runnable onBack;
    private Runnable onCourses;
    private Runnable onAssignments;
    private Runnable onStudyAvailability;
    private Runnable onDashboard;
    private final AssignmentRepository repository;
    private final AssignmentWorkLogRepository assignmentWorkLogRepository;

    


    public BigPicturePresenter(Model model, BigPictureView view, AssignmentRepository repository,
                               AssignmentWorkLogRepository assignmentWorkLogRepository) {
        super(model, view);
        this.repository = repository;
        this.assignmentWorkLogRepository = assignmentWorkLogRepository;


        view.getBackButton().setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        view.getCoursesButton().setOnAction(e -> {
            if (onCourses != null) onCourses.run();
        });

        view.getAssignmentsButton().setOnAction(e -> {
            if (onAssignments != null) onAssignments.run();
        });

        view.getStudyAvailabilityButton().setOnAction(e -> {
            if (onStudyAvailability != null) onStudyAvailability.run();
        });

        view.getDashboardButton().setOnAction(e -> {
            if (onDashboard != null) onDashboard.run();
        });


        updateView();
    }

    @Override
    public String getViewTitle() {
        return "Big Picture";
    }

    private static XYChart.Data<String, Number> createDataPoint(String label, double y,
                                                                  List<Assignment> activeAssignments) {
        XYChart.Data<String, Number> point = new XYChart.Data<>(label, y);
        point.setExtraValue(activeAssignments);
        return point;
    }

    /**
     * Returns remaining hours for an assignment as of end of given day.
     * Uses work logs when available; falls back to current remaining for legacy data.
     */
    private double remainingHoursAt(Assignment a, LocalDate day,
                                    Map<String, Double> cumulativeByEndOfDay) {
        Double logged = cumulativeByEndOfDay.get(a.getID());
        if (logged != null) {
            return Math.max(0, a.getEstimatedHours() - logged);
        }
        return a.getRemainingHours();
    }

    @Override
    public void updateView() {
        List<Assignment> assignments = repository.getAllAssignments().stream()
                .filter(a -> !a.isDone())
                .collect(Collectors.toList());

        if (assignments.isEmpty()) {
            view.getChart().getData().clear();
            return;
        }
        Map<Assignment, LocalDate[]> effectiveRanges =
                BigPictureEffectiveRanges.computeEffectiveRanges(assignments);

        LocalDate chartStart = effectiveRanges.values().stream()
                .map(r -> r[0])
                .min(LocalDate::compareTo)
                .get();

        LocalDate chartEnd = effectiveRanges.values().stream()
                .map(r -> r[1])
                .max(LocalDate::compareTo)
                .get();

        XYChart.Series<String, Number> workload = new XYChart.Series<>();
        workload.setName("Workload");

        XYChart.Series<String, Number> burndown = new XYChart.Series<>();
        burndown.setName("IdealBurndown");

        long totalDays = ChronoUnit.DAYS.between(chartStart, chartEnd);
        double runningWorkload = 0;
        double maxWorkload = 0;
        Map<String, Double> prevCumulative = Map.of();

        for (int i = 0; i <= totalDays; i++) {
            LocalDate day = chartStart.plusDays(i);
            LocalDate prevDay = i > 0 ? chartStart.plusDays(i - 1) : null;
            Map<String, Double> cumulativeWork = assignmentWorkLogRepository.getCumulativeHoursByEndOf(day);

            List<Assignment> endsToday = assignments.stream()
                    .filter(a -> effectiveRanges.get(a)[1].equals(day))
                    .collect(Collectors.toList());

            List<Assignment> startsToday = assignments.stream()
                    .filter(a -> effectiveRanges.get(a)[0].equals(day))
                    .collect(Collectors.toList());

            // Assignments active on this day (effectiveStart <= day <= effectiveEnd)
            List<Assignment> activeOnDay = assignments.stream()
                    .filter(a -> {
                        LocalDate[] r = effectiveRanges.get(a);
                        return !day.isBefore(r[0]) && !day.isAfter(r[1]);
                    })
                    .collect(Collectors.toList());

            String label = day.getMonthValue() + "/" + day.getDayOfMonth();

            // Assignments active at start of day (started before today, not ended yet)
            List<Assignment> activeAtStart = assignments.stream()
                    .filter(a -> {
                        LocalDate[] r = effectiveRanges.get(a);
                        return day.compareTo(r[0]) > 0 && !day.isAfter(r[1]);
                    })
                    .collect(Collectors.toList());

            // Point at start of day (connects from previous day)
            // Skip (day, 0) when assignments start today so line starts at workload level on the left
            boolean skipStartPoint = runningWorkload == 0 && !startsToday.isEmpty();
            if (!skipStartPoint) {
                workload.getData().add(createDataPoint(label, runningWorkload, activeOnDay));
            }

            // Work logged today: remaining dropped for active assignments
            if (prevDay != null) {
                boolean workLoggedToday = false;
                for (Assignment a : activeAtStart) {
                    double remPrev = remainingHoursAt(a, prevDay, prevCumulative);
                    double remToday = remainingHoursAt(a, day, cumulativeWork);
                    if (remToday != remPrev) {
                        runningWorkload += (remToday - remPrev);
                        workLoggedToday = true;
                    }
                }
                if (workLoggedToday) {
                    workload.getData().add(createDataPoint(label, runningWorkload, activeOnDay));
                }
            }
            prevCumulative = cumulativeWork;

            if (!endsToday.isEmpty()) {
                for (Assignment a : endsToday) {
                    runningWorkload -= remainingHoursAt(a, day, cumulativeWork);
                }
                List<Assignment> activeAfterEnds = activeOnDay.stream()
                        .filter(a -> !endsToday.contains(a))
                        .collect(Collectors.toList());
                workload.getData().add(createDataPoint(label, runningWorkload, activeAfterEnds));
            }

            if (!startsToday.isEmpty()) {
                for (Assignment a : startsToday) {
                    runningWorkload += remainingHoursAt(a, day, cumulativeWork);
                }
                workload.getData().add(createDataPoint(label, runningWorkload, activeOnDay));
            }

            maxWorkload = Math.max(maxWorkload, runningWorkload);
        }

        double firstHeight = workload.getData().isEmpty() ? 0
                : workload.getData().get(0).getYValue().doubleValue();
        double maxWork = Math.max(firstHeight, maxWorkload);

        // Ideal burndown ends at final workload level, not 0, so it ends horizontally
        if (!workload.getData().isEmpty()) {
            String first = workload.getData().get(0).getXValue();
            String last = workload.getData().get(workload.getData().size() - 1).getXValue();
            double finalWorkload = workload.getData().get(workload.getData().size() - 1).getYValue().doubleValue();
            burndown.getData().add(new XYChart.Data<>(first, maxWork));
            burndown.getData().add(new XYChart.Data<>(last, finalWorkload));
        }

        view.getChart().getData().setAll(List.of(workload, burndown));

        NumberAxis yAxis = (NumberAxis) view.getChart().getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(Math.max(1, maxWork));
        yAxis.setTickUnit(Math.max(1, maxWork / 5));

        view.getChart().applyCss();
        view.getChart().layout();

        if (workload.getNode() != null) {
            workload.getNode().setStyle("-fx-stroke-width: 2;");
        }
        if (burndown.getNode() != null) {
            burndown.getNode().setStyle("-fx-stroke-width: 2; -fx-stroke-dash-array: 6 6;");
        }

        // Tooltips with HH:MM formatting
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> point : workload.getData()) {
                if (point.getNode() == null) continue;

                @SuppressWarnings("unchecked")
                List<Assignment> matches = (List<Assignment>) point.getExtraValue();
                if (matches == null || matches.isEmpty()) continue;

                StringBuilder sb = new StringBuilder();
                for (Assignment a : matches) {
                    sb.append(a.getName())
                            .append(" (").append(a.getCourseID()).append(")\n")
                            .append("Due: ").append(a.getDeadline().toLocalDate()).append("\n")
                            .append("Estimated: ").append(TimeFormatUtils.formatHoursAsHHMM(a.getEstimatedHours())).append("\n")
                            .append("Completed: ").append(TimeFormatUtils.formatHoursAsHHMM(a.getCumulativeHours())).append("\n")
                            .append("Remaining: ").append(TimeFormatUtils.formatHoursAsHHMM(a.getRemainingHours())).append("\n");
                    if (a.isDone()) sb.append("Status: DONE\n");
                    sb.append("\n");
                }

                Tooltip tooltip = new Tooltip(sb.toString().trim());
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setHideDelay(Duration.ZERO);
                tooltip.setShowDuration(Duration.seconds(60));
                point.getNode().setPickOnBounds(true);
                point.getNode().setMouseTransparent(false);
                point.getNode().setStyle("-fx-cursor: hand;");
                Tooltip.install(point.getNode(), tooltip);
            }
        });
    }

    public void setOnBack(Runnable r) {
        onBack = r;
    }
    public void setOnCourses(Runnable r) {
        onCourses = r; 
    }
    public void setOnAssignments(Runnable r) { 
        onAssignments = r; 
    }
    public void setOnStudyAvailability(Runnable r) { 
        onStudyAvailability = r; 
    }
    public void setOnDashboard(Runnable r) { 
        onDashboard = r; 
    }

}
