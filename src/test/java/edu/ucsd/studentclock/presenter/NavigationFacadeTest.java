package edu.ucsd.studentclock.presenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Story DS6, Task 5 (Enforce facade): Unit tests for NavigationFacade.
 * MS1: US6 (Dashboard), US9 (Assignments view), US10 (Big Picture) navigation.
 */
@DisplayName("DS6-5: NavigationFacade")
class NavigationFacadeTest {

    private NavigationFacade createFacade(List<String> calls) {
        return new NavigationFacade(
                () -> calls.add("dashboard"),
                () -> calls.add("courses"),
                () -> calls.add("study"),
                () -> calls.add("big-picture"),
                () -> calls.add("switch-assignments"),
                () -> calls.add("prepare-all"),
                () -> calls.add("prepare-open"));
    }

    @Test
    @DisplayName("DS6-5: Constructor throws when dashboard runnable is null")
    void constructorThrowsWhenDashboardRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationFacade(
                null,
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                }));
    }

    @Test
    @DisplayName("DS6-5: Constructor throws when switchToAssignments runnable is null")
    void constructorThrowsWhenSwitchToAssignmentsRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationFacade(
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                null,
                () -> {
                },
                () -> {
                }));
    }

    @Test
    @DisplayName("DS6-5: Constructor throws when prepareAssignmentsAll runnable is null")
    void constructorThrowsWhenPrepareAssignmentsAllRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationFacade(
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                null,
                () -> {
                }));
    }

    @Test
    @DisplayName("DS6-5: Constructor throws when prepareAssignmentsOpen runnable is null")
    void constructorThrowsWhenPrepareAssignmentsOpenRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationFacade(
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                () -> {
                },
                null));
    }

    @Test
    @DisplayName("DS6-5: toDashboard runs only dashboard navigation")
    void toDashboard_runsDashboardRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationFacade facade = createFacade(calls);

        facade.toDashboard();

        assertEquals(List.of("dashboard"), calls);
    }

    @Test
    @DisplayName("DS6-5: toCourses runs only courses navigation")
    void toCourses_runsCoursesRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationFacade facade = createFacade(calls);

        facade.toCourses();

        assertEquals(List.of("courses"), calls);
    }

    @Test
    @DisplayName("DS6-5: toStudyAvailability runs only study availability navigation")
    void toStudyAvailability_runsStudyAvailabilityRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationFacade facade = createFacade(calls);

        facade.toStudyAvailability();

        assertEquals(List.of("study"), calls);
    }

    @Test
    @DisplayName("DS6-5: toBigPicture runs only big picture navigation")
    void toBigPicture_runsBigPictureRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationFacade facade = createFacade(calls);

        facade.toBigPicture();

        assertEquals(List.of("big-picture"), calls);
    }

    @Test
    @DisplayName("DS6-5: toAssignmentsOpen runs prepareOpen before switching to assignments view")
    void toAssignmentsOpen_runsPrepareOpenBeforeSwitch() {
        List<String> calls = new ArrayList<>();
        NavigationFacade facade = createFacade(calls);

        facade.toAssignmentsOpen();

        assertEquals(List.of("prepare-open", "switch-assignments"), calls);
    }

    @Test
    @DisplayName("DS6-5: toAssignmentsAll runs prepareAll before switching to assignments view")
    void toAssignmentsAll_runsPrepareAllBeforeSwitch() {
        List<String> calls = new ArrayList<>();
        NavigationFacade facade = createFacade(calls);

        facade.toAssignmentsAll();

        assertEquals(List.of("prepare-all", "switch-assignments"), calls);
    }
}
