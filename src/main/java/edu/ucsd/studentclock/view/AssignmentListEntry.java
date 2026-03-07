package edu.ucsd.studentclock.view;

import edu.ucsd.studentclock.model.Assignment;

/**
 * A single row in the assignment list:
 * - either a HEADER row (course section header)
 * - or a ROW (an assignment with an optional series tag)
 */
public final class AssignmentListEntry {

    public enum Kind {
        HEADER,
        ROW
    }

    private final Kind kind;
    private final String tagName;
    private final String tagColor;
    private final String headerText;
    private final Assignment assignment;

    private AssignmentListEntry(Kind kind, String headerText, String tagName, String tagColor, Assignment assignment) {
        this.kind = kind;
        this.headerText = headerText;
        this.tagName = tagName;
        this.tagColor = tagColor;
        this.assignment = assignment;
    }

    // Creates a non-selectable section header row (e.g., a course id).
    public static AssignmentListEntry forHeader(String headerText) {
        if (headerText == null) throw new NullPointerException("headerText must not be null");
        return new AssignmentListEntry(Kind.HEADER, headerText, null, null, null);
    }

    // Creates an assignment row with no tag (assignment not in a series).
    public static AssignmentListEntry forRowWithoutTag(Assignment assignment) {
        if (assignment == null) throw new NullPointerException("assignment must not be null");
        return new AssignmentListEntry(Kind.ROW, null, null, null, assignment);
    }

    // Creates an assignment row with a series tag. tagColor is the hex color for the tag pill.
    public static AssignmentListEntry forRow(Assignment assignment, String tagName, String tagColor) {
        if (assignment == null) throw new NullPointerException("assignment must not be null");
        if (tagName == null) throw new NullPointerException("tagName must not be null");
        if (tagColor == null) throw new NullPointerException("tagColor must not be null");
        return new AssignmentListEntry(Kind.ROW, null, tagName, tagColor, assignment);
    }

    public boolean isHeader() {
        return kind == Kind.HEADER;
    }

    public String getHeaderText() {
        return headerText;
    }

    // Tag text for the pill; null if no series / no tag.
    public String getDisplayName() {
        return tagName;
    }

    // Hex color for the tag pill; null if no tag.
    public String getTagColor() {
        return tagColor;
    }

    // The assignment for this row (null for header rows).
    public Assignment getAssignment() {
        return assignment;
    }
}