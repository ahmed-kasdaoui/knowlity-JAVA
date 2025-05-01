package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.controller.DialogConfirmationController;
import com.esprit.knowlity.controller.student.QuestionFormController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.ServiceCours;

import java.io.IOException;
import java.util.List;

public class EvaluationQuestionsController {

    @FXML private ScrollPane questionScrollPane;
    @FXML private VBox questionCardContainer;
    @FXML private Button addQuestionButton;
    @FXML private Button backButton;

    private VBox emptyStateBox;
    private Evaluation evaluation;
    private final QuestionService questionService = new QuestionService();
    private final ObservableList<Question> questionList = FXCollections.observableArrayList();
    private Runnable onBack;

    // Permet de définir l’évaluation courante
    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        loadQuestions();
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    public void initialize() {
        addQuestionButton.setOnAction(this::handleAddQuestion);
        backButton.setOnAction(e -> handleBackNavigation());
    }

    private void handleBackNavigation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            Parent root = loader.load();
            TeacherController controller = loader.getController();

            ServiceCours coursService = new ServiceCours();
            controller.setCourse(coursService.getCoursById(evaluation.getCoursId()));
            backButton.getScene().setRoot(root);

        } catch (IOException e) {
            System.err.println("Failed to load teacher.fxml: " + e.getMessage());
        }
    }

    private void loadQuestions() {
        if (evaluation == null) return;

        List<Question> questions = questionService.getQuestionsByEvaluationId(evaluation.getId());
        questionCardContainer.getChildren().clear();

        // Nettoyer l’état vide s’il existe
        if (emptyStateBox != null) {
            questionCardContainer.getChildren().remove(emptyStateBox);
        }

        if (questions.isEmpty()) {
            showEmptyState();
        } else {
            for (Question q : questions) {
                VBox card = createQuestionCard(q);
                questionCardContainer.getChildren().add(card);
            }
        }
    }

    private void showEmptyState() {
        emptyStateBox = new VBox(16);
        emptyStateBox.setAlignment(Pos.CENTER);
        emptyStateBox.setPadding(new Insets(60, 0, 0, 0));
        emptyStateBox.setStyle("-fx-background-color: transparent;");

        Label icon = new Label("\u2753"); // Question mark
        icon.setStyle("-fx-font-size: 70px; -fx-text-fill: #43cea2; -fx-effect: dropshadow(gaussian, #185a9d44, 8, 0.2, 0, 2);");

        Label msg = new Label("No questions yet for this evaluation!\nClick 'Add Question' to get started.");
        msg.setStyle("-fx-font-size: 22px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #fff; -fx-background-radius: 16; -fx-padding: 12 24 12 24; -fx-background-color: rgba(67,206,162,0.28);");

        emptyStateBox.getChildren().addAll(icon, msg);
        questionCardContainer.getChildren().add(emptyStateBox);

        // Animation
        emptyStateBox.setOpacity(0);
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(650), emptyStateBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private VBox createQuestionCard(Question q) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #185a9d33, 10, 0.18, 0, 2);");
        card.setMaxWidth(770);

        Label title = new Label(q.getTitle());
        title.setStyle("-fx-font-size: 20px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #222;");

        Label enonce = new Label(q.getEnonce());
        enonce.setStyle("-fx-font-size: 15px; -fx-text-fill: #185a9d;");

        HBox meta = new HBox(16);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().addAll(
                createMetaLabel("Point: " + q.getPoint()),
                createMetaLabel("Order: " + q.getOrdreQuestion()),
                createMetaLabel("Language: " + (q.getProgrammingLanguage() != null ? q.getProgrammingLanguage() : "N/A")),
                createMetaLabel(q.isHasMathFormula() ? "Math Formula" : "No Math Formula", "#43cea2")
        );

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = createStyledButton("Edit", "#43cea2");
        Button delBtn = createStyledButton("Delete", "#ff512f");

        editBtn.setOnAction(e -> handleEditQuestion(q));
        delBtn.setOnAction(e -> handleDeleteQuestion(q));

        actions.getChildren().addAll(editBtn, delBtn);

        card.getChildren().addAll(title, enonce, meta, actions);

        card.setOnMouseEntered(ev -> card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, #185a9d77, 16, 0.32, 0, 4); -fx-translate-y: -2;"));
        card.setOnMouseExited(ev -> card.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #185a9d33, 10, 0.18, 0, 2);"));

        return card;
    }

    private Label createMetaLabel(String text) {
        return createMetaLabel(text, "#444");
    }

    private Label createMetaLabel(String text, String color) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + color + ";");
        return label;
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-radius: 12; -fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Segoe UI Semibold';");
        return btn;
    }

    private void handleAddQuestion(ActionEvent event) {
        openQuestionForm(null);
    }

    private void handleEditQuestion(Question question) {
        openQuestionForm(question);
    }

    private void handleDeleteQuestion(Question question) {
        DialogConfirmationController.showDialog(
                "Delete Question",
                "Are you sure you want to delete this question? This action cannot be undone.",
                confirmed -> {
                    if (confirmed) {
                        questionService.deleteQuestion(question.getId());
                        loadQuestions();
                    }
                }
        );
    }

    private void openQuestionForm(Question question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/question_form.fxml"));
            Parent root = loader.load();
            QuestionFormController controller = loader.getController();
            controller.setEvaluation(evaluation);
            if (question != null) controller.setQuestion(question);

            controller.setOnSave(this::loadQuestions);
            backButton.getScene().setRoot(root);


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
