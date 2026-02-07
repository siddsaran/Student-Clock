package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.StudyAvailability;
import edu.ucsd.studentclock.view.StudyAvailabilityView;

/**
 * Presenter for the Study Availability screen.
 */
public class StudyAvailabilityPresenter
        extends AbstractPresenter<StudyAvailabilityView> {

    private Runnable onBack;

    /**
     * Creates a StudyAvailabilityPresenter.
     *
     * @param model shared application model
     * @param view study availability view
     */
    public StudyAvailabilityPresenter(Model model,
                                      StudyAvailabilityView view) {
        super(model, view);
        view.setPresenter(this);
        updateView();
    }

    @Override
    public String getViewTitle() {
        return "Study Availability";
    }

    @Override
    public void updateView() {
        StudyAvailability availability = model.getStudyAvailability();
        view.setWeeklyHoursText(
                String.valueOf(availability.getTotalWeeklyHours())
        );
        view.clearStatus();
    }

    // ---- Called by the view ----

    public void onSave() {
        String raw = view.getWeeklyHoursText();
        raw = (raw == null) ? "" : raw.trim();

        if (raw.isEmpty()) {
            view.showError("Please enter an integer number of hours.");
            return;
        }

        int hours;
        try {
            hours = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            view.showError("Hours must be a whole number (e.g. 8).");
            return;
        }

        if (hours < 0) {
            view.showError("Hours must be 0 or greater.");
            return;
        }

        StudyAvailability availability = model.getStudyAvailability();
        availability.setTotalWeeklyHours(hours);

        String validationError = availability.validate();
        if (validationError != null) {
            view.showError(validationError);
            return;
        }

        view.showMessage("Saved weekly study hours: " + hours);
    }

    public void onBack() {
        if (onBack != null) onBack.run();
    }

    /** Used by PresenterManager */
    public void setOnBack(Runnable runnable) {
        this.onBack = runnable;
    }
}
