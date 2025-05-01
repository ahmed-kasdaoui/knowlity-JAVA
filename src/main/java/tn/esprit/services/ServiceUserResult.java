package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.UserResult;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserResult implements IService<UserResult> {
    private Connection cnx;

    public ServiceUserResult() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(UserResult userResult) {
        String qry = "INSERT INTO `user_result` (`user_id`, `quiz_id`, `result`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, userResult.getUserId());
            pstm.setInt(2, userResult.getQuizId());
            pstm.setInt(3, userResult.getResult());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                userResult.setId(rs.getInt(1));
            }
            System.out.println("Saved UserResult: ID=" + userResult.getId() +
                    ", UserId=" + userResult.getUserId() +
                    ", QuizId=" + userResult.getQuizId() +
                    ", Result=" + userResult.getResult());
        } catch (SQLException e) {
            System.out.println("Error adding UserResult: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<UserResult> getAll() {
        List<UserResult> results = new ArrayList<>();
        String qry = "SELECT * FROM `user_result`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                UserResult result = new UserResult();
                result.setId(rs.getInt("id"));
                result.setUserId(rs.getInt("user_id"));
                result.setQuizId(rs.getInt("quiz_id"));
                result.setResult(rs.getInt("result"));
                results.add(result);
            }
            System.out.println("Retrieved " + results.size() + " UserResults from the database.");
        } catch (SQLException e) {
            System.out.println("Error retrieving UserResults: " + e.getMessage());
        }
        return results;
    }

    @Override
    public void update(UserResult userResult) {
        String qry = "UPDATE `user_result` SET `user_id`=?, `quiz_id`=?, `result`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userResult.getUserId());
            pstm.setInt(2, userResult.getQuizId());
            pstm.setInt(3, userResult.getResult());
            pstm.setInt(4, userResult.getId());
            pstm.executeUpdate();
            System.out.println("Updated UserResult: ID=" + userResult.getId());
        } catch (SQLException e) {
            System.out.println("Error updating UserResult: " + e.getMessage());
        }
    }

    @Override
    public void delete(UserResult userResult) {
        String qry = "DELETE FROM `user_result` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userResult.getId());
            pstm.executeUpdate();
            System.out.println("Deleted UserResult: ID=" + userResult.getId());
        } catch (SQLException e) {
            System.out.println("Error deleting UserResult: " + e.getMessage());
        }
    }

    public UserResult getById(int id) {
        UserResult userResult = null;
        String qry = "SELECT * FROM `user_result` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                userResult = new UserResult();
                userResult.setId(rs.getInt("id"));
                userResult.setUserId(rs.getInt("user_id"));
                userResult.setQuizId(rs.getInt("quiz_id"));
                userResult.setResult(rs.getInt("result"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving UserResult by ID: " + e.getMessage());
        }
        return userResult;
    }
}