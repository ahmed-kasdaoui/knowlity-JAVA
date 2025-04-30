package com.esprit.knowlity.Service;

import com.esprit.knowlity.Model.Cours;
import com.esprit.knowlity.Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursService {
    private Connection conn;

    public CoursService() {
        conn = DataSource.getInstance().getCnx();
    }

    public List<Cours> readAll() {
        List<Cours> cours = new ArrayList<>();
        String query = "SELECT * FROM cours ORDER BY id DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Cours c = new Cours(
                        rs.getInt("id"),
                        rs.getInt("matiere_id"),
                        rs.getInt("enseignant_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("url_image"),
                        rs.getString("langue"),
                        rs.getInt("prix"),
                        rs.getString("lien_de_paiment")
                );
                cours.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cours;
    }

    public Cours getCoursById(int id) {
        String query = "SELECT * FROM cours WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Cours(
                    rs.getInt("id"),
                    rs.getInt("matiere_id"),
                    rs.getInt("enseignant_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("url_image"),
                    rs.getString("langue"),
                    rs.getInt("prix"),
                    rs.getString("lien_de_paiment")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}