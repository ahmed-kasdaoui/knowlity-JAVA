package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Service.CoursService;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Model.Cours;
import com.esprit.knowlity.Model.Evaluation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.util.List;

public class TeacherController {
    @FXML private ListView<Evaluation> evaluationListView;
    @FXML private Button addEvaluationButton;
    @FXML private Button backButton;

    private EvaluationService evaluationService = new EvaluationService();
    private CoursService coursService = new CoursService();

    @FXML
    public void initialize() {
        loadEvaluations();
        addEvaluationButton.setOnAction(e -> openAddEvaluationForm());
        backButton.setOnAction(e -> goBack());
    }

    private void loadEvaluations() {
        List<Evaluation> evals = evaluationService.getAllEvaluations();
        evaluationListView.getItems().setAll(evals);
        evaluationListView.setCellFactory(lv -> new ListCell<Evaluation>() {
            @Override
            protected void updateItem(Evaluation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    Label title = new Label(item.getTitle());
                    title.setStyle("-fx-font-size: 20px; -fx-font-family: 'Segoe UI Semibold';");
                    Label desc = new Label(item.getDescription());
                    desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
                    Cours course = coursService.getCoursById(item.getCoursId());
                    Label courseName;
                    if (course != null) {
                        courseName = new Label("Course: " + course.getTitle());
                    } else {
                        courseName = new Label("Course: [Not Found]");
                    }
                    courseName.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");
                    HBox actions = new HBox(10);
                    Button editBtn = new Button("Edit");
                    Button deleteBtn = new Button("Delete");
                    Button questionBtn = new Button("Questions");
                    Button correctBtn = new Button("Correct");
                    editBtn.setOnAction(e -> openEvaluationForm(item));
                    deleteBtn.setOnAction(e -> {
                        com.esprit.knowlity.controller.DialogConfirmationController.showDialog(
                            "Delete Evaluation",
                            "Are you sure you want to delete this evaluation? This action cannot be undone.",
                            confirmed -> {
                                if (confirmed) {
                                    evaluationService.deleteEvaluation(item.getId());
                                    loadEvaluations();
                                }
                            }
                        );
                    });
                    questionBtn.setOnAction(e -> openQuestionManagement(item));
                    correctBtn.setOnAction(e -> openCorrection(item));
                    editBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #43cea2; -fx-text-fill: white;");
                    deleteBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #ff512f; -fx-text-fill: white;");
                    questionBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #185a9d; -fx-text-fill: white;");
                    correctBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #dd2476; -fx-text-fill: white;");
                    actions.getChildren().addAll(editBtn, deleteBtn, questionBtn, correctBtn);
                    vbox.getChildren().addAll(title, desc, courseName, actions);
                    setGraphic(vbox);
                }
            }
        });
    }

    private void openQuestionManagement(Evaluation evaluation) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/evaluation_questions.fxml"));
        Parent root = loader.load();
        EvaluationQuestionsController controller = loader.getController();
        controller.setEvaluation(evaluation);
        Stage stage = (Stage) evaluationListView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Questions for: " + evaluation.getTitle());
        controller.setOnBack(() -> loadEvaluations());
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

    private void openCorrection(Evaluation evaluation) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/correction.fxml"));
        Parent root = loader.load();
        CorrectionController controller = loader.getController();
        controller.setEvaluation(evaluation);
        Stage stage = (Stage) evaluationListView.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Correction for: " + evaluation.getTitle());
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

    private void openEvaluationForm(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher_evaluation_form.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) evaluationListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(evaluation == null ? "Add Evaluation" : "Edit Evaluation");
            TeacherEvaluationFormController controller = loader.getController();
            controller.setEditingEvaluation(evaluation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openAddEvaluationForm() {
        openEvaluationForm(null);
    }

    private void goBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/esprit/knowlity/view/home.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Knowlity Home");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
