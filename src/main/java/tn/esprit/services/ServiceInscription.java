package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Cours;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceInscription {
    private Connection cnx;

    public ServiceInscription() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void inscrireEtudiant(int userId, int coursId) {
        String qry = "INSERT INTO `cours_user` (`user_id`, `cours_id`) VALUES (?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            pstm.setInt(2, coursId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean estInscrit(int userId, int coursId) {
        String qry = "SELECT COUNT(*) FROM `cours_user` WHERE `user_id`=? AND `cours_id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            pstm.setInt(2, coursId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public List<Cours> getCoursInscrits(int userId) {
        List<Cours> coursList = new ArrayList<>();
        String qry = "SELECT c.* FROM `cours` c INNER JOIN `cours_user` cu ON c.id = cu.cours_id WHERE cu.user_id = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();
            ServiceCours serviceCours = new ServiceCours();
            while (rs.next()) {
                Cours cours = serviceCours.getById(rs.getInt("id"));
                if (cours != null) {
                    coursList.add(cours);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coursList;
    }

    public void desinscrireEtudiant(int userId, int coursId) {
        String qry = "DELETE FROM `cours_user` WHERE `user_id`=? AND `cours_id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            pstm.setInt(2, coursId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
} 