package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Model.Evaluation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;

import java.util.List;

public class EvaluationSelectController {
    @FXML private ScrollPane evaluationScrollPane;
    @FXML private VBox evaluationCardContainer;
    @FXML private Button backButton;

    private List<Evaluation> evaluations;
    private java.util.function.Consumer<javafx.event.ActionEvent> onBack;
    private EvaluationSelectListener listener;

    public interface EvaluationSelectListener {
        void onEvaluationSelected(Evaluation evaluation, javafx.event.ActionEvent event);
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
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

            // Action button
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
                if (listener != null) listener.onEvaluationSelected(eval, event);
            });
            actions.getChildren().add(passBtn);

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

    public void setOnBack(java.util.function.Consumer<javafx.event.ActionEvent> onBack) {
        this.onBack = onBack;
    }

    public void setListener(EvaluationSelectListener listener) {
        this.listener = listener;
    }

    @FXML
    private void initialize() {
        backButton.setOnAction(e -> {
            if (onBack != null) onBack.accept(e);
            else ((Stage) backButton.getScene().getWindow()).close();
        });
    }
}
