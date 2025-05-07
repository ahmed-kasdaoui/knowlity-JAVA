package tn.knowlity.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.oauth2.model.Userinfo;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.knowlity.entity.User;
import tn.knowlity.service.GoogleAuthExample;
import tn.knowlity.service.userService;
import tn.knowlity.tools.GoogleOAuthUtil;
import tn.knowlity.tools.UserSessionManager;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LoginPageController {
    private static final Logger LOGGER = Logger.getLogger(LoginPageController.class.getName());
    private final userService userService = new userService();

    @FXML private Circle circle1, circle2, circle3, circle4, circle5, circle6, circle7, circle8, circle9, circle10, circle11, circle12;
    @FXML private Label errorMessageLabel;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private Button googleSignInButton;

    @FXML
    public void initialize() {
        animateBackgroundCircles();
    }

    private void animateBackgroundCircles() {
        createCircleTransition(circle1, 0, 0, 100, -50, 3);
        createCircleTransition(circle2, 0, 0, -100, 50, 4);
        createCircleTransition(circle3, 200, 0, 300, 100, 3);
        createCircleTransition(circle4, 300, 0, 400, -75, 4);
        createCircleTransition(circle5, -100, 0, 0, 50, 3);
        createCircleTransition(circle6, 150, 0, 200, -50, 4);
        createCircleTransition(circle7, 600, 0, 700, -50, 3);
        createCircleTransition(circle8, 700, 0, 800, 100, 4);
        createCircleTransition(circle9, 800, 0, 900, -75, 3);
        createCircleTransition(circle10, 900, 0, 950, 50, 4);
        createCircleTransition(circle11, 1000, 0, 1100, -50, 3);
        createCircleTransition(circle12, 950, 0, 1050, 75, 4);
    }

    private void createCircleTransition(Circle circle, double fromX, double fromY, double toX, double toY, double seconds) {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(seconds), circle);
        transition.setFromX(fromX);
        transition.setFromY(fromY);
        transition.setToX(toX);
        transition.setToY(toY);
        transition.setAutoReverse(true);
        transition.setCycleCount(TranslateTransition.INDEFINITE);
        transition.play();
    }

    public void pageChoice() throws IOException {
        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/user/choice.fxml", currentStage);
    }

    public void pageinscriptionEtudiant() throws IOException {
        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/user/inscriptionEtudiant.fxml", currentStage);
    }


    private Map<String, Integer> failedAttempts = new HashMap<>();

    int compteur = 0;

    @FXML
    public void signin(ActionEvent event) {
        String emailInput = email.getText();
        String passwordInput = password.getText();

        try {
            User user = userService.Authentification(emailInput, passwordInput);
            User user1 = userService.recherparemail(emailInput);
            if(user1!=null){
                if(user1.getBanned()==1){
                    showError(" Utilisateur banni.");
                    return;
                }
            }
            if (user != null) {
                UserSessionManager.getInstance().setCurrentUser(user);
                LOGGER.info("User authenticated via email/password: " + emailInput);
                String file = "/User/backUser.fxml";
                String[] userRoles = user.getRoles();
                List<String> roles = Arrays.asList(userRoles);
                if (roles.contains("Enseignant") || roles.contains("Etudiant")) {
                    if (roles.contains("Enseignant")) {file="/ListeCours.fxml";}
                    else if (roles.contains("Etudiant"))file = "/ListeCoursEtudiant.fxml";
                    else file="/User/backUser.fxml";
                }

                Stage currentStage = (Stage) email.getScene().getWindow();
                navbarController.changeScene(file, currentStage);
            } else {
                showError("Email ou pass Incorrecte");
                failedAttempts.put(emailInput, failedAttempts.getOrDefault(emailInput, 0) + 1);

                if (failedAttempts.get(emailInput) >= 3) {

                    User userban= userService.recherparemail(emailInput);
                    userService.bannneruser(userban);
                    showError("Trop de tentatives incorrectes. Utilisateur banni.");
                    return;
                }
                LOGGER.warning("Authentication failed for email: " + emailInput);
                showError("Email ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during authentication", e);
            showError("Erreur de connexion à la base de données. Veuillez réessayer.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading next scene", e);
            showError("Erreur lors du chargement de la page suivante.");
        }
    }

    public void authgoolee() throws IOException {
        GoogleAuthExample googleAuthExample = new GoogleAuthExample();
        Stage stage = new Stage();

        googleAuthExample.test(stage, success -> {
            System.out.println(success); // true ou false après authentification

            if (success) {
                Platform.runLater(() -> {
                    try {
                        Stage currentStage = (Stage) circle1.getScene().getWindow();
                        navbarController.changeScene("/ListeCoursEtudiant.fxml", currentStage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("Authentication failed, stay on page.");
            }
        });
    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setOpacity(1);
    }

    public void pageforget() throws IOException {
        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/User/mail.fxml", currentStage);
    }
}