package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.util.TimeFormatUtils;

final class BigPictureTooltipTextFormatter {

    String format(BigPictureTooltipPayload payload) {
        if (payload == null || payload.getItems() == null || payload.getItems().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (BigPictureTooltipItem item : payload.getItems()) {
            sb.append(item.getName())
              .append(" (").append(item.getCourseId()).append(")\n")
              .append("Due: ").append(item.getDueDate()).append("\n")
              .append("Estimated: ").append(TimeFormatUtils.formatHoursAsHHMM(item.getEstimatedHours())).append("\n")
              .append("Completed: ").append(TimeFormatUtils.formatHoursAsHHMM(item.getCompletedHours())).append("\n")
              .append("Remaining: ").append(TimeFormatUtils.formatHoursAsHHMM(item.getRemainingHours())).append("\n");

            if (item.isDone()) {
                sb.append("Status: DONE\n");
            }
            sb.append("\n");
        }

        return sb.toString().trim();
    }
}