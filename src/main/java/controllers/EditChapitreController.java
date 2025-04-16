package controllers;

import controllers.CourseDetailsController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceChapitre;
import tn.esprit.services.ServiceCours;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class EditChapitreController {

    @FXML private TextField titleField;
    @FXML private TextField chapOrderField;
    @FXML private TextField dureeEstimeeField;
    @FXML private Button uploadButton;
    @FXML private Label fileLabel;
    @FXML private Button saveButton;
    @FXML private Button saveAndAddAnotherButton;
    @FXML private Button saveAndAddEvaluationButton;
    @FXML private Label titleError;
    @FXML private Label chapOrderError;
    @FXML private Label dureeEstimeeError;
    @FXML private Label brochureError;

    private Chapitre chapitre;

    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceCours serviceCours = new ServiceCours();
    private static final String UPLOAD_DIR = "Uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public void setChapitre(Chapitre chapitre) {
        this.chapitre = chapitre;

    }

    @FXML
    public void initialize() {
        // Ensure upload directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        fileLabel.setText("Aucun PDF");
    }

    private void loadChapitreData() {
        if (chapitre != null) {
            titleField.setText(chapitre.getTitle());
            chapOrderField.setText(String.valueOf(chapitre.getChapOrder()));
            dureeEstimeeField.setText(String.valueOf(chapitre.getDureeEstimee()));
            fileLabel.setText(chapitre.getContenu() != null ? chapitre.getContenu() : "Aucun PDF");
            if (!fileLabel.getText().equals("Aucun PDF")) {
                fileLabel.getStyleClass().removeAll("text-muted", "text-danger");
                fileLabel.getStyleClass().add("text-success");
            }
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
                return; // User cancelled, keep existing file
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
        if (validateAndSave()) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Chapitre mis à jour.");
            navigateToCourseDetails();
        }
    }

    @FXML
    void saveAndAddAnotherAction(ActionEvent event) {
        if (validateAndSave()) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Chapitre mis à jour. Ajoutez un autre.");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterChapitre.fxml"));
                Parent root = loader.load();
                AjouterChapitreController controller = loader.getController();
                controller.setCourse(chapitre.getCours());
                titleField.getScene().setRoot(root);
            } catch (IOException e) {
                System.err.println("Failed to load AjouterChapitre.fxml: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire d'ajout.");
            }
        }
    }

    @FXML
    void saveAndAddEvaluationAction(ActionEvent event) {
        if (validateAndSave()) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Chapitre mis à jour. Redirection vers ajout d'évaluation.");
            // TODO: Implement navigation to evaluation form
            navigateToCourseDetails(); // Fallback
        }
    }

    private boolean validateAndSave() {
        try {
            if (chapitre == null ) {
                throw new IllegalStateException("Chapitre ou cours non sélectionné.");
            }

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
            } else if (title.length() < 5 || title.length() > 255) {
                titleError.setText("Le titre doit avoir entre 5 et 255 caractères.");
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
                } else {
                    // Check for duplicate chapOrder (excluding current chapitre)
                    List<Chapitre> existingChapters = serviceCours.getChapitres(chapitre.getCours());
                    Integer finalChapOrder = chapOrder;
                    if (existingChapters != null && existingChapters.stream()
                            .anyMatch(ch -> ch.getId() != chapitre.getId() && ch.getChapOrder() == finalChapOrder)) {
                        chapOrderError.setText("Cet ordre existe déjà.");
                        chapOrderError.setVisible(true);
                        chapOrderError.setManaged(true);
                        chapOrderField.getStyleClass().add("error");
                        hasError = true;
                    }
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

            // Update chapitre
            chapitre.setTitle(title);
            chapitre.setChapOrder(chapOrder);
            chapitre.setCours(chapitre.getCours());
            chapitre.setDureeEstimee(dureeEstimee);
            chapitre.setContenu(brochure);

            serviceChapitre.update(chapitre);
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void navigateToCourseDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
            Parent root = loader.load();
            CourseDetailsController controller = loader.getController();
            controller.setCourse(chapitre.getCours());
            titleField.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load CourseDetails.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner aux détails du cours.");
        }
    }
}