package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceChapitre;
import tn.esprit.services.ServiceCours;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class AjouterChapitreController {

    @FXML
    private TextField titleField;

    @FXML
    private TextField chapOrderField;

    @FXML
    private TextField dureeEstimeeField;


    @FXML
    private Button uploadButton;

    @FXML
    private Label fileLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button addAnotherButton;

    @FXML
    private Button addEvaluationButton;

    @FXML
    private Label titleError;

    @FXML
    private Label chapOrderError;

    @FXML
    private Label dureeEstimeeError;


    @FXML
    private Label brochureError;

    private Cours cours;
    private ServiceChapitre serviceChapitre = new ServiceChapitre();
    private ServiceCours serviceCours = new ServiceCours();
    private static final String UPLOAD_DIR = "Uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public void setCourse(Cours course) {
        this.cours = course;

    }
    @FXML
    public void initialize() {
        // Ensure upload directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    @FXML
    void uploadButtonAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );
            File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
            if (selectedFile == null) {
                fileLabel.setText("Aucun PDF");
                fileLabel.getStyleClass().removeAll("text-success", "text-danger");
                fileLabel.getStyleClass().add("text-muted");
                return;
            }

            // Validate file
            if (!selectedFile.exists()) {
                throw new IllegalArgumentException("Fichier introuvable.");
            }
            if (selectedFile.length() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Fichier trop volumineux (max 10 Mo).");
            }
            if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                throw new IllegalArgumentException("Seuls les fichiers PDF sont acceptés.");
            }

            // Generate unique filename
            String newFilename = "chapter-" + UUID.randomUUID() + ".pdf";
            Path destination = Paths.get(UPLOAD_DIR, newFilename);

            // Copy file
            Files.copy(selectedFile.toPath(), destination);

            // Update fileLabel
            fileLabel.setText(newFilename);
            fileLabel.getStyleClass().removeAll("text-muted", "text-danger");
            fileLabel.getStyleClass().add("text-success");

        } catch (Exception e) {
            fileLabel.setText("Erreur: " + e.getMessage());
            fileLabel.getStyleClass().removeAll("text-muted", "text-success");
            fileLabel.getStyleClass().add("text-danger");
            showAlert(Alert.AlertType.ERROR, "Erreur d'upload", e.getMessage());
        }
    }

    @FXML
    void saveAction(ActionEvent event) {
        if (validateAndSave(false)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Chapitre enregistré.");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
                Parent root = loader.load();
                CourseDetailsController controller = loader.getController();
                controller.setCourse(cours);
                titleField.getScene().setRoot(root);
            } catch (IOException e) {
                System.err.println("Failed to load CourseDetails.fxml: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner aux détails du cours.");
            }
        }
    }

    @FXML
    void addAnotherAction(ActionEvent event) {
        if (validateAndSave(true)) {
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Chapitre enregistré. Ajoutez un autre.");
        }
    }

    @FXML
    void addEvaluationAction(ActionEvent event) {
        if (validateAndSave(true)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Chapitre enregistré. Redirection vers ajout d'évaluation.");
            // Navigate to evaluation form (implement as needed)
        }
    }

    private boolean validateAndSave(boolean keepForm) {
        try {
            // Reset validation
            resetValidation();

            // Retrieve inputs
            String title = titleField.getText().trim();
            String chapOrderText = chapOrderField.getText().trim();
            String dureeEstimeeText = dureeEstimeeField.getText().trim();
            String brochure = fileLabel.getText();

            // Validate
            boolean hasError = false;
            if (title.isEmpty()) {
                titleError.setText("Le titre est requis.");
                titleError.setVisible(true);
                titleError.setManaged(true);
                titleField.getStyleClass().add("error");
                hasError = true;
            } else if (title.length() < 5) {
                titleError.setText("Minimum 5 caractères.");
                titleError.setVisible(true);
                titleError.setManaged(true);
                titleField.getStyleClass().add("error");
                hasError = true;
            } else if (title.length() > 255) {
                titleError.setText("Maximum 255 caractères.");
                titleError.setVisible(true);
                titleError.setManaged(true);
                titleField.getStyleClass().add("error");
                hasError = true;
            }

            Integer chapOrder = null;
            try {
                chapOrder = Integer.parseInt(chapOrderText);
                if (chapOrder < 0) {
                    chapOrderError.setText("Doit être positif ou zéro.");
                    chapOrderError.setVisible(true);
                    chapOrderError.setManaged(true);
                    chapOrderField.getStyleClass().add("error");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                chapOrderError.setText("Nombre entier requis.");
                chapOrderError.setVisible(true);
                chapOrderError.setManaged(true);
                chapOrderField.getStyleClass().add("error");
                hasError = true;
            }

            Integer dureeEstimee = null;
            try {
                dureeEstimee = Integer.parseInt(dureeEstimeeText);
                if (dureeEstimee < 0) {
                    dureeEstimeeError.setText("Doit être positif ou zéro.");
                    dureeEstimeeError.setVisible(true);
                    dureeEstimeeError.setManaged(true);
                    dureeEstimeeField.getStyleClass().add("error");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                dureeEstimeeError.setText("Nombre entier requis.");
                dureeEstimeeError.setVisible(true);
                dureeEstimeeError.setManaged(true);
                dureeEstimeeField.getStyleClass().add("error");
                hasError = true;
            }



            if (brochure.equals("Aucun PDF") || brochure.startsWith("Erreur")) {
                brochureError.setText("Le PDF est requis.");
                brochureError.setVisible(true);
                brochureError.setManaged(true);
                fileLabel.getStyleClass().removeAll("text-muted", "text-success");
                fileLabel.getStyleClass().add("text-danger");
                hasError = true;
            }

            if (hasError) {
                return false;
            }

            // Create and save chapitre
            Chapitre chapitre = new Chapitre();
            chapitre.setTitle(title);
            chapitre.setChapOrder(chapOrder);
            chapitre.setCours(cours);
            chapitre.setDureeEstimee(dureeEstimee);
            chapitre.setNbrVues(0);
            chapitre.setContenu(brochure);

            serviceChapitre.add(chapitre);
            return true;

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            return false;
        }
    }

    private void resetValidation() {
        titleError.setVisible(false);
        titleError.setManaged(false);
        chapOrderError.setVisible(false);
        chapOrderError.setManaged(false);
        dureeEstimeeError.setVisible(false);
        dureeEstimeeError.setManaged(false);
        brochureError.setVisible(false);
        brochureError.setManaged(false);

        titleField.getStyleClass().remove("error");
        chapOrderField.getStyleClass().remove("error");
        dureeEstimeeField.getStyleClass().remove("error");
        fileLabel.getStyleClass().removeAll("text-success", "text-danger");
        fileLabel.getStyleClass().add("text-muted");
    }

    private void clearForm() {
        titleField.clear();
        chapOrderField.clear();
        dureeEstimeeField.clear();
        fileLabel.setText("Aucun PDF");
        fileLabel.getStyleClass().removeAll("text-success", "text-danger");
        fileLabel.getStyleClass().add("text-muted");
        resetValidation();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void retourAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 800); // Match CourseDetails.fxml dimensions
            CourseDetailsController controller = loader.getController();
            controller.setCourse(cours); // Pass the Cours object
            Stage stage = (Stage) fileLabel.getScene().getWindow(); // Adjust to your @FXML node
            stage.setScene(scene);
            stage.setTitle("Détails du Cours - " + cours.getTitle());
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading CourseDetails.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCours.fxml");
    }
    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from a known node
            Stage stage = (Stage) fileLabel.getScene().getWindow();
            // Create a new scene with the loaded root
            Scene scene = new Scene(root, 1000, 700); // Match FXML dimensions
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }

    }
}