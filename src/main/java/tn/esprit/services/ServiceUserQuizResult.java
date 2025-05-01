package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.UserQuizResult;
import tn.esprit.models.Quiz;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserQuizResult implements IService<UserQuizResult> {
    private Connection cnx;
    private ServiceQuiz serviceQuiz;

    public ServiceUserQuizResult() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceQuiz = new ServiceQuiz();
    }

    @Override
    public void add(UserQuizResult result) {
        String qry = "INSERT INTO `user_quiz_result` (`score`, `soumis_le`, `feedback`, `user`, `quiz_id`, `duration`) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, result.getScore());
            pstm.setTimestamp(2, Timestamp.valueOf(result.getSoumisLe()));
            pstm.setString(3, result.getFeedback());
            pstm.setInt(4, result.getUserId());
            pstm.setInt(5, result.getQuiz().getId());
            pstm.setInt(6, result.getDuration()); // Added duration
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                result.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Error adding UserQuizResult: " + e.getMessage());
        }
    }

    @Override
    public List<UserQuizResult> getAll() {
        List<UserQuizResult> results = new ArrayList<>();
        String qry = "SELECT * FROM `user_quiz_result`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                UserQuizResult r = new UserQuizResult();
                r.setId(rs.getInt("id"));
                r.setScore(rs.getInt("score"));
                r.setSoumisLe(rs.getTimestamp("soumis_le").toLocalDateTime());
                r.setFeedback(rs.getString("feedback"));
                r.setUserId(rs.getInt("user"));
                r.setDuration(rs.getInt("duration")); // Added duration
                Quiz quiz = serviceQuiz.getById(rs.getInt("quiz_id"));
                r.setQuiz(quiz);
                results.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving UserQuizResults: " + e.getMessage());
        }
        return results;
    }

    @Override
    public void update(UserQuizResult result) {
        String qry = "UPDATE `user_quiz_result` SET `score`=?, `soumis_le`=?, `feedback`=?, `user`=?, `quiz_id`=?, `duration`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, result.getScore());
            pstm.setTimestamp(2, Timestamp.valueOf(result.getSoumisLe()));
            pstm.setString(3, result.getFeedback());
            pstm.setInt(4, result.getUserId());
            pstm.setInt(5, result.getQuiz().getId());
            pstm.setInt(6, result.getDuration()); // Added duration
            pstm.setInt(7, result.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating UserQuizResult: " + e.getMessage());
        }
    }

    @Override
    public void delete(UserQuizResult result) {
        String qry = "DELETE FROM `user_quiz_result` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, result.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting UserQuizResult: " + e.getMessage());
        }
    }

    public UserQuizResult getById(int id) {
        UserQuizResult result = null;
        String qry = "SELECT * FROM `user_quiz_result` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                result = new UserQuizResult();
                result.setId(rs.getInt("id"));
                result.setScore(rs.getInt("score"));
                result.setSoumisLe(rs.getTimestamp("soumis_le").toLocalDateTime());
                result.setFeedback(rs.getString("feedback"));
                result.setUserId(rs.getInt("user"));
                result.setDuration(rs.getInt("duration")); // Added duration
                Quiz quiz = serviceQuiz.getById(rs.getInt("quiz_id"));
                result.setQuiz(quiz);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving UserQuizResult by ID: " + e.getMessage());
        }
        return result;
    }
}