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
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.List;

public class CorrectionController {
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
        answerListView.getItems().setAll(answers);
        answerListView.setCellFactory(lv -> new ListCell<Reponse>() {
            @Override
            protected void updateItem(Reponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Question question = questionService.getQuestionById(item.getQuestionId());
                    String questionText = question != null ? question.getEnonce() : "[Question introuvable]";
                    setText("User " + item.getUserId() + " | Q " + questionText);
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
            Stage stage = (Stage) backButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Teacher Back Office");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
