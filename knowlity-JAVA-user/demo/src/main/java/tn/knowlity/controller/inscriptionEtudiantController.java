package tn.knowlity.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;
import tn.knowlity.entity.User;
import tn.knowlity.service.userService;
import tn.knowlity.tools.GoogleOAuthUtil;
import tn.knowlity.tools.UserSessionManager;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.oauth2.model.Userinfo;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

public class inscriptionEtudiantController {
    private static final Logger LOGGER = Logger.getLogger(inscriptionEtudiantController.class.getName());
    private final userService userService = new userService();

    @FXML private Circle circle1, circle2, circle3, circle4, circle5, circle6, circle7, circle8, circle9, circle10, circle11, circle12;
    @FXML private ComboBox<String> localisation1;
    @FXML private TextField email, nom, prenom, numTelephone, dateNaissance, localisation;
    @FXML private PasswordField password, confirmPassword;
    @FXML private Label imagePathLabel, errornom, imageError, prenomError, numTelephoneError, dateNaissanceError, localisationError, passwordError, confirmPasswordError, emailError;
    @FXML private Button googleSignUpButton;

    @FXML
    public void initialize() {
        animateBackgroundCircles();
        localisation1.getItems().addAll("Homme", "Femme", "Autre");
        User currentUser = UserSessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getGoogle_id() != null) {
            populateFormWithGoogleUser(currentUser);
        }
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

    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une Image de Profil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imagePathLabel.setText(selectedFile.getAbsolutePath());
            LOGGER.info("Image selected: " + selectedFile.getAbsolutePath());
        }
    }

    @FXML
    public void signUpWithGoogle() {
        googleSignUpButton.setDisable(true);
        try {
            String authUrl = GoogleOAuthUtil.getAuthorizationUrl();
            Desktop.getDesktop().browse(new URI(authUrl));
            LOGGER.info("Opened Google authorization URL");

            new Thread(() -> {
                try {
                    Credential credential = GoogleOAuthUtil.authorize();
                    Userinfo userInfo = GoogleOAuthUtil.getUserInfo(credential);

                    User newUser = new User();
                    newUser.setGoogle_id(userInfo.getId());
                    newUser.setEmail(userInfo.getEmail());
                    newUser.setNom(userInfo.getFamilyName() != null ? userInfo.getFamilyName() : "");
                    newUser.setPrenom(userInfo.getGivenName() != null ? userInfo.getGivenName() : "");
                    newUser.setImage(userInfo.getPicture() != null ? userInfo.getPicture() : "");
                    newUser.setRoles(new String[]{"Etudiant"});

                    UserSessionManager.getInstance().setCurrentUser(newUser);
                    LOGGER.info("Google user authenticated: " + userInfo.getEmail());

                    javafx.application.Platform.runLater(() -> {
                        populateFormWithGoogleUser(newUser);
                        showError("Veuillez compléter les champs restants pour finaliser l'inscription.", emailError);
                    });
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "IO error during Google authentication", e);
                    javafx.application.Platform.runLater(() -> showError("Erreur réseau lors de l'authentification Google. Veuillez réessayer.", emailError));
                } catch (GeneralSecurityException e) {
                    LOGGER.log(Level.SEVERE, "Security error during Google authentication", e);
                    javafx.application.Platform.runLater(() -> showError("Erreur de sécurité lors de l'authentification Google.", emailError));
                } finally {
                    javafx.application.Platform.runLater(() -> googleSignUpButton.setDisable(false));
                }
            }).start();
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.log(Level.SEVERE, "Error initiating Google authentication", e);
            showError("Impossible de lancer l'authentification Google. Veuillez réessayer.", emailError);
            googleSignUpButton.setDisable(false);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during Google authentication", e);
            showError("Une erreur inattendue s'est produite. Veuillez réessayer.", emailError);
            googleSignUpButton.setDisable(false);
        }
    }

    private void populateFormWithGoogleUser(User user) {
        email.setText(user.getEmail());
        email.setDisable(true);
        nom.setText(user.getNom());
        prenom.setText(user.getPrenom());
        imagePathLabel.setText(user.getImage() != null && !user.getImage().isEmpty() ? user.getImage() : "Aucune image sélectionnée");
        password.setDisable(true);
        confirmPassword.setDisable(true);
        LOGGER.info("Form populated with Google user data: " + user.getEmail());
    }

    @FXML
    public void afficher() throws ParseException, SQLException, IOException {
        int compteur = 0;
        String genre = localisation1.getValue();
        String nomInput = nom.getText();
        String prenomInput = prenom.getText();
        Date dateNaissanceInput = null;
        String localisationInput = localisation.getText();
        String emailInput = email.getText();
        String passwordInput = password.getText();
        String confirmPasswordInput = confirmPassword.getText();
        String imagePathInput = imagePathLabel.getText();

        if (nomInput == null || nomInput.isEmpty() || nomInput.length() < 3) {
            compteur++;
            showError("Nom invalide (minimum 3 caractères)", errornom);
            LOGGER.warning("Invalid nom: " + nomInput);
        } else {
            errornom.setOpacity(0);
        }

        if (prenomInput == null || prenomInput.isEmpty() || prenomInput.length() < 3) {
            compteur++;
            showError("Prénom invalide (minimum 3 caractères)", prenomError);
            LOGGER.warning("Invalid prenom: " + prenomInput);
        } else {
            prenomError.setOpacity(0);
        }

        if (genre == null) {
            compteur++;
            showError("Veuillez sélectionner un genre", imageError);
            LOGGER.warning("Genre not selected");
        } else {
            imageError.setOpacity(0);
        }

        String phoneInput = numTelephone.getText();
        if (!phoneInput.matches("[0-9]{8}")) {
            compteur++;
            showError("Numéro invalide (8 chiffres)", numTelephoneError);
            LOGGER.warning("Invalid phone number: " + phoneInput);
        } else {
            numTelephoneError.setOpacity(0);
        }

        if (emailInput.isEmpty() || !emailInput.matches("^[\\w.-]+@(gmail\\.com|esprit\\.tn)$")) {
            compteur++;
            showError("Email invalide (doit être @gmail.com ou @esprit.tn)", emailError);
            LOGGER.warning("Invalid email: " + emailInput);
        } else {
            emailError.setOpacity(0);
        }

        User currentUser = UserSessionManager.getInstance().getCurrentUser();
        boolean isGoogleSignIn = currentUser != null && currentUser.getGoogle_id() != null;

        if (!isGoogleSignIn) {
            if (passwordInput == null || passwordInput.isEmpty() || passwordInput.length() < 3) {
                compteur++;
                showError("Mot de passe invalide (minimum 3 caractères)", passwordError);
                LOGGER.warning("Invalid password");
            } else {
                passwordError.setOpacity(0);
            }

            if (confirmPasswordInput == null || confirmPasswordInput.isEmpty() || confirmPasswordInput.length() < 3) {
                compteur++;
                showError("Confirmation invalide", confirmPasswordError);
                LOGGER.warning("Invalid confirm password");
            } else {
                confirmPasswordError.setOpacity(0);
            }

            if (!confirmPasswordInput.equals(passwordInput)) {
                compteur++;
                showError("Les mots de passe ne correspondent pas", confirmPasswordError);
                showError("Les mots de passe ne correspondent pas", passwordError);
                LOGGER.warning("Passwords do not match");
            } else {
                passwordError.setOpacity(0);
                confirmPasswordError.setOpacity(0);
            }
        }

        if (localisationInput.length() < 3 || localisationInput.isEmpty()) {
            compteur++;
            showError("Localisation invalide (minimum 3 caractères)", localisationError);
            LOGGER.warning("Invalid localisation: " + localisationInput);
        } else {
            localisationError.setOpacity(0);
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateNaissanceInput = dateFormat.parse(dateNaissance.getText());
            dateNaissanceError.setOpacity(0);
        } catch (ParseException e) {
            compteur++;
            showError("Date invalide (format YYYY-MM-DD)", dateNaissanceError);
            LOGGER.warning("Invalid date format: " + dateNaissance.getText());
        }

        Path sourcePath = Paths.get(imagePathInput);
        if (!Files.exists(sourcePath) || imagePathInput.equals("Aucune image sélectionnée")) {
            compteur++;
            showError("Veuillez sélectionner une image valide", imageError);
            LOGGER.warning("Invalid image path: " + imagePathInput);
        } else {
            imageError.setOpacity(0);
        }

        if (compteur == 0) {
            Path destinationFolder = Paths.get("src/main/resources/images");
            Files.createDirectories(destinationFolder);
            String fileName = sourcePath.getFileName().toString();
            Path destinationPath = destinationFolder.resolve(fileName);
            int counter = 1;
            while (Files.exists(destinationPath)) {
                String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
                destinationPath = destinationFolder.resolve(fileNameWithoutExtension + "_" + counter + fileExtension);
                counter++;
            }
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Image copied to: " + destinationPath);

            int numTelephoneInput = Integer.parseInt(phoneInput);
            String hashedPassword = isGoogleSignIn ? "" : BCrypt.hashpw(passwordInput, BCrypt.gensalt());
            String[] roles = isGoogleSignIn ? currentUser.getRoles() : new String[]{"Etudiant"};
            User user = new User(
                    prenomInput, emailInput, dateNaissanceInput, numTelephoneInput, hashedPassword,
                    destinationPath.toString(), genre, localisationInput, hashedPassword, 0, "0", roles, nomInput
            );
            if (isGoogleSignIn) {
                user.setGoogle_id(currentUser.getGoogle_id());
            }
            userService.ajouterEtudiant(user);
            UserSessionManager.getInstance().setCurrentUser(user);
            LOGGER.info("Student registered successfully: " + emailInput);
            pagelogin();
        } else {
            LOGGER.warning("Registration failed due to " + compteur + " validation errors");
        }
    }

    public void pageinscriptionEtudiant() throws IOException {
        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/user/inscriptionEtudiant.fxml", currentStage);
    }

    private void showError(String message, Label label) {
        label.setText(message);
        label.setOpacity(1);
    }

    public void pagelogin() throws IOException {
        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/user/loginPage.fxml", currentStage);
    }
}