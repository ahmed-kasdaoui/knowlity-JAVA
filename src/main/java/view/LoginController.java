package view;

import Entities.User;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Label errorLabel;
    
    private Stage stage;
    private String selectedRole;
    private UserService userService;
    private LoginCallback callback;
    
    public LoginController() {
        userService = new UserService();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setSelectedRole(String role) {
        this.selectedRole = role;
    }
    
    public void setCallback(LoginCallback callback) {
        this.callback = callback;
    }
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }
        
        try {
            User user = userService.authenticate(email, password);
            if (user != null) {
                // Vérifier si le compte est actif
                if (user.isBanned()) {
                    showError("Votre compte a été suspendu");
                    return;
                }
                if (user.isDeleted()) {
                    showError("Ce compte n'existe plus");
                    return;
                }

                // Vérifier si le rôle correspond
                boolean isValidRole = (selectedRole.equals("admin") && user.isAdmin()) ||
                                    (selectedRole.equals("user") && !user.isAdmin());
                
                if (isValidRole) {
                    if (callback != null) {
                        callback.onLoginSuccess(user);
                    }
                    stage.close();
                } else {
                    showError("Vous n'avez pas les droits nécessaires pour ce rôle");
                }
            } else {
                showError("Email ou mot de passe incorrect");
            }
        } catch (Exception e) {
            showError("Erreur lors de la connexion : " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
    }
    
    public interface LoginCallback {
        void onLoginSuccess(User user);

        void onLoginSuccess(tn.knowlity.entity.User user);
    }
}
