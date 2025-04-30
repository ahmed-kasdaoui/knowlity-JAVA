package com.esprit.knowlity.controller.teacher;

import javafx.scene.control.*;
import tn.esprit.services.ServiceCours;
import com.esprit.knowlity.Service.EvaluationService;
import tn.esprit.models.Cours;
import com.esprit.knowlity.Model.Evaluation;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class TeacherController {
    @FXML private ListView<Evaluation> evaluationListView;
    @FXML private Button addEvaluationButton;
    @FXML private Button backButton;
    @FXML private javafx.scene.control.TextField searchField;
    private List<Evaluation> allEvaluationsCache = null;
    private EvaluationService evaluationService = new EvaluationService();
    private ServiceCours coursService = new ServiceCours();
    private Cours cours; // variable cours reçue

    public void setCourse(Cours cours) {
        this.cours = cours;
        initialize();
    }

    @FXML
    public void initialize() {
        loadEvaluations();
        addEvaluationButton.setOnAction(e -> openAddEvaluationForm());
        backButton.setOnAction(e -> goBack());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterEvaluations(newVal);
            });
        }
    }

    private void loadEvaluations() {
        if (cours == null) {
            return;
        }
        List<Evaluation> evals = evaluationService.getEvaluationsByCoursId(cours.getId()); // Je suppose que tu as cette méthode sinon on ajuste
        allEvaluationsCache = evals;
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

                    Label courseName = new Label("Course: " + cours.getTitle());
                    courseName.setStyle("-fx-font-size: 13px; -fx-text-fill: #888;");

                    HBox actions = new HBox(10);
                    Button editBtn = new Button("Edit");
                    Button deleteBtn = new Button("Delete");
                    Button questionBtn = new Button("Questions");
                    Button correctBtn = new Button("Correct");
                    Button statsBtn = new Button("Statistique");
                    statsBtn.setStyle("-fx-background-radius: 12; -fx-background-color: linear-gradient(to right, #f7971e, #ffd200); -fx-text-fill: white; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, #ffd20088, 4, 0.13, 0, 2);");

                    statsBtn.setOnAction(e -> openStatistics(item));
                    editBtn.setOnAction(e -> openEvaluationForm(item));
                    deleteBtn.setOnAction(e -> confirmDeleteEvaluation(item));
                    questionBtn.setOnAction(e -> openQuestionManagement(item));
                    correctBtn.setOnAction(e -> openCorrection(item));

                    editBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #43cea2; -fx-text-fill: white;");
                    deleteBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #ff512f; -fx-text-fill: white;");
                    questionBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #185a9d; -fx-text-fill: white;");
                    correctBtn.setStyle("-fx-background-radius: 12; -fx-background-color: #dd2476; -fx-text-fill: white;");

                    actions.getChildren().addAll(editBtn, deleteBtn, questionBtn, correctBtn, statsBtn);
                    vbox.getChildren().addAll(title, desc, courseName, actions);
                    setGraphic(vbox);
                }
            }
        });
    }

    private void openStatistics(Evaluation item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/statistics.fxml"));
            Parent root = loader.load();
            StatisticsController controller = loader.getController();
            controller.setEvaluation(item);
            Stage stage = (Stage) evaluationListView.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistics for: " + item.getTitle());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void confirmDeleteEvaluation(Evaluation item) {
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
            TeacherEvaluationFormController controller = loader.getController();

            controller.setCourse(cours);



            searchField.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditChapitre.fxml: " + e.getMessage());
        }
    }

    private void openAddEvaluationForm() {
        openEvaluationForm(null);
    }

    private void filterEvaluations(String query) {
        if (allEvaluationsCache == null) {
            loadEvaluations();
        }
        if (query == null || query.trim().isEmpty()) {
            evaluationListView.getItems().setAll(allEvaluationsCache);
            return;
        }
        String lower = query.toLowerCase();
        evaluationListView.getItems().setAll(
                allEvaluationsCache.stream()
                        .filter(ev -> {
                            boolean matchTitle = ev.getTitle() != null && ev.getTitle().toLowerCase().contains(lower);
                            boolean matchDesc = ev.getDescription() != null && ev.getDescription().toLowerCase().contains(lower);
                            boolean matchCourse = cours != null && cours.getTitle().toLowerCase().contains(lower);
                            return matchTitle || matchDesc || matchCourse;
                        })
                        .toList()
        );
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
