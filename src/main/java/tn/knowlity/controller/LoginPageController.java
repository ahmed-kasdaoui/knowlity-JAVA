package tn.knowlity.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.oauth2.model.Userinfo;
import javafx.animation.TranslateTransition;
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
import tn.knowlity.service.userService;
import tn.knowlity.tools.GoogleOAuthUtil;
import tn.knowlity.tools.UserSessionManager;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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

    @FXML
    public void signin(ActionEvent event) {
        String emailInput = email.getText();
        String passwordInput = password.getText();

        try {
            User user = userService.Authentification(emailInput, passwordInput);
            if (user != null) {
                UserSessionManager.getInstance().setCurrentUser(user);
                LOGGER.info("User authenticated via email/password: " + emailInput);

                String file = "/ListeCours.fxml";
                //ListeCoursEtudiant
                String[] userRoles = user.getRoles();
                List<String> roles = Arrays.asList(userRoles);
                if (roles.contains("Enseignant") || roles.contains("Etudiant")) {
                    file = "/ListeCours.fxml";
                }

                Stage currentStage = (Stage) email.getScene().getWindow();
                navbarController.changeScene(file, currentStage);
            } else {
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

    @FXML
    public void signInWithGoogle(ActionEvent event) {
        googleSignInButton.setDisable(true);
        try {
            String authUrl = GoogleOAuthUtil.getAuthorizationUrl();
            Desktop.getDesktop().browse(new URI(authUrl));
            LOGGER.info("Opened Google authorization URL for login");

            new Thread(() -> {
                try {
                    Credential credential = GoogleOAuthUtil.authorize();
                    Userinfo userInfo = GoogleOAuthUtil.getUserInfo(credential);
                    User user = userService.authenticateWithGoogle(userInfo.getId());

                    if (user == null) {
                        User newUser = new User();
                        newUser.setGoogle_id(userInfo.getId());
                        newUser.setEmail(userInfo.getEmail());
                        newUser.setNom(userInfo.getFamilyName() != null ? userInfo.getFamilyName() : "");
                        newUser.setPrenom(userInfo.getGivenName() != null ? userInfo.getGivenName() : "");
                        newUser.setImage(userInfo.getPicture() != null ? userInfo.getPicture() : "");
                        UserSessionManager.getInstance().setCurrentUser(newUser);

                        LOGGER.info("New Google user, redirecting to role selection: " + userInfo.getEmail());
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Stage currentStage = (Stage) email.getScene().getWindow();
                                navbarController.changeScene("/user/choice.fxml", currentStage);
                            } catch (IOException e) {
                                LOGGER.log(Level.SEVERE, "Error redirecting to role selection", e);
                                showError("Erreur lors de la redirection: " + e.getMessage());
                            }
                        });
                    } else {
                        UserSessionManager.getInstance().setCurrentUser(user);
                        LOGGER.info("Existing Google user authenticated: " + userInfo.getEmail());

                        String file = "/User/backUser.fxml";
                        String[] roles = user.getRoles();
                        List<String> roleList = Arrays.asList(roles);
                        if (roleList.contains("Enseignant") || roleList.contains("Etudiant")) {
                            file = "/ListeCours.fxml";
                        }

                        String finalFile = file;
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Stage currentStage = (Stage) email.getScene().getWindow();
                                navbarController.changeScene(finalFile, currentStage);
                            } catch (IOException e) {
                                LOGGER.log(Level.SEVERE, "Error redirecting to dashboard", e);
                                showError("Erreur lors de la redirection: " + e.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "IO error during Google authentication", e);
                    javafx.application.Platform.runLater(() -> showError("Erreur réseau lors de l'authentification Google. Veuillez réessayer."));
                } catch (GeneralSecurityException e) {
                    LOGGER.log(Level.SEVERE, "Security error during Google authentication", e);
                    javafx.application.Platform.runLater(() -> showError("Erreur de sécurité lors de l'authentification Google."));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error during Google authentication", e);
                    javafx.application.Platform.runLater(() -> showError("Erreur inattendue lors de l'authentification Google."));
                } finally {
                    javafx.application.Platform.runLater(() -> googleSignInButton.setDisable(false));
                }
            }).start();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error initiating Google authentication", e);
            showError("Impossible de lancer l'authentification Google. Veuillez réessayer.");
            googleSignInButton.setDisable(false);
        } catch (GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, "Security error initiating Google authentication", e);
            showError("Erreur de sécurité lors de l'authentification Google.");
            googleSignInButton.setDisable(false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error initiating Google authentication", e);
            showError("Erreur inattendue. Veuillez réessayer.");
            googleSignInButton.setDisable(false);
        }
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