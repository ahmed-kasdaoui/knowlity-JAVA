package com.esprit.knowlity.controller.teacher;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Model.Reponse;
import com.esprit.knowlity.Service.EvaluationService;
import com.esprit.knowlity.Service.QuestionService;
import com.esprit.knowlity.Service.ReponseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import tn.esprit.services.ServiceCours;
import tn.knowlity.entity.User;
import tn.knowlity.service.userService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {
    @FXML
    private Label dashboardTitle;
    @FXML
    private Label participantsLabel;
    @FXML
    private Label completionRateLabel;
    @FXML
    private ProgressBar completionRateProgressBar;

    @FXML
    private PieChart completionPieChart;
    @FXML
    private Label topPerformerNameLabel;
    @FXML
    private Label topPerformerScoreLabel;
    @FXML
    private Label bottomPerformerNameLabel;
    @FXML
    private Label bottomPerformerScoreLabel;
    @FXML
    private Label badgeStatsLabel;
    @FXML
    private Label mostMissedQuestionLabel;
    @FXML
    private ProgressBar badgeProgressBar;
    @FXML
    private Button backButton;

    private Evaluation evaluation;
    private final ReponseService reponseService = new ReponseService();
    private final QuestionService questionService = new QuestionService();
    private final ServiceCours coursService = new ServiceCours();
    private final EvaluationService evaluationService = new EvaluationService();
    private final userService userService = new userService();
    @FXML
    private javafx.scene.control.ListView<String> rankingListView;

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        dashboardTitle.setText("Statistics for: " + evaluation.getTitle());
        loadStats();
    }

    @FXML
    private void initialize() {
        backButton.setOnAction(e -> goBack());
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

    private void loadStats() {
        // 1. Gather data
        List<Reponse> allReponses = reponseService.getReponsesByEvaluationId(evaluation.getId());
        List<Question> allQuestions = questionService.getQuestionsByEvaluationId(evaluation.getId());
        int maxScore = evaluation.getMaxScore();

        // --- Participants ---
        Set<Integer> participantIds = allReponses.stream().map(Reponse::getUserId).collect(Collectors.toSet());
        int participants = participantIds.size();
        participantsLabel.setText(String.valueOf(participants));

        // --- Scores ---
        Map<Integer, Integer> userTotalScores = new HashMap<>();
        for (Reponse r : allReponses) {
            if (r.getNote() != null && r.getUserId() != null) {
                userTotalScores.put(r.getUserId(), userTotalScores.getOrDefault(r.getUserId(), 0) + r.getNote());
            }
        }
        List<Integer> totalScores = new ArrayList<>(userTotalScores.values());

        // --- Score Distribution (by % Ranges) ---
        int[] ranges = {0, 50, 70, 90, 101}; // 0-49, 50-69, 70-89, 90-100
        String[] rangeLabels = {"0-49%", "50-69%", "70-89%", "90-100%"};
        int[] rangeCounts = new int[4];
        for (int score : totalScores) {
            double pct = maxScore > 0 ? (score * 100.0 / maxScore) : 0;
            for (int i = 0; i < 4; i++) {
                if (pct >= ranges[i] && pct < ranges[i+1]) {
                    rangeCounts[i]++;
                    break;
                }
            }
        }

        // --- Classement des étudiants ---
        List<Map.Entry<Integer, Integer>> ranking = userTotalScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        List<String> rankingDisplay = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : ranking) {
            try {
                User user = userService.recherparid(entry.getKey());
                if (user != null) {
                    String display = String.format("%d. %s %s | %s | %d pts",
                            rank, user.getNom(), user.getPrenom(), user.getEmail(), entry.getValue());
                    rankingDisplay.add(display);
                } else {
                    rankingDisplay.add(rank + ". Unknown User | - | " + entry.getValue() + " pts");
                }
            } catch (SQLException e) {
                System.err.println("Error fetching user details for ID " + entry.getKey() + ": " + e.getMessage());
                rankingDisplay.add(rank + ". Unknown User | - | " + entry.getValue() + " pts");
            }
            rank++;
        }
        if (rankingListView != null) {
            rankingListView.getItems().setAll(rankingDisplay);
        }

        // --- Completion Rate ---
        int totalQuestions = allQuestions.size();
        int completed = 0;
        for (Integer uid : participantIds) {
            long graded = allQuestions.stream()
                    .filter(q -> allReponses.stream()
                            .anyMatch(r -> r.getUserId() != null && r.getUserId().equals(uid)
                                    && r.getQuestionId() == q.getId()
                                    && r.getNote() != null))
                    .count();
            if (graded == totalQuestions) completed++;
        }
        int incomplete = participants - completed;
        double completionRate = participants > 0 ? (completed * 100.0 / participants) : 0;
        completionRateLabel.setText(String.format("%d%%", Math.round(completionRate)));
        completionRateProgressBar.setProgress(Math.max(0, Math.min(1, completionRate / 100.0)));
        completionPieChart.getData().clear();
        PieChart.Data completedData = new PieChart.Data("Completed", completed);
        PieChart.Data incompleteData = new PieChart.Data("Incomplete", incomplete);
        completionPieChart.getData().add(completedData);
        completionPieChart.getData().add(incompleteData);

        // Add tooltips with percentage
        int total = completed + incomplete;
        if (total > 0) {
            double completedPct = (completed * 100.0) / total;
            double incompletePct = (incomplete * 100.0) / total;
            javafx.scene.control.Tooltip.install(completedData.getNode(), new javafx.scene.control.Tooltip(String.format("%.1f%% Completed", completedPct)));
            javafx.scene.control.Tooltip.install(incompleteData.getNode(), new javafx.scene.control.Tooltip(String.format("%.1f%% Incomplete", incompletePct)));
        }

        // --- Badges Earned ---
        Integer badgeThreshold = evaluation.getBadgeThreshold();
        if (badgeThreshold != null && badgeThreshold > 0 && participants > 0) {
            long badgeEarners = userTotalScores.values().stream().filter(score -> score >= badgeThreshold).count();
            double badgePct = (badgeEarners * 100.0) / participants;
            badgeStatsLabel.setText(String.format("%d/%d (%.0f%%)", badgeEarners, participants, badgePct));
            badgeProgressBar.setProgress(Math.max(0, Math.min(1, badgePct / 100.0)));
        } else {
            badgeStatsLabel.setText("-");
            badgeProgressBar.setProgress(0);
        }

        // --- Most Missed Question ---
        String mostMissed = "-";
        int maxMissed = 0;
        for (Question q : allQuestions) {
            long missed = allReponses.stream()
                    .filter(r -> r.getQuestionId() == q.getId() && r.getNote() != null && r.getNote() < q.getPoint())
                    .count();
            if (missed > maxMissed) {
                maxMissed = (int) missed;
                mostMissed = q.getTitle() != null && !q.getTitle().isEmpty() ? q.getTitle() : (q.getEnonce().length() > 40 ? q.getEnonce().substring(0, 40) + "..." : q.getEnonce());
            }
        }
        if (maxMissed == 0) {
            mostMissed = "None";
        }
        mostMissedQuestionLabel.setText(mostMissed);

        // --- Top/Bottom Performers ---
        List<Map.Entry<Integer, Integer>> sorted = userTotalScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()).collect(Collectors.toList());
        if (!sorted.isEmpty()) {
            int topId = sorted.get(0).getKey();
            int topScore = sorted.get(0).getValue();
            double topPct = maxScore > 0 ? (topScore * 100.0 / maxScore) : 0;
            try {
                User topUser = userService.recherparid(topId);
                if (topUser != null) {
                    topPerformerNameLabel.setText(topUser.getNom() + " " + topUser.getPrenom());
                } else {
                    topPerformerNameLabel.setText("Unknown User");
                }
            } catch (SQLException e) {
                System.err.println("Error fetching top performer details for ID " + topId + ": " + e.getMessage());
                topPerformerNameLabel.setText("Unknown User");
            }
            topPerformerScoreLabel.setText(String.format("%.1f%%", topPct));

            int bottomId = sorted.get(sorted.size() - 1).getKey();
            int bottomScore = sorted.get(sorted.size() - 1).getValue();
            double bottomPct = maxScore > 0 ? (bottomScore * 100.0 / maxScore) : 0;
            try {
                User bottomUser = userService.recherparid(bottomId);
                if (bottomUser != null) {
                    bottomPerformerNameLabel.setText(bottomUser.getNom() + " " + bottomUser.getPrenom());
                } else {
                    bottomPerformerNameLabel.setText("Unknown User");
                }
            } catch (SQLException e) {
                System.err.println("Error fetching bottom performer details for ID " + bottomId + ": " + e.getMessage());
                bottomPerformerNameLabel.setText("Unknown User");
            }
            bottomPerformerScoreLabel.setText(String.format("%.1f%%", bottomPct));
        } else {
            topPerformerNameLabel.setText("-");
            topPerformerScoreLabel.setText("-");
            bottomPerformerNameLabel.setText("-");
            bottomPerformerScoreLabel.setText("-");
        }
    }
}