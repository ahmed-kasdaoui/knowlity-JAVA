package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.EventRegistration;
import tn.esprit.models.Events;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ServiceEvents implements IService<Events> {
    private Connection cnx;

    public ServiceEvents() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void checkEvents(){
        ServiceEventRegistration serviceEventRegistration = new ServiceEventRegistration();
        List<Events> eventsList = new ArrayList<>();
        eventsList.addAll(this.getAll());
        for (Events event : eventsList) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime createdAt = event.getCreatedAt();
            long daysDifference = ChronoUnit.DAYS.between(createdAt, now);
            List<EventRegistration> eventRegistrationsList = serviceEventRegistration.getByEvent(event.getId());
            if(daysDifference>3 && eventRegistrationsList.isEmpty()){
                this.delete(event);
                System.out.println("Event " + event.getId() + " has been deleted");
            }
        }
    }
    @Override
    public void add(Events event) {
        String qry = "INSERT INTO `events` (`title`, `description`, `start_date`, `end_date`, `type`, `max_participants`, `seats_available`, `location`, `created_at`, `image`, `category`, `longitude`, `latitude`,`organizer_id`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, event.getTitle());
            pstm.setString(2, event.getDescription());
            pstm.setTimestamp(3, event.getStartDate() != null ? Timestamp.valueOf(event.getStartDate()) : null);
            pstm.setTimestamp(4, event.getEndDate() != null ? Timestamp.valueOf(event.getEndDate()) : null);
            pstm.setString(5, event.getType());
            pstm.setObject(6, event.getMaxParticipants(), Types.INTEGER);
            pstm.setObject(7, event.getSeatsAvailable(), Types.INTEGER);
            pstm.setString(8, event.getLocation());
            pstm.setTimestamp(9, Timestamp.valueOf(event.getCreatedAt()));
            pstm.setString(10, event.getImage());
            pstm.setString(11, event.getCategory());
            pstm.setObject(12, event.getLongitude(), Types.FLOAT);
            pstm.setObject(13, event.getLatitude(), Types.FLOAT);
            pstm.setObject(14, event.getOrganizerId(), Types.INTEGER);
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                event.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Events> getAll() {
        List<Events> events = new ArrayList<>();
        String qry = "SELECT * FROM `events`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Events e = new Events();
                e.setId(rs.getInt("id"));
                e.setOrganizerId(rs.getInt("organizer_id"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setStartDate(rs.getTimestamp("start_date") != null ? rs.getTimestamp("start_date").toLocalDateTime() : null);
                e.setEndDate(rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date").toLocalDateTime() : null);
                e.setType(rs.getString("type"));
                e.setMaxParticipants(rs.getObject("max_participants") != null ? rs.getInt("max_participants") : null);
                e.setSeatsAvailable(rs.getObject("seats_available") != null ? rs.getInt("seats_available") : null);
                e.setLocation(rs.getString("location"));
                e.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                e.setImage(rs.getString("image"));
                e.setCategory(rs.getString("category"));
                e.setLongitude(rs.getObject("longitude") != null ? rs.getFloat("longitude") : null);
                e.setLatitude(rs.getObject("latitude") != null ? rs.getFloat("latitude") : null);
                events.add(e);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return events;
    }

    @Override
    public void update(Events event) {
        String qry = "UPDATE `events` SET `title`=?, `description`=?, `start_date`=?, `end_date`=?, `type`=?, `max_participants`=?, `seats_available`=?, `location`=?, `created_at`=?, `image`=?, `category`=?, `organizer_id`=?, `longitude`=?, `latitude`=? WHERE `id`=?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setString(1, event.getTitle() != null ? event.getTitle() : "");
            pstm.setString(2, event.getDescription() != null ? event.getDescription() : "");
            pstm.setTimestamp(3, event.getStartDate() != null ? Timestamp.valueOf(event.getStartDate()) : null);
            pstm.setTimestamp(4, event.getEndDate() != null ? Timestamp.valueOf(event.getEndDate()) : null);
            pstm.setString(5, event.getType() != null ? event.getType() : "");
            pstm.setObject(6, event.getMaxParticipants(), Types.INTEGER);
            pstm.setObject(7, event.getSeatsAvailable(), Types.INTEGER);
            pstm.setString(8, event.getLocation() != null ? event.getLocation() : "");
            pstm.setTimestamp(9, event.getCreatedAt() != null ? Timestamp.valueOf(event.getCreatedAt()) : null);
            pstm.setString(10, event.getImage() != null ? event.getImage() : "");
            pstm.setString(11, event.getCategory() != null ? event.getCategory() : "");
            pstm.setObject(12, event.getOrganizerId(), Types.INTEGER);
            pstm.setObject(13, event.getLongitude() != null ? event.getLongitude() : 0.0, Types.DOUBLE); // Default to 0.0 or handle differently
            pstm.setObject(14, event.getLatitude() != null ? event.getLatitude() : 0.0, Types.DOUBLE);  // Default to 0.0 or handle differently
            pstm.setInt(15, event.getId());

            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected == 0) {
                String checkExists = "SELECT COUNT(*) FROM `events` WHERE `id`=?";
                try (PreparedStatement checkStmt = cnx.prepareStatement(checkExists)) {
                    checkStmt.setInt(1, event.getId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new RuntimeException("Event not found");
                    }
                    System.out.println("No changes made to event with ID: " + event.getId());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update event: " + e.getMessage(), e);
        }
    }

    public void updateSeatsAvailable(int eventId, int newSeatsAvailable) {
        String qry = "UPDATE `events` SET `seats_available`=? WHERE `id`=?";
        try (PreparedStatement pstm = cnx.prepareStatement(qry)) {
            pstm.setObject(1, newSeatsAvailable, Types.INTEGER);
            pstm.setInt(2, eventId);
            int rowsAffected = pstm.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Not enough seats available or event not found!");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update seats available: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Events event) {
        String disableFK = "SET FOREIGN_KEY_CHECKS = 0";
        String qry = "DELETE FROM `events` WHERE `id`=?";
        String enableFK = "SET FOREIGN_KEY_CHECKS = 1";

        try {
            PreparedStatement disableStmt = cnx.prepareStatement(disableFK);
            disableStmt.executeUpdate();

            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, event.getId());
            pstm.executeUpdate();

            PreparedStatement enableStmt = cnx.prepareStatement(enableFK);
            enableStmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Events getById(int id) {
        Events event = null;
        String qry = "SELECT * FROM `events` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                event = new Events();
                event.setId(rs.getInt("id"));
                event.setOrganizerId(rs.getInt("organizer_id"));
                event.setTitle(rs.getString("title"));
                event.setDescription(rs.getString("description"));
                event.setStartDate(rs.getTimestamp("start_date") != null ? rs.getTimestamp("start_date").toLocalDateTime() : null);
                event.setEndDate(rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date").toLocalDateTime() : null);
                event.setType(rs.getString("type"));
                event.setMaxParticipants(rs.getObject("max_participants") != null ? rs.getInt("max_participants") : null);
                event.setSeatsAvailable(rs.getObject("seats_available") != null ? rs.getInt("seats_available") : null);
                event.setLocation(rs.getString("location"));
                event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                event.setImage(rs.getString("image"));
                event.setCategory(rs.getString("category"));
                event.setLongitude(rs.getObject("longitude") != null ? rs.getFloat("longitude") : null);
                event.setLatitude(rs.getObject("latitude") != null ? rs.getFloat("latitude") : null);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return event;
    }


}