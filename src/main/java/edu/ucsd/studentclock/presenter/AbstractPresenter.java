package edu.ucsd.studentclock.presenter;

import edu.ucsd.studentclock.model.Model;
import javafx.scene.Scene;
import javafx.scene.layout.Region;


public abstract class AbstractPresenter<V extends Region> {
    protected Model model;
    protected V view;
    protected Scene scene;

    public AbstractPresenter(Model model, V view) {
        if (model == null) {
            throw new NullPointerException("model must not be null");
        }
        if (view == null) {
            throw new NullPointerException("view must not be null");
        }
        
        this.model = model;
        this.view = view;
        this.scene = new Scene(view, 1200, 800);
    }

    public abstract String getViewTitle();

    public abstract void updateView();

    /**
     * Runs the given runnable if non-null. Use for optional navigation callbacks.
     */
    protected static void runIfSet(Runnable r) {
        if (r != null) r.run();
    }

    public Scene getScene() {
        return this.scene;
    }

    /** Returns the view (content region) for embedding in a shared layout. */
    public Region getView() {
        return view;
    }
}

