# Abstract

**Problem:** Recurring assignments (e.g. PAs) had to be added one by one and then grouped in a separate "Create Series + Link Selected" step, which was tedious and easy to forget.

**Solution:** When adding an assignment, the user can now optionally create a new series or add to an existing one in the same step. The add-assignment form drives both assignment creation and series creation/selection.

**Changes:**
- **AssignmentPresenter:** New overload `createAssignment(..., String seriesId)` so assignments can be created with a series; when `seriesId` is set, the assignment uses that series' default late days. Added `createSeries(seriesId, courseId, seriesName, defaultLateDays)` and `getSeriesForCourse(courseId)` so the view can create series and list series per course.
- **AssignmentView:** Added a series choice (No series / Create new series / Add to existing series) and conditional UI: for "Create new series," fields for series name, optional series ID (auto-generated if blank), and default late days; for "Add to existing series," a dropdown of that course's series, refreshed when the course changes. `handleCreate()` branches on that choice and calls the new presenter APIs; inputs are cleared after a successful add.
- **Tests:** New `AssignmentPresenterTest` (with JavaFX init) covering: create with no series; create with existing series (correct late days and `seriesId`); unknown series throws; create series then create assignment; `getSeriesForCourse`; six-arg `createAssignment` delegating to the new overload.

## Type of Change

- New feature (non-breaking change which adds functionality)

# Testing

- **Unit tests:** `AssignmentPresenterTest` (7 tests) verifies: assignment without series; assignment in existing series (late days and `seriesId`); unknown series throws; create series + assignment in one flow; `getSeriesForCourse`; null/blank course returns empty list; six-arg `createAssignment` still works.
- **Manual:** Add assignment with "No series" (unchanged). Add assignment with "Create new series" (fill name, optional ID, late days) and confirm series and assignment appear. Add assignment with "Add to existing series" (pick course, then series) and confirm assignment is in that series. Change course and confirm "Add to existing series" dropdown updates.

# Checklist

- [x] **The code is understandable.** Formatting and naming follow existing style; non-obvious logic is commented.
- [x] **The code is well structured.** Presenter handles series/assignment creation; view handles series choice and conditional fields; responsibilities are clear.
- [x] **The code is correct.** New tests pass; full test suite passes; no new build errors or warnings.

# Pending Items
