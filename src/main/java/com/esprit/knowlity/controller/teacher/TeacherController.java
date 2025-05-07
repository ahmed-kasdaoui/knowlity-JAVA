package com.esprit.knowlity.controller.teacher;

import controllers.CourseDetailsController;
import javafx.scene.control.*;
import tn.esprit.services.ServiceCours;
import com.esprit.knowlity.Service.EvaluationService;
import tn.esprit.models.Cours;
import com.esprit.knowlity.Model.Evaluation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.List;

public class TeacherController {
    @FXML private ListView<Evaluation> evaluationListView;
    @FXML private Button addEvaluationButton;
    @FXML private Button backButton;
    @FXML private TextField searchField;

    private List<Evaluation> allEvaluationsCache = null;
    private final EvaluationService evaluationService = new EvaluationService();
    private final ServiceCours coursService = new ServiceCours();
    private Cours cours;

    public void setCourse(Cours cours) {
        this.cours = cours;
        initialize();
    }

    @FXML
    public void initialize() {
        if (addEvaluationButton != null) {
            styleButton(addEvaluationButton, "#4CAF50", "white");
            addEvaluationButton.setOnAction(e -> openAddEvaluationForm());
        }

        if (backButton != null) {
            styleButton(backButton, "#607D8B", "white");
            backButton.setOnAction(e -> goBack());
        }

        if (searchField != null) {
            searchField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #ccc; -fx-padding: 8 16;");
            searchField.setPromptText("Rechercher des évaluations...");

            DropShadow shadow = new DropShadow();
            shadow.setRadius(4.0);
            shadow.setOffsetX(0.0);
            shadow.setOffsetY(2.0);
            shadow.setColor(Color.rgb(0, 0, 0, 0.2));
            searchField.setEffect(shadow);

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterEvaluations(newVal);
            });
        }

        loadEvaluations();
    }

    private void styleButton(Button button, String bgColor, String textColor) {
        button.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 10 20;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setRadius(5.0);
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        button.setEffect(shadow);
    }

    private void loadEvaluations() {
        if (cours == null) return;

        List<Evaluation> evals = evaluationService.getEvaluationsByCoursId(cours.getId());
        allEvaluationsCache = evals;

        if (evaluationListView != null) {
            evaluationListView.getItems().clear();
            evaluationListView.getItems().addAll(evals);
            evaluationListView.setStyle("-fx-background-color: transparent;");

            // Configuration du cell factory pour personnaliser l'apparence des cellules
            evaluationListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Evaluation item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        // Création de la carte
                        VBox card = new VBox(10);
                        card.setPadding(new Insets(15));
                        card.setStyle(
                                "-fx-background-color: white;" +
                                        "-fx-background-radius: 15;" +
                                        "-fx-border-radius: 15;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
                        );

                        // Ajouter un effet au survol
                        card.setOnMouseEntered(e -> {
                            card.setStyle(
                                    "-fx-background-color: #f8f9fa;" +
                                            "-fx-background-radius: 15;" +
                                            "-fx-border-radius: 15;" +
                                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);"
                            );

                            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                            st.setToX(1.01);
                            st.setToY(1.01);
                            st.play();
                        });

                        card.setOnMouseExited(e -> {
                            card.setStyle(
                                    "-fx-background-color: white;" +
                                            "-fx-background-radius: 15;" +
                                            "-fx-border-radius: 15;" +
                                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"
                            );

                            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                            st.setToX(1.0);
                            st.setToY(1.0);
                            st.play();
                        });

                        // En-tête avec badge et titre
                        HBox header = new HBox(10);
                        header.setAlignment(Pos.CENTER_LEFT);

                        Label badge = new Label("EVAL");
                        badge.setStyle(
                                "-fx-background-color: linear-gradient(to right, #3494e6, #ec6ead);" +
                                        "-fx-text-fill: white;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-padding: 5 10;" +
                                        "-fx-background-radius: 15;"
                        );

                        Label title = new Label(item.getTitle());
                        title.setStyle(
                                "-fx-font-size: 18px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-text-fill: #2C3E50;"
                        );
                        title.setMaxWidth(Double.MAX_VALUE);
                        title.setWrapText(true);

                        header.getChildren().addAll(badge, title);

                        // Séparateur
                        Region separator = new Region();
                        separator.setPrefHeight(2);
                        separator.setMaxWidth(Double.MAX_VALUE);
                        separator.setStyle("-fx-background-color: linear-gradient(to right, #3494e6, #ec6ead);");

                        // Description
                        Label desc = new Label(item.getDescription());
                        desc.setStyle(
                                "-fx-font-size: 14px;" +
                                        "-fx-text-fill: #555;"
                        );
                        desc.setWrapText(true);

                        // Nom du cours
                        Label courseName = new Label("Cours: " + cours.getTitle());
                        courseName.setStyle(
                                "-fx-font-size: 13px;" +
                                        "-fx-text-fill: #888;" +
                                        "-fx-font-style: italic;"
                        );
                        courseName.setWrapText(true);

                        // Container pour les boutons
                        HBox actions = new HBox(8);
                        actions.setAlignment(Pos.CENTER_RIGHT);
                        actions.setPadding(new Insets(10, 0, 0, 0));

                        // Boutons d'action
                        Button editBtn = createActionButton("Modifier", "#43cea2");
                        Button deleteBtn = createActionButton("Supprimer", "#ff512f");
                        Button questionBtn = createActionButton("Questions", "#185a9d");
                        Button correctBtn = createActionButton("Corriger", "#dd2476");
                        Button statsBtn = createActionButton("Stats", "#f7971e");

                        // Définir les actions des boutons
                        statsBtn.setOnAction(e -> openStatistics(item));
                        editBtn.setOnAction(e -> openEditEvaluationForm(item));
                        deleteBtn.setOnAction(e -> confirmDeleteEvaluation(item));
                        questionBtn.setOnAction(e -> openQuestionManagement(item));
                        correctBtn.setOnAction(e -> openCorrection(item));

                        actions.getChildren().addAll(editBtn, deleteBtn, questionBtn, correctBtn, statsBtn);

                        // Ajouter tous les éléments à la carte
                        card.getChildren().addAll(header, separator, desc, courseName, actions);

                        // Animation d'entrée
                        FadeTransition ft = new FadeTransition(Duration.millis(500), card);
                        ft.setFromValue(0);
                        ft.setToValue(1);
                        ft.setDelay(Duration.millis(getIndex() * 100)); // Effet cascade
                        ft.play();

                        setGraphic(card);

                        // Définir un espacement entre les cartes
                        setPadding(new Insets(5));
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            });
        }
    }

    private Button createActionButton(String text, String baseColor) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + baseColor + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 10;"
        );

        // Effet d'ombre légère
        DropShadow shadow = new DropShadow();
        shadow.setRadius(3.0);
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(2.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        button.setEffect(shadow);

        // Animation au survol
        button.setOnMouseEntered(e -> {
            String lighterColor = "derive(" + baseColor + ", 20%)";
            button.setStyle(
                    "-fx-background-color: " + lighterColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6 10;"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                    "-fx-background-color: " + baseColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6 10;"
            );

            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        return button;
    }

    private void openStatistics(Evaluation item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/statistics.fxml"));
            Parent root = loader.load();
            StatisticsController controller = loader.getController();
            controller.setEvaluation(item);
            evaluationListView.getScene().setRoot(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void confirmDeleteEvaluation(Evaluation item) {
        com.esprit.knowlity.controller.DialogConfirmationController.showDialog(
                "Supprimer l'évaluation",
                "Êtes-vous sûr de vouloir supprimer cette évaluation ? Cette action ne peut pas être annulée.",
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
            controller.setOnBack(this::loadEvaluations);
            evaluationListView.getScene().setRoot(root);
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
            evaluationListView.getScene().setRoot(root);
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
            if (evaluation != null) {
                controller.setEditingEvaluation(evaluation);
            }
            searchField.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load teacher_evaluation_form.fxml: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CourseDetails.fxml"));
            Parent root = loader.load();
            CourseDetailsController controller = loader.getController();
            controller.setCourse(cours);
            backButton.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load CourseDetails.fxml: " + e.getMessage());
        }
    }

    private void openEditEvaluationForm(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher_evaluation_form.fxml"));
            Parent root = loader.load();
            TeacherEvaluationFormController controller = loader.getController();
            controller.setCourse(cours);
            controller.setEditingEvaluation(evaluation);
            searchField.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load teacher_evaluation_form.fxml: " + e.getMessage());
        }
    }
}
