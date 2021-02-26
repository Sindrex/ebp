package app.main;

import app.ViewControllerSelected;
import app.sidebar.SidebarController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import logging.Logger;

import javax.swing.text.View;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller class for the main view, effectively controlling the application in the logged-in state.
 *
 * @author Joergen Bele Reinfjell
 * @author Sindre Paulshus
 * @author Aleksander Johansen
 */

public class MainController implements Initializable {
    @FXML private BorderPane mainView;

    /* The controller for the sidebar needed to communicate
     * between the sidebar and this class. */
    private SidebarController sidebarController = new SidebarController();

    private final String cssPath = getClass().getResource("/app/css/AdminApp.css").toExternalForm();

    /* A map storing all currently loaded views. */
    private Map<String, ParentControllerPair> loadedViews = new ConcurrentHashMap<>();

    // The currently selected parent, set on the last changeView() call.
    private ParentControllerPair currentParent = null;

    /**
     * Loads a view from a given path using a passed controller, or default if null.
     * @implNote Adds the loaded view to a map in the form of a pair of the Parent and the controller instances.
     * @implNote Supports concurrent use.
     * @param path the relative view path.
     * @param controller a view controller instance.
     */
    private void loadView(String path, Object controller) {
        try {
            final URL url = getClass().getResource(path);
            System.out.println("loadView(): " + path);
            //Logger.logf("loadView(): Loading resource %s. ", url.getPath()); causes NullPointer now?

            final FXMLLoader fxmlLoader = new FXMLLoader(url);

            // Use the provided controller if non-null, or use the default one.
            if (controller != null) {
                Logger.logf("loadView(): %s: setting custom controller", url.getPath());
                fxmlLoader.setController(controller);
            } else {
                Logger.logf("loadView(): %s: using default controller", url.getPath());
                //controller = fxmlLoader.getController();
            }

            // Load the parent.
            Parent parent = fxmlLoader.load();

            Logger.logf("loadView(): %s: loading style sheet: %s.", path, cssPath);
            parent.getStylesheets().add(cssPath);

            // Must be after fxmlLoader.load().
            controller = fxmlLoader.getController();

            loadedViews.put(path, new ParentControllerPair(parent, controller));
            Logger.logf("loadView(): %s: added to loaded views", path);
        } catch (IOException e) {
            Logger.errf("loadView(): Failed to load resource %s.", path);
            e.printStackTrace();
        }
    }

    /**
     * Gets or loads a view implemented at the given path. The view is loaded
     * if it is not currently in the Map of loaded views.
     * @param path the view
     * @param controller the view controller instance.
     * @return the Parent instance of the view.
     */
    private ParentControllerPair getOrLoadView(String path, Object controller) {
        if (!loadedViews.containsKey(path)) {
            loadView(path, controller);
        }
        return loadedViews.get(path);
    }

    /**
     * Changes view to the view implemented at the given path.
     * @param path the relative path of the view.
     */
    private void changeView(String path) {
        changeView(path, null);
    }

    /**
     * Changes view to the view implemented at the given path using the passed
     * object as controller.
     *
     * @implNote Sends onViewSelected() and onViewUnselected() to the new
     *      and old selected views respectively, if they implement the
     *      ViewControllerSelected interface.
     * @param path the relative path of the view.
     * @param controller the view controller instance.
     */
    private synchronized void changeView(String path, Object controller) {
        if (!loadedViews.containsKey(path)) {
            loadView(path, controller);
        }
        if (!loadedViews.containsKey(path)) {
            Logger.log("Path: " + path + " does not exist");
            return;
        }

        // Load and set pair.
        ParentControllerPair pair = getOrLoadView(path, controller);
        if (pair != null) {
            mainView.setCenter(pair.getParent());

            // Enable the sidebar only if the view has id set to "sidebar-view".
            setSidebarVisible("sidebar-view".equals(pair.getParent().getId()));

            // Call onViewSelected on the new pair if it implements the ViewControllerSelected interface.
            Object newParentObj = pair.getController();
            if (newParentObj != null && newParentObj instanceof ViewControllerSelected) {
                ViewControllerSelected v = (ViewControllerSelected) newParentObj;
                Logger.logf("Calling onViewSelected on %s", path);
                v.onViewSelected();
            }

            // Call onViewUnselected on the "old" (set as currentPair) pair.
            if (currentParent != null) {
                Object oldParentObj = currentParent.getController();
                if (oldParentObj != null && oldParentObj instanceof ViewControllerSelected) {
                    ViewControllerSelected v = (ViewControllerSelected) oldParentObj;
                    Logger.logf("Calling onViewUnselected on currentlyselected parent!");
                    v.onViewUnselected();
                }
            }
        } else {
            mainView.setCenter(null);
        }

        currentParent = loadedViews.get(path);
    }

    /**
     *
     * @param visible boolean specifying if the sidebar should be visible.
     */
    private void setSidebarVisible(boolean visible) {
        if (visible) {
            if (loadedViews.containsKey("/app/sidebar/SidebarView.fxml")) {
                mainView.setLeft(loadedViews.get("/app/sidebar/SidebarView.fxml").getParent());
                return;
            }
            ParentControllerPair pair = getOrLoadView("/app/sidebar/SidebarView.fxml", sidebarController);
            if (pair != null) {
                pair.getParent().getStylesheets().add(cssPath);
                mainView.setLeft(pair.getParent());
            }
        } else {
            mainView.setLeft(null);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        /*
         * Concurrently load all views.
         */
        List<String> views = Arrays.asList(
                "/app/bikes/BikesView.fxml",
                "/app/dockingstation/DockingStationView.fxml"
                //"/app/deleted/DeletedView.fxml",
                //"/app/statistics/StatisticsView.fxml"
        );

        List<Thread> threadList = new ArrayList<>();
        views.forEach(v -> {
            Thread t = new Thread(() -> {
                loadView(v, null);
            });
            t.start();
        });

        for (Thread t : threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        changeView("/app/bikes/BikesView.fxml");

        sidebarController.setControllerCommunicator(this::changeView);
        setSidebarVisible(true);
    }
}
