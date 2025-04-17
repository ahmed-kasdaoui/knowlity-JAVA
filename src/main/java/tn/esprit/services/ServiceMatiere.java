package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Matiere;
import tn.esprit.models.Categorie;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceMatiere implements IService<Matiere> {
    private Connection cnx;
    private ServiceCategorie serviceCategorie;

    public ServiceMatiere() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceCategorie = new ServiceCategorie();
    }

    @Override
    public void add(Matiere matiere) {
        String qry = "INSERT INTO `matiere` (`titre`, `created_at`, `updated_at`, `categorie_id`, `prerequis`, `description`, `couleur_theme`) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, matiere.getTitre());
            pstm.setTimestamp(2, Timestamp.valueOf(matiere.getCreatedAt()));
            pstm.setTimestamp(3, Timestamp.valueOf(matiere.getUpdatedAt()));
            pstm.setInt(4, matiere.getCategorie().getId());
            pstm.setString(5, matiere.getPrerequis());
            pstm.setString(6, matiere.getDescription());
            pstm.setString(7, matiere.getCouleurTheme());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                matiere.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Matiere> getAll() {
        List<Matiere> matieres = new ArrayList<>();
        String qry = "SELECT * FROM `matiere`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Matiere m = new Matiere();
                m.setId(rs.getInt("id"));
                m.setTitre(rs.getString("titre"));
                m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                m.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                Categorie categorie = serviceCategorie.getById(rs.getInt("categorie_id"));
                m.setCategorie(categorie);
                m.setPrerequis(rs.getString("prerequis"));
                m.setDescription(rs.getString("description"));
                m.setCouleurTheme(rs.getString("couleur_theme"));
                matieres.add(m);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return matieres;
    }

    @Override
    public void update(Matiere matiere) {
        String qry = "UPDATE `matiere` SET `titre`=?, `created_at`=?, `updated_at`=?, `categorie_id`=?, `prerequis`=?, `description`=?, `couleur_theme`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, matiere.getTitre());
            pstm.setTimestamp(2, Timestamp.valueOf(matiere.getCreatedAt()));
            pstm.setTimestamp(3, Timestamp.valueOf(matiere.getUpdatedAt()));
            pstm.setInt(4, matiere.getCategorie().getId());
            pstm.setString(5, matiere.getPrerequis());
            pstm.setString(6, matiere.getDescription());
            pstm.setString(7, matiere.getCouleurTheme());
            pstm.setInt(8, matiere.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Matiere matiere) {
        String qry = "DELETE FROM `matiere` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, matiere.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Matiere getById(int id) {
        Matiere matiere = null;
        String qry = "SELECT * FROM `matiere` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                matiere = new Matiere();
                matiere.setId(rs.getInt("id"));
                matiere.setTitre(rs.getString("titre"));
                matiere.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                matiere.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                Categorie categorie = serviceCategorie.getById(rs.getInt("categorie_id"));
                matiere.setCategorie(categorie);
                matiere.setPrerequis(rs.getString("prerequis"));
                matiere.setDescription(rs.getString("description"));
                matiere.setCouleurTheme(rs.getString("couleur_theme"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return matiere;
    }
}