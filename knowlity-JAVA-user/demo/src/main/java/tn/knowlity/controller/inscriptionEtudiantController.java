package tn.knowlity.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
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
import tn.knowlity.tools.UserSessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class inscriptionEtudiantController {
    userService userService = new userService();

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
    private ComboBox<String> localisation1;
    @FXML
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private TextField nom;
    @FXML
    private TextField prenom;
    @FXML
    private TextField numTelephone;
    @FXML
    private TextField dateNaissance;
    @FXML
    private TextField localisation;
    @FXML
    private Label imagePathLabel;
    @FXML
    private Label errornom;
    @FXML
    private Label imageError;
    @FXML
    private Label prenomError;
    @FXML
    private Label numTelephoneError;
    @FXML
    private Label dateNaissanceError;
    @FXML
    private Label localisationError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmPasswordError;
    @FXML
    private Label emailError;

    @FXML
    public void initialize() {
        animateBackgroundCircles();
        // Pre-fill fields if user comes from Google Sign-In
        User currentUser = UserSessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getGoogle_id() != null) {
            email.setText(currentUser.getEmail());
            email.setDisable(true);
            nom.setText(currentUser.getNom());
            prenom.setText(currentUser.getPrenom());
            imagePathLabel.setText(currentUser.getImage() != null ? currentUser.getImage() : "Aucune image sélectionnée");
        }
    }

    private void animateBackgroundCircles() {
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

    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une Image de Profil");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imagePathLabel.setText(selectedFile.getAbsolutePath());
        }
    }

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
            showError("Nom invalide", errornom);
            System.out.println("Nom invalide");
        } else {
            errornom.setOpacity(0);
        }

        if (prenomInput == null || prenomInput.isEmpty() || prenomInput.length() < 3) {
            compteur++;
            showError("Prénom invalide", prenomError);
            System.out.println("Prénom invalide");
        } else {
            prenomError.setOpacity(0);
        }

        if (genre == null) {
            compteur++;
            showError("Genre requis", imageError);
            System.out.println("Genre invalide");
        } else {
            imageError.setOpacity(0);
        }

        String phoneInput1 = numTelephone.getText();
        if (!phoneInput1.matches("[0-9]{8}")) {
            compteur++;
            showError("Numéro invalide", numTelephoneError);
            System.out.println("Numéro invalide");
        } else {
            numTelephoneError.setOpacity(0);
        }

        if (emailInput.isEmpty() || !emailInput.matches("^[\\w.-]+@(gmail\\.com|esprit\\.tn)$")) {
            compteur++;
            showError("Email invalide", emailError);
            System.out.println("Email invalide");
        } else {
            emailError.setOpacity(0);
        }

        User currentUser = UserSessionManager.getInstance().getCurrentUser();
        boolean isGoogleSignIn = currentUser != null && currentUser.getGoogle_id() != null;

        if (!isGoogleSignIn) {
            if (passwordInput == null || passwordInput.isEmpty() || passwordInput.length() < 3) {
                compteur++;
                showError("Mot de passe invalide", passwordError);
                System.out.println("Mot de passe invalide");
            } else {
                passwordError.setOpacity(0);
            }

            if (confirmPasswordInput == null || confirmPasswordInput.isEmpty() || confirmPasswordInput.length() < 3) {
                compteur++;
                showError("Confirmation mot de passe invalide", confirmPasswordError);
                System.out.println("Confirmation mot de passe invalide");
            } else {
                confirmPasswordError.setOpacity(0);
            }

            if (!confirmPasswordInput.equals(passwordInput)) {
                compteur++;
                showError("Les mots de passe ne correspondent pas", confirmPasswordError);
                showError("Les mots de passe ne correspondent pas", passwordError);
            } else {
                passwordError.setOpacity(0);
                confirmPasswordError.setOpacity(0);
            }
        }

        if (localisationInput.length() < 3 || localisationInput.isEmpty()) {
            compteur++;
            showError("Localisation invalide", localisationError);
            System.out.println("Localisation invalide");
        } else {
            localisationError.setOpacity(0);
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateNaissanceInput = dateFormat.parse(dateNaissance.getText());
            dateNaissanceError.setOpacity(0);
        } catch (ParseException e) {
            compteur++;
            showError("Date de naissance invalide", dateNaissanceError);
            System.out.println("Format de date invalide. Utilisez YYYY-MM-DD.");
        }

        Path sourcePath = Paths.get(imagePathInput);
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
        System.out.println("Image déplacée avec succès vers : " + destinationPath);

        if (compteur == 0) {
            int numTelephoneInput = Integer.parseInt(numTelephone.getText());
            String hashedPassword = isGoogleSignIn ? "" : BCrypt.hashpw(passwordInput, BCrypt.gensalt());
            String[] roles = new String[]{"Etudiant"};
            User user = new User(
                    prenomInput, emailInput, dateNaissanceInput, numTelephoneInput, hashedPassword,
                    destinationPath.toString(), genre, localisationInput, hashedPassword, 0, "0", roles, nomInput
            );
            if (isGoogleSignIn) {
                user.setGoogle_id(currentUser.getGoogle_id());
            }
            userService.ajouterEtudiant(user);
            UserSessionManager.getInstance().setCurrentUser(user);
            this.pagelogin();
        }
    }

    public void pageinscriptionEtudiant() throws IOException {
        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/user/inscriptionEtudiant.fxml", currentStage);
    }

    private void showError(String message, Label nomlabel) {
        nomlabel.setText(message);
        nomlabel.setOpacity(1);
    }

    public void pagelogin() throws IOException {
        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/user/loginPage.fxml", currentStage);
    }
}