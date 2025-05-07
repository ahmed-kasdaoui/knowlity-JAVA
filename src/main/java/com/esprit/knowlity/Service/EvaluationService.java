package com.esprit.knowlity.Service;

import com.esprit.knowlity.Model.Evaluation;
import com.esprit.knowlity.Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationService {
    /**
     * Returns the Evaluation object with the specified id, or null if not found.
     */
    public Evaluation getEvaluationById(int id) {
        String query = "SELECT * FROM evaluation WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Evaluation(
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // ... existing code ...
    /**
     * Returns the id of the most recently inserted evaluation with the given title.
     * If there are multiple with the same title, returns the one with the highest id.
     */
    public int getLastInsertedEvaluationIdByTitle(String title) {
        String query = "SELECT id FROM evaluation WHERE title = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Not found
    }
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
    
    /**
     * Retrieves the evaluation notes for a specific evaluation and user.
     * 
     * @param evaluationId The ID of the evaluation
     * @param userId The ID of the user
     * @return List of evaluation notes, or an empty list if no notes found
     */
    public List<String> getEvaluationNotes(int evaluationId, int userId) {
        List<String> notes = new ArrayList<>();
        String query = "SELECT note FROM reponse WHERE evaluation_id = ? AND user_id = ? AND note IS NOT NULL";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, evaluationId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String note = rs.getString("note");
                if (note != null && !note.trim().isEmpty()) {
                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving evaluation notes: " + e.getMessage());
            e.printStackTrace();
        }
        return notes;
    }
    
}