package app;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;

/**
 * Displays a alert box for success and failure messages.
 * @author Aleksander Johansen
 */
public class AlertBox {

    public static void errorMessage(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void successMessage(String title, String header, String message){
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);

        javafx.scene.image.Image image = new Image(AlertBox.class.getResource("images/CheckMark.png").toString());
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitHeight(dimension.getWidth() * 0.0520);
        imageView.setFitWidth(dimension.getWidth() * 0.0520);

        alert.setGraphic(imageView);

        alert.showAndWait();
    }

    public static void successMessage(String header, String message){
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Success!");
        alert.setHeaderText(header);
        alert.setContentText(message);

        javafx.scene.image.Image image = new Image(AlertBox.class.getResource("images/CheckMark.png").toString());
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitHeight(dimension.getWidth() * 0.0520);
        imageView.setFitWidth(dimension.getWidth() * 0.0520);

        alert.setGraphic(imageView);

        alert.showAndWait();
    }
}