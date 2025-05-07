package tn.esprit.services;

import tn.esprit.utils.MyDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceFavoris {
    private final Connection connection;

    public ServiceFavoris() {
        connection = MyDataBase.getInstance().getCnx();
    }

    public void ajouterAuxFavoris(int userId, int coursId) throws SQLException {
        String query = "INSERT INTO cours_etudiants_favoris (cours_id, user_id) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, coursId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        }
    }

    public void retirerDesFavoris(int userId, int coursId) throws SQLException {
        String query = "DELETE FROM cours_etudiants_favoris WHERE cours_id = ? AND user_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, coursId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        }
    }

    public boolean estDansFavoris(int userId, int coursId) throws SQLException {
        String query = "SELECT COUNT(*) FROM cours_etudiants_favoris WHERE cours_id = ? AND user_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, coursId);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Integer> getCoursIdsFavoris(int userId) throws SQLException {
        List<Integer> coursIds = new ArrayList<>();
        String query = "SELECT cours_id FROM cours_etudiants_favoris WHERE user_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    coursIds.add(rs.getInt("cours_id"));
                }
            }
        }
        return coursIds;
    }

    public int getNombreFavoris(int coursId) {
        String query = "SELECT COUNT(*) FROM cours_etudiants_favoris WHERE cours_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, coursId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting favorites: " + e.getMessage());
        }
        return 0;
    }
} 