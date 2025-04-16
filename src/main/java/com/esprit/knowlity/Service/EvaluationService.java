package com.esprit.knowlity.Service;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationService {
    private Connection conn;

    public EvaluationService() {
        conn = DataSource.getInstance().getCnx();
    }

    public List<Evaluation> getEvaluationsByCoursId(int coursId) {
        List<Evaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM evaluation WHERE cours_id = ? ORDER BY id DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, coursId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Evaluation e = new Evaluation(
                        rs.getInt("id"),
                        rs.getInt("cours_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("max_score"),
                        rs.getTimestamp("create_at"),
                        rs.getTimestamp("deadline"),
                        rs.getInt("badge_threshold"),
                        rs.getString("badge_image"),
                        rs.getString("badge_title")
                );
                evaluations.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluations;
    }

    public void addEvaluation(Evaluation evaluation) {
        String query = "INSERT INTO evaluation (cours_id, title, description, max_score, create_at, deadline, badge_threshold, badge_image, badge_title) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, evaluation.getCoursId());
            pstmt.setString(2, evaluation.getTitle());
            pstmt.setString(3, evaluation.getDescription());
            pstmt.setInt(4, evaluation.getMaxScore());
            pstmt.setTimestamp(5, evaluation.getCreateAt());
            pstmt.setTimestamp(6, evaluation.getDeadline());
            pstmt.setObject(7, evaluation.getBadgeThreshold(), Types.INTEGER);
            pstmt.setString(8, evaluation.getBadgeImage());
            pstmt.setString(9, evaluation.getBadgeTitle());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEvaluation(int evaluationId) {
        // 1. Delete all reponse records linked directly to this evaluation
        String deleteReponses = "DELETE FROM reponse WHERE evaluation_id = ?";
        // 2. Delete all reponse records linked to questions of this evaluation
        String selectQuestions = "SELECT id FROM question WHERE evaluation_id = ?";
        String deleteReponsesByQuestion = "DELETE FROM reponse WHERE question_id = ?";
        // 3. Delete all questions linked to this evaluation
        String deleteQuestions = "DELETE FROM question WHERE evaluation_id = ?";
        // 4. Delete the evaluation itself
        String deleteEvaluation = "DELETE FROM evaluation WHERE id = ?";
        try {
            // Delete all reponse records linked directly to this evaluation
            try (PreparedStatement pstmt = conn.prepareStatement(deleteReponses)) {
                pstmt.setInt(1, evaluationId);
                pstmt.executeUpdate();
            }
            // Delete all reponse records linked to questions of this evaluation
            try (PreparedStatement pstmtQ = conn.prepareStatement(selectQuestions)) {
                pstmtQ.setInt(1, evaluationId);
                ResultSet rs = pstmtQ.executeQuery();
                while (rs.next()) {
                    int questionId = rs.getInt("id");
                    try (PreparedStatement pstmtR = conn.prepareStatement(deleteReponsesByQuestion)) {
                        pstmtR.setInt(1, questionId);
                        pstmtR.executeUpdate();
                    }
                }
            }
            // Delete all questions linked to this evaluation
            try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestions)) {
                pstmt.setInt(1, evaluationId);
                pstmt.executeUpdate();
            }
            // Delete the evaluation itself
            try (PreparedStatement pstmt = conn.prepareStatement(deleteEvaluation)) {
                pstmt.setInt(1, evaluationId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateEvaluation(Evaluation evaluation) {
        String query = "UPDATE evaluation SET cours_id = ?, title = ?, description = ?, max_score = ?, create_at = ?, deadline = ?, badge_threshold = ?, badge_image = ?, badge_title = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, evaluation.getCoursId());
            pstmt.setString(2, evaluation.getTitle());
            pstmt.setString(3, evaluation.getDescription());
            pstmt.setInt(4, evaluation.getMaxScore());
            pstmt.setTimestamp(5, evaluation.getCreateAt());
            pstmt.setTimestamp(6, evaluation.getDeadline());
            pstmt.setObject(7, evaluation.getBadgeThreshold(), Types.INTEGER);
            pstmt.setString(8, evaluation.getBadgeImage());
            pstmt.setString(9, evaluation.getBadgeTitle());
            pstmt.setInt(10, evaluation.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Evaluation> getAllEvaluations() {
        List<Evaluation> evaluations = new ArrayList<>();
        String query = "SELECT * FROM evaluation ORDER BY id DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Evaluation e = new Evaluation(
                    rs.getInt("id"),
                    rs.getInt("cours_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getInt("max_score"),
                    rs.getTimestamp("create_at"),
                    rs.getTimestamp("deadline"),
                    rs.getInt("badge_threshold"),
                    rs.getString("badge_image"),
                    rs.getString("badge_title")
                );
                evaluations.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluations;
    }
    
}