package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceChapitre implements IService<Chapitre> {
    private Connection cnx;
    private ServiceCours serviceCours;

    public ServiceChapitre() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceCours = new ServiceCours();
    }

    @Override
    public void add(Chapitre chapitre) {
        String qry = "INSERT INTO chapitre (title, chap_order, cours_id, contenu, duree_estimee, nbr_vues) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, chapitre.getTitle());
            pstm.setInt(2, chapitre.getChapOrder());
            pstm.setInt(3, chapitre.getCours().getId());
            pstm.setString(4, chapitre.getContenu());
            pstm.setInt(5, chapitre.getDureeEstimee());
            pstm.setInt(6, chapitre.getNbrVues());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                chapitre.setId(rs.getInt(1));
            }

            // Update nbr_chapitre in cours table
            String updateCoursQry = "UPDATE cours SET nbr_chapitre = (SELECT COUNT(*) FROM chapitre WHERE cours_id = ?) WHERE id = ?";
            PreparedStatement updateCoursPstm = cnx.prepareStatement(updateCoursQry);
            updateCoursPstm.setInt(1, chapitre.getCours().getId());
            updateCoursPstm.setInt(2, chapitre.getCours().getId());
            updateCoursPstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Chapitre> getAll() {
        List<Chapitre> chapitres = new ArrayList<>();
        String qry = "SELECT * FROM chapitre";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Chapitre c = new Chapitre();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setChapOrder(rs.getInt("chap_order"));
                Cours cours = serviceCours.getById(rs.getInt("cours_id"));
                c.setCours(cours);
                c.setContenu(rs.getString("contenu"));
                c.setDureeEstimee(rs.getInt("duree_estimee"));
                c.setNbrVues(rs.getInt("nbr_vues"));
                chapitres.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return chapitres;
    }

    @Override
    public void update(Chapitre chapitre) {
        String qry = "UPDATE chapitre SET title`=?, chap_order`=?, cours_id`=?, contenu`=?, duree_estimee`=?, nbr_vues`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, chapitre.getTitle());
            pstm.setInt(2, chapitre.getChapOrder());
            pstm.setInt(3, chapitre.getCours().getId());
            pstm.setString(4, chapitre.getContenu());
            pstm.setInt(5, chapitre.getDureeEstimee());
            pstm.setInt(6, chapitre.getNbrVues());
            pstm.setInt(7, chapitre.getId());
            pstm.executeUpdate();

            // Update nbr_chapitre in cours table if cours_id changed
            String updateCoursQry = "UPDATE cours SET nbr_chapitre = (SELECT COUNT(*) FROM chapitre WHERE cours_id = ?) WHERE id = ?";
            PreparedStatement updateOldCoursPstm = cnx.prepareStatement(updateCoursQry);
            updateOldCoursPstm.setInt(1, chapitre.getCours().getId());
            updateOldCoursPstm.setInt(2, chapitre.getCours().getId());
            updateOldCoursPstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Chapitre chapitre) {
        String qry = "DELETE FROM chapitre WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, chapitre.getId());
            pstm.executeUpdate();

            // Update nbr_chapitre in cours table
            String updateCoursQry = "UPDATE cours SET nbr_chapitre = (SELECT COUNT(*) FROM chapitre WHERE cours_id = ?) WHERE id = ?";
            PreparedStatement updateCoursPstm = cnx.prepareStatement(updateCoursQry);
            updateCoursPstm.setInt(1, chapitre.getCours().getId());
            updateCoursPstm.setInt(2, chapitre.getCours().getId());
            updateCoursPstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Chapitre getById(int id) {
        Chapitre chapitre = null;
        String qry = "SELECT * FROM chapitre WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                chapitre = new Chapitre();
                chapitre.setId(rs.getInt("id"));
                chapitre.setTitle(rs.getString("title"));
                chapitre.setChapOrder(rs.getInt("chap_order"));
                Cours cours = serviceCours.getById(rs.getInt("cours_id"));
                chapitre.setCours(cours);
                chapitre.setContenu(rs.getString("contenu"));
                chapitre.setDureeEstimee(rs.getInt("duree_estimee"));
                chapitre.setNbrVues(rs.getInt("nbr_vues"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return chapitre;
    }
}
