package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.QuizResponse;
import tn.esprit.models.QuizQuestion;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceQuizResponse implements IService<QuizResponse> {
    private Connection cnx;
    private ServiceQuizQuestion serviceQuizQuestion;

    public ServiceQuizResponse() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceQuizQuestion = new ServiceQuizQuestion();
    }

    @Override
    public void add(QuizResponse response) {
        String qry = "INSERT INTO `quiz_reponse` (`texte`, `est_correcte`, `question_id`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, response.getTexte());
            pstm.setBoolean(2, response.isEstCorrecte());
            pstm.setInt(3, response.getQuestion().getId());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                response.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<QuizResponse> getAll() {
        List<QuizResponse> responses = new ArrayList<>();
        String qry = "SELECT * FROM `quiz_reponse`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                QuizResponse r = new QuizResponse();
                r.setId(rs.getInt("id"));
                r.setTexte(rs.getString("texte"));
                r.setEstCorrecte(rs.getBoolean("est_correcte"));
                QuizQuestion question = serviceQuizQuestion.getById(rs.getInt("question_id"));
                r.setQuestion(question);
                responses.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return responses;
    }

    @Override
    public void update(QuizResponse response) {
        String qry = "UPDATE `quiz_reponse` SET `texte`=?, `est_correcte`=?, `question_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, response.getTexte());
            pstm.setBoolean(2, response.isEstCorrecte());
            pstm.setInt(3, response.getQuestion().getId());
            pstm.setInt(4, response.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(QuizResponse response) {
        String qry = "DELETE FROM `quiz_reponse` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, response.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public QuizResponse getById(int id) {
        QuizResponse response = null;
        String qry = "SELECT * FROM `quiz_reponse` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                response = new QuizResponse();
                response.setId(rs.getInt("id"));
                response.setTexte(rs.getString("texte"));
                response.setEstCorrecte(rs.getBoolean("est_correcte"));
                QuizQuestion question = serviceQuizQuestion.getById(rs.getInt("question_id"));
                response.setQuestion(question);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return response;
    }
}