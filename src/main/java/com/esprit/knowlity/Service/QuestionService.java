package com.esprit.knowlity.Service;

import com.esprit.knowlity.Model.Question;
import com.esprit.knowlity.Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {
    private Connection conn;

    public QuestionService() {
        conn = DataSource.getInstance().getCnx();
    }

    public List<Question> getQuestionsByEvaluationId(int evaluationId) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM question WHERE evaluation_id = ? ORDER BY ordre_question";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, evaluationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Question q = new Question(
                        rs.getInt("id"),
                        rs.getInt("evaluation_id"),
                        rs.getInt("point"),
                        rs.getInt("ordre_question"),
                        rs.getString("enonce"),
                        rs.getString("code_snippet"),
                        rs.getString("programming_language"),
                        rs.getBoolean("has_math_formula"),
                        rs.getString("math_formula"),
                        rs.getString("title")
                );
                questions.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public void addQuestion(Question question) {
        String query = "INSERT INTO question (evaluation_id, point, ordre_question, enonce, code_snippet, programming_language, has_math_formula, math_formula, title) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, question.getEvaluationId());
            pstmt.setInt(2, question.getPoint());
            pstmt.setInt(3, question.getOrdreQuestion());
            pstmt.setString(4, question.getEnonce());
            pstmt.setString(5, question.getCodeSnippet());
            pstmt.setString(6, question.getProgrammingLanguage());
            pstmt.setBoolean(7, question.isHasMathFormula());
            pstmt.setString(8, question.getMathFormula());
            pstmt.setString(9, question.getTitle());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteQuestion(int questionId) {
        // First delete all responses related to this question
        String deleteResponsesQuery = "DELETE FROM reponse WHERE question_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteResponsesQuery)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Now delete the question
        String deleteQuestionQuery = "DELETE FROM question WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuestionQuery)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateQuestion(Question question) {
        String query = "UPDATE question SET evaluation_id = ?, point = ?, ordre_question = ?, enonce = ?, code_snippet = ?, programming_language = ?, has_math_formula = ?, math_formula = ?, title = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, question.getEvaluationId());
            pstmt.setInt(2, question.getPoint());
            pstmt.setInt(3, question.getOrdreQuestion());
            pstmt.setString(4, question.getEnonce());
            pstmt.setString(5, question.getCodeSnippet());
            pstmt.setString(6, question.getProgrammingLanguage());
            pstmt.setBoolean(7, question.isHasMathFormula());
            pstmt.setString(8, question.getMathFormula());
            pstmt.setString(9, question.getTitle());
            pstmt.setInt(10, question.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Question getQuestionById(int id) {
        String query = "SELECT * FROM question WHERE id = ? ORDER BY id DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Question(
                    rs.getInt("id"),
                    rs.getInt("evaluation_id"),
                    rs.getInt("point"),
                    rs.getInt("ordre_question"),
                    rs.getString("enonce"),
                    rs.getString("code_snippet"),
                    rs.getString("programming_language"),
                    rs.getBoolean("has_math_formula"),
                    rs.getString("math_formula"),
                    rs.getString("title")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void saveOrUpdateQuestion(Question q) {
        if (q.getId() == 0) {
            addQuestion(q);
        } else {
            updateQuestion(q);
        }
    }
    
}