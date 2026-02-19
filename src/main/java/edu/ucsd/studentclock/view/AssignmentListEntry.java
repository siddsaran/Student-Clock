package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.model.Assignment;

/**
 * A single row in the assignment list: an assignment with an optional series tag
 * (null means no series / no tag shown).
 */
public final class AssignmentListEntry {

    private final String tagName;
    private final Assignment assignment;

    private AssignmentListEntry(String tagName, Assignment assignment) {
        this.tagName = tagName;
        this.assignment = assignment;
    }

    /**
     * Creates an entry with no tag (assignment not in a series).
     */
    public static AssignmentListEntry forRowWithoutTag(Assignment assignment) {
        if (assignment == null) {
            throw new NullPointerException("assignment must not be null");
        }
        return new AssignmentListEntry(null, assignment);
    }

    /**
     * Creates an entry with a series tag.
     *
     * @param assignment the assignment
     * @param tagName    display name for the tag (e.g. "PA")
     * @return row entry
     */
    public static AssignmentListEntry forRow(Assignment assignment, String tagName) {
        if (assignment == null) {
            throw new NullPointerException("assignment must not be null");
        }
        if (tagName == null) {
            throw new NullPointerException("tagName must not be null");
        }
        return new AssignmentListEntry(tagName, assignment);
    }

    /**
     * Tag text for the pill; null if no series / no tag.
     */
    public String getDisplayName() {
        return tagName;
    }

    /**
     * The assignment for this row.
     */
    public Assignment getAssignment() {
        return assignment;
    }
}
