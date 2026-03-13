package edu.ucsd.studentclock.presenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("NavigationRouter")
class NavigationRouterTest {

    private NavigationRouter createRouter(List<String> calls) {
        return new NavigationRouter(
                () -> calls.add("dashboard"),
                () -> calls.add("courses"),
                () -> calls.add("study"),
                () -> calls.add("big-picture"),
                () -> calls.add("switch-assignments"),
                () -> calls.add("prepare-all"),
                () -> calls.add("prepare-open"));
    }

    @Test
    @DisplayName("Constructor throws when dashboard runnable is null")
    void constructorThrowsWhenDashboardRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationRouter(
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
    @DisplayName("Constructor throws when switchToAssignments runnable is null")
    void constructorThrowsWhenSwitchToAssignmentsRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationRouter(
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
    @DisplayName("Constructor throws when prepareAssignmentsAll runnable is null")
    void constructorThrowsWhenPrepareAssignmentsAllRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationRouter(
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
    @DisplayName("Constructor throws when prepareAssignmentsOpen runnable is null")
    void constructorThrowsWhenPrepareAssignmentsOpenRunnableNull() {
        assertThrows(NullPointerException.class, () -> new NavigationRouter(
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
    @DisplayName("toDashboard runs only dashboard navigation")
    void toDashboard_runsDashboardRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationRouter router = createRouter(calls);

        router.toDashboard();

        assertEquals(List.of("dashboard"), calls);
    }

    @Test
    @DisplayName("toCourses runs only courses navigation")
    void toCourses_runsCoursesRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationRouter router = createRouter(calls);

        router.toCourses();

        assertEquals(List.of("courses"), calls);
    }

    @Test
    @DisplayName("toStudyAvailability runs only study availability navigation")
    void toStudyAvailability_runsStudyAvailabilityRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationRouter router = createRouter(calls);

        router.toStudyAvailability();

        assertEquals(List.of("study"), calls);
    }

    @Test
    @DisplayName("toBigPicture runs only big picture navigation")
    void toBigPicture_runsBigPictureRunnable() {
        List<String> calls = new ArrayList<>();
        NavigationRouter router = createRouter(calls);

        router.toBigPicture();

        assertEquals(List.of("big-picture"), calls);
    }

    @Test
    @DisplayName("toAssignmentsOpen runs prepareOpen before switching to assignments view")
    void toAssignmentsOpen_runsPrepareOpenBeforeSwitch() {
        List<String> calls = new ArrayList<>();
        NavigationRouter router = createRouter(calls);

        router.toAssignmentsOpen();

        assertEquals(List.of("prepare-open", "switch-assignments"), calls);
    }

    @Test
    @DisplayName("toAssignmentsAll runs prepareAll before switching to assignments view")
    void toAssignmentsAll_runsPrepareAllBeforeSwitch() {
        List<String> calls = new ArrayList<>();
        NavigationRouter router = createRouter(calls);

        router.toAssignmentsAll();

        assertEquals(List.of("prepare-all", "switch-assignments"), calls);
    }
}