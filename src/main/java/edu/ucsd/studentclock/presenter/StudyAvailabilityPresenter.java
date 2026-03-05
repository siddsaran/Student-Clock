package edu.ucsd.studentclock.presenter;

import java.time.DayOfWeek;

import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.model.StudyAvailability;
import edu.ucsd.studentclock.view.StudyAvailabilityView;

/**
 * Presenter for the Study Availability screen.
 */
public class StudyAvailabilityPresenter
        extends AbstractPresenter<StudyAvailabilityView> implements IStudyAvailabilityScreenPresenter{

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

        for (DayOfWeek d : DayOfWeek.values()) {
            view.setOnDayToggled(d, () -> model.setAvailable(d, view.isDaySelected(d)));
        }

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

        for (DayOfWeek d : DayOfWeek.values()) {
            view.setDaySelected(d, availability.isAvailable(d));
        }

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

        // Update in-memory first (so we can validate before persisting)
        StudyAvailability availability = model.getStudyAvailability();
        availability.setTotalWeeklyHours(hours);

        for (DayOfWeek d : DayOfWeek.values()) {
            boolean selected = view.isDaySelected(d);
            availability.setAvailable(d, selected);
        }

        String validationError = availability.validate();
        if (validationError != null) {
            view.showError(validationError);
            return;
        }

        model.saveStudyAvailability();

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
