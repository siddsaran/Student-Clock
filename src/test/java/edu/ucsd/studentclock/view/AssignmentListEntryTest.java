package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.model.Assignment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentListEntryTest {

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
        Assignment a = new Assignment(
                "PA1",
                "CSE 110",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 5, 23, 59),
                0,
                2.0
        );

        AssignmentListEntry row = AssignmentListEntry.forRowWithoutTag(a);

        assertFalse(row.isHeader());
        assertEquals(a, row.getAssignment());
        assertNull(row.getDisplayName());
    }

    @Test
    void rowWithTag_isNotHeaderAndStoresAssignmentAndTag() {
        Assignment a = new Assignment(
                "PA1",
                "CSE 110",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 5, 23, 59),
                0,
                2.0
        );

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
        Assignment a = new Assignment(
                "PA1",
                "CSE 110",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                LocalDateTime.of(2026, 2, 5, 23, 59),
                0,
                2.0
        );
        assertThrows(NullPointerException.class, () -> AssignmentListEntry.forRow(a, null));
    }
}