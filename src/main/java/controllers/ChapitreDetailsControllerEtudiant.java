package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceChapitre;
import tn.esprit.services.ServiceCours;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

public class ChapitreDetailsControllerEtudiant {

    @FXML private Label titleLabel;
    @FXML private Label chapterNumberLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label creationDateLabel;
    @FXML private Label pdfStatusLabel;
    @FXML private Button openPdfButton;
    @FXML private Button downloadPdfButton;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private VBox mainContainer;
    @FXML private VBox pdfSection;

    private Chapitre chapitre;
    private Cours cours;
    private final ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceCours serviceCours = new ServiceCours();
    private static final String UPLOAD_DIR = "uploads/";

    public void setChapitre(Chapitre chapitre, Cours cours) {
        this.chapitre = chapitre;
        this.cours = cours;
        loadChapitreData();
    }

    @FXML
    public void initialize() {
        // Ensure upload directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    private void loadChapitreData() {
        if (chapitre == null || cours == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Chapitre ou cours non spécifié.");
            return;
        }

        // Set chapter title and number
        titleLabel.setText(chapitre.getTitle() != null ? chapitre.getTitle() : "Sans titre");
        chapterNumberLabel.setText("Chapitre " + chapitre.getChapOrder());

        // Set description
        String description = chapitre.getDescription() != null ? chapitre.getDescription() : "Aucune description disponible";
        descriptionLabel.setText(description);

        // Set creation date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String creationDate = chapitre.getCreationDate() != null ? 
            dateFormat.format(chapitre.getCreationDate()) : "Date inconnue";
        creationDateLabel.setText("Créé le " + creationDate);

        // Handle PDF section
        boolean hasPdf = chapitre.getContenu() != null && !chapitre.getContenu().isEmpty();
        pdfStatusLabel.setText(hasPdf ? "PDF disponible" : "Aucun PDF disponible");
        pdfSection.setVisible(hasPdf);
        openPdfButton.setDisable(!hasPdf);
        downloadPdfButton.setDisable(!hasPdf);
    }

    @FXML
    void openPdfAction(ActionEvent event) {
        try {
            if (chapitre.getContenu() == null || chapitre.getContenu().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Aucun PDF", "Aucun fichier PDF associé à ce chapitre.");
                return;
            }

            File pdfFile = new File(UPLOAD_DIR + chapitre.getContenu());
            if (!pdfFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier PDF n'existe pas: " + pdfFile.getAbsolutePath());
                return;
            }

            // Open PDF with default system viewer
            java.awt.Desktop.getDesktop().open(pdfFile);
        } catch (IOException e) {
            System.err.println("Failed to open PDF: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le PDF: " + e.getMessage());
        }
    }

    @FXML
    void downloadPdfAction(ActionEvent event) {
        try {
            if (chapitre.getContenu() == null || chapitre.getContenu().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Aucun PDF", "Aucun fichier PDF associé à ce chapitre.");
                return;
            }

            File pdfFile = new File(UPLOAD_DIR + chapitre.getContenu());
            if (!pdfFile.exists()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le fichier PDF n'existe pas: " + pdfFile.getAbsolutePath());
                return;
            }

            // Prompt user to choose download location
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.setInitialFileName(chapitre.getContenu());
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File targetFile = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());

            if (targetFile != null) {
                Files.copy(pdfFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF téléchargé avec succès.");
            }
        } catch (IOException e) {
            System.err.println("Failed to download PDF: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de télécharger le PDF: " + e.getMessage());
        }
    }

    @FXML
    void backAction(ActionEvent event) {
        navigateToCourseDetails();
    }

    @FXML
    void editAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditChapitre.fxml"));
            Parent root = loader.load();
            EditChapitreController controller = loader.getController();
            controller.setChapitre(chapitre);
            mainContainer.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditChapitre.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire de modification.");
        }
    }

    @FXML
    void deleteAction(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer le chapitre '" + chapitre.getTitle() + "' ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Delete PDF if exists
                    if (chapitre.getContenu() != null && !chapitre.getContenu().isEmpty()) {
                        Files.deleteIfExists(Paths.get(UPLOAD_DIR + chapitre.getContenu()));
                    }
                    serviceChapitre.delete(chapitre);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Chapitre supprimé.");
                    navigateToCourseDetails();
                } catch (Exception e) {
                    System.err.println("Failed to delete chapitre: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le chapitre: " + e.getMessage());
                }
            }
        });
    }

    private void navigateToCourseDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetailsEtudiant.fxml"));
            Parent root = loader.load();
            CourseDetailsControllerEtudiant controller = loader.getController();
            controller.setCourse(cours);
            mainContainer.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load CourseDetails.fxml: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner aux détails du cours.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleListes(ActionEvent event) {
        System.out.println("handleListes clicked");
        loadScene("/ListeCoursEtudiant.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            // Load the new FXML
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Get the current stage from a known node
            Stage stage = (Stage) mainContainer.getScene().getWindow();
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