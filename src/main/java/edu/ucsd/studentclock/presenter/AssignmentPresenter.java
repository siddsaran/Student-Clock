package edu.ucsd.studentclock.presenter;

import java.time.LocalDateTime;

import edu.ucsd.studentclock.model.Assignment;
import edu.ucsd.studentclock.model.Model;
import edu.ucsd.studentclock.repository.AssignmentRepository;
import edu.ucsd.studentclock.view.AssignmentView;

/**
 * Presenter for the Assignment screen.
 * Handles user actions and coordinates between AssignmentView and AssignmentRepository.
 */
public class AssignmentPresenter extends AbstractPresenter<AssignmentView> {

    private final AssignmentRepository repository;
    private Runnable onBack;
    /**
     * Creates an AssignmentPresenter.
     *
     * @param model shared application model
     * @param view assignment view
     * @param repository assignment repository
     */
    public AssignmentPresenter(Model model,
                               AssignmentView view,
                               AssignmentRepository repository) {
        super(model, view);
        this.repository = repository;
        view.setPresenter(this);
        updateView();
    }

    /**
     * Returns the title displayed in the window.
     *
     * @return view title
     */
    @Override
    public String getViewTitle() {
        return "Assignments";
    }

    /**
     * Refreshes the view.
     * (Later will reload assignments from database.)
     */
    @Override
    public void updateView() {
        view.showAssignments(repository.getAssignmentsForCourse("CSE 110"));
    }

    /**
     * Creates a new assignment and stores it in the database.
     *
     * @param name assignment name
     * @param course course id
     * @param start start date/time
     * @param deadline deadline date/time
     * @param lateDays allowed late days
     */
    public void createAssignment(String name,
                                 String course,
                                 LocalDateTime start,
                                 LocalDateTime deadline,
                                 int lateDays) {

        Assignment assignment = new Assignment(
                name,
                course,
                start,
                deadline,
                lateDays,
                0
        );
        repository.addAssignment(assignment);
        updateView();
    }

    /**
     * Registers callback for back navigation.
     *
     * @param onBack runnable executed when back is pressed
     */
    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    /**
     * Invoked by view when back button is pressed.
     */
    public void back() {
        if (onBack != null) {
            onBack.run();
        }
    }


}
