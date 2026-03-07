package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentListEntryTest {

    private Assignment makeAssignment() {
        return new AssignmentBuilder()
                .setName("PA1")
                .setCourseId("CSE 110")
                .setStart(LocalDateTime.of(2026, 2, 1, 9, 0))
                .setDeadline(LocalDateTime.of(2026, 2, 5, 23, 59))
                .setLateDaysAllowed(0)
                .setEstimatedHours(2.0)
                .build();
    }

    @Test
    void headerEntry_isHeaderAndHasHeaderText() {
        AssignmentListEntry h = AssignmentListEntry.forHeader("CSE 110");
        assertTrue(h.isHeader());
        assertEquals("CSE 110", h.getHeaderText());
        assertNull(h.getAssignment());
        assertNull(h.getDisplayName());
    }

    @Test
    void rowWithoutTag_isNotHeaderAndStoresAssignment() {
        Assignment a = makeAssignment();
        AssignmentListEntry row = AssignmentListEntry.forRowWithoutTag(a);

        assertFalse(row.isHeader());
        assertEquals(a, row.getAssignment());
        assertNull(row.getDisplayName());
    }

    @Test
    void rowWithTag_isNotHeaderAndStoresAssignmentAndTag() {
        Assignment a = makeAssignment();
        AssignmentListEntry row = AssignmentListEntry.forRow(a, "Programming Assignments");

        assertFalse(row.isHeader());
        assertEquals(a, row.getAssignment());
        assertEquals("Programming Assignments", row.getDisplayName());
    }

    @Test
    void forHeader_nullThrows() {
        assertThrows(NullPointerException.class, () -> AssignmentListEntry.forHeader(null));
    }

    @Test
    void forRow_nullAssignmentThrows() {
        assertThrows(NullPointerException.class, () -> AssignmentListEntry.forRowWithoutTag(null));
        assertThrows(NullPointerException.class, () -> AssignmentListEntry.forRow(null, "x"));
    }

    @Test
    void forRow_nullTagThrows() {
        assertThrows(NullPointerException.class, () -> AssignmentListEntry.forRow(makeAssignment(), null));
    }
}