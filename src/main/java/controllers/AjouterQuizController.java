package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Cours;
import tn.esprit.models.Quiz;
import tn.esprit.models.QuizQuestion;
import tn.esprit.models.QuizResponse;
import tn.esprit.services.ServiceQuiz;
import tn.esprit.services.ServiceQuizQuestion;
import tn.esprit.services.ServiceQuizResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
// Added imports for org.json
import org.json.JSONArray;

import org.json.JSONObject;

public class AjouterQuizController {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField scoreMaxField;

    @FXML
    private DatePicker dateLimitePicker;

    private ServiceQuiz serviceQuiz;
    private ServiceQuizQuestion serviceQuizQuestion;
    private ServiceQuizResponse serviceQuizResponse;
    private static final String GEMINI_API_KEY = "AIzaSyCk_6RncTcvq-yFMVJI1KrrTss-vhupLbE";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
    private Cours cours;

    @FXML
    void initialize() {
        serviceQuiz = new ServiceQuiz();
        serviceQuizQuestion = new ServiceQuizQuestion();
        serviceQuizResponse = new ServiceQuizResponse();

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
                scoreMaxField.setStyle("-fx-pref-width: 350; -fx-background-radius: 5; -fx-border-radius: 5; -fx-background-color: #ffffff; -fx-border-color: red; -fx-padding: 8; -fx-font-size: 14;");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le score maximum doit être un nombre valide !");
                return;
            }

            // Convert LocalDate to LocalDateTime (set time to midnight)
            LocalDateTime dateLimiteTime = dateLimite.atTime(LocalTime.MIDNIGHT);

            // Create and save the quiz
            Quiz quiz = new Quiz(titre, description, scoreMax, dateLimiteTime,cours.getId());
            serviceQuiz.add(quiz);

            // Generate questions using Gemini API
            generateAndSaveQuestions(quiz);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Quiz ajouté avec succès avec 5 questions générées !");
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout du quiz : " + e.getMessage());
        }
    }

    private void generateAndSaveQuestions(Quiz quiz) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String prompt = String.format(
                    "Generate 5 beginner-level quiz questions based on the following quiz details:\n" +
                            "Title: %s\n" +
                            "Description: %s\n" +
                            "Each question should have 4 responses: 1 correct and 3 incorrect. Format the response as a JSON array where each question is an object with 'question' (the question text), 'correctAnswer' (the correct response), and 'incorrectAnswers' (an array of 3 incorrect responses). Example:\n" +
                            "[{\"question\": \"What is Symfony?\", \"correctAnswer\": \"A PHP framework\", \"incorrectAnswers\": [\"A Java library\", \"A Python framework\", \"A database system\"]}]",
                    quiz.getTitre(), quiz.getDescription()
            );

            JSONObject requestBody = new JSONObject();
            JSONObject content = new JSONObject();
            content.put("role", "user");
            content.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
            requestBody.put("contents", new JSONArray().put(content));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new Exception("Gemini API error: " + response.statusCode() + " - " + response.body());
            }

            JSONObject jsonResponse = new JSONObject(response.body());
            String generatedText = jsonResponse.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // Clean up the response if it contains code block markers
            generatedText = generatedText.replace("```json", "").replace("```", "").trim();

            // Parse the generated questions
            JSONArray questionsArray = new JSONArray(generatedText);
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObj = questionsArray.getJSONObject(i);
                String questionText = questionObj.getString("question");
                String correctAnswer = questionObj.getString("correctAnswer");
                JSONArray incorrectAnswers = questionObj.getJSONArray("incorrectAnswers");

                // Create and save the question
                QuizQuestion question = new QuizQuestion("multiple_choice", 2, questionText, i + 1, quiz);
                serviceQuizQuestion.add(question);

                // Add the correct response
                QuizResponse correctResponse = new QuizResponse(correctAnswer, true, question);
                serviceQuizResponse.add(correctResponse);

                // Add the incorrect responses
                for (int j = 0; j < incorrectAnswers.length(); j++) {
                    String incorrectAnswer = incorrectAnswers.getString(j);
                    QuizResponse incorrectResponse = new QuizResponse(incorrectAnswer, false, question);
                    serviceQuizResponse.add(incorrectResponse);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la génération des questions : " + e.getMessage());
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

    public void setCours(Cours cours) {
        this.cours = cours;
    }

}