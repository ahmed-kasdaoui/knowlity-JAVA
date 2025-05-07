package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Utils.FacebookPoster;
import com.esprit.knowlity.controller.CustomDialogController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import tn.esprit.models.Cours;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TeacherEvaluationFormController {

    // UI Elements
    @FXML private Text formTitle;
    @FXML private ComboBox<Cours> courseComboBox;
    @FXML private TextField titleField;
    @FXML private TextField descField;
    @FXML private TextField scoreField;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextField badgeTitleField;
    @FXML private Button selectBadgeImageButton;
    @FXML private ImageView badgeImageView;
    private String selectedBadgeImageName = null;
    @FXML private TextField badgeThresholdField;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    // Data
    private Cours cours;
    private final EvaluationService evaluationService = new EvaluationService();
    private Evaluation editingEvaluation = null;
    private static final String IMAGE_DESTINATION_DIR = "C:\\xampp\\htdocs\\knowlity\\";

    private String copyImageToXampp(String sourcePath) throws IOException {
        if (sourcePath == null || sourcePath.isEmpty()) return "";
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) return "";
        
        // Generate a unique filename to prevent overwriting
        String uniqueFileName = UUID.randomUUID().toString() + 
            sourcePath.substring(sourcePath.lastIndexOf('.'));
        
        File destFile = new File(IMAGE_DESTINATION_DIR + uniqueFileName);
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return uniqueFileName;
    }

    // Setter pour injecter le cours
    public void setCourse(Cours cours) {
        this.cours = cours;
    }

    // Setter pour injecter une évaluation à modifier
    public void setEditingEvaluation(Evaluation eval) {
        this.editingEvaluation = eval;

        if (eval != null) {
            formTitle.setText("Modifier l'Évaluation");
            titleField.setText(eval.getTitle());
            descField.setText(eval.getDescription());
            scoreField.setText(String.valueOf(eval.getMaxScore()));
            deadlinePicker.setValue(eval.getDeadline().toLocalDateTime().toLocalDate());
            badgeTitleField.setText(eval.getBadgeTitle());
            
            // Set badge image if exists
            if (eval.getBadgeImage() != null && !eval.getBadgeImage().isEmpty()) {
                selectedBadgeImageName = eval.getBadgeImage();
                File imageFile = new File(IMAGE_DESTINATION_DIR + selectedBadgeImageName);
                if (imageFile.exists()) {
                    badgeImageView.setImage(new javafx.scene.image.Image(imageFile.toURI().toString()));
                    badgeImageView.setFitWidth(100);
                    badgeImageView.setFitHeight(100);
                    badgeImageView.setPreserveRatio(true);
                }
            }
            
            badgeThresholdField.setText(String.valueOf(eval.getBadgeThreshold()));
        } else {
            formTitle.setText("Ajouter une Évaluation");
            clearFields();
        }
    }

    // Nettoyer les champs
    private void clearFields() {
        titleField.clear();
        descField.clear();
        scoreField.clear();
        deadlinePicker.setValue(null);
        badgeTitleField.clear();
        selectedBadgeImageName = null;
        badgeImageView.setImage(null);
        badgeThresholdField.clear();
    }

    // Initialisation de l'interface
    @FXML
    private void initialize() {
        selectBadgeImageButton.setOnAction(e -> selectBadgeImage());

        saveButton.setOnAction(e -> saveEvaluation());
        backButton.setOnAction(e -> goBack());
    }

    private void selectBadgeImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de badge");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(selectBadgeImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Copy image to destination
                selectedBadgeImageName = copyImageToXampp(selectedFile.getAbsolutePath());
                
                // Display image in ImageView
                badgeImageView.setImage(new javafx.scene.image.Image(selectedFile.toURI().toString()));
                badgeImageView.setFitWidth(100);
                badgeImageView.setFitHeight(100);
                badgeImageView.setPreserveRatio(true);
            } catch (IOException e) {
                CustomDialogController.showDialog("Erreur", "Impossible de copier l'image", CustomDialogController.DialogType.ERROR);
            }
        }
    }

    // Enregistrer ou modifier une évaluation
    private void saveEvaluation() {
        String title = titleField.getText();
        String desc = descField.getText();
        String scoreText = scoreField.getText();
        LocalDate deadlineDate = deadlinePicker.getValue();

        boolean hasError = false;
        StringBuilder errorMsg = new StringBuilder();

        if (title == null || title.trim().isEmpty()) {
            errorMsg.append("Titre, ");
            hasError = true;
        }

        if (desc == null || desc.trim().isEmpty()) {
            errorMsg.append("Description, ");
            hasError = true;
        }

        int score = 0;
        try {
            score = Integer.parseInt(scoreText.trim());
            if (score <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            errorMsg.append("Score Max (nombre positif), ");
            hasError = true;
        }

        if (deadlineDate == null || !deadlineDate.isAfter(LocalDate.now())) {
            errorMsg.append("Date limite (future), ");
            hasError = true;
        }

        if (hasError) {
            errorMsg.setLength(errorMsg.length() - 2); // Retirer dernière virgule
            errorMsg.append(" sont obligatoires !");
            CustomDialogController.showDialog("Erreur de validation", errorMsg.toString(), CustomDialogController.DialogType.ERROR);
            return;
        }

        // Champs optionnels
        String badgeTitle = badgeTitleField.getText();
        String badgeImage = selectedBadgeImageName; // Use the saved image name
        int badgeThreshold = 0;
        try {
            badgeThreshold = Integer.parseInt(badgeThresholdField.getText().trim());
        } catch (Exception ignored) {}

        Timestamp deadline = Timestamp.valueOf(LocalDateTime.of(deadlineDate, java.time.LocalTime.of(23, 59)));

        if (editingEvaluation == null) {
            // Ajout
            Evaluation newEval = new Evaluation(0, cours.getId(), title, desc, score,
                    new Timestamp(System.currentTimeMillis()), deadline, badgeThreshold, badgeImage, badgeTitle);

            evaluationService.addEvaluation(newEval);

            int evaluationId = evaluationService.getLastInsertedEvaluationIdByTitle(title);
            String deeplink = String.format("knowlity://evaluate?evaluationId=%d", evaluationId);
            String fbMessage = String.format("""
                🌟 Nouvelle Évaluation Disponible ! 🌟

                📚 Cours : %s
                📝 Évaluation : %s
                📅 Date limite : %s

                Cliquez ici pour passer l'évaluation : %s

                Participez dès maintenant et donnez le meilleur de vous-même ! 💪✨
                """, cours.getTitle(), title, deadlineDate, deeplink);

            boolean fbSuccess = FacebookPoster.postToPage(fbMessage);
            if (fbSuccess) {
                CustomDialogController.showDialog("Succès", "Évaluation ajoutée et partagée sur Facebook !", CustomDialogController.DialogType.SUCCESS);
            } else {
                System.err.println("Erreur de partage Facebook. Vérifiez le token et l'ID de la page.");
            }

        } else {
            // Modification
            editingEvaluation.setCoursId(cours.getId());
            editingEvaluation.setTitle(title);
            editingEvaluation.setDescription(desc);
            editingEvaluation.setMaxScore(score);
            editingEvaluation.setDeadline(deadline);
            editingEvaluation.setBadgeTitle(badgeTitle);
            editingEvaluation.setBadgeImage(badgeImage);
            editingEvaluation.setBadgeThreshold(badgeThreshold);

            evaluationService.updateEvaluation(editingEvaluation);

            CustomDialogController.showDialog("Succès", "Évaluation mise à jour avec succès !", CustomDialogController.DialogType.SUCCESS);
        }

        goBack();
    }

    // Retour à l'écran principal enseignant
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            Parent root = loader.load();
            TeacherController controller = loader.getController();
            controller.setCourse(cours);
            formTitle.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Échec du chargement de teacher.fxml: " + e.getMessage());
        }
    }
}
