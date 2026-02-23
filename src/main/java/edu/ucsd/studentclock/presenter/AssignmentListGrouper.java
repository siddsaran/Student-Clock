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

/**
 * Builds a grouped list of assignment list entries (by course, then by series) for display.
 * Keeps list-building logic out of the presenter.
 */
public final class AssignmentListGrouper {

    private AssignmentListGrouper() {}

    /**
     * Builds a list of assignment rows with course headers. No-series assignments have no tag;
     * series assignments have a tag. Order: no series first, then each series by name.
     *
     * @param allAssignments all assignments (e.g. from repository)
     * @param showOnlyOpen   if true, filter to non-done assignments only
     * @param courseFilter   course id to filter by, or allCoursesLabel to show all
     * @param allCoursesLabel value that means "no course filter" (e.g. "All Courses")
     * @param model          used to resolve series display names
     * @return list of headers and rows for the assignment list
     */
    public static List<AssignmentListEntry> buildGroupedList(
            List<Assignment> allAssignments,
            boolean showOnlyOpen,
            String courseFilter,
            String allCoursesLabel,
            Model model) {
        List<Assignment> assignments = allAssignments;
        if (showOnlyOpen) {
            assignments = assignments.stream()
                    .filter(a -> !a.isDone())
                    .collect(Collectors.toList());
        }
        if (courseFilter != null
                && !allCoursesLabel.equals(courseFilter)
                && !courseFilter.isBlank()) {
            assignments = assignments.stream()
                    .filter(a -> courseFilter.equals(a.getCourseID()))
                    .collect(Collectors.toList());
        }

        Map<String, String> seriesIdToName = new HashMap<>();
        for (Assignment a : assignments) {
            String sid = a.getSeriesId();
            if (sid != null && !seriesIdToName.containsKey(sid)) {
                String name = model.getSeries(sid)
                        .map(Series::getName)
                        .orElse(sid);
                seriesIdToName.put(sid, name);
            }
        }

        Map<String, List<Assignment>> byCourse = assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getCourseID));
        List<String> courseIds = byCourse.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        List<AssignmentListEntry> result = new ArrayList<>();
        for (String courseId : courseIds) {
            result.add(AssignmentListEntry.forHeader(courseId));

            List<Assignment> courseAssignments = byCourse.get(courseId);
            if (courseAssignments == null || courseAssignments.isEmpty()) continue;

            Map<String, List<Assignment>> bySeries = new HashMap<>();
            for (Assignment a : courseAssignments) {
                String key = a.getSeriesId() != null ? a.getSeriesId() : null;
                bySeries.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
            }

            List<Assignment> noSeriesList = bySeries.get(null);
            if (noSeriesList != null) {
                for (Assignment a : noSeriesList) {
                    result.add(AssignmentListEntry.forRowWithoutTag(a));
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
                for (Assignment a : bySeries.get(seriesId)) {
                    result.add(AssignmentListEntry.forRow(a, displayName));
                }
            }
        }
        return result;
    }
}
