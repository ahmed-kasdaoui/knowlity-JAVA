package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.DemandeCovoiturage;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceDemandeCovoiturage implements IService<DemandeCovoiturage> {
    private Connection cnx;

    public ServiceDemandeCovoiturage() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(DemandeCovoiturage demande) {
        String qry = "INSERT INTO `demande_covoiturage` (`passager_id`, `depart`, `destination`, `date`, `statut`, `budget`) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, demande.getPassagerId());
            pstm.setString(2, demande.getDepart());
            pstm.setString(3, demande.getDestination());
            pstm.setTimestamp(4, Timestamp.valueOf(demande.getDate()));
            pstm.setString(5, demande.getStatut());
            pstm.setFloat(6, demande.getBudget());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                demande.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<DemandeCovoiturage> getAll() {
        List<DemandeCovoiturage> demandes = new ArrayList<>();
        String qry = "SELECT * FROM `demande_covoiturage`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                DemandeCovoiturage d = new DemandeCovoiturage();
                d.setId(rs.getInt("id"));
                d.setPassagerId(rs.getInt("passager_id"));
                d.setDepart(rs.getString("depart"));
                d.setDestination(rs.getString("destination"));
                d.setDate(rs.getTimestamp("date").toLocalDateTime());
                d.setStatut(rs.getString("statut"));
                d.setBudget(rs.getFloat("budget"));
                demandes.add(d);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return demandes;
    }

    @Override
    public void update(DemandeCovoiturage demande) {
        String qry = "UPDATE `demande_covoiturage` SET `passager_id`=?, `depart`=?, `destination`=?, `date`=?, `statut`=?, `budget`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1,888888);
            pstm.setString(2, demande.getDepart());
            pstm.setString(3, demande.getDestination());
            pstm.setTimestamp(4, Timestamp.valueOf(demande.getDate()));
            pstm.setString(5, demande.getStatut());
            pstm.setFloat(6, demande.getBudget());
            pstm.setInt(7, demande.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(DemandeCovoiturage demande) {
        String qry = "DELETE FROM `demande_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, demande.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public DemandeCovoiturage getById(int id) {
        DemandeCovoiturage demande = null;
        String qry = "SELECT * FROM `demande_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                demande = new DemandeCovoiturage();
                demande.setId(rs.getInt("id"));
                demande.setPassagerId(rs.getInt("passager_id"));
                demande.setDepart(rs.getString("depart"));
                demande.setDestination(rs.getString("destination"));
                demande.setDate(rs.getTimestamp("date").toLocalDateTime());
                demande.setStatut(rs.getString("statut"));
                demande.setBudget(rs.getFloat("budget"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return demande;
    }
}