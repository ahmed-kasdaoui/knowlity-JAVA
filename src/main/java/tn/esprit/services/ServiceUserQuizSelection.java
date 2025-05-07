package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.UserQuizSelection;
import tn.esprit.models.QuizResponse;
import tn.esprit.models.QuizQuestion;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserQuizSelection implements IService<UserQuizSelection> {
    private Connection cnx;
    private ServiceQuizResponse serviceQuizResponse;
    private ServiceQuizQuestion serviceQuizQuestion;

    public ServiceUserQuizSelection() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceQuizResponse = new ServiceQuizResponse();
        serviceQuizQuestion = new ServiceQuizQuestion();
    }

    @Override
    public void add(UserQuizSelection selection) {
        String qry = "INSERT INTO `user_quiz_selection` (`response_selectionnee_id`, `est_correcte`, `question_id`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, selection.getResponseSelectionnee().getId());
            pstm.setBoolean(2, selection.isEstCorrecte());
            pstm.setInt(3, selection.getQuestion().getId());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                selection.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<UserQuizSelection> getAll() {
        List<UserQuizSelection> selections = new ArrayList<>();
        String qry = "SELECT * FROM `user_quiz_selection`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                UserQuizSelection s = new UserQuizSelection();
                s.setId(rs.getInt("id"));
                QuizResponse response = serviceQuizResponse.getById(rs.getInt("response_selectionnee_id"));
                s.setResponseSelectionnee(response);
                s.setEstCorrecte(rs.getBoolean("est_correcte"));
                QuizQuestion question = serviceQuizQuestion.getById(rs.getInt("question_id"));
                s.setQuestion(question);
                selections.add(s);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return selections;
    }

    @Override
    public void update(UserQuizSelection selection) {
        String qry = "UPDATE `user_quiz_selection` SET `response_selectionnee_id`=?, `est_correcte`=?, `question_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, selection.getResponseSelectionnee().getId());
            pstm.setBoolean(2, selection.isEstCorrecte());
            pstm.setInt(3, selection.getQuestion().getId());
            pstm.setInt(4, selection.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(UserQuizSelection selection) {
        String qry = "DELETE FROM `user_quiz_selection` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, selection.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public UserQuizSelection getById(int id) {
        UserQuizSelection selection = null;
        String qry = "SELECT * FROM `user_quiz_selection` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                selection = new UserQuizSelection();
                selection.setId(rs.getInt("id"));
                QuizResponse response = serviceQuizResponse.getById(rs.getInt("response_selectionnee_id"));
                selection.setResponseSelectionnee(response);
                selection.setEstCorrecte(rs.getBoolean("est_correcte"));
                QuizQuestion question = serviceQuizQuestion.getById(rs.getInt("question_id"));
                selection.setQuestion(question);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return selection;
    }
}