package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.QuizQuestion;
import tn.esprit.models.Quiz;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceQuizQuestion implements IService<QuizQuestion> {
    private Connection cnx;
    private ServiceQuiz serviceQuiz;

    public ServiceQuizQuestion() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceQuiz = new ServiceQuiz();
    }

    @Override
    public void add(QuizQuestion question) {
        String qry = "INSERT INTO `quiz_question` (`type`, `points`, `texte`, `ordre`, `quiz_id`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, question.getType());
            pstm.setInt(2, question.getPoints());
            pstm.setString(3, question.getTexte());
            pstm.setInt(4, question.getOrdre());
            pstm.setInt(5, question.getQuiz().getId());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                question.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<QuizQuestion> getAll() {
        List<QuizQuestion> questions = new ArrayList<>();
        String qry = "SELECT * FROM `quiz_question`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                QuizQuestion q = new QuizQuestion();
                q.setId(rs.getInt("id"));
                q.setType(rs.getString("type"));
                q.setPoints(rs.getInt("points"));
                q.setTexte(rs.getString("texte"));
                q.setOrdre(rs.getInt("ordre"));
                Quiz quiz = serviceQuiz.getById(rs.getInt("quiz_id"));
                q.setQuiz(quiz);
                questions.add(q);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return questions;
    }

    @Override
    public void update(QuizQuestion question) {
        String qry = "UPDATE `quiz_question` SET `type`=?, `points`=?, `texte`=?, `ordre`=?, `quiz_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, question.getType());
            pstm.setInt(2, question.getPoints());
            pstm.setString(3, question.getTexte());
            pstm.setInt(4, question.getOrdre());
            pstm.setInt(5, question.getQuiz().getId());
            pstm.setInt(6, question.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(QuizQuestion question) {
        String qry = "DELETE FROM `quiz_question` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, question.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public QuizQuestion getById(int id) {
        QuizQuestion question = null;
        String qry = "SELECT * FROM `quiz_question` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                question = new QuizQuestion();
                question.setId(rs.getInt("id"));
                question.setType(rs.getString("type"));
                question.setPoints(rs.getInt("points"));
                question.setTexte(rs.getString("texte"));
                question.setOrdre(rs.getInt("ordre"));
                Quiz quiz = serviceQuiz.getById(rs.getInt("quiz_id"));
                question.setQuiz(quiz);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return question;
    }
}