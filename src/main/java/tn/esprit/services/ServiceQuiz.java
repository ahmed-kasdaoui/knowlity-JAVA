package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Quiz;
import tn.esprit.models.Cours;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceQuiz implements IService<Quiz> {
    private Connection cnx;

    public ServiceQuiz() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(Quiz quiz) {
        String qry = "INSERT INTO `quiz` (`titre`, `description`, `score_max`, `date_limite`, `cours_id`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, quiz.getTitre());
            pstm.setString(2, quiz.getDescription());
            pstm.setInt(3, quiz.getScoreMax());
            pstm.setTimestamp(4, Timestamp.valueOf(quiz.getDateLimite()));
            pstm.setInt(5, quiz.getCours_id());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                quiz.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Quiz> getAll() {
        List<Quiz> quizzes = new ArrayList<>();
        String qry = "SELECT * FROM `quiz`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitre(rs.getString("titre"));
                q.setDescription(rs.getString("description"));
                q.setScoreMax(rs.getInt("score_max"));
                q.setDateLimite(rs.getTimestamp("date_limite").toLocalDateTime());
                q.setCours_id(rs.getInt("cours_id"));
                quizzes.add(q);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return quizzes;
    }

    @Override
    public void update(Quiz quiz) {
        String qry = "UPDATE `quiz` SET `titre`=?, `description`=?, `score_max`=?, `date_limite`=?, `cours_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, quiz.getTitre());
            pstm.setString(2, quiz.getDescription());
            pstm.setInt(3, quiz.getScoreMax());
            pstm.setTimestamp(4, Timestamp.valueOf(quiz.getDateLimite()));
            pstm.setInt(5, quiz.getCours_id());
            pstm.setInt(6, quiz.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Quiz quiz) {
        String qry = "DELETE FROM `quiz` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, quiz.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Quiz getById(int id) {
        Quiz quiz = null;
        String qry = "SELECT * FROM `quiz` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                quiz = new Quiz();
                quiz.setId(rs.getInt("id"));
                quiz.setTitre(rs.getString("titre"));
                quiz.setDescription(rs.getString("description"));
                quiz.setScoreMax(rs.getInt("score_max"));
                quiz.setDateLimite(rs.getTimestamp("date_limite").toLocalDateTime());
                quiz.setCours_id(rs.getInt("cours_id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return quiz;
    }

    // Méthode optionnelle pour récupérer les quizzes par id de cours
    public List<Quiz> getByCoursId(int coursId) {
        List<Quiz> quizzes = new ArrayList<>();
        String qry = "SELECT * FROM `quiz` WHERE `cours_id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, coursId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTitre(rs.getString("titre"));
                q.setDescription(rs.getString("description"));
                q.setScoreMax(rs.getInt("score_max"));
                q.setDateLimite(rs.getTimestamp("date_limite").toLocalDateTime());
                q.setCours_id(rs.getInt("cours_id"));
                quizzes.add(q);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return quizzes;
    }

    public List<Quiz> getQuizByCours(int coursId) {
        return getByCoursId(coursId);
    }
}
