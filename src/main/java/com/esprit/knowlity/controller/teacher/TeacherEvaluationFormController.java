package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Model.Cours;
import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Service.CoursService;
import com.esprit.knowlity.Service.EvaluationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TeacherEvaluationFormController {
    @FXML private Text formTitle;
    @FXML private ComboBox<Cours> courseComboBox;
    @FXML private TextField titleField;
    @FXML private TextField descField;
    @FXML private TextField scoreField;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextField badgeTitleField;
    @FXML private TextField badgeImageField;
    @FXML private TextField badgeThresholdField;
    @FXML private Button saveButton;
    @FXML private Button backButton;

    private EvaluationService evaluationService = new EvaluationService();
    private CoursService coursService = new CoursService();
    private Evaluation editingEvaluation = null;

    public void setEditingEvaluation(Evaluation eval) {
    this.editingEvaluation = eval;
    if (eval != null) {
        courseComboBox.setValue(coursService.getCoursById(eval.getCoursId()));
        titleField.setText(eval.getTitle());
        descField.setText(eval.getDescription());
        scoreField.setText(String.valueOf(eval.getMaxScore()));
        deadlinePicker.setValue(eval.getDeadline().toLocalDateTime().toLocalDate());
        badgeTitleField.setText(eval.getBadgeTitle());
        badgeImageField.setText(eval.getBadgeImage());
        badgeThresholdField.setText(String.valueOf(eval.getBadgeThreshold()));
        formTitle.setText("Edit Evaluation");
    } else {
        formTitle.setText("Add Evaluation");
    }
}

    @FXML
    private void initialize() {
        List<Cours> allCourses = coursService.readAll();
        courseComboBox.setItems(FXCollections.observableArrayList(allCourses));
        courseComboBox.setCellFactory(lv -> new ListCell<Cours>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });
        courseComboBox.setButtonCell(new ListCell<Cours>() {
            @Override
            protected void updateItem(Cours item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });
        saveButton.setOnAction(e -> saveEvaluation());
        backButton.setOnAction(e -> goBack());
    }

    private void saveEvaluation() {
        Cours selectedCourse = courseComboBox.getValue();
        String title = titleField.getText();
        String desc = descField.getText();
        String scoreText = scoreField.getText() != null ? scoreField.getText().trim() : "";
        LocalDate deadlineDate = deadlinePicker.getValue();

        boolean courseEmpty = (selectedCourse == null);
        boolean titleEmpty = (title == null || title.trim().isEmpty());
        boolean descEmpty = (desc == null || desc.trim().isEmpty());
        boolean scoreEmpty = (scoreText.isEmpty());
        boolean scoreInvalid = false;
        int score = 0;
        if (!scoreEmpty) {
            try {
                score = Integer.parseInt(scoreText);
                if (score <= 0) scoreInvalid = true;
            } catch (NumberFormatException e) {
                scoreInvalid = true;
            }
        }
        boolean deadlineEmpty = (deadlineDate == null);

        // Deadline must be after today
        boolean deadlineInvalid = false;
        if (!deadlineEmpty) {
            LocalDate today = LocalDate.now();
            if (!deadlineDate.isAfter(today)) {
                deadlineInvalid = true;
            }
        }

        if (courseEmpty || titleEmpty || descEmpty || scoreEmpty || scoreInvalid || deadlineEmpty) {
            StringBuilder msg = new StringBuilder();
            if (courseEmpty) msg.append("Course, ");
            if (titleEmpty) msg.append("Title, ");
            if (descEmpty) msg.append("Description, ");
            if (scoreEmpty) msg.append("Max Score, ");
            else if (scoreInvalid) msg.append("Max Score (must be a positive number), ");
            if (deadlineEmpty) msg.append("Deadline, ");
            // Remove last comma and space
            if (msg.length() > 2) msg.setLength(msg.length() - 2);
            msg.append(" are required!");
            com.esprit.knowlity.controller.CustomDialogController.showDialog(
                "Validation Error",
                msg.toString(),
                com.esprit.knowlity.controller.CustomDialogController.DialogType.ERROR
            );
            return;
        }
        if (deadlineInvalid) {
            com.esprit.knowlity.controller.CustomDialogController.showDialog(
                "Validation Error",
                "Deadline must be after today's date!",
                com.esprit.knowlity.controller.CustomDialogController.DialogType.ERROR
            );
            return;
        }

        String badgeTitle = badgeTitleField.getText();
        String badgeImage = badgeImageField.getText();
        int badgeThreshold = 0;
        try {
            badgeThreshold = Integer.parseInt(badgeThresholdField.getText());
        } catch (NumberFormatException e) {
            // badgeThreshold remains 0 if not set, but not required
        }
        Timestamp deadline = Timestamp.valueOf(LocalDateTime.of(deadlineDate, java.time.LocalTime.of(23, 59)));

        if (editingEvaluation == null) {
            Evaluation eval = new Evaluation(0, selectedCourse.getId(), title, desc, score, new Timestamp(System.currentTimeMillis()), deadline, badgeThreshold, badgeImage, badgeTitle);
            evaluationService.addEvaluation(eval);
        } else {
            editingEvaluation.setCoursId(selectedCourse.getId());
            editingEvaluation.setTitle(title);
            editingEvaluation.setDescription(desc);
            editingEvaluation.setMaxScore(score);
            editingEvaluation.setDeadline(deadline);
            editingEvaluation.setBadgeTitle(badgeTitle);
            editingEvaluation.setBadgeImage(badgeImage);
            editingEvaluation.setBadgeThreshold(badgeThreshold);
            evaluationService.updateEvaluation(editingEvaluation);
        }
        goBack();
    }

    private void goBack() {
        try {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            stage.setScene(new Scene(root));
            stage.setTitle("Teacher Back Office");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
