package com.esprit.knowlity.Service;

import com.esprit.knowlity.Model.Reponse;
import com.esprit.knowlity.Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {
    private Connection conn;

    public ReponseService() {
        conn = DataSource.getInstance().getCnx();
    }

    public void addReponse(Reponse reponse) {
        String query = "INSERT INTO reponse (question_id, evaluation_id, text, user_id, note, start_time, submit_time, status, is_correct, plagiat_suspect) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, reponse.getQuestionId());
            pstmt.setInt(2, reponse.getEvaluationId());
            pstmt.setString(3, reponse.getText());
            pstmt.setInt(4, reponse.getUserId());
            pstmt.setObject(5, reponse.getNote(), java.sql.Types.INTEGER);
            pstmt.setTimestamp(6, reponse.getStartTime());
            pstmt.setTimestamp(7, reponse.getSubmitTime());
            pstmt.setString(8, reponse.getStatus());
            pstmt.setBoolean(9, reponse.isCorrect());
            pstmt.setBoolean(10, reponse.isPlagiatSuspect());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reponse> getReponsesByEvaluationIdAndUserId(int evaluationId, int userId) {
        List<Reponse> reponses = new ArrayList<>();
        String query = "SELECT * FROM reponse WHERE evaluation_id = ? AND user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, evaluationId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Reponse r = new Reponse(
                        rs.getInt("id"),
                        rs.getInt("question_id"),
                        rs.getInt("evaluation_id"),
                        rs.getInt("resultat_id"),
                        rs.getString("text"),
                        rs.getInt("user_id"),
                        rs.getInt("note"),
                        rs.getTimestamp("start_time"),
                        rs.getTimestamp("submit_time"),
                        rs.getString("commentaire"),
                        rs.getString("status"),
                        rs.getTimestamp("corrected_at"),
                        rs.getBoolean("is_correct"),
                        rs.getBoolean("plagiat_suspect")
                );
                reponses.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reponses;
    }

    public void updateReponseStatusAndNote(int reponseId, String status, Integer note, String commentaire) {
        String query = "UPDATE reponse SET status = ?, note = ?, commentaire = ?, corrected_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setObject(2, note, Types.INTEGER);
            pstmt.setString(3, commentaire);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(5, reponseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reponse> getReponsesByEvaluationId(int evaluationId) {
        List<Reponse> reponses = new ArrayList<>();
        String query = "SELECT * FROM reponse WHERE evaluation_id = ? ORDER BY id DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, evaluationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Reponse r = new Reponse(
                    rs.getInt("id"),
                    rs.getInt("question_id"),
                    rs.getInt("evaluation_id"),
                    rs.getInt("resultat_id"),
                    rs.getString("text"),
                    rs.getInt("user_id"),
                    rs.getInt("note"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("submit_time"),
                    rs.getString("commentaire"),
                    rs.getString("status"),
                    rs.getTimestamp("corrected_at"),
                    rs.getBoolean("is_correct"),
                    rs.getBoolean("plagiat_suspect")
                );
                reponses.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reponses;
    }

    public void updateReponse(Reponse reponse) {
        String query = "UPDATE reponse SET note = ?, status = ?, commentaire = ?, corrected_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setObject(1, reponse.getNote(), Types.INTEGER);
            pstmt.setString(2, reponse.getStatus());
            pstmt.setString(3, reponse.getCommentaire());
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(5, reponse.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Reponse getReponseByQuestionAndEvaluation(int questionId, int evaluationId, int userId) {
        String query = "SELECT * FROM reponse WHERE question_id = ? AND evaluation_id = ? AND user_id = ? ORDER BY id DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.setInt(2, evaluationId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Reponse(
                    rs.getInt("id"),
                    rs.getInt("question_id"),
                    rs.getInt("evaluation_id"),
                    rs.getInt("resultat_id"),
                    rs.getString("text"),
                    rs.getInt("user_id"),
                    rs.getInt("note"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("submit_time"),
                    rs.getString("commentaire"),
                    rs.getString("status"),
                    rs.getTimestamp("corrected_at"),
                    rs.getBoolean("is_correct"),
                    rs.getBoolean("plagiat_suspect")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}