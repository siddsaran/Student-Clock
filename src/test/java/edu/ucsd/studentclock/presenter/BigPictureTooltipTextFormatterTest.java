package edu.ucsd.studentclock.presenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 3: Presenter tests for core user actions (BigPictureTooltipTextFormatter).
 * MS1: US10 (View a Big Picture workload visualization).
 */
@DisplayName("DS7-3: BigPictureTooltipTextFormatter")
class BigPictureTooltipTextFormatterTest {

    @Test
    @DisplayName("format returns empty string for null or empty payload")
    void format_returnsEmptyForNullOrEmptyPayload() {
        BigPictureTooltipTextFormatter f = new BigPictureTooltipTextFormatter();
        assertEquals("", f.format(null));
        assertEquals("", f.format(new BigPictureTooltipPayload(List.of())));
    }

    @Test
    @DisplayName("format includes assignment name, course, and due date")
    void format_includesAssignmentNameAndCourse() {
        BigPictureTooltipTextFormatter f = new BigPictureTooltipTextFormatter();

        BigPictureTooltipItem item = new BigPictureTooltipItem(
                "HW1", "CSE 110",
                LocalDate.of(2026, 2, 10),
                5.0, 2.0, 3.0, false);

        String text = f.format(new BigPictureTooltipPayload(List.of(item)));

        assertTrue(text.contains("HW1"));
        assertTrue(text.contains("CSE 110"));
        assertTrue(text.contains("Due: 2026-02-10"));
    }
}