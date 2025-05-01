package com.esprit.knowlity.controller.student;

import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.controller.CustomDialogController;
import com.esprit.knowlity.controller.teacher.TeacherController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import tn.esprit.services.ServiceCours;

import java.io.IOException;

public class QuestionFormController {
    @FXML private TextField titleField;
    @FXML private TextArea statementArea;
    @FXML private TextArea codeSnippetArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private javafx.scene.control.CheckBox hasMathFormulaCheck;
    @FXML private javafx.scene.control.TextArea mathFormulaArea;
    @FXML private TextField pointField;
    @FXML private TextField orderField;
    @FXML private TextField programmingLanguageField;

    private QuestionService questionService = new QuestionService();
    private Question question;
    private com.esprit.knowlity.Model.Evaluation evaluation;
    private Runnable onSave;

    public void setEvaluation(com.esprit.knowlity.Model.Evaluation evaluation) {
        this.evaluation = evaluation;
    }
    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public void setQuestion(Question question) {
    this.question = question;
    if (question != null) {
        titleField.setText(question.getTitle());
        statementArea.setText(question.getEnonce());
        codeSnippetArea.setText(question.getCodeSnippet());
        // Fill the rest of the fields
        pointField.setText(String.valueOf(question.getPoint()));
        orderField.setText(String.valueOf(question.getOrdreQuestion()));
        programmingLanguageField.setText(question.getProgrammingLanguage() != null ? question.getProgrammingLanguage() : "");
        hasMathFormulaCheck.setSelected(question.isHasMathFormula());
        mathFormulaArea.setText(question.getMathFormula() != null ? question.getMathFormula() : "");
        mathFormulaArea.setDisable(!hasMathFormulaCheck.isSelected());
    }
}
    @FXML
    public void initialize() {
        saveButton.setOnAction(e -> saveQuestion());
        cancelButton.setOnAction(e -> goBack());
        // Enable/disable math formula area based on checkbox
        hasMathFormulaCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            mathFormulaArea.setDisable(!newVal);
        });
        // Set initial state
        mathFormulaArea.setDisable(!hasMathFormulaCheck.isSelected());
    }
    private void saveQuestion() {
    String title = titleField.getText();
    String statement = statementArea.getText();

    // Input validation
    boolean titleEmpty = (title == null || title.trim().isEmpty());
    boolean statementEmpty = (statement == null || statement.trim().isEmpty());
    boolean pointEmpty = (pointField.getText() == null || pointField.getText().trim().isEmpty());
    String pointText = pointField.getText() != null ? pointField.getText().trim() : "";
    boolean pointInvalid = false;
    int points = 0;
    if (!pointEmpty) {
        try {
            points = Integer.parseInt(pointText);
            if (points <= 0) pointInvalid = true;
        } catch (NumberFormatException e) {
            pointInvalid = true;
        }
    }
    if (titleEmpty || statementEmpty || pointEmpty || pointInvalid) {
        String message = null;
        if (titleEmpty && statementEmpty && (pointEmpty || pointInvalid)) {
            message = "Title, Statement and Point are required!";
        } else if (titleEmpty && statementEmpty) {
            message = "Title and Statement are required!";
        } else if (titleEmpty && (pointEmpty || pointInvalid)) {
            message = "Title and Point are required!";
        } else if (statementEmpty && (pointEmpty || pointInvalid)) {
            message = "Statement and Point are required!";
        } else if (titleEmpty) {
            message = "Title is required!";
        } else if (statementEmpty) {
            message = "Statement is required!";
        } else if (pointEmpty) {
            message = "Point is required!";
        } else if (pointInvalid) {
            message = "Point must be a positive number!";
        }
        CustomDialogController.showDialog(
            "Validation Error",
            message,
            CustomDialogController.DialogType.ERROR
        );
        return;
    }

    if (question == null) question = new Question();
    question.setTitle(title);
    question.setEnonce(statement);
    question.setCodeSnippet(codeSnippetArea.getText());
    question.setPoint(points);

    // Set order
    try {
        int order = Integer.parseInt(orderField.getText().trim());
        question.setOrdreQuestion(order);
    } catch (NumberFormatException e) {
        question.setOrdreQuestion(0); // or show validation error
    }

    // Set programming language
    question.setProgrammingLanguage(programmingLanguageField.getText());

    // Set math formula fields
    question.setHasMathFormula(hasMathFormulaCheck.isSelected());
    if (hasMathFormulaCheck.isSelected()) {
        question.setMathFormula(mathFormulaArea.getText());
    } else {
        question.setMathFormula(null);
    }

    if (evaluation != null) {
        question.setEvaluationId(evaluation.getId());
    }
    questionService.saveOrUpdateQuestion(question);
    if (onSave != null) {
        onSave.run();
    }
    goBack();
}
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            Parent root = loader.load();
            TeacherController controller = loader.getController();

            ServiceCours coursService = new ServiceCours();
            controller.setCourse(coursService.getCoursById(evaluation.getCoursId()));
           cancelButton.getScene().setRoot(root);

        } catch (IOException e) {
            System.err.println("Failed to load teacher.fxml: " + e.getMessage());
        }
    }
}
