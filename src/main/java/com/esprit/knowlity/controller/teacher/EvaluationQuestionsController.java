package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.controller.student.QuestionFormController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class EvaluationQuestionsController {
    @FXML private ScrollPane questionScrollPane;
    @FXML private VBox questionCardContainer;
    @FXML private Button addQuestionButton;
    @FXML private Button backButton;

    private Evaluation evaluation;
    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionList = FXCollections.observableArrayList();
    private Runnable onBack;

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
        backButton.setOnAction(e -> {
            // Go back to teacher evaluation list
            try {
                Stage stage = (Stage) backButton.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
                stage.setScene(new Scene(root));
                stage.setTitle("Teacher Back Office");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        loadQuestions();
    }

    private void loadQuestions() {
        if (evaluation == null) return;
        List<Question> questions = questionService.getQuestionsByEvaluationId(evaluation.getId());
        questionCardContainer.getChildren().clear();
        for (Question q : questions) {
            VBox card = new VBox(6);
            card.setPadding(new Insets(16, 20, 16, 20));
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #185a9d33, 10, 0.18, 0, 2);");

            card.setMaxWidth(770); // Slightly smaller to fit compact layout
            card.setPrefWidth(Region.USE_COMPUTED_SIZE);

            Label title = new Label(q.getTitle());
            title.setStyle("-fx-font-size: 20px; -fx-font-family: 'Segoe UI Semibold'; -fx-text-fill: #222;");
            Label enonce = new Label(q.getEnonce());
            enonce.setStyle("-fx-font-size: 15px; -fx-text-fill: #185a9d; -fx-padding: 2 0 0 0;");
            HBox meta = new HBox(16);
            meta.setAlignment(Pos.CENTER_LEFT);
            Label point = new Label("Point: " + q.getPoint());
            point.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");
            Label order = new Label("Order: " + q.getOrdreQuestion());
            order.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");
            Label lang = new Label("Language: " + (q.getProgrammingLanguage() != null ? q.getProgrammingLanguage() : "N/A"));
            lang.setStyle("-fx-font-size: 13px; -fx-text-fill: #444;");
            Label math = new Label(q.isHasMathFormula() ? "Math Formula" : "No Math Formula");
            math.setStyle("-fx-font-size: 13px; -fx-text-fill: #43cea2;");
            meta.getChildren().addAll(point, order, lang, math);

            HBox actions = new HBox(12);
            actions.setAlignment(Pos.CENTER_RIGHT);
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #43cea2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Segoe UI Semibold';");
            Button delBtn = new Button("Delete");
            delBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #ff512f; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Segoe UI Semibold';");
            editBtn.setOnAction(e -> handleEditQuestion(q));
            delBtn.setOnAction(e -> handleDeleteQuestion(q));
            actions.getChildren().addAll(editBtn, delBtn);

            card.getChildren().addAll(title, enonce, meta, actions);

            // Smooth hover effect
            card.setOnMouseEntered(ev -> card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, #185a9d77, 16, 0.32, 0, 4); -fx-translate-y: -2;"));
            card.setOnMouseExited(ev -> card.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 18; -fx-effect: dropshadow(gaussian, #185a9d33, 10, 0.18, 0, 2);"));

            questionCardContainer.getChildren().add(card);
        }
    }

    // No longer needed with card-based UI

    private void handleAddQuestion(ActionEvent event) {
        openQuestionForm(null);
    }

    private void handleEditQuestion(Question question) {
        openQuestionForm(question);
    }

    private void handleDeleteQuestion(Question question) {
    com.esprit.knowlity.controller.DialogConfirmationController.showDialog(
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
        controller.setOnSave(() -> {
            // After save, reload questions and return to Manage Questions page
            loadQuestions();
        });
        Stage stage = (Stage) questionCardContainer.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(question == null ? "Add Question" : "Edit Question");
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}
}
