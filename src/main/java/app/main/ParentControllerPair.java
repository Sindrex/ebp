package app.main;

import javafx.scene.Parent;

/**
 * Helper class for a pair of a parent and a controller.
 *
 * @author Joergen Bele Reinfjell
 */
class ParentControllerPair {
    private Parent parent;
    private Object controller;

    public ParentControllerPair(Parent parent, Object controller) {
        this.parent = parent;
        this.controller = controller;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}
