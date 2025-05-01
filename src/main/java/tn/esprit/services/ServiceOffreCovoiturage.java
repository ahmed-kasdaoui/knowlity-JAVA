package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceOffreCovoiturage implements IService<OffreCovoiturage> {
    private Connection cnx;

    public ServiceOffreCovoiturage() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public  void add(OffreCovoiturage offre) {
        String qry = "INSERT INTO `offre_covoiturage` (`depart`, `conducteur_id`, `destination`, `mat_vehicule`, `places_dispo`, `date`, `statut`, `prix`, `img`) VALUES (?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, offre.getDepart());
            pstm.setInt(2, offre.getConducteurId());
            pstm.setString(3, offre.getDestination());
            pstm.setInt(4, offre.getMatVehicule());
            pstm.setInt(5, offre.getPlacesDispo());
            pstm.setTimestamp(6, Timestamp.valueOf(offre.getDate()));
            pstm.setString(7, offre.getStatut());
            pstm.setFloat(8, offre.getPrix());
            pstm.setString(9, offre.getImg());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                offre.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<OffreCovoiturage> getAll() {
        List<OffreCovoiturage> offres = new ArrayList<>();
        String qry = "SELECT * FROM `offre_covoiturage`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                OffreCovoiturage o = new OffreCovoiturage();
                o.setId(rs.getInt("id"));
                o.setDepart(rs.getString("depart"));
                o.setConducteurId(rs.getInt("conducteur_id"));
                o.setDestination(rs.getString("destination"));
                o.setMatVehicule(rs.getInt("mat_vehicule"));
                o.setPlacesDispo(rs.getInt("places_dispo"));
                o.setDate(rs.getTimestamp("date").toLocalDateTime());
                o.setStatut(rs.getString("statut"));
                o.setPrix(rs.getFloat("prix"));
                o.setImg(rs.getString("img"));
                offres.add(o);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offres;
    }

    @Override
    public void update(OffreCovoiturage offre) {
        String qry = "UPDATE `offre_covoiturage` SET `depart`=?, `conducteur_id`=?, `destination`=?, `mat_vehicule`=?, `places_dispo`=?, `date`=?, `statut`=?, `prix`=?, `img`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, offre.getDepart());
            pstm.setInt(2, offre.getConducteurId());
            pstm.setString(3, offre.getDestination());
            pstm.setInt(4, offre.getMatVehicule());
            pstm.setInt(5,77777777);
            pstm.setTimestamp(6, Timestamp.valueOf(offre.getDate()));
            pstm.setString(7, offre.getStatut());
            pstm.setFloat(8, offre.getPrix());
            pstm.setString(9, offre.getImg());
            pstm.setInt(10, offre.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(OffreCovoiturage offre) {
        String qry = "DELETE FROM `offre_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, offre.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public OffreCovoiturage getById(int id) {
        OffreCovoiturage offre = null;
        String qry = "SELECT * FROM `offre_covoiturage` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                offre = new OffreCovoiturage();
                offre.setId(rs.getInt("id"));
                offre.setDepart(rs.getString("depart"));
                offre.setConducteurId(rs.getInt("conducteur_id"));
                offre.setDestination(rs.getString("destination"));
                offre.setMatVehicule(rs.getInt("mat_vehicule"));
                offre.setPlacesDispo(rs.getInt("places_dispo"));
                offre.setDate(rs.getTimestamp("date").toLocalDateTime());
                offre.setStatut(rs.getString("statut"));
                offre.setPrix(rs.getFloat("prix"));
                offre.setImg(rs.getString("img"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return offre;
    }
}