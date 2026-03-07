package edu.ucsd.studentclock.presenter;

import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.studentclock.model.Assignment;

public final class BigPictureTooltipPayloadFactory {

    public BigPictureTooltipPayload fromAssignments(List<Assignment> activeAssignments) {
        List<BigPictureTooltipItem> items = activeAssignments.stream()
                .map(a -> new BigPictureTooltipItem(
                        a.getName(),
                        a.getCourseId(),
                        a.getDeadline().toLocalDate(),
                        a.getEstimatedHours(),
                        a.getCumulativeHours(),
                        a.getRemainingHours(),
                        a.isDone()
                ))
                .collect(Collectors.toList());

        return new BigPictureTooltipPayload(items);
    }
}