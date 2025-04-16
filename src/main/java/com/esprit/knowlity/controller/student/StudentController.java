package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Service.CoursService;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Model.Cours;
import com.esprit.knowlity.Model.Evaluation;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentController {
    @FXML private ListView<Cours> courseListView;
    @FXML private Button backButton;

    private CoursService coursService = new CoursService();
    private EvaluationService evaluationService = new EvaluationService();

    @FXML
    public void initialize() {
        loadCoursesWithEvaluations();
        backButton.setOnAction(e -> goBack());
    }

    private void loadCoursesWithEvaluations() {
        List<Cours> courses = coursService.readAll();
        courseListView.getItems().clear();

        for (Cours cours : courses) {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByCoursId(cours.getId());
            if (!evaluations.isEmpty()) {
                courseListView.getItems().add(cours);
            }
        }

        courseListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                // Card container with consistent padding and modern styling
                VBox card = new VBox(8);
                card.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 16;" +
                                "-fx-effect: dropshadow(gaussian, rgba(24, 90, 157, 0.1), 10, 0.2, 0, 2);"
                );

                // Course title
                Label title = new Label(item.getTitle());
                title.setStyle(
                        "-fx-font-family: 'Segoe UI Semibold';" +
                                "-fx-font-size: 18px;" +
                                "-fx-text-fill: #1a1a1a;"
                );

                // Course description
                Label description = new Label(item.getDescription());
                description.setWrapText(true);
                description.setStyle(
                        "-fx-font-family: 'Segoe UI';" +
                                "-fx-font-size: 13px;" +
                                "-fx-text-fill: #4a4a4a;" +
                                "-fx-padding: 6 0 0 0;"
                );

                // Evaluation badge
                int evalCount = evaluationService.getEvaluationsByCoursId(item.getId()).size();
                Label badge = new Label(evalCount + (evalCount == 1 ? " Evaluation" : " Evaluations"));
                badge.setStyle(
                        "-fx-background-color: linear-gradient(to right, #ff6b6b, #ff8e53);" +
                                "-fx-background-radius: 16;" +
                                "-fx-text-fill: #ffffff;" +
                                "-fx-font-family: 'Segoe UI Semibold';" +
                                "-fx-font-size: 12px;" +
                                "-fx-padding: 4 12 4 12;"
                );

                HBox badgeBox = new HBox(badge);
                badgeBox.setAlignment(Pos.CENTER_RIGHT);
                badgeBox.setPrefHeight(24);

                card.getChildren().addAll(title, description, badgeBox);
                setGraphic(card);

                // Smooth hover effect
                card.setOnMouseEntered(event -> card.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 16;" +
                                "-fx-effect: dropshadow(gaussian, rgba(24, 90, 157, 0.2), 14, 0.3, 0, 4);" +
                                "-fx-translate-y: -3;"
                ));

                card.setOnMouseExited(event -> card.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 16;" +
                                "-fx-effect: dropshadow(gaussian, rgba(24, 90, 157, 0.1), 10, 0.2, 0, 2);"
                ));
            }
        });

        courseListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && courseListView.getSelectionModel().getSelectedItem() != null) {
                openEvaluationSelection(courseListView.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void openEvaluationSelection(Cours cours) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_select.fxml"));
            Parent evalRoot = loader.load();
            EvaluationSelectController evalCtrl = loader.getController();
            List<Evaluation> evals = evaluationService.getEvaluationsByCoursId(cours.getId());
            evalCtrl.setEvaluations(evals);
            evalCtrl.setOnBack(event -> {
                try {
                    FXMLLoader courseLoader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                    Parent courseRoot = courseLoader.load();
                    Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(courseRoot));
                    stage.setTitle("Course List");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            evalCtrl.setListener((evaluation, event) -> {
                openEvaluationForm(cours, evaluation, event);
            });
            Stage stage = (Stage) courseListView.getScene().getWindow();
            stage.setScene(new Scene(evalRoot));
            stage.setTitle("Select Evaluation for: " + cours.getTitle());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openEvaluationForm(Cours cours, Evaluation evaluation, javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_form.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Evaluation: " + evaluation.getTitle());
            EvaluationFormController controller = loader.getController();
            controller.setCourse(cours);
            controller.setEvaluation(evaluation);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
