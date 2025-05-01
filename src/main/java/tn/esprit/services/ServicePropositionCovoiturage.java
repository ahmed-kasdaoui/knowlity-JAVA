package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.PropositionCovoiturage;
import tn.esprit.models.DemandeCovoiturage;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServicePropositionCovoiturage implements IService<PropositionCovoiturage> {
    private Connection cnx;
    private ServiceDemandeCovoiturage serviceDemande;

    public ServicePropositionCovoiturage() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceDemande = new ServiceDemandeCovoiturage();
    }

    @Override
    public void add(PropositionCovoiturage proposition) {
        String qry = "INSERT INTO `proposition_covoiturage` (`conducteur_id`, `statut`, `demande_id`, `places_dispo`, `created_at`) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, proposition.getConducteurId());
            pstm.setString(2, proposition.getStatut());
            pstm.setInt(3, proposition.getDemande().getId());
            pstm.setInt(4, proposition.getPlacesDispo());
            pstm.setTimestamp(5, Timestamp.valueOf(proposition.getCreatedAt()));
            pstm.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<PropositionCovoiturage> getAll() {
        List<PropositionCovoiturage> propositions = new ArrayList<>();
        String qry = "SELECT * FROM `proposition_covoiturage`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                PropositionCovoiturage p = new PropositionCovoiturage();
                p.setId(rs.getInt("id"));
                p.setConducteurId(rs.getInt("conducteur_id"));
                p.setStatut(rs.getString("statut"));
                DemandeCovoiturage demande = serviceDemande.getById(rs.getInt("demande_id"));
                p.setDemande(demande);
                p.setPlacesDispo(rs.getInt("places_dispo"));
                p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                propositions.add(p);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return propositions;
    }

    @Override
    public void update(PropositionCovoiturage proposition) {
        String qry = "UPDATE `proposition_covoiturage` SET `conducteur_id`=?, `statut`=?, `demande_id`=?, `places_dispo`=?, `created_at`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1,888888);
            pstm.setString(2, proposition.getStatut());
            pstm.setInt(3, proposition.getDemande().getId());
            pstm.setInt(4, proposition.getPlacesDispo());
            pstm.setTimestamp(5, Timestamp.valueOf(proposition.getCreatedAt()));
            pstm.setInt(6, proposition.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(PropositionCovoiturage proposition) {
        String qry = "DELETE FROM `proposition_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, proposition.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public PropositionCovoiturage getById(int id) {
        PropositionCovoiturage proposition = null;
        String qry = "SELECT * FROM `proposition_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                proposition = new PropositionCovoiturage();
                proposition.setId(rs.getInt("id"));
                proposition.setConducteurId(rs.getInt("conducteur_id"));
                proposition.setStatut(rs.getString("statut"));
                DemandeCovoiturage demande = serviceDemande.getById(rs.getInt("demande_id"));
                proposition.setDemande(demande);
                proposition.setPlacesDispo(rs.getInt("places_dispo"));
                proposition.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return proposition;
    }
}