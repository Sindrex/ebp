package app.login;

import app.LoginDetails;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import managers.AdminManager;

import java.awt.*;
import java.io.IOException;

/**
 * Controller class for the login view.
 *
 * @author Sindre Paulshus
 * @author Joergen Bele Reinfjell
 * @author Odd-Erik Frantzen
 */
public class LoginController extends Application {
    @FXML
    public TextField usernameTextField;
    public PasswordField passwordField;
    public Button loginButton;
    @FXML private Label error;

    private EventHandler<javafx.event.Event> loginEventHandler = null;

    private AdminManager am = LoginDetails.adminManager;

    @FXML
    public void initialize() {
        usernameTextField.setOnAction(e -> loginButtonAction());
        passwordField.setOnAction(e -> loginButtonAction());
    }

    /**
     * Verifies the login details (not implemented) and
     * starts up the main application.
     */
    public void loginButtonAction() {
        String username = usernameTextField.getText();
        String pass = passwordField.getText();

        if(checkFields()){
            if(am.login(username, pass)){
                LoginDetails.currentAdmin = am.getAdmin();
                try {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Stage stage = new Stage();
                    Parent parent = FXMLLoader.load(getClass().getResource("/app/main/MainView.fxml"));
                    stage.setResizable(true);
                    stage.setTitle("Admin Application");
                    stage.setScene(new Scene(parent, screenSize.getWidth() * 0.9, screenSize.getHeight() * 0.9));
                    stage.show();

                    Stage thisStage = (Stage) usernameTextField.getScene().getWindow();
                    thisStage.close();

                    stage.setOnCloseRequest(e -> {
                        System.err.println("Main: Shutting down!");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                setInfoMessage("Wrong username and/or password", true);
            }
        }
        else{
            setInfoMessage("You have to enter username/password", true);
        }
    }

    private boolean checkFields(){
        if(!usernameTextField.getText().equals("") && !passwordField.getText().equals("")){
            return true;
        }
        return false;
    }

    private void setInfoMessage(String message, boolean red){
        error.setText(message);
        if(red){
            error.setStyle("-fx-text-fill: red");
        }
        else{
            error.setStyle("-fx-text-fill: green");
        }
        error.setVisible(true);
    }

    @Override
    public void start(Stage stage) throws Exception { }
}
