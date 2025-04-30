package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Model.Cours;
import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Service.CoursService;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Utils.FacebookPoster;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
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
    @FXML private ComboBox<String> badgeImageComboBox;
    @FXML private TextField badgeThresholdField;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    private tn.esprit.models.Cours cours; // variable cours reçue

    public void setCourse(tn.esprit.models.Cours cours) {
        this.cours = cours;
        initialize();
    }


    private EvaluationService evaluationService = new EvaluationService();
    private CoursService coursService = new CoursService();
    private Evaluation editingEvaluation = null;

    public void setEditingEvaluation(Evaluation eval) {
        this.editingEvaluation = eval;
        if (eval != null) {
            titleField.setText(eval.getTitle());
            descField.setText(eval.getDescription());
            scoreField.setText(String.valueOf(eval.getMaxScore()));
            deadlinePicker.setValue(eval.getDeadline().toLocalDateTime().toLocalDate());
            badgeTitleField.setText(eval.getBadgeTitle());
            badgeImageComboBox.setValue(eval.getBadgeImage());
            badgeThresholdField.setText(String.valueOf(eval.getBadgeThreshold()));
            formTitle.setText("Edit Evaluation");
        } else {
            formTitle.setText("Add Evaluation");
            titleField.clear();
            descField.clear();
            scoreField.clear();
            deadlinePicker.setValue(null);
            badgeTitleField.clear();
            badgeImageComboBox.setValue(null);
            badgeThresholdField.clear();
        }
    }

    @FXML
    private void initialize() {


        // Badge Emoji ComboBox setup
        badgeImageComboBox.setItems(FXCollections.observableArrayList(
                "🎓", "🏅", "🏆", "📜", "💼", "🌟", "🥇", "👨‍🎓", "🔖"
        ));
        badgeImageComboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                setStyle("-fx-font-size: 28px; -fx-alignment: center; -fx-background-color: #fff; -fx-cursor: hand;");
            }
        });
        badgeImageComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
            }
        });
        saveButton.setOnAction(e -> saveEvaluation());
        backButton.setOnAction(e -> goBack());
    }

    private void saveEvaluation() {
        String title = titleField.getText();
        String desc = descField.getText();
        String scoreText = scoreField.getText() != null ? scoreField.getText().trim() : "";
        LocalDate deadlineDate = deadlinePicker.getValue();

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

        if ( titleEmpty || descEmpty || scoreEmpty || scoreInvalid || deadlineEmpty) {
            StringBuilder msg = new StringBuilder();

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
        String badgeImage = badgeImageComboBox.getValue();
        int badgeThreshold = 0;
        try {
            badgeThreshold = Integer.parseInt(badgeThresholdField.getText());
        } catch (NumberFormatException e) {
            // badgeThreshold remains 0 if not set, but not required
        }
        Timestamp deadline = Timestamp.valueOf(LocalDateTime.of(deadlineDate, java.time.LocalTime.of(23, 59)));

        if (editingEvaluation == null) {
            Evaluation eval = new Evaluation(0, cours.getId(), title, desc, score, new Timestamp(System.currentTimeMillis()), deadline, badgeThreshold, badgeImage, badgeTitle);
            evaluationService.addEvaluation(eval);
            // Share on Facebook with deeplink
            String formattedDeadline = deadlineDate != null ? deadlineDate.toString() : "N/A";
            // Build deeplink URL for this evaluation
            // Assume localhost:80 is the base, and the evaluationId is available after addEvaluation (fetch latest by title if needed)
            int evaluationId = evaluationService.getLastInsertedEvaluationIdByTitle(title); // You may need to implement this method
            String deeplink = String.format("knowlity://evaluate?evaluationId=%d", evaluationId);
            String fbMessage = String.format(
                "\uD83C\uDF1F Nouvelle Évaluation Disponible ! \uD83C\uDF1F\n\n" +
                "\uD83D\uDCDA Cours : %s\n" +
                "\uD83D\uDCDD Évaluation : %s\n" +
                "\uD83D\uDCC5 Date limite : %s\n" +
                "\nCliquez ici pour passer l'évaluation : %s\n\n" +
                "Participez dès maintenant et donnez le meilleur de vous-même ! \uD83D\uDCAA✨",
                cours != null ? cours.getTitle() : "-",
                title,
                formattedDeadline,
                deeplink
            );
            boolean fbSuccess = FacebookPoster.postToPage(fbMessage);
            if (!fbSuccess) {
                System.err.println("Failed to share evaluation on Facebook. Check your Page Access Token and Page ID.");
            }
            else
            {
                com.esprit.knowlity.controller.CustomDialogController.showDialog(
                    "Success",
                    "Evaluation added successfully and shared on Facebook!",
                    com.esprit.knowlity.controller.CustomDialogController.DialogType.SUCCESS
                );
            }
        } else {
            editingEvaluation.setCoursId(cours.getId());
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/esprit/knowlity/view/teacher/teacher.fxml"));
            Parent root = loader.load();
            TeacherController controller = loader.getController();

            controller.setCourse(cours);



            formTitle.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Failed to load EditChapitre.fxml: " + e.getMessage());
        }
    }
}
