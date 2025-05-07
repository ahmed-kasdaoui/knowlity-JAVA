package Services;

import Entities.User;
import Utils.DataSource;
import java.sql.*;

public class UserService {
    private Connection conn;

    public UserService() {
        conn = DataSource.getInstance().getCnx();
    }

    public User authenticate(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ? AND password = ? AND deleted = false AND banned = false";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setNumTelephone(rs.getString("num_telephone"));
                user.setGenre(rs.getString("genre"));
                user.setDateNaissance(rs.getDate("date_naissance"));
                user.setVerificationCode(rs.getString("verification_code"));
                user.setBanned(rs.getBoolean("banned"));
                user.setDeleted(rs.getBoolean("deleted"));
                user.setGradeLevel(rs.getInt("grade_level"));
                user.setSpecialite(rs.getString("specialite"));
                user.setRole(rs.getString("roles"));
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'authentification : " + e.getMessage());
        }
        return null;
    }

    public void register(User user) {
        String query = "INSERT INTO user (email, password, nom, prenom, num_telephone, genre, date_naissance, verification_code, banned, deleted, grade_level, specialite, roles) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, false, false, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getPassword());
            pst.setString(3, user.getNom() != null ? user.getNom() : "");
            pst.setString(4, user.getPrenom() != null ? user.getPrenom() : "");
            pst.setString(5, user.getNumTelephone() != null ? user.getNumTelephone() : "");
            pst.setString(6, user.getGenre());
            pst.setDate(7, user.getDateNaissance() != null ? user.getDateNaissance() : new java.sql.Date(System.currentTimeMillis()));
            pst.setString(8, user.getVerificationCode() != null ? user.getVerificationCode() : "");
            pst.setInt(9, user.getGradeLevel());
            pst.setString(10, user.getSpecialite() != null ? user.getSpecialite() : "");
            pst.setString(11, user.getRole() != null ? user.getRole() : "user");
            
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) as count FROM user WHERE email = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de l'email : " + e.getMessage());
        }
        return false;
    }

    public void updateUser(User user) {
        String query = "UPDATE user SET nom = ?, prenom = ?, num_telephone = ?, genre = ?, " +
                      "date_naissance = ?, verification_code = ?, banned = ?, deleted = ?, " +
                      "grade_level = ?, specialite = ?, roles = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getNumTelephone());
            pst.setString(4, user.getGenre());
            pst.setDate(5, user.getDateNaissance());
            pst.setString(6, user.getVerificationCode());
            pst.setBoolean(7, user.isBanned());
            pst.setBoolean(8, user.isDeleted());
            pst.setInt(9, user.getGradeLevel());
            pst.setString(10, user.getSpecialite());
            pst.setString(11, user.getRole());
            pst.setInt(12, user.getId());
            
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }

    public void updatePassword(int userId, String newPassword) {
        String query = "UPDATE user SET password = ? WHERE id = ?";
        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, newPassword);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du mot de passe : " + e.getMessage());
        }
    }
}
