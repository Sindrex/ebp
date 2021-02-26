package app;

/**
 * Communication interface used by the SidebarController for
 * changing the current view by communicating with the MainController.
 *
 * @author Aleksander Johansen
 */
public interface ControllerCommunication {
    /**
     * Change to another parent.
     * @param path the path of the parent.
     */
    void changeParent(String path);
}
