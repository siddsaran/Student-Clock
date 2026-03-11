package edu.ucsd.studentclock.presenter;

/**
 * Centralizes navigation rules between high-level screens.
 *
 * This class is intentionally free of JavaFX so it can be tested headlessly.
 */
public final class NavigationRouter {

    private final Runnable toDashboard;
    private final Runnable toCourses;
    private final Runnable toStudyAvailability;
    private final Runnable toBigPicture;

    private final Runnable switchToAssignments;
    private final Runnable prepareAssignmentsAll;
    private final Runnable prepareAssignmentsOpen;

    public NavigationRouter(
            Runnable toDashboard,
            Runnable toCourses,
            Runnable toStudyAvailability,
            Runnable toBigPicture,
            Runnable switchToAssignments,
            Runnable prepareAssignmentsAll,
            Runnable prepareAssignmentsOpen
    ) {
        this.toDashboard = requireNonNull(toDashboard, "toDashboard");
        this.toCourses = requireNonNull(toCourses, "toCourses");
        this.toStudyAvailability = requireNonNull(toStudyAvailability, "toStudyAvailability");
        this.toBigPicture = requireNonNull(toBigPicture, "toBigPicture");
        this.switchToAssignments = requireNonNull(switchToAssignments, "switchToAssignments");
        this.prepareAssignmentsAll = requireNonNull(prepareAssignmentsAll, "prepareAssignmentsAll");
        this.prepareAssignmentsOpen = requireNonNull(prepareAssignmentsOpen, "prepareAssignmentsOpen");
    }

    public void toDashboard() {
        toDashboard.run();
    }

    public void toCourses() {
        toCourses.run();
    }

    public void toStudyAvailability() {
        toStudyAvailability.run();
    }

    public void toBigPicture() {
        toBigPicture.run();
    }

    /**
     * Shows the assignment screen with all assignments visible.
     * Ensures the presenter prepares state before switching screens.
     */
    public void toAssignmentsAll() {
        prepareAssignmentsAll.run();
        switchToAssignments.run();
    }

    /**
     * Shows the assignment screen filtered to open assignments only.
     * Ensures the presenter prepares state before switching screens.
     */
    public void toAssignmentsOpen() {
        prepareAssignmentsOpen.run();
        switchToAssignments.run();
    }

    private static Runnable requireNonNull(Runnable r, String name) {
        if (r == null) {
            throw new NullPointerException(name + " runnable must not be null");
        }
        return r;
    }
}

