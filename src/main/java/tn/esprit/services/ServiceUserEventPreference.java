package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Events;
import tn.esprit.models.UserEventPreference;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserEventPreference implements IService<UserEventPreference> {
    private Connection cnx;

    public ServiceUserEventPreference() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void add(UserEventPreference preference) {
        String qry = "INSERT INTO `user_event_preference` (`user_id`, `category`,`type`, `preference_score`) VALUES (?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, 1);
            pstm.setString(2, preference.getCategory());
            pstm.setString(3, preference.getType());
            pstm.setInt(4, preference.getPreferenceScore());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                preference.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<UserEventPreference> getAll() {
        List<UserEventPreference> preferences = new ArrayList<>();
        String qry = "SELECT * FROM `user_event_preference`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                UserEventPreference p = new UserEventPreference();
                p.setId(rs.getInt("id"));
                p.setUser_id(rs.getInt("user_id"));
                p.setCategory(rs.getString("category"));
                p.setPreferenceScore(rs.getInt("preference_score"));
                preferences.add(p);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return preferences;
    }

    @Override
    public void update(UserEventPreference preference) {
        String qry = "UPDATE `user_event_preference` SET `user_id`=?, `category`=?,`type`=?, `preference_score`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, preference.getUser_id());
            pstm.setString(2, preference.getCategory());
            pstm.setString(3, preference.getType());
            pstm.setInt(4, preference.getPreferenceScore());
            pstm.setInt(5, preference.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(UserEventPreference preference) {
        String qry = "DELETE FROM `user_event_preference` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, preference.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public UserEventPreference getById(int id) {
        UserEventPreference preference = null;
        String qry = "SELECT * FROM `user_event_preference` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                preference = new UserEventPreference();
                preference.setId(rs.getInt("id"));
                preference.setUser_id(id);
                preference.setCategory(rs.getString("category"));
                preference.setType(rs.getString("type"));
                preference.setPreferenceScore(rs.getInt("preference_score"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return preference;
    }
    public UserEventPreference getByUserIdCategoryAndTpe(int userId , String category , String type) {
        UserEventPreference preference = null;
        String qry = "SELECT * FROM `user_event_preference` WHERE `user_id`=? AND `category`=? AND `type`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            pstm.setString(2, category);
            pstm.setString(3, type);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                preference = new UserEventPreference();
                preference.setId(rs.getInt("id"));
                preference.setUser_id(userId);
                preference.setCategory(rs.getString("category"));
                preference.setType(rs.getString("type"));
                preference.setPreferenceScore(rs.getInt("preference_score"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return preference;
    }
    public List<Events> getRecommendedEvents(int userId, int limit) {
        List<Events> recommendedEvents = new ArrayList<>();
        String query = "SELECT e.* " +
                "FROM events e " +
                "INNER JOIN user_event_preference p ON p.category = e.category AND p.type = e.type " +
                "WHERE p.user_id = ? " +
                "ORDER BY p.preference_score DESC " +
                "LIMIT ?";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, userId);
            pstm.setInt(2, limit);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Events event = new Events();
                    event.setId(rs.getInt("id"));
                    event.setTitle(rs.getString("title"));
                    event.setDescription(rs.getString("description"));
                    event.setCategory(rs.getString("category"));
                    event.setType(rs.getString("type"));
                    event.setStartDate(rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate().atStartOfDay() : null);
                    event.setLocation(rs.getString("location"));
                    event.setImage(rs.getString("image"));
                    recommendedEvents.add(event);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recommended events: " + e.getMessage());
            throw new RuntimeException("Failed to fetch recommended events", e);
        }

        return recommendedEvents;
    }


}