package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Model.Cours;
import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Model.Reponse;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.Service.ReponseService;
import com.esprit.knowlity.Utils.Snippet.CodeSnippetView;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;
import javafx.scene.web.WebView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.esprit.knowlity.Utils.BadWordsFilter;
import tn.knowlity.entity.User;
import tn.knowlity.tools.UserSessionManager;

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
    private final int DEFAULT_USER_ID = user.getId();
    private final String DEFAULT_USER_EMAIL = user.getEmail();
    private final String DEFAULT_USER_NAME = user.getNom();


    public void setCourse(Cours course) {
        this.course = course;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
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
            // Save answer before moving
            answersMap.put(questions.get(currentIndex).getId(), answerTextArea.getText());
            if (currentIndex > 0) showQuestion(currentIndex - 1);
        });
        nextButton.setOnAction(e -> {
            // Save answer before moving
            answersMap.put(questions.get(currentIndex).getId(), answerTextArea.getText());
            if (currentIndex < questions.size() - 1) showQuestion(currentIndex + 1);
        });
        submitButton.setOnAction(e -> {
            String answer = answerTextArea.getText();
            if (answer == null || answer.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Empty Response");
                alert.setHeaderText(null);
                alert.setContentText("Please type your answer before submitting.");
                alert.showAndWait();
                return;
            }
            submitAnswers();
        });
        // Back button logic
        backButton.setOnAction(ev -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                stage.setScene(new Scene(root));
                stage.setTitle("Student Dashboard");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void submitAnswers() {
        updateProgressDisplay();
        // Save current answer before submitting
        if (!questions.isEmpty()) {
            Question q = questions.get(currentIndex);
            answersMap.put(q.getId(), BadWordsFilter.filterBadWords(answerTextArea.getText()));
        }
        // Save all answers to DB
        for (Question q : questions) {
            String answer = answersMap.getOrDefault(q.getId(), "");
            if (!answer.trim().isEmpty()) {
                String filtered = BadWordsFilter.filterBadWords(answer);
                boolean hasBadWord = filtered.contains("****");
                Reponse r = new Reponse();
                r.setNote(0);
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
                            String evalName = evaluation.getTitle();

                            System.out.println("Student Email: " + studentEmail);
                            System.out.println("Student Name: " + studentName);

                            String questionTitle = q.getTitle();
                            // Email to teacher
                            String teacherSubject = "[Knowlity] Inappropriate Answer Detected";
                            String teacherHtml = com.esprit.knowlity.Utils.MailUtil.getTeacherHtmlEmail(studentName, evalName, questionTitle, answer);
                            com.esprit.knowlity.Utils.MailUtil.sendHtmlMail(teacherEmail, teacherSubject, teacherHtml);
                            // Email to student
                            String studentSubject = "[Knowlity] Inappropriate Answer Detected";
                            String studentHtml = com.esprit.knowlity.Utils.MailUtil.getStudentHtmlEmail(studentName, evalName, questionTitle, filtered);
                            com.esprit.knowlity.Utils.MailUtil.sendHtmlMail(studentEmail, studentSubject, studentHtml);
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
        // Animate and show review
        FadeTransition ft = new FadeTransition(Duration.millis(500), submitButton);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(evt -> openAwaitCorrectionPage());
        ft.play();
    }


    private void openAwaitCorrectionPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_await_correction.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) answerTextArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Awaiting Correction");
            // Wire up the back button
            EvaluationAwaitCorrectionController controller = loader.getController();
            controller.backToCoursesButton.setOnAction(ev -> {
                try {
                    Parent courseRoot = FXMLLoader.load(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                    stage.setScene(new Scene(courseRoot));
                    stage.setTitle("Course List");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
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
}