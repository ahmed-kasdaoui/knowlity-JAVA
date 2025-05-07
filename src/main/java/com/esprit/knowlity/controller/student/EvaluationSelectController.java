package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Model.Evaluation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import tn.knowlity.entity.User;
import tn.knowlity.tools.UserSessionManager;
import tn.esprit.models.Cours;
import java.io.IOException;
import java.util.List;

public class EvaluationSelectController {
    @FXML private ScrollPane evaluationScrollPane;
    @FXML private VBox evaluationCardContainer;
    @FXML private Button backButton;
    private Cours course; // Store the course for back navigation
    private java.util.function.Consumer<javafx.event.ActionEvent> onBack;
    private User user = UserSessionManager.getInstance().getCurrentUser();
    private final int DEFAULT_USER_ID = user.getId();

    public void setEvaluations(List<Evaluation> evaluations) {
        evaluationCardContainer.getChildren().clear();

        for (Evaluation eval : evaluations) {
            // Card container with reduced spacing
            VBox card = new VBox(6);
            card.setPadding(new Insets(12));
            card.setAlignment(Pos.TOP_LEFT);
            card.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #e0e4e8;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 8, 0.1, 0, 3);"
            );
            card.setMaxWidth(770); // Slightly smaller to fit compact layout
            card.setPrefWidth(Region.USE_COMPUTED_SIZE);

            // Evaluation title
            Label name = new Label(eval.getTitle());
            name.setStyle(
                    "-fx-font-family: 'System';" +
                            "-fx-font-size: 15px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #1f2a44;"
            );

            // Deadline
            Label deadline = new Label(eval.getDeadline() != null ? "Due: " + eval.getDeadline().toString() : "No deadline");
            deadline.setStyle(
                    "-fx-font-family: 'System';" +
                            "-fx-font-size: 12px;" +
                            "-fx-text-fill: #6b7280;" +
                            "-fx-padding: 3 0 0 0;"
            );

            // Badge
            Label badge = new Label(eval.getBadgeTitle() != null ? eval.getBadgeTitle() : "");
            badge.setStyle(
                    "-fx-font-family: 'System';" +
                            "-fx-font-size: 11px;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-background-color: #10b981;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 2 8 2 8;" +
                            "-fx-alignment: center;"
            );
            badge.setVisible(eval.getBadgeTitle() != null && !eval.getBadgeTitle().isEmpty());

            // Action buttons
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);
            Button passBtn = new Button("Start");
            passBtn.setStyle(
                    "-fx-background-color: #3b82f6;" +
                            "-fx-background-radius: 6;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-font-family: 'System';" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 5 12 5 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 1);"
            );
            passBtn.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_form.fxml"));
                    Parent root = loader.load();
                    EvaluationFormController controller = loader.getController();
                    controller.setEvaluation(eval);
                    // Remplacez le contenu de la scène actuelle
                    evaluationCardContainer.getScene().setRoot(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Only show the Result button if there are results for this evaluation and user
            com.esprit.knowlity.Service.ReponseService reponseService = new com.esprit.knowlity.Service.ReponseService();
            boolean hasResults = !reponseService.getReponsesByEvaluationIdAndUserId(eval.getId(), DEFAULT_USER_ID).isEmpty();
            if (hasResults) {
                Button resultBtn = new Button("Result");
                resultBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #43cea2, #185a9d);" +
                                "-fx-background-radius: 6;" +
                                "-fx-text-fill: #fff;" +
                                "-fx-font-family: 'Segoe UI Semibold';" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5 12 5 12;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, #43cea2, 4, 0.14, 0, 2);"
                );
                resultBtn.setOnAction(event -> {
                    try {
                        // Check if evaluation notes are null or empty
                        com.esprit.knowlity.Service.EvaluationService evaluationService = new com.esprit.knowlity.Service.EvaluationService();
                        List<String> evaluationNotes = evaluationService.getEvaluationNotes(eval.getId(), DEFAULT_USER_ID);
                        
                        System.out.println("Evaluation ID: " + eval.getId());
                        System.out.println("User ID: " + DEFAULT_USER_ID);
                        System.out.println("Evaluation Notes: " + evaluationNotes);
                        
                        if (evaluationNotes == null || evaluationNotes.isEmpty()) {
                            System.out.println("Redirecting to await correction view");
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/evaluation_await_correction.fxml"));
                            Parent root = loader.load();
                            EvaluationAwaitCorrectionController controller = loader.getController();
                            controller.setCourse(course);
                            evaluationCardContainer.getScene().setRoot(root);
                        } else {
                            System.out.println("Showing result evaluation");
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/ResultEvaluation.fxml"));
                            Parent root = loader.load();
                            ResultEvaluationController controller = loader.getController();
                            controller.setEvaluation(eval);
                            // Remplacez le contenu de la scène actuelle
                            evaluationCardContainer.getScene().setRoot(root);
                        }
                    } catch (IOException e) {
                        System.err.println("Error processing evaluation result: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                actions.getChildren().addAll(passBtn, resultBtn);
            } else {
                actions.getChildren().add(passBtn);
            }

            // Reduced spacing between badge and actions
            VBox.setMargin(actions, new Insets(8, 0, 0, 0));
            card.getChildren().addAll(name, deadline, badge, actions);

            // Smooth hover effect with reduced shadow
            card.setOnMouseEntered(event -> card.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #e0e4e8;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 12, 0.15, 0, 4);" +
                            "-fx-translate-y: -2;"
            ));
            card.setOnMouseExited(event -> card.setStyle(
                    "-fx-background-color: #ffffff;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #e0e4e8;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 8, 0.1, 0, 3);"
            ));

            // Add card with reduced margin to prevent crowding
            evaluationCardContainer.getChildren().add(card);
            VBox.setMargin(card, new Insets(6, 0, 6, 0));
        }
    }

    public void setCourse(Cours course) {
        this.course = course;
    }

    public void setOnBack(java.util.function.Consumer<javafx.event.ActionEvent> onBack) {
        this.onBack = event -> {
            try {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                Parent root = loader.load();
                StudentController controller = loader.getController();
                controller.setCourse(course);
                evaluationCardContainer.getScene().setRoot(root);
            } catch (IOException e) {
                System.err.println("Failed to load student.fxml: " + e.getMessage());
            }
        };
    }



    @FXML
    private void initialize() {
        backButton.setOnAction(e -> {
            if (onBack != null) {
                onBack.accept(e);
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/student/student.fxml"));
                    Parent root = loader.load();
                    StudentController controller = loader.getController();
                    controller.setCourse(course);
                    evaluationCardContainer.getScene().setRoot(root);
                } catch (IOException ioException) {
                    System.err.println("Failed to load student.fxml: " + ioException.getMessage());
                    backButton.getScene().getWindow().hide();
                }
            }
        });
    }
}