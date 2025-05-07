package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.QuizQuestion;
import tn.esprit.models.QuizResponse;
import tn.esprit.services.ServiceQuizResponse;

public class AjouterQuizResponseController {

    @FXML
    private Label questionLabel;

    @FXML
    private TextField texteField1;

    @FXML
    private CheckBox correctCheckBox1;

    @FXML
    private TextField texteField2;

    @FXML
    private CheckBox correctCheckBox2;

    @FXML
    private TextField texteField3;

    @FXML
    private CheckBox correctCheckBox3;

    @FXML
    private TextField texteField4;

    @FXML
    private CheckBox correctCheckBox4;

    private QuizQuestion selectedQuestion;
    private ServiceQuizResponse serviceQuizResponse;

    @FXML
    void initialize() {
        serviceQuizResponse = new ServiceQuizResponse();

        // Ensure only one checkbox can be selected as correct
        ChangeListener<Boolean> correctListener = (obs, oldVal, newVal) -> {
            if (newVal) {
                // Uncheck the other checkboxes based on which one triggered the change
                if (obs == correctCheckBox1.selectedProperty()) {
                    correctCheckBox2.setSelected(false);
                    correctCheckBox3.setSelected(false);
                    correctCheckBox4.setSelected(false);
                } else if (obs == correctCheckBox2.selectedProperty()) {
                    correctCheckBox1.setSelected(false);
                    correctCheckBox3.setSelected(false);
                    correctCheckBox4.setSelected(false);
                } else if (obs == correctCheckBox3.selectedProperty()) {
                    correctCheckBox1.setSelected(false);
                    correctCheckBox2.setSelected(false);
                    correctCheckBox4.setSelected(false);
                } else if (obs == correctCheckBox4.selectedProperty()) {
                    correctCheckBox1.setSelected(false);
                    correctCheckBox2.setSelected(false);
                    correctCheckBox3.setSelected(false);
                }
            }
        };

        correctCheckBox1.selectedProperty().addListener(correctListener);
        correctCheckBox2.selectedProperty().addListener(correctListener);
        correctCheckBox3.selectedProperty().addListener(correctListener);
        correctCheckBox4.selectedProperty().addListener(correctListener);
    }

    public void setQuestion(QuizQuestion question) {
        this.selectedQuestion = question;
        questionLabel.setText("Question : " + question.getTexte());
    }

    @FXML
    private void saveResponses() {
        try {
            // Reset field styles
            resetFieldStyles();

            // Validate inputs
            String texte1 = texteField1.getText().trim();
            String texte2 = texteField2.getText().trim();
            String texte3 = texteField3.getText().trim();
            String texte4 = texteField4.getText().trim();

            // Check if all text fields are non-empty
            if (texte1.isEmpty()) {
                texteField1.setStyle("-fx-border-color: #ff4d4d; -fx-background-color: #fff0f0;");
                showErrorMessage("Le texte de la réponse 1 est obligatoire.");
                return;
            }
            if (texte2.isEmpty()) {
                texteField2.setStyle("-fx-border-color: #ff4d4d; -fx-background-color: #fff0f0;");
                showErrorMessage("Le texte de la réponse 2 est obligatoire.");
                return;
            }
            if (texte3.isEmpty()) {
                texteField3.setStyle("-fx-border-color: #ff4d4d; -fx-background-color: #fff0f0;");
                showErrorMessage("Le texte de la réponse 3 est obligatoire.");
                return;
            }
            if (texte4.isEmpty()) {
                texteField4.setStyle("-fx-border-color: #ff4d4d; -fx-background-color: #fff0f0;");
                showErrorMessage("Le texte de la réponse 4 est obligatoire.");
                return;
            }

            // Check if exactly one response is marked as correct
            int correctCount = 0;
            if (correctCheckBox1.isSelected()) correctCount++;
            if (correctCheckBox2.isSelected()) correctCount++;
            if (correctCheckBox3.isSelected()) correctCount++;
            if (correctCheckBox4.isSelected()) correctCount++;

            if (correctCount != 1) {
                showErrorMessage("Vous devez sélectionner exactement une réponse correcte.");
                return;
            }

            // Create and save the responses
            serviceQuizResponse.add(new QuizResponse(texte1, correctCheckBox1.isSelected(), selectedQuestion));
            serviceQuizResponse.add(new QuizResponse(texte2, correctCheckBox2.isSelected(), selectedQuestion));
            serviceQuizResponse.add(new QuizResponse(texte3, correctCheckBox3.isSelected(), selectedQuestion));
            serviceQuizResponse.add(new QuizResponse(texte4, correctCheckBox4.isSelected(), selectedQuestion));

            showMessage("Réponses ajoutées avec succès !");
            closeWindow();
        } catch (Exception e) {
            showErrorMessage("Erreur lors de l'ajout des réponses : " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) texteField1.getScene().getWindow();
        stage.close();
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void resetFieldStyles() {
        texteField1.setStyle("");
        texteField2.setStyle("");
        texteField3.setStyle("");
        texteField4.setStyle("");
    }
}