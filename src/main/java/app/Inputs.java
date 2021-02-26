package app;

import db.dao.BikeDao;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import managers.DockingStationManager;
import objects.Bike;
import objects.DockingStation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Helper methods for getting various user inputs.
 *
 * @author Aleksander Johansen
 * @author Sindre Paulshus
 */
public class Inputs {

    /**
     * Pops up a window with all the active docking stations
     * @return the selected dockingID.
     */
    public static int activeDockingList(){
        List<Integer> choices = new ArrayList<>();
        DockingStationManager manager = new DockingStationManager();
        for(DockingStation d : manager.getDocks()){
            choices.add(d.getDockID());
        }


        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(null, choices);
        dialog.setTitle("Choices");
        dialog.setHeaderText("Docking list");
        dialog.setContentText("Choose an ID.");

        Optional<Integer> result = dialog.showAndWait();
        if(result.isPresent()){
            return result.get();
        }
        return -1;
    }

    public static int allBikes(){
        List<Integer> choices = new ArrayList<>();
        List<Bike> allBikes = BikeDao.getInstance().getAll();
        for(Bike b : allBikes){
            choices.add(b.getId());
        }
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(null, choices);
        dialog.setTitle("Choices");
        dialog.setHeaderText("Bike list");
        dialog.setContentText("Choose an ID.");

        Optional<Integer> result = dialog.showAndWait();
        if(result.isPresent()){
            return result.get();
        }
        return -1;
    }

    public static String inputDialog(String title, String header, String content){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        Image image = new Image(Inputs.class.getResource("rip.png").toString());
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);
        dialog.setGraphic(imageView);

        Optional<String> result = dialog.showAndWait();
        if(result.isPresent()){
            return result.get();
        }
        return null;
    }

    /**
     * Method for rounding doubles to the desired number of decimals.
     * @param value The double value to be rounded
     * @param places How many decimals that are included.
     * @return returns the initial value, but rounded up
     */
    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}