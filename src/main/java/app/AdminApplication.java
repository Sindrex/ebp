package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logging.Logger;

import java.awt.*;
import java.net.URL;

/**
 * Starts the applications login window.
 * @author Joergen Bele Reinfjell
 * @author Aleksander Johansen
 * @author Sindre Thomassen
 * @author Sindre Paulshus
 */
public class AdminApplication extends Application {
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    @Override
    public void start(Stage stage) throws Exception {
        long start = System.currentTimeMillis();
        stage.setTitle("Login");
        stage.setResizable(true);
        URL url = getClass().getResource("login/LoginView.fxml");

        System.err.println("Loading resource: " + url.getPath());
        Parent parent = FXMLLoader.load(url);
        stage.setScene(new Scene(parent, screenSize.getWidth() * 0.9, screenSize.getHeight() * 0.9));
        stage.show();
        long end = System.currentTimeMillis();
        System.out.println("This took: " + (double)(end - start)/1000 + " seconds");
        stage.setOnCloseRequest(e -> {
            /*
            Logger.log("AdminApplication: Closing connections!");
            Database.closeAllConnections();
            assert(!Database.hasActiveConnections());
            assert(!Database.hasAvailableConnections());
            */
            Logger.log("AdminApplication: Shutting down!");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}