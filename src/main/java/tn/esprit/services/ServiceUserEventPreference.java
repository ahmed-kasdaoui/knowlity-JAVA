package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.UserEventPreference;
import tn.esprit.models.Events;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUserEventPreference implements IService<UserEventPreference> {
    private Connection cnx;
    private ServiceEvents serviceEvents;

    public ServiceUserEventPreference() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceEvents = new ServiceEvents();
    }

    @Override
    public void add(UserEventPreference preference) {
        String qry = "INSERT INTO `user_event_preference` (`event_id`, `category`, `preference_score`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setInt(1, preference.getEvent().getId());
            pstm.setString(2, preference.getCategory());
            pstm.setInt(3, preference.getPreferenceScore());
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
                Events event = serviceEvents.getById(rs.getInt("event_id"));
                p.setEvent(event);
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
        String qry = "UPDATE `user_event_preference` SET `event_id`=?, `category`=?, `preference_score`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, preference.getEvent().getId());
            pstm.setString(2, preference.getCategory());
            pstm.setInt(3, preference.getPreferenceScore());
            pstm.setInt(4, preference.getId());
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
                Events event = serviceEvents.getById(rs.getInt("event_id"));
                preference.setEvent(event);
                preference.setCategory(rs.getString("category"));
                preference.setPreferenceScore(rs.getInt("preference_score"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return preference;
    }
}