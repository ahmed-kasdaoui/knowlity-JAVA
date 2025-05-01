package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Model.Reponse;
import com.esprit.knowlity.Service.ReponseService;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.controller.CustomDialogController;
import javafx.scene.control.ListCell;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.services.ServiceCours;

import java.io.IOException;
import java.util.List;

public class CorrectionController {
    @FXML
    private javafx.scene.control.ToggleButton ascButton;
    @FXML
    private javafx.scene.control.ToggleButton descButton;
    @FXML
    private ListView<Reponse> answerListView;
    @FXML
    private TextArea selectedAnswerText;
    @FXML
    private TextField gradeField;
    @FXML
    private TextArea commentField;
    @FXML
    private Button setGradeButton;
    @FXML
    private Button backButton;

    private ReponseService reponseService = new ReponseService();
    private QuestionService questionService = new QuestionService();
    private Evaluation evaluation;

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        loadAnswers();
    }

    private void loadAnswers() {
        List<Reponse> answers = reponseService.getReponsesByEvaluationId(evaluation.getId());
        // Sort by submitTime
        boolean ascSelected = ascButton != null && ascButton.isSelected();
        boolean descSelected = descButton != null && descButton.isSelected();
        if (ascSelected || descSelected) {
            boolean asc = ascSelected;
            answers.sort((a, b) -> {
                if (a.getSubmitTime() == null && b.getSubmitTime() == null) return 0;
                if (a.getSubmitTime() == null) return 1;
                if (b.getSubmitTime() == null) return -1;
                int cmp = a.getSubmitTime().compareTo(b.getSubmitTime());
                return asc ? cmp : -cmp;
            });
        }

        // Check if there are no answers and set a placeholder if needed
        if (answers.isEmpty()) {
            // Create a custom placeholder node for no responses
            javafx.scene.layout.VBox placeholderBox = new javafx.scene.layout.VBox(10);
            placeholderBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Create an icon using the nodata image
            javafx.scene.image.Image noDataImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/images/nodata.png"));
            javafx.scene.image.ImageView iconImageView = new javafx.scene.image.ImageView(noDataImage);
            iconImageView.setFitWidth(120);
            iconImageView.setFitHeight(120);
            iconImageView.setPreserveRatio(true);
            
            // Create a message label
            javafx.scene.control.Label messageLabel = new javafx.scene.control.Label("Aucune réponse pour le moment");
            messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            
            javafx.scene.control.Label subMessageLabel = new javafx.scene.control.Label("Les étudiants n'ont pas encore soumis de réponses");
            subMessageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
            
            placeholderBox.getChildren().addAll(iconImageView, messageLabel, subMessageLabel);
            
            // Set the placeholder for the ListView
            answerListView.setPlaceholder(placeholderBox);
        }
        
        answerListView.getItems().clear();
        answerListView.getItems().addAll(answers);
        
        // Fade transition for smooth update
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(350), answerListView);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        answerListView.setCellFactory(lv -> new ListCell<Reponse>() {
            @Override
            protected void updateItem(Reponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Question question = questionService.getQuestionById(item.getQuestionId());
                    String questionText = question != null ? question.getEnonce() : "[Question introuvable]";
                    String baseText = "Etudiant N° " + item.getUserId() + " | Q " + questionText;
                    String submitTimeStr = item.getSubmitTime() != null ?
                            new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm").format(item.getSubmitTime()) : "No submit time";
                    String maxNoteStr = question != null ? " | Max: " + question.getPoint() + " pts" : "";
                    String display = baseText + "\nSubmitted: " + submitTimeStr + maxNoteStr;
                    if (item.getText() != null && item.getText().contains("****")) {
                        setText(display + "  [Inappropriate Answer Detected]");
                        setStyle("-fx-background-color: #ffe5e5; -fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                        setTooltip(new javafx.scene.control.Tooltip("This answer was flagged for inappropriate content and automatically graded 0."));
                    } else {
                        setText(display);
                        setStyle("");
                        setTooltip(null);
                    }
                }
            }
        });
        answerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedAnswerText.setText(newVal.getText());
                gradeField.setText(newVal.getNote() != null ? String.valueOf(newVal.getNote()) : "");
                commentField.setText(newVal.getCommentaire() != null ? newVal.getCommentaire() : "");
            } else {
                selectedAnswerText.clear();
                gradeField.clear();
                commentField.clear();
            }
        });
    }

    @FXML
    public void initialize() {
        if (ascButton != null && descButton != null) {
            // No button selected by default
            ascButton.setSelected(false);
            descButton.setSelected(false);
            ascButton.setStyle(ascButton.getStyle().replace("-fx-opacity: 1.0", "-fx-opacity: 0.7").replace("-fx-border-width: 2", "-fx-border-width: 2").replace("-fx-border-color: #fff", "-fx-border-color: #43cea2"));
            descButton.setStyle(descButton.getStyle().replace("-fx-opacity: 1.0", "-fx-opacity: 0.7").replace("-fx-border-width: 2", "-fx-border-width: 2").replace("-fx-border-color: #fff", "-fx-border-color: #185a9d"));

            ascButton.setOnAction(e -> {
                if (ascButton.isSelected()) {
                    descButton.setSelected(false);
                    ascButton.setStyle(ascButton.getStyle().replace("-fx-opacity: 0.7", "-fx-opacity: 1.0").replace("-fx-border-color: #43cea2", "-fx-border-color: #fff"));
                    descButton.setStyle(descButton.getStyle().replace("-fx-opacity: 1.0", "-fx-opacity: 0.7").replace("-fx-border-color: #fff", "-fx-border-color: #185a9d"));
                    loadAnswers();
                } else {
                    ascButton.setStyle(ascButton.getStyle().replace("-fx-opacity: 1.0", "-fx-opacity: 0.7").replace("-fx-border-color: #fff", "-fx-border-color: #43cea2"));
                    loadAnswers();
                }
            });
            descButton.setOnAction(e -> {
                if (descButton.isSelected()) {
                    ascButton.setSelected(false);
                    descButton.setStyle(descButton.getStyle().replace("-fx-opacity: 0.7", "-fx-opacity: 1.0").replace("-fx-border-color: #185a9d", "-fx-border-color: #fff"));
                    ascButton.setStyle(ascButton.getStyle().replace("-fx-opacity: 1.0", "-fx-opacity: 0.7").replace("-fx-border-color: #fff", "-fx-border-color: #43cea2"));
                    loadAnswers();
                } else {
                    descButton.setStyle(descButton.getStyle().replace("-fx-opacity: 1.0", "-fx-opacity: 0.7").replace("-fx-border-color: #fff", "-fx-border-color: #185a9d"));
                    loadAnswers();
                }
            });
        }
        setGradeButton.setOnAction(e -> setGrade());
        backButton.setOnAction(e -> goBack());
    }

    private void setGrade() {
        Reponse selected = answerListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            int note = Integer.parseInt(gradeField.getText());
            String comment = commentField.getText();
            selected.setNote(note);
            selected.setCommentaire(comment);
            selected.setStatus("corrige");
            reponseService.updateReponse(selected);
            CustomDialogController.showDialog(
                    "Succès",
                    "Grade, comment, and status updated!",
                    CustomDialogController.DialogType.SUCCESS
            );
        } catch (NumberFormatException ex) {
            CustomDialogController.showDialog(
                    "Erreur",
                    "Invalid grade value!",
                    CustomDialogController.DialogType.ERROR
            );
        }
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            Parent root = loader.load();
            TeacherController controller = loader.getController();
            ServiceCours coursService = new ServiceCours();

            controller.setCourse(coursService.getCoursById(evaluation.getCoursId()));


            backButton.getScene().setRoot(root);
        } catch (IOException e1) {
            System.err.println("Failed to load EditChapitre.fxml: " + e1.getMessage());
        }
    }
}
