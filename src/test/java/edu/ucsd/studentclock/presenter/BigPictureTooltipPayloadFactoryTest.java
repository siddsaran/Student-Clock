package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 3: Presenter tests for core user actions (BigPictureTooltipPayloadFactory).
 * MS1: US10 (View a Big Picture workload visualization).
 */
@DisplayName("DS7-3: BigPictureTooltipPayloadFactory")
class BigPictureTooltipPayloadFactoryTest {

    private Assignment makeAssignment(
            String name,
            String courseId,
            double estimated,
            double cumulative,
            double remaining,
            boolean done) {
        return new AssignmentBuilder()
                .setName(name)
                .setCourseId(courseId)
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(estimated)
                .setRemainingHours(remaining)
                .setCumulativeHours(cumulative)
                .setDone(done)
                .build();
    }

    @Test
    @DisplayName("fromAssignments returns payload with empty items for empty input")
    void fromAssignments_emptyListReturnsPayloadWithEmptyItems() {
        BigPictureTooltipPayloadFactory factory = new BigPictureTooltipPayloadFactory();

        BigPictureTooltipPayload payload = factory.fromAssignments(List.of());

        assertNotNull(payload);
        assertTrue(payload.getItems().isEmpty());
    }

    @Test
    @DisplayName("fromAssignments maps all fields from a single assignment")
    void fromAssignments_singleAssignmentMapsAllFields() {
        BigPictureTooltipPayloadFactory factory = new BigPictureTooltipPayloadFactory();
        Assignment a = makeAssignment("PA1", "CSE 110", 5.0, 2.0, 3.0, false);

        BigPictureTooltipPayload payload = factory.fromAssignments(List.of(a));

        assertEquals(1, payload.getItems().size());
        BigPictureTooltipItem item = payload.getItems().get(0);
        assertEquals("PA1", item.getName());
        assertEquals("CSE 110", item.getCourseId());
        assertEquals(5.0, item.getEstimatedHours());
        assertEquals(2.0, item.getCompletedHours());
        assertEquals(3.0, item.getRemainingHours());
        assertFalse(item.isDone());
    }

    @Test
    @DisplayName("fromAssignments preserves input order when mapping multiple assignments")
    void fromAssignments_multipleAssignmentsMapsInOrder() {
        BigPictureTooltipPayloadFactory factory = new BigPictureTooltipPayloadFactory();
        Assignment a1 = makeAssignment("PA1", "CSE 110", 5.0, 2.0, 3.0, false);
        Assignment a2 = makeAssignment("Quiz", "CSE 101", 1.0, 0.5, 0.5, false);

        BigPictureTooltipPayload payload = factory.fromAssignments(List.of(a1, a2));

        assertEquals(2, payload.getItems().size());
        assertEquals("PA1", payload.getItems().get(0).getName());
        assertEquals("Quiz", payload.getItems().get(1).getName());
    }
}