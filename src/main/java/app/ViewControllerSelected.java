package app;

/**
 * Interface used by views wanting to be notified on view selection and unselection.
 * @author Joergen Bele Reinfjell
 */
public interface ViewControllerSelected {
    /**
     * Called by the MainController when the view is selected.
     */
    void onViewSelected();

    /**
     * Called by the MainController when the view is unselected.
     */
    void onViewUnselected();
}
