package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.AssignmentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story DS7, Task 2: Unit tests for view/domain logic (AssignmentListEntry).
 * MS1: US2 (Add assignments), US9 (View all open assignments).
 */
@DisplayName("DS7-2: AssignmentListEntry")
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
    @DisplayName("Header entry is marked as header and stores header text")
    void headerEntry_isHeaderAndHasHeaderText() {
        AssignmentListEntry h = AssignmentListEntry.forHeader("CSE 110");

        assertTrue(h.isHeader());
        assertEquals("CSE 110", h.getHeaderText());
        assertNull(h.getAssignment());
        assertNull(h.getDisplayName());
    }

    @Test
    @DisplayName("Row without tag stores assignment and is not a header")
    void rowWithoutTag_isNotHeaderAndStoresAssignment() {
        Assignment a = makeAssignment();
        AssignmentListEntry row = AssignmentListEntry.forRowWithoutTag(a);

        assertFalse(row.isHeader());
        assertEquals(a, row.getAssignment());
        assertNull(row.getDisplayName());
    }

    @Test
    @DisplayName("Row with tag stores assignment, tag name, and tag color")
    void rowWithTag_isNotHeaderAndStoresAssignmentTagAndColor() {
        Assignment a = makeAssignment();
        AssignmentListEntry row = AssignmentListEntry.forRow(a, "Programming Assignments", "#4A90D9");

        assertFalse(row.isHeader());
        assertEquals(a, row.getAssignment());
        assertEquals("Programming Assignments", row.getDisplayName());
        assertEquals("#4A90D9", row.getTagColor());
    }

    @Test
    @DisplayName("Row without tag has null tag color")
    void rowWithoutTag_hasNullTagColor() {
        Assignment a = makeAssignment();
        AssignmentListEntry row = AssignmentListEntry.forRowWithoutTag(a);

        assertNull(row.getTagColor());
    }

    @Test
    @DisplayName("Creating header with null text throws NullPointerException")
    void forHeader_nullThrows() {
        assertThrows(NullPointerException.class,
                () -> AssignmentListEntry.forHeader(null));
    }

    @Test
    @DisplayName("Creating row with null assignment throws NullPointerException")
    void forRow_nullAssignmentThrows() {
        assertThrows(NullPointerException.class,
                () -> AssignmentListEntry.forRowWithoutTag(null));

        assertThrows(NullPointerException.class,
                () -> AssignmentListEntry.forRow(null, "x", "#4A90D9"));
    }

    @Test
    @DisplayName("Creating row with null tag name throws NullPointerException")
    void forRow_nullTagThrows() {
        assertThrows(NullPointerException.class,
                () -> AssignmentListEntry.forRow(makeAssignment(), null, "#4A90D9"));
    }

    @Test
    @DisplayName("Creating row with null tag color throws NullPointerException")
    void forRow_nullTagColorThrows() {
        assertThrows(NullPointerException.class,
                () -> AssignmentListEntry.forRow(makeAssignment(), "PAs", null));
    }
}