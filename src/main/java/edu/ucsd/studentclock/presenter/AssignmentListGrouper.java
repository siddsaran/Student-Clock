package edu.ucsd.studentclock.presenter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.Series;
import edu.ucsd.studentclock.view.AssignmentListEntry;

public final class AssignmentListGrouper {

    private static final String[] TAG_COLORS = {
            "#4A90D9", "#7B68A6", "#50A060", "#C07850", "#B85450",
            "#5B9AA0", "#E8A838", "#6B8E6B", "#9B6B8E", "#4A7C9E"
    };

    private AssignmentListGrouper() {
    }

    private static String colorForSeries(String displayName) {
        int index = Math.abs(displayName.hashCode()) % TAG_COLORS.length;
        return TAG_COLORS[index];
    }

    public static List<AssignmentListEntry> buildGroupedList(
            List<Assignment> allAssignments,
            boolean showOnlyOpen,
            String courseFilter,
            String allCoursesLabel,
            Model model
    ) {
        List<Assignment> assignments = List.copyOf(allAssignments);

        if (showOnlyOpen) {
            assignments = AssignmentFilters.openAssignments(assignments);
        }

        assignments = AssignmentFilters.filterByCourse(assignments, courseFilter, allCoursesLabel);

        Map<String, String> seriesIdToName = new HashMap<>();
        for (Assignment assignment : assignments) {
            String seriesId = assignment.getSeriesId();
            if (seriesId != null && !seriesIdToName.containsKey(seriesId)) {
                String seriesName = model.getSeries(seriesId)
                        .map(Series::getName)
                        .orElse(seriesId);
                seriesIdToName.put(seriesId, seriesName);
            }
        }

        Map<String, List<Assignment>> assignmentsByCourse = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getCourseId));

        List<String> courseIds = assignmentsByCourse.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        List<AssignmentListEntry> groupedList = new ArrayList<>();
        for (String courseId : courseIds) {
            groupedList.add(AssignmentListEntry.forHeader(courseId));

            List<Assignment> courseAssignments = assignmentsByCourse.get(courseId);
            if (courseAssignments == null || courseAssignments.isEmpty()) {
                continue;
            }

            Map<String, List<Assignment>> assignmentsBySeries = new HashMap<>();
            for (Assignment assignment : courseAssignments) {
                String seriesId = assignment.getSeriesId();
                assignmentsBySeries.computeIfAbsent(seriesId, key -> new ArrayList<>()).add(assignment);
            }

            List<Assignment> noSeriesAssignments = assignmentsBySeries.get(null);
            if (noSeriesAssignments != null) {
                for (Assignment assignment : noSeriesAssignments) {
                    groupedList.add(AssignmentListEntry.forRowWithoutTag(assignment));
                }
            }

            List<String> seriesIds = courseAssignments.stream()
                    .map(Assignment::getSeriesId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator
                            .comparing((String id) -> seriesIdToName.getOrDefault(id, id))
                            .thenComparing(id -> id))
                    .collect(Collectors.toList());

            for (String seriesId : seriesIds) {
                String displayName = seriesIdToName.getOrDefault(seriesId, seriesId);
                List<Assignment> seriesAssignments = assignmentsBySeries.get(seriesId);

                if (seriesAssignments == null) {
                    continue;
                }

                String tagColor = colorForSeries(displayName);
                for (Assignment assignment : seriesAssignments) {
                    groupedList.add(AssignmentListEntry.forRow(assignment, displayName, tagColor));
                }
            }
        }

        return groupedList;
    }
}