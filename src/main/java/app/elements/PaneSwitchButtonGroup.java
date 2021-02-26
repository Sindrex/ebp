package app.elements;

import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles a group of toggleable buttons
 * used to switch between panes.
 *
 * @author Joergen Bele Reinfjell
 */
public class PaneSwitchButtonGroup {
    static class PaneCallbacksPair {
        Pane pane;
        Callback<PaneSwitchButton, Void> setupCallback;
        Callback<PaneSwitchButton, Void> destructCallback;

        PaneCallbacksPair(Pane pane, Callback<PaneSwitchButton, Void> setupCallback,
                          Callback<PaneSwitchButton, Void> destructCallback) {
            this.pane = pane;
            this.setupCallback = setupCallback;
            this.destructCallback = destructCallback;
        }
    }
    private Map<PaneSwitchButton, PaneCallbacksPair> viewSwitchButtonMap = new HashMap<>();
    private ToggleGroup toggleGroup = new ToggleGroup();

    private static void handleButtonClick(MouseEvent event) {
        PaneSwitchButton button = (PaneSwitchButton) event.getSource();
        List<Toggle> toggleList = button.getToggleGroup().getToggles();
        for (Toggle toggle : toggleList) {
            if (toggle instanceof PaneSwitchButton) {
                PaneSwitchButton paneSwitchButton = (PaneSwitchButton) toggle;

                boolean selected = button == paneSwitchButton;
                paneSwitchButton.setSelected(selected);
                PaneCallbacksPair pair = (PaneCallbacksPair) paneSwitchButton.getUserData();

                if (pair.pane != null) {
                    pair.pane.setVisible(selected);
                }

                if (selected) {
                    pair.setupCallback.call(paneSwitchButton);
                } else {
                    pair.destructCallback.call(paneSwitchButton);
                }
            }
        }
    }

    public void addView(PaneSwitchButton button, Pane pane, Callback<PaneSwitchButton, Void> setupCallback,
                        Callback<PaneSwitchButton, Void> destructCallback) {
        PaneCallbacksPair pair = new PaneCallbacksPair(pane, setupCallback, destructCallback);
        viewSwitchButtonMap.put(button, pair);
        button.setUserData(pair);

        button.setToggleGroup(toggleGroup);
        button.addEventFilter(MouseEvent.MOUSE_CLICKED, PaneSwitchButtonGroup::handleButtonClick);
        //button.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
        //    setSelectedPanelByButton((PaneSwitchButton) event.getSource());
        //});

        if (pane != null) {
            pane.setVisible(false);
        }
        button.setSelected(false);
    }

    public void addView(PaneSwitchButton button, Pane pane, Callback<PaneSwitchButton, Void> setupCallback) {
    //public void addView(PaneSwitchButton button, Pane pane, PaneSwitchButtonCallback setupCallback) {
        addView(button, pane, setupCallback, event -> {return null;});
    }

    public void addView(PaneSwitchButton button, Pane pane) {
        addView(button, pane, event -> {return null;});
    }

    public void setSelectedPanelByButton(PaneSwitchButton button) {
        button.setSelected(true);
        viewSwitchButtonMap.forEach((button1, paneCallbackPair) -> {
            boolean same = button == button1;
            //button.setSelected(same);
            if (paneCallbackPair.pane != null) {
                paneCallbackPair.pane.setVisible(same);
            }
            if (same) {
                paneCallbackPair.setupCallback.call(button);
            } else {
                paneCallbackPair.destructCallback.call(button);
            }
        });
    }
}