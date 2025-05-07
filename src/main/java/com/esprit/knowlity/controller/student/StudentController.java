package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Model.Evaluation;
import controllers.CourseDetailsControllerEtudiant;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.models.Cours;
import tn.esprit.services.ServiceCours;

import java.io.IOException;
import java.util.List;

public class StudentController {
    @FXML private ListView<Cours> courseListView;
    @FXML private Button backButton;
    private Cours cours;

    private ServiceCours coursService = new ServiceCours();
    private EvaluationService evaluationService = new EvaluationService();

    @FXML
    public void initialize() {
        loadCoursesWithEvaluations();
        backButton.setOnAction(e -> goBack());
    }

    private void loadCoursesWithEvaluations() {
        List<Cours> courses = coursService.getAll();
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
            evalCtrl.setCourse(cours);
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
            evalCtrl.setOnBack(event -> {
                try {
                    FXMLLoader studentLoader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                    Parent studentRoot = studentLoader.load();
                    backButton.getScene().setRoot(studentRoot);
                } catch (IOException e) {
                    System.err.println("Failed to load student.fxml: " + e.getMessage());
                }
            });
            backButton.getScene().setRoot(evalRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetailsEtudiant.fxml"));
            Parent root = loader.load();
            CourseDetailsControllerEtudiant controller = loader.getController();
            controller.setCourse(cours);
            backButton.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load CourseDetails.fxml: " + e.getMessage());
        }
    }

    public void setCourse(Cours course) {
         cours = course;
        courseListView.getItems().clear();
        List<Evaluation> evaluations = evaluationService.getEvaluationsByCoursId(course.getId());
        if (!evaluations.isEmpty()) {
            courseListView.getItems().add(course);
        }
    }}