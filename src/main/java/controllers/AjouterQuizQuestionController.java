package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Quiz;
import tn.esprit.models.QuizQuestion;
import tn.esprit.services.ServiceQuiz;
import tn.esprit.services.ServiceQuizQuestion;
import java.util.List;

public class AjouterQuizQuestionController {

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField pointsField;

    @FXML
    private TextArea texteField;

    @FXML
    private TextField ordreField;

    @FXML
    private ComboBox<Quiz> quizComboBox;

    private ServiceQuizQuestion serviceQuizQuestion;
    private ServiceQuiz serviceQuiz;

    @FXML
    void initialize() {
        System.out.println("AjouterQuizQuestionController: initialize() called");
        System.out.println("typeComboBox: " + (typeComboBox != null ? "Initialized" : "Null"));
        System.out.println("pointsField: " + (pointsField != null ? "Initialized" : "Null"));
        System.out.println("texteField: " + (texteField != null ? "Initialized" : "Null"));
        System.out.println("ordreField: " + (ordreField != null ? "Initialized" : "Null"));
        System.out.println("quizComboBox: " + (quizComboBox != null ? "Initialized" : "Null"));

        serviceQuizQuestion = new ServiceQuizQuestion();
        serviceQuiz = new ServiceQuiz();

        // Initialize typeComboBox items
        typeComboBox.setItems(FXCollections.observableArrayList("Choix Multiple", "Vrai/Faux", "Réponse Ouverte"));

        // Populate quizComboBox with all quizzes
        List<Quiz> quizzes = serviceQuiz.getAll();
        System.out.println("Number of quizzes retrieved: " + (quizzes != null ? quizzes.size() : "null"));
        quizComboBox.setItems(FXCollections.observableArrayList(quizzes));
        quizComboBox.setCellFactory(param -> new ListCell<Quiz>() {
            @Override
            protected void updateItem(Quiz item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitre());
                }
            }
        });
        quizComboBox.setButtonCell(new ListCell<Quiz>() {
            @Override
            protected void updateItem(Quiz item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitre());
                }
            }
        });

        // Add real-time validation for points (only numbers)
        pointsField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pointsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Add real-time validation for ordre (only numbers)
        ordreField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                ordreField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Add hover effects for typeComboBox
        typeComboBox.setOnMouseEntered(e -> typeComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-font-size: 14;"));
        typeComboBox.setOnMouseExited(e -> typeComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14;"));

        // Add hover effects for pointsField
        pointsField.setOnMouseEntered(e -> pointsField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-padding: 8; -fx-font-size: 14;"));
        pointsField.setOnMouseExited(e -> pointsField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;"));

        // Add hover effects for texteField
        texteField.setOnMouseEntered(e -> texteField.setStyle("-fx-pref-width: 300; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-padding: 8; -fx-font-size: 14;"));
        texteField.setOnMouseExited(e -> texteField.setStyle("-fx-pref-width: 300; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;"));

        // Add hover effects for ordreField
        ordreField.setOnMouseEntered(e -> ordreField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-padding: 8; -fx-font-size: 14;"));
        ordreField.setOnMouseExited(e -> ordreField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;"));

        // Add hover effects for quizComboBox
        quizComboBox.setOnMouseEntered(e -> quizComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #3b28cc; -fx-font-size: 14;"));
        quizComboBox.setOnMouseExited(e -> quizComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14;"));
    }

    @FXML
    private void saveQuestion() {
        try {
            System.out.println("saveQuestion() called");
            // Reset field styles to remove previous error highlights
            resetFieldStyles();

            // Retrieve and validate inputs
            String type = typeComboBox.getValue();
            String pointsText = pointsField.getText().trim();
            String texte = texteField.getText().trim();
            String ordreText = ordreField.getText().trim();
            Quiz quiz = quizComboBox.getValue();

            // Validation
            if (type == null) {
                typeComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #fff0f0; -fx-border-color: #ff4d4d; -fx-font-size: 14;");
                showErrorMessage("Le type est obligatoire.");
                return;
            }

            int points;
            try {
                points = Integer.parseInt(pointsText);
                if (points <= 0) {
                    pointsField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #fff0f0; -fx-border-color: #ff4d4d; -fx-padding: 8; -fx-font-size: 14;");
                    showErrorMessage("Les points doivent être supérieurs à 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                pointsField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #fff0f0; -fx-border-color: #ff4d4d; -fx-padding: 8; -fx-font-size: 14;");
                showErrorMessage("Les points doivent être un nombre valide.");
                return;
            }

            if (texte.isEmpty()) {
                texteField.setStyle("-fx-pref-width: 300; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #fff0f0; -fx-border-color: #ff4d4d; -fx-padding: 8; -fx-font-size: 14;");
                showErrorMessage("Le texte de la question est obligatoire.");
                return;
            }

            int ordre;
            try {
                ordre = Integer.parseInt(ordreText);
                if (ordre <= 0) {
                    ordreField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #fff0f0; -fx-border-color: #ff4d4d; -fx-padding: 8; -fx-font-size: 14;");
                    showErrorMessage("L'ordre doit être supérieur à 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                ordreField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #fff0f0; -fx-border-color: #ff4d4d; -fx-padding: 8; -fx-font-size: 14;");
                showErrorMessage("L'ordre doit être un nombre valide.");
                return;
            }

            if (quiz == null) {
                quizComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #fff0f0; -fx-border-color: #ff4d4d; -fx-font-size: 14;");
                showErrorMessage("Vous devez sélectionner un quiz.");
                return;
            }

            // Create new QuizQuestion
            QuizQuestion question = new QuizQuestion(type, points, texte, ordre, quiz);
            serviceQuizQuestion.add(question);

            showMessage("Question créée avec succès !");
            closeWindow();
        } catch (Exception e) {
            showErrorMessage("Erreur lors de la création de la question : " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        System.out.println("cancel() called");
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) typeComboBox.getScene().getWindow();
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
        typeComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14;");
        pointsField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;");
        texteField.setStyle("-fx-pref-width: 300; -fx-pref-height: 100; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;");
        ordreField.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-padding: 8; -fx-font-size: 14;");
        quizComboBox.setStyle("-fx-pref-width: 300; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-font-size: 14;");
    }
}