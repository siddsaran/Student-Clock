package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Assignment;

/**
 * Contract for the dashboard screen presenter as used by DashboardView.
 */
public interface IDashboardScreenPresenter {

    void openAssignment(Assignment assignment);
}
