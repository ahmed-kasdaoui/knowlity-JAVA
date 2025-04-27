package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.EventRegistration;
import tn.esprit.models.Events;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEventRegistration implements IService<EventRegistration> {
    private Connection cnx;
    private ServiceEvents serviceEvents;

    public ServiceEventRegistration() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceEvents = new ServiceEvents();
    }

    @Override
    public void add(EventRegistration registration) {
        String qry = "INSERT INTO `event_registration` (`event_id`, `registration_date`, `status`, `disabled_parking`, `coming_from`, `name`, `places_reserved`,`user_id`) VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, registration.getEvent().getId());
            pstm.setTimestamp(2, Timestamp.valueOf(registration.getRegistrationDate()));
            pstm.setString(3, registration.getStatus());
            pstm.setBoolean(4, registration.isDisabledParking());
            pstm.setString(5, registration.getComingFrom());
            pstm.setString(6, registration.getName());
            pstm.setObject(7, registration.getPlacesReserved(), Types.INTEGER);
            pstm.setInt(8,registration.getUserId());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                registration.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<EventRegistration> getAll() {
        List<EventRegistration> registrations = new ArrayList<>();
        String qry = "SELECT * FROM `event_registration`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                EventRegistration r = new EventRegistration();
                r.setId(rs.getInt("id"));
                Events event = serviceEvents.getById(rs.getInt("event_id"));
                r.setEvent(event);
                r.setUserId(rs.getInt("user_id"));
                r.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                r.setStatus(rs.getString("status"));
                r.setDisabledParking(rs.getBoolean("disabled_parking"));
                r.setComingFrom(rs.getString("coming_from"));
                r.setName(rs.getString("name"));
                r.setPlacesReserved(rs.getObject("places_reserved") != null ? rs.getInt("places_reserved") : null);
                registrations.add(r);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return registrations;
    }

    @Override
    public void update(EventRegistration registration) {
        String qry = "UPDATE `event_registration` SET `event_id`=?, `registration_date`=?, `status`=?, `disabled_parking`=?, `coming_from`=?, `name`=?, `places_reserved`=?,`check_in_code`=?,`user_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, registration.getEvent().getId());
            pstm.setTimestamp(2, Timestamp.valueOf(registration.getRegistrationDate()));
            pstm.setString(3, registration.getStatus());
            pstm.setBoolean(4, registration.isDisabledParking());
            pstm.setString(5, registration.getComingFrom());
            pstm.setString(6, registration.getName());
            pstm.setObject(7, registration.getPlacesReserved(), Types.INTEGER);
            pstm.setString(8, registration.getCheck_in_code());
            pstm.setInt(9, registration.getUserId());
            pstm.setInt(10, registration.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(EventRegistration registration) {
        String qry = "DELETE FROM `event_registration` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, registration.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public EventRegistration getById(int id) {
        EventRegistration registration = null;
        String qry = "SELECT * FROM `event_registration` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                registration = new EventRegistration();
                registration.setId(rs.getInt("id"));
                Events event = serviceEvents.getById(rs.getInt("event_id"));
                registration.setEvent(event);
                registration.setUserId(rs.getInt("user_id"));
                registration.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                registration.setStatus(rs.getString("status"));
                registration.setDisabledParking(rs.getBoolean("disabled_parking"));
                registration.setComingFrom(rs.getString("coming_from"));
                registration.setName(rs.getString("name"));
                registration.setPlacesReserved(rs.getObject("places_reserved") != null ? rs.getInt("places_reserved") : null);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return registration;
    }

    public List<EventRegistration> getByEvent(int IdEvent) {
        List<EventRegistration> registrations = new ArrayList<>();
        String qry = "SELECT * FROM `event_registration` WHERE `event_id`=?";
        try{
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, IdEvent);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                EventRegistration r = new EventRegistration();
                r.setId(rs.getInt("id"));
                Events event = serviceEvents.getById(rs.getInt("event_id"));
                r.setEvent(event);
                r.setUserId(rs.getInt("user_id"));
                r.setRegistrationDate(rs.getTimestamp("registration_date").toLocalDateTime());
                r.setStatus(rs.getString("status"));
                r.setDisabledParking(rs.getBoolean("disabled_parking"));
                r.setComingFrom(rs.getString("coming_from"));
                r.setName(rs.getString("name"));
                r.setPlacesReserved(rs.getObject("places_reserved") != null ? rs.getInt("places_reserved") : null);
                registrations.add(r);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return registrations;
    }
}