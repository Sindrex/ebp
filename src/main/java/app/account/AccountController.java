package app.account;

import app.LoginDetails;
import db.dao.AdminDao;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import managers.AdminManager;
import objects.Admin;

import static logging.SendMail.newAdmin;

/**
 * Controller class for the Account view.
 * @author Sindre Paulshus
 * @author Aleksander Johansen
 */
public class AccountController {
    @FXML private Button createAccountButton;
    @FXML private Button backButton;
    @FXML private Button changePasswordButton;
    @FXML private Button createAccount;

    @FXML private HBox accInfo;

    @FXML private GridPane createAccountPanel;

    @FXML private Label usernameLabel;
    @FXML private Label feedbackMessage;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField newPasswordField2;
    @FXML private TextField createAccountName;
    @FXML private PasswordField createAccountPass;
    @FXML private PasswordField createAccountPass2;
    @FXML private Label feedbackMessageAccount;

    private final AdminManager am = LoginDetails.adminManager;
    private final Admin myAdmin = LoginDetails.currentAdmin;

    public void initialize(){
        createAccountButton.setOnAction(e -> accountPanel());
        backButton.setOnAction(e -> infoPanel());
        changePasswordButton.setOnAction(e -> changePassword());
        createAccount.setOnAction(e -> createAccount());

        oldPasswordField.setOnAction(e -> changePassword());
        newPasswordField.setOnAction(e -> changePassword());
        newPasswordField2.setOnAction(e -> changePassword());

        oldPasswordField.textProperty().addListener(e -> clearFeedbackMessage());
        oldPasswordField.setOnMouseClicked(e -> clearFeedbackMessage());
        newPasswordField.textProperty().addListener(e -> clearFeedbackMessage());
        newPasswordField.setOnMouseClicked(e -> clearFeedbackMessage());
        newPasswordField2.textProperty().addListener(e -> clearFeedbackMessage());
        newPasswordField2.setOnMouseClicked(e -> clearFeedbackMessage());

        createAccountPass.textProperty().addListener(e -> clearFeedbackMessage());
        createAccountPass.setOnMouseClicked(e -> clearFeedbackMessage());
        createAccountPass2.textProperty().addListener(e -> clearFeedbackMessage());
        createAccountPass2.setOnMouseClicked(e -> clearFeedbackMessage());

        createAccountPass.setOnAction(e -> createAccount());
        createAccountPass2.setOnAction(e -> createAccount());

        usernameLabel.setText(myAdmin.getUsername());
    }

    /**
     * Method for hiding the account info panel and showing the create new account panel.
     */
    public void accountPanel(){
        createAccountButton.setVisible(false);
        accInfo.setVisible(false);
        createAccountPanel.setVisible(true);
        backButton.setVisible(true);
        feedbackMessageAccount.setVisible(false);
    }

    /**
     * Method for hiding the create new account panel and showing the info panel.
     */
    public void infoPanel(){
        createAccountButton.setVisible(true);
        accInfo.setVisible(true);
        createAccountPanel.setVisible(false);
        backButton.setVisible(false);
    }

    private void changePassword(){
        if(checkPasswordFields() && passwordMatch()){
            if(am.newPassword(oldPasswordField.getText(), newPasswordField.getText())){
                setFeedbackmessage("Password changed!", false);
            }
            else{
                setFeedbackmessage("Something went wrong!", true);
            }
        }
    }

    private boolean checkPasswordFields(){
        if(oldPasswordField.getText() != null && newPasswordField.getText() != null && newPasswordField2.getText() != null && !oldPasswordField.getText().equals("") &&
                !newPasswordField.getText().equals("") && !newPasswordField2.getText().equals("")){
            return true;
        }
        setFeedbackmessage("Fill in all the fields!", true);
        return false;
    }

    private boolean passwordMatch(){
        if(AdminDao.getInstance().checkPassword(myAdmin.getUsername(), oldPasswordField.getText()) && newPasswordField.getText().equals(newPasswordField2.getText())){
            setFeedbackmessage("Password has been changed", false);
            return true;
        }
        else if(!newPasswordField.getText().equals(newPasswordField2.getText())){
            setFeedbackmessage("The new passwords does not match", true);
            return false;
        }
        else{
            setFeedbackmessage("The previous password doesn't match", true);
            return false;
        }
    }

    private void createAccount(){
        if(checkAccountFields() && passwordMatchAccount()){
            if(am.addAdmin(new Admin(createAccountName.getText(), createAccountPass.getText()))){
                setFeedbackmessageAccount("Account Created!", false);
                newAdmin(new Admin(createAccountName.getText(), createAccountPass.getText()));
            }
            else{
                setFeedbackmessageAccount("Something went wrong!", true);
            }
        }
    }

    private boolean checkAccountFields(){
        if(createAccountPass.getText() != null && createAccountPass2.getText() != null && !createAccountPass.getText().trim().equals("") && !createAccountPass2.getText().trim().equals("") &&
                !createAccountName.getText().trim().equals("") && createAccountName != null){
            return true;
        }
        setFeedbackmessageAccount("Fill in all the fields!", true);
        return false;
    }

    private boolean passwordMatchAccount(){
        if(createAccountPass.getText().equals(createAccountPass2.getText())){
            return true;
        }
        else if(oldPasswordField.getText().equals(myAdmin.getPassword())){
            setFeedbackmessageAccount("The passwords doesnt match", true);
        }
        return false;
    }

    private void setFeedbackmessage(String message, boolean red){
        if(red){
            feedbackMessage.setStyle("-fx-text-fill: red");
        }
        else{
            feedbackMessage.setStyle("-fx-text-fill: green");
        }
        feedbackMessage.setText(message);
        feedbackMessage.setVisible(true);
    }

    private void setFeedbackmessageAccount(String message, boolean red){
        if(red){
            feedbackMessageAccount.setStyle("-fx-text-fill: red");
        }
        else{
            feedbackMessageAccount.setStyle("-fx-text-fill: green");
        }
        feedbackMessageAccount.setText(message);
        feedbackMessageAccount.setVisible(true);
    }

    private void clearFeedbackMessage(){
        feedbackMessage.setVisible(false);
    }
}