package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Model.Reponse;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.Service.ReponseService;
import com.esprit.knowlity.Utils.Snippet.CodeSnippetView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.esprit.knowlity.Utils.BadWordsFilter;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;
import tn.knowlity.entity.User;
import tn.knowlity.tools.UserSessionManager;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EvaluationFormController {
    @FXML
    private Text titleText;
    @FXML
    private Text questionLabel;
    @FXML
    private TextArea answerTextArea;
    @FXML
    private javafx.scene.layout.StackPane codeSnippetPane; // Added for code snippet display
    @FXML
    private javafx.scene.layout.StackPane mathFormulaPane;
    private javafx.scene.web.WebView mathFormulaWebView = null;
    @FXML
    private javafx.scene.layout.HBox mathFormulaLabelBox; // Label for math formula

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button submitButton;
    @FXML
    private Button backButton;
    @FXML
    private Text deadlineText;
    @FXML
    private Text alertText;
    @FXML
    private javafx.scene.layout.VBox questionFormVBox;
    @FXML
    private javafx.scene.layout.StackPane noQuestionsBox;
    @FXML
    private javafx.scene.layout.HBox readonlyInfoBox;
    @FXML
    private Label readonlyInfoLabel;
    private Cours course;
    private Evaluation evaluation;
    private List<Question> questions;
    private int currentIndex = 0;
    private QuestionService questionService = new QuestionService();
    private ReponseService reponseService = new ReponseService();
    private EvaluationService evaluationService = new EvaluationService();
    // Store answers as user navigates
    private Map<Integer, String> answersMap = new HashMap<>();

    private User user = UserSessionManager.getInstance().getCurrentUser();
    private final int DEFAULT_USER_ID = user != null ? user.getId() : -1;
    private final String DEFAULT_USER_EMAIL = user != null ? user.getEmail() : "";
    private final String DEFAULT_USER_NAME = user != null ? user.getNom() : "";
    private static final String GMAIL_USERNAME = "chamseddinedoula7@gmail.com";
    private static final String GMAIL_PASSWORD = "xlvmkpnbcrjbrysu";

    private MediaPlayer backgroundMusicPlayer;
    private static final String BACKGROUND_MUSIC_PATH = "/music/soft_background.mp3";

    public void setCourse(Cours course) {
        this.course = course;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        // Retrieve the course based on the evaluation's course ID
        if (evaluation != null && evaluation.getCoursId() > 0) {
            ServiceCours serviceCours = new ServiceCours();
            this.course = serviceCours.getCoursById(evaluation.getCoursId());
        }
        loadQuestions();
    }

    private boolean readOnlyMode = false;

    private void loadQuestions() {
        updateProgressDisplay();
        if (evaluation == null) return;
        questions = questionService.getQuestionsByEvaluationId(evaluation.getId());
        titleText.setText(evaluation.getTitle());
        // Check if already completed
        readOnlyMode = isEvaluationCompletedByUser(evaluation.getId(), DEFAULT_USER_ID);
        if (readOnlyMode) {
            // Load all previous answers
            answersMap.clear();
            for (Question q : questions) {
                Reponse r = reponseService.getReponseByQuestionAndEvaluation(q.getId(), evaluation.getId(), DEFAULT_USER_ID);
                if (r != null) {
                    answersMap.put(q.getId(), r.getText());
                }
            }
        }
        if (questions == null || questions.isEmpty()) {
            // Hide question form, show indicator with fade in
            questionFormVBox.setVisible(false);
            questionFormVBox.setManaged(false);
            noQuestionsBox.setOpacity(0);
            noQuestionsBox.setVisible(true);
            noQuestionsBox.setManaged(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), noQuestionsBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            // Show form, hide indicator
            noQuestionsBox.setVisible(false);
            noQuestionsBox.setManaged(false);
            questionFormVBox.setOpacity(0);
            questionFormVBox.setVisible(true);
            questionFormVBox.setManaged(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), questionFormVBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            showQuestion(0);
        }
        LocalDateTime deadline = evaluation.getDeadline().toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        deadlineText.setText("Deadline: " + deadline.format(formatter));
        if (LocalDateTime.now().isAfter(deadline)) {
            alertText.setText("Deadline passed!");
            submitButton.setDisable(true);
            answerTextArea.setDisable(true);
        }
        // UI changes for read-only mode
        if (readOnlyMode) {
            answerTextArea.setEditable(false);
            answerTextArea.setStyle(answerTextArea.getStyle() + "; -fx-background-color: #f3f3f3; -fx-text-fill: #888; -fx-font-style: italic;");
            submitButton.setVisible(false);
            submitButton.setManaged(false);
            readonlyInfoBox.setVisible(true);
            readonlyInfoBox.setManaged(true);
        } else {
            answerTextArea.setEditable(true);
            submitButton.setVisible(true);
            submitButton.setManaged(true);
            readonlyInfoBox.setVisible(false);
            readonlyInfoBox.setManaged(false);
        }
    }

    // Mock implementation, replace with actual DB check
    private boolean isEvaluationCompletedByUser(int evaluationId, int userId) {
        // For demo: consider completed if at least one answer exists for all questions
        int answered = 0;
        for (Question q : questionService.getQuestionsByEvaluationId(evaluationId)) {
            Reponse r = reponseService.getReponseByQuestionAndEvaluation(q.getId(), evaluationId, userId);
            if (r != null && r.getText() != null && !r.getText().isEmpty()) {
                answered++;
            }
        }
        return (questions != null && !questions.isEmpty() && answered == questions.size());
    }

    private void showQuestion(int idx) {
        if (!readOnlyMode) {
            // Save current answer before switching
            if (currentIndex >= 0 && currentIndex < questions.size()) {
                Question prevQ = questions.get(currentIndex);
                answersMap.put(prevQ.getId(), answerTextArea.getText());
            }
        }
        currentIndex = idx;
        Question q = questions.get(idx);
        questionLabel.setText((idx + 1) + ". " + q.getTitle() + "\n" + q.getEnonce());

        // --- Math Formula Display ---
        mathFormulaPane.getChildren().clear();
        boolean hasFormula = q.isHasMathFormula() && q.getMathFormula() != null && !q.getMathFormula().trim().isEmpty();
        if (mathFormulaLabelBox != null) {
            mathFormulaLabelBox.setVisible(hasFormula);
            mathFormulaLabelBox.setManaged(hasFormula);
            if (hasFormula) {
                javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), mathFormulaLabelBox);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        }
        if (hasFormula) {
            if (mathFormulaWebView == null) {
                mathFormulaWebView = new javafx.scene.web.WebView();
                mathFormulaWebView.setContextMenuEnabled(false);
                mathFormulaWebView.setPrefHeight(60);
                mathFormulaWebView.setPrefWidth(580);
                mathFormulaWebView.setStyle("-fx-background-color: transparent;");
            }
            String latex = q.getMathFormula();
            String html = "<!DOCTYPE html><html><head>"
                    + "<meta charset='utf-8'>"
                    + "<script src='https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js'></script>"
                    + "<style>body{margin:0;background:transparent;} #math{font-size:22px;}</style>"
                    + "</head><body><div id='math'>\\(" + latex.replace("\\", "\\\\") + "\\)</div></body></html>";
            mathFormulaWebView.getEngine().loadContent(html);
            mathFormulaPane.getChildren().add(mathFormulaWebView);
            mathFormulaPane.setVisible(true);
            mathFormulaPane.setManaged(true);
            // Fade-in animation
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), mathFormulaPane);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } else {
            mathFormulaPane.setVisible(false);
            mathFormulaPane.setManaged(false);
        }

        // --- Code Snippet Display ---
        codeSnippetPane.getChildren().clear();
        boolean hasCodeSnippet = q.getCodeSnippet() != null && !q.getCodeSnippet().trim().isEmpty();
        if (hasCodeSnippet) {
            CodeSnippetView codeView = new CodeSnippetView(q.getCodeSnippet(), q.getProgrammingLanguage());
            codeSnippetPane.getChildren().add(codeView);
        }
        // Restore answer if exists
        String prevAnswer = answersMap.getOrDefault(q.getId(), "");
        answerTextArea.setText(prevAnswer);
        prevButton.setDisable(idx == 0);
        nextButton.setDisable(idx == questions.size() - 1);
        // UI for read-only mode per question
        answerTextArea.setEditable(!readOnlyMode);
        if (readOnlyMode) {
            answerTextArea.setStyle(answerTextArea.getStyle() + "; -fx-background-color: #f3f3f3; -fx-text-fill: #888; -fx-font-style: italic;");
        } else {
            answerTextArea.setStyle("-fx-background-radius: 18; -fx-font-size: 18px; -fx-padding: 18; -fx-background-color: #f7fafd; -fx-border-color: #43cea2; -fx-border-radius: 18; -fx-border-width: 1.5;");
        }
        updateProgressDisplay();
    }

    @FXML
    public void initialize() {
        // Start background music
        try {
            Media backgroundMusic = new Media(getClass().getResource(BACKGROUND_MUSIC_PATH).toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(backgroundMusic);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop continuously
            backgroundMusicPlayer.setVolume(0.2); // Set initial volume to 20%
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.err.println("Impossible de charger la musique de fond: " + e.getMessage());
        }

        // Add real-time bad words filter
        answerTextArea.textProperty().addListener((obs, oldText, newText) -> {
            String filtered = BadWordsFilter.filterBadWords(newText);
            if (!filtered.equals(newText)) {
                final int caretPos = answerTextArea.getCaretPosition();
                javafx.application.Platform.runLater(() -> {
                    answerTextArea.setText(filtered);
                    // Restore caret position safely
                    int safeCaret = caretPos > filtered.length() ? filtered.length() : caretPos;
                    answerTextArea.positionCaret(safeCaret);
                });
            }
        });
        prevButton.setOnAction(e -> {
            answersMap.put(questions.get(currentIndex).getId(), answerTextArea.getText());
            if (currentIndex > 0) showQuestion(currentIndex - 1);
        });
        nextButton.setOnAction(e -> {
            answersMap.put(questions.get(currentIndex).getId(), answerTextArea.getText());
            if (currentIndex < questions.size() - 1) showQuestion(currentIndex + 1);
        });
        submitButton.setOnAction(e -> submitAnswers());
        backButton.setOnAction(e -> handleBackAction());
    }

    private void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose();
        }
    }

    private void submitAnswers() {
        stopBackgroundMusic(); // Stop music when submitting
        updateProgressDisplay();
        // Save current answer before submitting
        if (currentIndex >= 0 && currentIndex < questions.size()) {
            Question currentQ = questions.get(currentIndex);
            answersMap.put(currentQ.getId(), answerTextArea.getText());
        }
        // Save all answers to DB
        for (Question q : questions) {
            String answer = answersMap.getOrDefault(q.getId(), "");
            if (!answer.trim().isEmpty()) {
                String filtered = BadWordsFilter.filterBadWords(answer);
                boolean hasBadWord = filtered.contains("**");
                Reponse r = new Reponse();
                //r.setNote(0);
                r.setSubmitTime(new java.sql.Timestamp(System.currentTimeMillis()));
                r.setQuestionId(q.getId());
                r.setEvaluationId(evaluation.getId());
                r.setText(filtered);
                r.setUserId(DEFAULT_USER_ID);
                if (hasBadWord) {
                    r.setNote(0);
                    // Email logic - send asynchronously
                    javafx.concurrent.Task<Void> emailTask = new javafx.concurrent.Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            String teacherEmail = "amennahali8@gmail.com";
                            String studentEmail = DEFAULT_USER_EMAIL;
                            String studentName = DEFAULT_USER_NAME;
                            String studentPrenom = user != null ? user.getPrenom() : "";
                            String evalName = evaluation.getTitle();

                            System.out.println("Student Email: " + studentEmail);
                            System.out.println("Student Name: " + studentName);

                            String questionTitle = q.getTitle();
                            // Email to teacher
                            String teacherSubject = "[Knowlity] Inappropriate Answer Detected";
                            System.out.println("Course object: " + course);
                            String courseName = course != null ? course.getTitle() : "Unknown Course";
                            System.out.println("Course Name: " + courseName);
                            String teacherHtml = com.esprit.knowlity.Utils.MailUtil.getTeacherHtmlEmail(studentName, studentPrenom, courseName, evalName, questionTitle, answer);

                            // Configuration SMTP
                            Properties props = new Properties();
                            props.put("mail.smtp.host", "smtp.gmail.com");
                            props.put("mail.smtp.port", "587");
                            props.put("mail.smtp.auth", "true");
                            props.put("mail.smtp.starttls.enable", "true");

                            Session session = Session.getInstance(props, new Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(GMAIL_USERNAME, GMAIL_PASSWORD);
                                }
                            });

                            // Envoi de l'email au professeur
                            Message teacherMessage = new MimeMessage(session);
                            teacherMessage.setFrom(new InternetAddress(GMAIL_USERNAME));
                            teacherMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(teacherEmail));
                            teacherMessage.setSubject(teacherSubject);
                            teacherMessage.setContent(teacherHtml, "text/html; charset=utf-8");
                            Transport.send(teacherMessage);

                            // Envoi de l'email à l'étudiant
                            String studentSubject = "[Knowlity] Inappropriate Answer Detected";
                            String studentHtml = com.esprit.knowlity.Utils.MailUtil.getStudentHtmlEmail(studentName, studentPrenom, courseName, evalName, questionTitle, filtered);
                            Message studentMessage = new MimeMessage(session);
                            studentMessage.setFrom(new InternetAddress(GMAIL_USERNAME));
                            studentMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(studentEmail));
                            studentMessage.setSubject(studentSubject);
                            studentMessage.setContent(studentHtml, "text/html; charset=utf-8");
                            Transport.send(studentMessage);

                            return null;
                        }
                    };
                    emailTask.setOnSucceeded(evt -> {
                        com.esprit.knowlity.controller.CustomDialogController.showDialog(
                                "Check Your Email",
                                "Your answer contained inappropriate content. Please check your email for more details.",
                                com.esprit.knowlity.controller.CustomDialogController.DialogType.INFO
                        );
                    });
                    emailTask.setOnFailed(evt -> {
                        Throwable exception = emailTask.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
                        com.esprit.knowlity.controller.CustomDialogController.showDialog(
                                "Email Error",
                                "Failed to send notification emails. Error: " + errorMessage,
                                com.esprit.knowlity.controller.CustomDialogController.DialogType.ERROR
                        );
                    });
                    new Thread(emailTask).start();
                }
                reponseService.addReponse(r);
            }
        }

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Vos réponses ont été soumises avec succès !");
        alert.showAndWait();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_select.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et initialiser les données
            EvaluationSelectController controller = loader.getController();

            // Récupérer les évaluations pour le cours actuel
            if (evaluation != null) {
                EvaluationService evaluationService = new EvaluationService();
                ServiceCours serviceCours = new ServiceCours();

                // Récupérer le cours et les évaluations
                Cours cours = serviceCours.getCoursById(evaluation.getCoursId());
                List<Evaluation> evals = evaluationService.getEvaluationsByCoursId(evaluation.getCoursId());

                // Configurer le contrôleur
                controller.setCourse(cours);
                controller.setEvaluations(evals);

                // Configurer le bouton retour du EvaluationSelectController
                controller.setOnBack(event -> {
                    try {
                        FXMLLoader studentLoader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                        Parent studentRoot = studentLoader.load();
                        StudentController studentController = studentLoader.getController();
                        studentController.setCourse(cours);
                        submitButton.getScene().setRoot(studentRoot);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorDialog("Erreur de navigation",
                                "Impossible de retourner à l'écran principal: " + e.getMessage());
                    }
                });
            }

            // Changer la scène
            submitButton.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Erreur de navigation",
                    "Impossible de retourner à la page précédente: " + ex.getMessage());
        }
    }

    // --- DYNAMIC PROGRESS LABEL LOGIC ---
    private void updateProgressDisplay() {
        // Handle no questions
        if (questions == null || questions.isEmpty()) {
            progressBar.setProgress(0);
            progressLabel.setText("");
            return;
        }
        // Handle deadline passed (if applicable)
        boolean deadlinePassed = false;
        if (evaluation != null && evaluation.getDeadline() != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime deadline = evaluation.getDeadline().toLocalDateTime();
            deadlinePassed = now.isAfter(deadline);
        }
        // Only one question
        if (questions.size() == 1) {
            progressBar.setProgress(1.0);
            if (readOnlyMode) {
                progressLabel.setText("Completed: 100%");
            } else if (deadlinePassed) {
                progressLabel.setText("Deadline Passed");
            } else {
                progressLabel.setText("Question 1 of 1 (100%)");
            }
            return;
        }
        // Multiple questions
        if (readOnlyMode) {
            progressBar.setProgress(1.0);
            progressLabel.setText("Completed: 100%");
        } else if (deadlinePassed) {
            progressBar.setProgress((double) (currentIndex + 1) / questions.size());
            progressLabel.setText("Deadline Passed");
        } else {
            double progress = (double) (currentIndex + 1) / questions.size();
            progressBar.setProgress(progress);
            progressLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size() + " (" + (int) (progress * 100) + "% )");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBackAction() {
        stopBackgroundMusic(); // Stop music when going back
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_select.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et initialiser les données
            EvaluationSelectController controller = loader.getController();

            // Récupérer les évaluations pour le cours actuel
            if (evaluation != null) {
                EvaluationService evaluationService = new EvaluationService();
                ServiceCours serviceCours = new ServiceCours();

                // Récupérer le cours et les évaluations
                Cours cours = serviceCours.getCoursById(evaluation.getCoursId());
                List<Evaluation> evals = evaluationService.getEvaluationsByCoursId(evaluation.getCoursId());

                // Configurer le contrôleur
                controller.setCourse(cours);
                controller.setEvaluations(evals);

                // Configurer le bouton retour du EvaluationSelectController
                controller.setOnBack(event -> {
                    try {
                        FXMLLoader studentLoader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                        Parent studentRoot = studentLoader.load();
                        StudentController studentController = studentLoader.getController();
                        studentController.setCourse(cours);
                        backButton.getScene().setRoot(studentRoot);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorDialog("Erreur de navigation",
                                "Impossible de retourner à l'écran principal: " + e.getMessage());
                    }
                });
            }

            // Changer la scène
            backButton.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorDialog("Erreur de navigation",
                    "Impossible de retourner à la page précédente: " + ex.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}