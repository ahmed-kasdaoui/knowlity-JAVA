package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Reservation;
import tn.esprit.models.OffreCovoiturage;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IService<Reservation> {
    private Connection cnx;
    private ServiceOffreCovoiturage serviceOffre;

    public ServiceReservation() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceOffre = new ServiceOffreCovoiturage();
    }

    @Override
    public void add(Reservation reservation) {
        String qry = "INSERT INTO `reservation` (`passager_id`, `statut`, `offre_id`, `created_at`) VALUES (?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, reservation.getPassagerId());
            pstm.setString(2, reservation.getStatut());
            pstm.setInt(3, reservation.getOffre().getId());
            pstm.setTimestamp(4, Timestamp.valueOf(reservation.getCreatedAt()));
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                reservation.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Reservation> getAll() {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM `reservation`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setPassagerId(rs.getInt("passager_id"));
                r.setStatut(rs.getString("statut"));
                OffreCovoiturage offre = serviceOffre.getById(rs.getInt("offre_id"));
                r.setOffre(offre);
                r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservations;
    }

    @Override
    public void update(Reservation reservation) {
        String qry = "UPDATE `reservation` SET `passager_id`=?, `statut`=?, `offre_id`=?, `created_at`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1,888888);
            pstm.setString(2, reservation.getStatut());
            pstm.setInt(3, reservation.getOffre().getId());
            pstm.setTimestamp(4, Timestamp.valueOf(reservation.getCreatedAt()));
            pstm.setInt(5, reservation.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Reservation reservation) {
        String qry = "DELETE FROM `reservation` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, reservation.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Reservation getById(int id) {
        Reservation reservation = null;
        String qry = "SELECT * FROM `reservation` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setPassagerId(rs.getInt("passager_id"));
                reservation.setStatut(rs.getString("statut"));
                OffreCovoiturage offre = serviceOffre.getById(rs.getInt("offre_id"));
                reservation.setOffre(offre);
                reservation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return reservation;
    }
}