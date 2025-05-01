package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.Quiz;
import tn.esprit.models.QuizQuestion;
import tn.esprit.models.QuizResponse;
import tn.esprit.models.UserQuizSelection;
import tn.esprit.models.UserResult;
import tn.esprit.services.ServiceQuizQuestion;
import tn.esprit.services.ServiceQuizResponse;
import tn.esprit.services.ServiceUserResult;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class TakeQuizController {

    @FXML
    private Label quizTitleLabel;

    @FXML
    private Label questionLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private VBox responseBox;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button submitButton;

    @FXML
    private Button playButton;

    @FXML
    private SVGPath playIcon;

    @FXML
    private StackPane resultsPane;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label percentageLabel;

    @FXML
    private Label finalTimeLabel;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button reviewButton;

    @FXML
    private Button finishButton;

    private Quiz quiz;
    private List<QuizQuestion> questions;
    private List<QuizResponse> responses;
    private List<UserQuizSelection> userSelections;
    private int currentQuestionIndex;
    private ServiceQuizQuestion serviceQuizQuestion;
    private ServiceQuizResponse serviceQuizResponse;
    private ServiceUserResult serviceUserResult;
    private ToggleGroup responseToggleGroup;
    private Timeline timer;
    private int remainingSeconds;
    private Process speakProcess;
    private boolean isAudioPlaying;
    private MediaPlayer mediaPlayer;

    private static final String ELEVENLABS_API_KEY = "sk_1279b167e258053d42aef71673e9be85d6ed14b02292fa44";
    private static final String ELEVENLABS_VOICE_ID = "29vD33N1CtxCmqQRPOHJ";

    @FXML
    void initialize() {
        serviceQuizQuestion = new ServiceQuizQuestion();
        serviceQuizResponse = new ServiceQuizResponse();
        serviceUserResult = new ServiceUserResult();
        userSelections = new ArrayList<>();
        currentQuestionIndex = 0;
        responseToggleGroup = new ToggleGroup();
        remainingSeconds = 60;
        isAudioPlaying = false;

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerLabel();
            if (remainingSeconds <= 0) {
                timer.stop();
                handleSubmitQuiz();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);

        finishButton.setOnAction(event -> {
            Stage stage = (Stage) finishButton.getScene().getWindow();
            stage.close();
        });

        reviewButton.setOnAction(event -> {
            resultsPane.setVisible(false);
            currentQuestionIndex = 0;
            displayCurrentQuestion();
        });
    }

    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("Temps restant : %02d:%02d", minutes, seconds));
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        quizTitleLabel.setText(quiz.getTitre());
        loadQuestions();
        displayCurrentQuestion();
        remainingSeconds = 60;
        updateTimerLabel();
        timer.play();
    }

    private void loadQuestions() {
        questions = serviceQuizQuestion.getAll().stream()
                .filter(q -> q.getQuiz() != null && q.getQuiz().getId() == quiz.getId())
                .collect(Collectors.toList());
        Collections.shuffle(questions);
        for (QuizQuestion question : questions) {
            userSelections.add(new UserQuizSelection(null, false, question));
        }
    }

    private void displayCurrentQuestion() {
        if (questions.isEmpty()) {
            questionLabel.setText("Aucune question disponible.");
            responseBox.getChildren().clear();
            return;
        }

        stopAudio();

        QuizQuestion currentQuestion = questions.get(currentQuestionIndex);
        questionLabel.setText(currentQuestion.getTexte());
        responseBox.getChildren().clear();

        progressLabel.setText((currentQuestionIndex + 1) + "/" + questions.size());
        progressBar.setProgress((double) (currentQuestionIndex + 1) / questions.size());

        responses = serviceQuizResponse.getAll().stream()
                .filter(r -> r.getQuestion() != null && r.getQuestion().getId() == currentQuestion.getId())
                .collect(Collectors.toList());

        Collections.shuffle(responses);

        responseToggleGroup.getToggles().clear();
        for (QuizResponse response : responses) {
            RadioButton radioButton = new RadioButton(response.getTexte());
            radioButton.setToggleGroup(responseToggleGroup);
            radioButton.setStyle("-fx-font-size: 16; -fx-text-fill: #ffffff; -fx-font-weight: normal;");
            radioButton.setUserData(response);
            responseBox.getChildren().add(radioButton);

            UserQuizSelection selection = userSelections.get(currentQuestionIndex);
            if (selection.getResponseSelectionnee() != null && selection.getResponseSelectionnee().getId() == response.getId()) {
                radioButton.setSelected(true);
            }
        }

        prevButton.setDisable(currentQuestionIndex == 0);
        nextButton.setVisible(currentQuestionIndex < questions.size() - 1);
        submitButton.setVisible(currentQuestionIndex == questions.size() - 1);
    }

    @FXML
    private void handlePreviousQuestion() {
        saveCurrentSelection();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayCurrentQuestion();
        }
    }

    @FXML
    private void handleNextQuestion() {
        saveCurrentSelection();
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayCurrentQuestion();
        }
    }

    private void saveCurrentSelection() {
        Toggle selectedToggle = responseToggleGroup.getSelectedToggle();
        if (selectedToggle != null) {
            QuizResponse selectedResponse = (QuizResponse) selectedToggle.getUserData();
            UserQuizSelection selection = userSelections.get(currentQuestionIndex);
            selection.setResponseSelectionnee(selectedResponse);
            boolean isCorrect = selectedResponse.isEstCorrecte();
            selection.setEstCorrecte(isCorrect);
        }
    }

    @FXML
    private void handlePlayVoice() {
        if (isAudioPlaying) {
            stopAudio();
        } else {
            QuizQuestion currentQuestion = questions.get(currentQuestionIndex);
            StringBuilder textToRead = new StringBuilder();
            textToRead.append("Question: ").append(currentQuestion.getTexte()).append(". ");
            textToRead.append("Options: ");
            for (int i = 0; i < responses.size(); i++) {
                textToRead.append("Option ").append(i + 1).append(": ").append(responses.get(i).getTexte()).append(". ");
            }

            try {
                String audioPath = generateAudioWithElevenLabs(textToRead.toString());
                if (audioPath != null) {
                    Media media = new Media(new java.io.File(audioPath).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setOnEndOfMedia(() -> stopAudio());
                    mediaPlayer.setOnError(() -> {
                        System.err.println("MediaPlayer error: " + mediaPlayer.getError().getMessage());
                        stopAudio();
                    });
                    mediaPlayer.play();
                    isAudioPlaying = true;
                    playButton.setText("Arrêter");
                    playIcon.setContent("M6 6h12v12H6z");
                    playButton.setDisable(false);
                } else {
                    throw new Exception("Failed to generate audio from ElevenLabs");
                }
            } catch (Exception e) {
                System.err.println("Error using ElevenLabs or playing audio: " + e.getMessage());
                try {
                    String escapedText = textToRead.toString().replace("\"", "\\\"");
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            "C:\\Program Files (x86)\\eSpeak\\command_line\\espeak.exe",
                            "-v", "en",
                            escapedText
                    );
                    speakProcess = processBuilder.start();
                    isAudioPlaying = true;
                    playButton.setText("Arrêter");
                    playIcon.setContent("M6 6h12v12H6z");
                    playButton.setDisable(true);

                    new Thread(() -> {
                        try {
                            speakProcess.waitFor();
                            javafx.application.Platform.runLater(this::stopAudio);
                        } catch (InterruptedException ex) {
                            System.err.println("Error waiting for eSpeak process: " + ex.getMessage());
                        }
                    }).start();
                } catch (Exception ex) {
                    System.err.println("Error using eSpeak: " + ex.getMessage());
                    System.out.println("Voice would read: " + textToRead.toString());
                    isAudioPlaying = true;
                    playButton.setText("Arrêter");
                    playIcon.setContent("M6 6h12v12H6z");
                    playButton.setDisable(true);
                }
            }
        }
    }

    private String generateAudioWithElevenLabs(String text) throws Exception {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = String.format(
                    "{\"text\": \"%s\", \"voice_settings\": {\"stability\": 0.5, \"similarity_boost\": 0.5}}",
                    text.replace("\"", "\\\"")
            );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.elevenlabs.io/v1/text-to-speech/" + ELEVENLABS_VOICE_ID))
                    .header("Content-Type", "application/json")
                    .header("xi-api-key", ELEVENLABS_API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                Path tempFile = Files.createTempFile("elevenlabs_audio_", ".mp3");
                Files.write(tempFile, response.body());
                return tempFile.toString();
            } else {
                System.err.println("ElevenLabs API error: " + response.statusCode() + " - " + new String(response.body()));
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception while calling ElevenLabs API: " + e.getMessage());
            return null;
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        if (speakProcess != null) {
            speakProcess.destroy();
            speakProcess = null;
        }
        isAudioPlaying = false;
        playButton.setText("Lire");
        playIcon.setContent("M8 5v14l11-7z");
        playButton.setDisable(false);
    }

    @FXML
    private void handleSubmitQuiz() {
        timer.stop();
        stopAudio();
        saveCurrentSelection();

        int totalScore = 0;
        for (UserQuizSelection selection : userSelections) {
            if (selection.getResponseSelectionnee() != null) {
                if (selection.isEstCorrecte()) {
                    totalScore += selection.getQuestion().getPoints();
                }
            }
        }

        String feedback;
        int maxScore = questions.stream().mapToInt(QuizQuestion::getPoints).sum();
        double scorePercentage = maxScore > 0 ? (double) totalScore / maxScore * 100 : 0;
        if (scorePercentage >= 80) {
            feedback = "Excellent travail !";
        } else if (scorePercentage >= 50) {
            feedback = "Bon effort !";
        } else {
            feedback = "Essayez encore !";
        }

        int elapsedTime = 60 - remainingSeconds;
        // Save the result to the user_result table
        UserResult userResult = new UserResult(1, quiz.getId(), totalScore); // Assuming user_id is 1
        serviceUserResult.add(userResult);

        scoreLabel.setText(totalScore + "/" + maxScore);
        percentageLabel.setText(String.format("%.0f%%", scorePercentage));
        int minutes = elapsedTime / 60;
        int seconds = elapsedTime % 60;
        finalTimeLabel.setText(String.format("%02d:%02d", minutes, seconds));
        feedbackLabel.setText(feedback);
        resultsPane.setVisible(true);
    }
}