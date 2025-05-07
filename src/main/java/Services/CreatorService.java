package Services;

import Entities.Creator;
import Interfaces.ICreator;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreatorService implements ICreator {
    private Connection connection;

    public CreatorService() {
        connection = DataSource.getInstance().getCnx();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String query = """
            CREATE TABLE IF NOT EXISTS creators (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(255) NOT NULL,
                profile TEXT NOT NULL,
                achievements TEXT NOT NULL,
                image VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        try (Statement st = connection.createStatement()) {
            st.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Creator creator) {
        String query = "INSERT INTO creators (name, profile, achievements, image) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, creator.getName());
            pst.setString(2, creator.getProfile());
            pst.setString(3, creator.getAchievements());
            pst.setString(4, creator.getImage());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Creator creator) {
        String query = "UPDATE creators SET name=?, profile=?, achievements=?, image=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, creator.getName());
            pst.setString(2, creator.getProfile());
            pst.setString(3, creator.getAchievements());
            pst.setString(4, creator.getImage());
            pst.setInt(5, creator.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String query = "DELETE FROM creators WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Creator getById(int id) {
        String query = "SELECT * FROM creators WHERE id=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToCreator(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Creator> getAll() {
        List<Creator> creators = new ArrayList<>();
        String query = "SELECT * FROM creators ORDER BY created_at DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                creators.add(mapResultSetToCreator(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return creators;
    }

    private Creator mapResultSetToCreator(ResultSet rs) throws SQLException {
        return new Creator(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("profile"),
            rs.getString("achievements"),
            rs.getString("image"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }
}
