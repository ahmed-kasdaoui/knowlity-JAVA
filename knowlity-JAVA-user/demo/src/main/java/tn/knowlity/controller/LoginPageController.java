package tn.knowlity.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.oauth2.model.Userinfo;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

public class LoginPageController {

    @FXML
    private Circle circle1;
    @FXML
    private Circle circle2;
    @FXML
    private Circle circle3;
    @FXML
    private Circle circle4;
    @FXML
    private Circle circle5;
    @FXML
    private Circle circle6;
    @FXML
    private Circle circle7;
    @FXML
    private Circle circle8;
    @FXML
    private Circle circle9;
    @FXML
    private Circle circle10;
    @FXML
    private Circle circle11;
    @FXML
    private Circle circle12;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;

    private userService userService = new userService();

    @FXML
    public void initialize() {
        animateBackgroundCircles();
    }

    private void animateBackgroundCircles() {
        // Left side circles (x from 0 to 400)
        TranslateTransition transition1 = new TranslateTransition(Duration.seconds(3), circle1);
        transition1.setFromX(0);
        transition1.setFromY(0);
        transition1.setToX(100);
        transition1.setToY(-50);
        transition1.setAutoReverse(true);
        transition1.setCycleCount(TranslateTransition.INDEFINITE);
        transition1.play();

        TranslateTransition transition2 = new TranslateTransition(Duration.seconds(4), circle2);
        transition2.setFromX(0);
        transition2.setFromY(0);
        transition2.setToX(-100);
        transition2.setToY(50);
        transition2.setAutoReverse(true);
        transition2.setCycleCount(TranslateTransition.INDEFINITE);
        transition2.play();

        TranslateTransition transition3 = new TranslateTransition(Duration.seconds(3), circle3);
        transition3.setFromX(200);
        transition3.setFromY(0);
        transition3.setToX(300);
        transition3.setToY(100);
        transition3.setAutoReverse(true);
        transition3.setCycleCount(TranslateTransition.INDEFINITE);
        transition3.play();

        TranslateTransition transition4 = new TranslateTransition(Duration.seconds(4), circle4);
        transition4.setFromX(300);
        transition4.setFromY(0);
        transition4.setToX(400);
        transition4.setToY(-75);
        transition4.setAutoReverse(true);
        transition4.setCycleCount(TranslateTransition.INDEFINITE);
        transition4.play();

        TranslateTransition transition5 = new TranslateTransition(Duration.seconds(3), circle5);
        transition5.setFromX(-100);
        transition5.setFromY(0);
        transition5.setToX(0);
        transition5.setToY(50);
        transition5.setAutoReverse(true);
        transition5.setCycleCount(TranslateTransition.INDEFINITE);
        transition5.play();

        TranslateTransition transition6 = new TranslateTransition(Duration.seconds(4), circle6);
        transition6.setFromX(150);
        transition6.setFromY(0);
        transition6.setToX(200);
        transition6.setToY(-50);
        transition6.setAutoReverse(true);
        transition6.setCycleCount(TranslateTransition.INDEFINITE);
        transition6.play();

        // Right side circles (x from 600 to 950)
        TranslateTransition transition7 = new TranslateTransition(Duration.seconds(3), circle7);
        transition7.setFromX(600);
        transition7.setFromY(0);
        transition7.setToX(700);
        transition7.setToY(-50);
        transition7.setAutoReverse(true);
        transition7.setCycleCount(TranslateTransition.INDEFINITE);
        transition7.play();

        TranslateTransition transition8 = new TranslateTransition(Duration.seconds(4), circle8);
        transition8.setFromX(700);
        transition8.setFromY(0);
        transition8.setToX(800);
        transition8.setToY(100);
        transition8.setAutoReverse(true);
        transition8.setCycleCount(TranslateTransition.INDEFINITE);
        transition8.play();

        TranslateTransition transition9 = new TranslateTransition(Duration.seconds(3), circle9);
        transition9.setFromX(800);
        transition9.setFromY(0);
        transition9.setToX(900);
        transition9.setToY(-75);
        transition9.setAutoReverse(true);
        transition9.setCycleCount(TranslateTransition.INDEFINITE);
        transition9.play();

        TranslateTransition transition10 = new TranslateTransition(Duration.seconds(4), circle10);
        transition10.setFromX(900);
        transition10.setFromY(0);
        transition10.setToX(950);
        transition10.setToY(50);
        transition10.setAutoReverse(true);
        transition10.setCycleCount(TranslateTransition.INDEFINITE);
        transition10.play();

        TranslateTransition transition11 = new TranslateTransition(Duration.seconds(3), circle11);
        transition11.setFromX(1000);
        transition11.setFromY(0);
        transition11.setToX(1100);
        transition11.setToY(-50);
        transition11.setAutoReverse(true);
        transition11.setCycleCount(TranslateTransition.INDEFINITE);
        transition11.play();

        TranslateTransition transition12 = new TranslateTransition(Duration.seconds(4), circle12);
        transition12.setFromX(950);
        transition12.setFromY(0);
        transition12.setToX(1050);
        transition12.setToY(75);
        transition12.setAutoReverse(true);
        transition12.setCycleCount(TranslateTransition.INDEFINITE);
        transition12.play();
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

                String file = "/User/backUser.fxml"; // valeur par défaut

                System.out.println("Utilisateur trouvé");
                String[] userRoles = user.getRoles(); // supposé renvoyer un tableau de rôles
                System.out.println("Rôles utilisateur : " + Arrays.toString(userRoles));

                List<String> roles = Arrays.asList(userRoles);

                if (roles.contains("Enseignant")) {
                    System.out.println("Enseignant");
                    file = "/User/Front.fxml";
                } else if (roles.contains("Etudiant")) {
                    System.out.println("Etudiant");
                    file = "/User/Front.fxml";
                }

                Stage currentStage = (Stage) email.getScene().getWindow();
                navbarController.changeScene(file, currentStage);
            } else {
                System.out.println("Utilisateur non trouvé");
                showError("Email ou mot de passe incorrect.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Impossible de se connecter à la base de données. Veuillez réessayer.");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Une erreur est survenue lors du chargement de la fenêtre suivante.");
        }
    }

    @FXML
    public void signInWithGoogle(ActionEvent event) {
        try {
            // Open the Google authorization URL in the default browser
            String authUrl = GoogleOAuthUtil.getAuthorizationUrl();
            Desktop.getDesktop().browse(new URI(authUrl));

            // Authorize and get credentials in a separate thread
            new Thread(() -> {
                try {
                    Credential credential = GoogleOAuthUtil.authorize();
                    Userinfo userInfo = GoogleOAuthUtil.getUserInfo(credential);

                    // Check if user exists by google_id
                    User user = userService.authenticateWithGoogle(userInfo.getId());

                    if (user == null) {
                        // New user, redirect to role selection
                        User newUser = new User();
                        newUser.setGoogle_id(userInfo.getId());
                        newUser.setEmail(userInfo.getEmail());
                        newUser.setNom(userInfo.getFamilyName());
                        newUser.setPrenom(userInfo.getGivenName());
                        newUser.setImage(userInfo.getPicture());
                        UserSessionManager.getInstance().setCurrentUser(newUser);

                        // Navigate to role selection page
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Stage currentStage = (Stage) email.getScene().getWindow();
                                navbarController.changeScene("/user/choice.fxml", currentStage);
                            } catch (IOException e) {
                                e.printStackTrace();
                                showError("Erreur lors de la redirection: " + e.getMessage());
                            }
                        });
                    } else {
                        // Existing user, log in
                        UserSessionManager.getInstance().setCurrentUser(user);

                        String file = "/User/Front.fxml";
                        String result1 = Arrays.toString(user.getRoles());
                        String formattedResult1 = "[" + result1.substring(1, result1.length() - 1) + "]";

                        String[] roles = new String[]{"Enseignant"};
                        String expected = "[[\"" + String.join("\", \"", roles) + "\"]]";

                        String[] roles1 = new String[]{"Etudiant"};
                        String expected1 = "[[\"" + String.join("\", \"", roles1) + "\"]]";

                        if (formattedResult1.equals(expected)) {
                            file = "/User/Front.fxml";
                        } else if (formattedResult1.equals(expected1)) {
                            file = "/User/Front.fxml";
                        } else {
                            file = "/User/BackUser.fxml";
                        }

                        String finalFile = file;
                        javafx.application.Platform.runLater(() -> {
                            try {
                                Stage currentStage = (Stage) email.getScene().getWindow();
                                navbarController.changeScene(finalFile, currentStage);
                            } catch (IOException e) {
                                e.printStackTrace();
                                showError("Erreur lors de la redirection: " + e.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> showError("Erreur lors de l'authentification Google: " + e.getMessage()));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> showError("Erreur de sécurité lors de l'authentification Google: " + e.getMessage()));
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> showError("Erreur inattendue lors de l'authentification Google: " + e.getMessage()));
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture de l'authentification Google: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            showError("Erreur de sécurité lors de l'authentification Google: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur inattendue: " + e.getMessage());
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