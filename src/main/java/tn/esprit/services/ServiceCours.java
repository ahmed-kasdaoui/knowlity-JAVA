package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Cours;
import tn.esprit.models.Matiere;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCours implements IService<Cours> {
    private Connection cnx;
    private ServiceMatiere serviceMatiere;

    public ServiceCours() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceMatiere = new ServiceMatiere();
    }

    @Override
    public void add(Cours cours) {
        String qry = "INSERT INTO `cours` (`title`, `description`, `url_image`, `matiere_id`, `langue`, `prix`, `lien_de_paiment`) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, cours.getTitle());
            pstm.setString(2, cours.getDescription());
            pstm.setString(3, cours.getUrlImage());
            pstm.setInt(4, cours.getMatiere().getId());
            pstm.setString(5, cours.getLangue());
            pstm.setInt(6, cours.getPrix());
            pstm.setString(7, cours.getLienDePaiment());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                cours.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String qry = "SELECT * FROM `cours`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                c.setMatiere(matiere);
                c.setLangue(rs.getString("langue"));
                c.setPrix(rs.getInt("prix"));
                c.setLienDePaiment(rs.getString("lien_de_paiment"));
                coursList.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coursList;
    }

    @Override
    public void update(Cours cours) {
        String qry = "UPDATE `cours` SET `title`=?, `description`=?, `url_image`=?, `matiere_id`=?, `langue`=?, `prix`=?, `lien_de_paiment`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, cours.getTitle());
            pstm.setString(2, cours.getDescription());
            pstm.setString(3, cours.getUrlImage());
            pstm.setInt(4, cours.getMatiere().getId());
            pstm.setString(5, cours.getLangue());
            pstm.setInt(6, cours.getPrix());
            pstm.setString(7, cours.getLienDePaiment());
            pstm.setInt(8, cours.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Cours cours) {
        String qry = "DELETE FROM `cours` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, cours.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Cours getById(int id) {
        Cours cours = null;
        String qry = "SELECT * FROM `cours` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setTitle(rs.getString("title"));
                cours.setDescription(rs.getString("description"));
                cours.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                cours.setMatiere(matiere);
                cours.setLangue(rs.getString("langue"));
                cours.setPrix(rs.getInt("prix"));
                cours.setLienDePaiment(rs.getString("lien_de_paiment"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cours;
    }
}