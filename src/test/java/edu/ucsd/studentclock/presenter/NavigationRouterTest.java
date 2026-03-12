package edu.ucsd.studentclock.presenter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("NavigationRouter")
class NavigationRouterTest {

    @Test
    @DisplayName("DS6-4: Centralize navigation - toAssignmentsOpen prepares then switches")
    void toAssignmentsOpen_runsPrepareBeforeSwitch() {
        List<String> calls = new ArrayList<>();

        NavigationRouter router = new NavigationRouter(
                () -> calls.add("dashboard"),
                () -> calls.add("courses"),
                () -> calls.add("study"),
                () -> calls.add("big-picture"),
                () -> calls.add("switch-assignments"),
                () -> calls.add("prepare-all"),
                () -> calls.add("prepare-open")
        );

        router.toAssignmentsOpen();

        assertEquals(List.of("prepare-open", "switch-assignments"), calls);
    }

    @Test
    @DisplayName("DS6-4: Centralize navigation - toAssignmentsAll prepares then switches")
    void toAssignmentsAll_runsPrepareBeforeSwitch() {
        List<String> calls = new ArrayList<>();

        NavigationRouter router = new NavigationRouter(
                () -> calls.add("dashboard"),
                () -> calls.add("courses"),
                () -> calls.add("study"),
                () -> calls.add("big-picture"),
                () -> calls.add("switch-assignments"),
                () -> calls.add("prepare-all"),
                () -> calls.add("prepare-open")
        );

        router.toAssignmentsAll();

        assertEquals(List.of("prepare-all", "switch-assignments"), calls);
    }
}

