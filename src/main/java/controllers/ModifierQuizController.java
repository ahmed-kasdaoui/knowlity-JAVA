package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Quiz;
import tn.esprit.services.ServiceQuiz;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ModifierQuizController {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField scoreMaxField;

    @FXML
    private DatePicker dateLimitePicker;

    private ServiceQuiz serviceQuiz;
    private Quiz quizToModify;

    @FXML
    void initialize() {
        serviceQuiz = new ServiceQuiz();

        // Add hover effects for titreField
        titreField.setOnMouseEntered(e -> titreField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-padding: 8; -fx-font-size: 14;"));
        titreField.setOnMouseExited(e -> titreField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;"));

        // Add hover effects for descriptionField
        descriptionField.setOnMouseEntered(e -> descriptionField.setStyle("-fx-pref-width: 350; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-padding: 8; -fx-font-size: 14;"));
        descriptionField.setOnMouseExited(e -> descriptionField.setStyle("-fx-pref-width: 350; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;"));

        // Add hover effects for scoreMaxField
        scoreMaxField.setOnMouseEntered(e -> scoreMaxField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-padding: 8; -fx-font-size: 14;"));
        scoreMaxField.setOnMouseExited(e -> scoreMaxField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;"));

        // Add hover effects for dateLimitePicker
        dateLimitePicker.setOnMouseEntered(e -> dateLimitePicker.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-font-size: 14;"));
        dateLimitePicker.setOnMouseExited(e -> dateLimitePicker.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14;"));
    }

    public void setQuizToModify(Quiz quiz) {
        this.quizToModify = quiz;
        titreField.setText(quiz.getTitre());
        descriptionField.setText(quiz.getDescription());
        scoreMaxField.setText(String.valueOf(quiz.getScoreMax()));
        if (quiz.getDateLimite() != null) {
            dateLimitePicker.setValue(quiz.getDateLimite().toLocalDate());
        }
    }

    @FXML
    private void saveQuiz() {
        try {
            // Reset field styles
            titreField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;");
            descriptionField.setStyle("-fx-pref-width: 350; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;");
            scoreMaxField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;");
            dateLimitePicker.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14;");

            // Validate inputs
            String titre = titreField.getText().trim();
            String description = descriptionField.getText().trim();
            String scoreMaxText = scoreMaxField.getText().trim();
            LocalDate dateLimite = dateLimitePicker.getValue();

            boolean isValid = true;
            if (titre.isEmpty()) {
                titreField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: red; -fx-padding: 8; -fx-font-size: 14;");
                isValid = false;
            }
            if (description.isEmpty()) {
                descriptionField.setStyle("-fx-pref-width: 350; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: red; -fx-padding: 8; -fx-font-size: 14;");
                isValid = false;
            }
            if (scoreMaxText.isEmpty()) {
                scoreMaxField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: red; -fx-padding: 8; -fx-font-size: 14;");
                isValid = false;
            }
            if (dateLimite == null) {
                dateLimitePicker.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: red; -fx-font-size: 14;");
                isValid = false;
            }

            if (!isValid) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            // Parse scoreMax
            int scoreMax;
            try {
                scoreMax = Integer.parseInt(scoreMaxText);
                if (scoreMax <= 0) {
                    scoreMaxField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: red; -fx-padding: 8; -fx-font-size: 14;");
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le score maximum doit être supérieur à 0 !");
                    return;
                }
            } catch (NumberFormatException e) {
                scoreMaxField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: red; -fx-padding reliably 8; -fx-font-size: 14;");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le score maximum doit être un nombre valide !");
                return;
            }

            // Convert LocalDate to LocalDateTime (set time to midnight)
            LocalDateTime dateLimiteTime = dateLimite.atTime(LocalTime.MIDNIGHT);

            // Update the quiz
            quizToModify.setTitre(titre);
            quizToModify.setDescription(description);
            quizToModify.setScoreMax(scoreMax);
            quizToModify.setDateLimite(dateLimiteTime);

            serviceQuiz.update(quizToModify);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Quiz modifié avec succès !");
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification du quiz : " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) titreField.getScene().getWindow();
        stage.close();
    }
}