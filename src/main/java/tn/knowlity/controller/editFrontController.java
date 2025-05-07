package tn.knowlity.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class editFrontController {
    private tn.knowlity.service.userService userService = new tn.knowlity.service.userService();

    @FXML
    private Label imagePathLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField nomTextField;

    @FXML
    private TextField prenomTextField;

    @FXML
    private TextField emailTextField;

    @FXML
    private Label nomlabel;

    @FXML
    private Label prenomlabel;

    @FXML
    private Label emaillabel;

    @FXML
    private Label tellabel;

    @FXML
    private Label localisationlabel;

    @FXML
    private TextField phoneTextField;

    @FXML
    private TextField localisationTextField;

    @FXML
    private Label localisationErrorLabel;

    @FXML
    private Label phoneErrorLabel;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private Label nomErrorLabel;

    @FXML
    private Label prenomErrorLabel;

    @FXML
    public void initialize() throws SQLException {
        User user = userService.recherparid(UserSessionManager.getInstance().getCurrentUser().getId());

        // Populate text fields
        nomTextField.setText(user.getNom());
        prenomTextField.setText(user.getPrenom());
        emailTextField.setText(user.getEmail());
        phoneTextField.setText(user.getNum_telephone() + "");
        localisationTextField.setText(user.getLocalisation());

        // Populate labels
        nomlabel.setText(user.getNom());
        prenomlabel.setText(user.getPrenom());
        emaillabel.setText(user.getEmail());
        localisationlabel.setText(user.getLocalisation());
        tellabel.setText(user.getNum_telephone() + "");

        // Handle image loading
        String imageFromDb = user.getImage();
        if (imageFromDb != null && !imageFromDb.isEmpty()) {
            try {
                // Convert to file URL if it's a local file path
                String imageUrl;
                if (imageFromDb.startsWith("file:/")) {
                    imageUrl = imageFromDb; // Already a file URL
                } else {
                    // Assume it's a local file path and convert to file:/ URL
                    File imageFile = new File(imageFromDb);
                    imageUrl = imageFile.toURI().toURL().toString(); // Converts to file:/C:/path/to/image.jpg
                }
                Image image = new Image(imageUrl);
                imageView.setImage(image);

                // Apply circular clip
                Circle clipCircle = new Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2,
                        Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2);
                imageView.setClip(clipCircle);
            } catch (Exception e) {
                System.err.println("Failed to load image: " + imageFromDb + ". Error: " + e.getMessage());
                // Set a default image
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
                imageView.setImage(defaultImage);
            }
        } else {
            // Set default image if no image is provided
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
            imageView.setImage(defaultImage);
        }

        // Update imagePathLabel
        imagePathLabel.setText(imageFromDb != null && !imageFromDb.isEmpty() ? imageFromDb : "Aucune image sélectionnée");
    }

    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une Image de Profil");

        // Filtrer uniquement les fichiers image
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imagePathLabel.setText(selectedFile.getAbsolutePath());
            String imagePath = imagePathLabel.getText();
            Image image = new Image(imagePath);

            imageView.setImage(image);

            // Crée un clip circulaire pour l'image
            Circle clipCircle = new Circle(imageView.getFitWidth() / 2, imageView.getFitHeight() / 2,
                    Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2);
            imageView.setClip(clipCircle);
        }
    }

    @FXML
    public void edit() throws SQLException, IOException {
        int compteur = 0;
        int id = UserSessionManager.getInstance().getCurrentUser().getId();
        String nomInput = nomTextField.getText();
        String prenomInput = prenomTextField.getText();
        String emailInput = emailTextField.getText();
        String localisationInput = localisationTextField.getText();
        String imagePathInput = imagePathLabel.getText();

        // Validation
        if (nomInput.isEmpty() || nomInput.length() <= 3) {
            compteur++;
            showError("Nom invalide", nomErrorLabel);
        } else {
            nomErrorLabel.setOpacity(0);
        }

        if (prenomInput.isEmpty() || prenomInput.length() <= 3) {
            compteur++;
            showError("Prénom invalide", prenomErrorLabel);
        } else {
            prenomErrorLabel.setOpacity(0);
        }

        if (emailInput.isEmpty() || !emailInput.matches("^[\\w.-]+@(gmail\\.com|esprit\\.tn)$")) {
            compteur++;
            showError("Email invalide", emailErrorLabel);
        } else {
            emailErrorLabel.setOpacity(0);
        }

        if (localisationInput.isEmpty() || localisationInput.length() <= 3) {
            compteur++;
            showError("Localisation invalide", localisationErrorLabel);
        } else {
            localisationErrorLabel.setOpacity(0);
        }

        String phoneInput1 = phoneTextField.getText();
        if (!phoneInput1.matches("[0-9]{8}")) {
            compteur++;
            showError("Numéro invalide", phoneErrorLabel);
        } else {
            phoneErrorLabel.setOpacity(0);
        }

        // Handle image upload
        if (!imagePathInput.equals("Aucune image sélectionnée")) {
            try {
                Path sourcePath = Paths.get(imagePathInput);
                
                // Verify source file exists
                if (!Files.exists(sourcePath)) {
                    showError("L'image source n'existe pas", imagePathLabel);
                    compteur++;
                    return;
                }

                // Create destination directory if it doesn't exist
                Path destinationFolder = Paths.get("src", "main", "resources", "images");
                try {
                    Files.createDirectories(destinationFolder);
                } catch (IOException e) {
                    System.err.println("Failed to create images directory: " + e.getMessage());
                    showError("Impossible de créer le dossier images", imagePathLabel);
                    compteur++;
                    return;
                }

                // Generate unique filename
                String fileName = sourcePath.getFileName().toString();
                String fileNameWithoutExtension = fileName;
                String fileExtension = "";
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileNameWithoutExtension = fileName.substring(0, dotIndex);
                    fileExtension = fileName.substring(dotIndex);
                }
                
                Path destinationPath = destinationFolder.resolve(fileName);
                int counter = 1;
                while (Files.exists(destinationPath)) {
                    destinationPath = destinationFolder.resolve(fileNameWithoutExtension + "_" + counter + fileExtension);
                    counter++;
                }

                try {
                    // Copy file
                    Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    // Store the relative path in the database
                    imagePathInput = destinationPath.toString().replace("\\", "/");
                    System.out.println("Image successfully copied to: " + imagePathInput);
                } catch (IOException e) {
                    System.err.println("Failed to copy image: " + e.getMessage());
                    showError("Erreur lors de la copie de l'image", imagePathLabel);
                    compteur++;
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error processing image: " + e.getMessage());
                showError("Erreur lors du traitement de l'image", imagePathLabel);
                compteur++;
                return;
            }
        } else {
            // Keep existing image path if no new image was selected
            imagePathInput = UserSessionManager.getInstance().getCurrentUser().getImage();
        }

        // Update user if no validation errors
        if (compteur == 0) {
            int numTelephoneInput = Integer.parseInt(phoneTextField.getText());
            userService.modifier(nomInput, prenomInput, emailInput, localisationInput, numTelephoneInput, imagePathInput, id);
            this.pageFront();
        }
    }

    private void showError(String message, Label nomlabel) {
        nomlabel.setText(message);
        nomlabel.setOpacity(1);
    }

    public void pageFront() throws IOException {
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        navbarController.changeScene("/User/Front.fxml", currentStage);
    }

    public void pageReclamation() throws IOException {
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        navbarController.changeScene("/Reclamation/affichageReclamationFront.fxml", currentStage);
    }

    public void logout() throws IOException {
        UserSessionManager.getInstance().logout();
        Stage currentStage = (Stage) imageView.getScene().getWindow();
        navbarController.changeScene("hello-view.fxml", currentStage);
    }
}