package tn.knowlity.service;

import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;
import tn.knowlity.entity.User;
import tn.knowlity.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class userService implements IService {
    Connection cnx;
    private final ScheduledExecutorService scheduler;

    public userService() {
        cnx = MyDataBase.getDataBase().getConnection();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private String normalizeImagePath(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null; // Or return a default image path if needed
        }
        // If already a relative path starting with /images/, return as is
        if (imagePath.startsWith("/images/")) {
            return imagePath;
        }
        // If it's a file name (e.g., image.jpg), prepend /images/
        if (!imagePath.startsWith("/") && !imagePath.contains(":")) {
            return "/images/" + imagePath;
        }
        // If it's a full path (e.g., src/main/resources/images/image.jpg), extract the file name
        if (imagePath.contains("src/main/resources/images/")) {
            return "/images/" + imagePath.substring(imagePath.lastIndexOf("images/") + 7);
        }
        // If it's a file:/ URL, extract the file name
        if (imagePath.startsWith("file:/")) {
            String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            return "/images/" + fileName;
        }
        // Otherwise, assume it's a file name and prepend /images/
        return "/images/" + new java.io.File(imagePath).getName();
    }

    @Override
    public void ajouterEtudiant(User user) throws SQLException {
        String sql = "INSERT INTO user (nom, prenom, date_naissance, email, num_telephone, password, image, genre, localisation, created_at, last_login, confirm_password, verification_code, banned, deleted, grade_level, specialite, google_id, roles) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setDate(3, user.getDate_naissance() != null ? new java.sql.Date(user.getDate_naissance().getTime()) : null);
        ps.setString(4, user.getEmail());
        ps.setInt(5, user.getNum_telephone());
        ps.setString(6, user.getPassword());
        ps.setString(7, normalizeImagePath(user.getImage())); // Normalize to relative path
        ps.setString(8, user.getGenre());
        ps.setString(9, user.getLocalisation());
        ps.setDate(10, java.sql.Date.valueOf(java.time.LocalDate.now()));
        ps.setDate(11, null);
        ps.setString(12, user.getConfirm_password());
        ps.setString(13, user.getVerification_code());
        ps.setInt(14, user.getBanned());
        ps.setInt(15, user.getDeleted());
        ps.setInt(16, user.getGrade_level());
        ps.setString(17, user.getSpecialite() != null ? user.getSpecialite() : "0");
        ps.setString(18, user.getGoogle_id());
        Gson gson = new Gson();
        String rolesJson = gson.toJson(user.getRoles());
        ps.setString(19, rolesJson);
        ps.executeUpdate();
        System.out.println("Utilisateur étudiant ajouté avec succès");
    }

    @Override
    public void ajouterEnseignant(User user) throws SQLException {
        String sql = "INSERT INTO user (nom, prenom, date_naissance, email, num_telephone, password, image, genre, localisation, created_at, last_login, confirm_password, verification_code, banned, deleted, grade_level, specialite, google_id, roles) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getNom());
        ps.setString(2, user.getPrenom());
        ps.setDate(3, user.getDate_naissance() != null ? new java.sql.Date(user.getDate_naissance().getTime()) : null);
        ps.setString(4, user.getEmail());
        ps.setInt(5, user.getNum_telephone());
        ps.setString(6, user.getPassword());
        ps.setString(7, normalizeImagePath(user.getImage())); // Normalize to relative path
        ps.setString(8, user.getGenre());
        ps.setString(9, user.getLocalisation());
        ps.setDate(10, java.sql.Date.valueOf(java.time.LocalDate.now()));
        ps.setDate(11, null);
        ps.setString(12, user.getConfirm_password());
        ps.setString(13, user.getVerification_code());
        ps.setInt(14, user.getBanned());
        ps.setInt(15, user.getDeleted());
        ps.setInt(16, 0);
        ps.setString(17, user.getSpecialite());
        ps.setString(18, user.getGoogle_id());
        Gson gson = new Gson();
        String rolesJson = gson.toJson(user.getRoles());
        ps.setString(19, rolesJson);
        ps.executeUpdate();
        System.out.println("Utilisateur enseignant ajouté avec succès");
    }

    @Override
    public void supprimerUser(User user) throws SQLException {
        String sql = "DELETE FROM user WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, user.getEmail());
        ps.executeUpdate();
        System.out.println("Utilisateur supprimé");
    }

    @Override
    public User Authentification(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));
            user.setNum_telephone(rs.getInt("num_telephone"));
            user.setDate_naissance(rs.getDate("date_naissance"));
            user.setLocalisation(rs.getString("localisation"));
            user.setImage((rs.getString("image")));
            user.setPassword(rs.getString("password"));
            user.setGoogle_id(rs.getString("google_id"));
            String rolesStr = rs.getString("roles");
            user.setBanned(rs.getInt("banned"));
            user.setCreted_at(rs.getDate("created_at"));
            if (rolesStr != null && !rolesStr.isEmpty()) {
                Gson gson = new Gson();
                String[] roles = gson.fromJson(rolesStr, String[].class);
                user.setRoles(roles);
            }
            boolean valid = BCrypt.checkpw(password, user.getPassword());
            if (!valid) {
                return null;
            }
            return user;
        }
        return null;
    }

    public User authenticateWithGoogle(String googleId) throws SQLException {
        String sql = "SELECT * FROM user WHERE google_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, googleId);

        ResultSet rs = ps.executeQuery();
        if (rs != null && rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));
            user.setNum_telephone(rs.getInt("num_telephone"));
            user.setDate_naissance(rs.getDate("date_naissance"));
            user.setLocalisation(rs.getString("localisation"));
            user.setImage(normalizeImagePath(rs.getString("image"))); // Normalize to relative path
            user.setGoogle_id(rs.getString("google_id"));
            String rolesStr = rs.getString("roles");
            if (rolesStr != null && !rolesStr.isEmpty()) {
                Gson gson = new Gson();
                String[] roles = gson.fromJson(rolesStr, String[].class);
                user.setRoles(roles);
            }
            return user;
        }
        return null;
    }

    @Override
    public void bannneruser(User user) throws SQLException {
        LocalDateTime bannedUntil = LocalDateTime.now().plusMinutes(1);
        Timestamp bannedUntilTimestamp = Timestamp.valueOf(bannedUntil);
        String sql = "UPDATE user SET banned = ?, created_at = ? WHERE email = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, 1);
            ps.setTimestamp(2, bannedUntilTimestamp);
            ps.setString(3, user.getEmail());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Utilisateur avec email " + user.getEmail() + " banni avec succès.");
                // Schedule unbanning task
                scheduleUnban(user.getEmail());
            } else {
                System.out.println("Aucun utilisateur trouvé avec email " + user.getEmail() + ".");
            }
        }
    }

    private void scheduleUnban(String email) {
        scheduler.schedule(() -> {
            try {
                unbanUser(email);
                System.out.println("Utilisateur avec email " + email + " débanni avec succès.");
            } catch (SQLException e) {
                System.err.println("Erreur lors du débannissement de l'utilisateur avec email " + email + ": " + e.getMessage());
            }
        }, 1, TimeUnit.MINUTES);
    }

    private void unbanUser(String email) throws SQLException {
        String sql = "UPDATE user SET banned = ? WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, 0);
            ps.setString(2, email);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Utilisateur avec email " + email + " débanni.");
            } else {
                System.out.println("Aucun utilisateur trouvé avec email " + email + " pour débannissement.");
            }
        }
    }

    @Override
    public List<User> afficherdetailsuser() throws SQLException {
        String sql = "SELECT nom, prenom, email, num_telephone, banned, image, roles FROM user";
        Statement ps = cnx.createStatement();
        ResultSet rs = ps.executeQuery(sql);
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            User user = new User();
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));
            user.setNum_telephone(rs.getInt("num_telephone"));
            user.setBanned(rs.getInt("banned"));
            user.setImage((rs.getString("image"))); // Normalize to relative path
            String rolesStr = rs.getString("roles");
            if (rolesStr != null && !rolesStr.isEmpty()) {
                Gson gson = new Gson();
                String[] roles = gson.fromJson(rolesStr, String[].class);
                user.setRoles(roles);
            }
            users.add(user);
        }
        return users;
    }

    public void bannner1user(User user) throws SQLException{
        LocalDateTime bannedUntil = LocalDateTime.now().plusMinutes(15);  // Ajoute 15 minutes

        // Convertir LocalDateTime en java.sql.Timestamp
        Timestamp bannedUntilTimestamp = Timestamp.valueOf(bannedUntil);
        String sql = "UPDATE user SET banned = ? ,created_at = ? WHERE email = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, 1);
            ps.setTimestamp(2, bannedUntilTimestamp);

            ps.setString(4, user.getEmail());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Utilisateur avec ID " + user.getEmail()+ " banni avec succès.");
            } else {
                System.out.println("Aucun utilisateur trouvé avec ID " + user.getEmail() + ".");
            }
        }
    }


    public void unbannneruser(User user) throws SQLException{
        String sql="update user  set banned=?  where email=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1,0);

        ps.setString(2, user.getEmail());

        ps.executeUpdate();
        System.out.println("code ajouté avec succées ");

    }
    @Override
    public List<User> recherparnom(String nom) throws SQLException {
        String sql = "SELECT nom, prenom, email, num_telephone, roles FROM user WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ? OR num_telephone LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "%" + nom + "%");
        ps.setString(2, "%" + nom + "%");
        ps.setString(3, "%" + nom + "%");
        ps.setString(4, "%" + nom + "%");

        ResultSet rs = ps.executeQuery();
        List<User> users = new ArrayList<>();
        while (rs.next()) {
            User user = new User();
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            user.setEmail(rs.getString("email"));
            user.setNum_telephone(rs.getInt("num_telephone"));
            String rolesStr = rs.getString("roles");
            if (rolesStr != null && !rolesStr.isEmpty()) {
                Gson gson = new Gson();
                String[] roles = gson.fromJson(rolesStr, String[].class);
                user.setRoles(roles);
            }
            users.add(user);
        }
        return users;
    }

    @Override
    public User recherparid(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        User user = null;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setNum_telephone(rs.getInt("num_telephone"));
                    user.setDate_naissance(rs.getDate("date_naissance"));
                    user.setLocalisation(rs.getString("localisation"));
                    user.setImage((rs.getString("image"))); // Normalize to relative path
                    user.setPassword(rs.getString("password"));
                    user.setGoogle_id(rs.getString("google_id"));
                }
            }
        }
        return user;
    }

    @Override
    public void modifier(String nom, String prenom, String email, String localisation, int numtel, String image, int id) throws SQLException {
        String sql = "UPDATE user SET nom = ?, prenom = ?, email = ?, localisation = ?, num_telephone = ?, image = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, nom);
        ps.setString(2, prenom);
        ps.setString(3, email);
        ps.setString(4, localisation);
        ps.setInt(5, numtel);
        ps.setString(6, normalizeImagePath(image)); // Normalize to relative path
        ps.setInt(7, id);
        ps.executeUpdate();
        System.out.println("Utilisateur modifié avec succès");
    }

    @Override
    public void ajouterverificationcode(String verifcode, String email) throws SQLException {
        String sql = "UPDATE user SET verification_code = ? WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, verifcode);
        ps.setString(2, email);

        ps.executeUpdate();
        System.out.println("Code ajouté avec succès");
    }

    @Override
    public void modifierpassword(String password, String confirmpassword, String mail) throws SQLException {
        String sql = "UPDATE user SET password = ?, confirm_password = ? WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, password);
        ps.setString(2, confirmpassword);
        ps.setString(3, mail);

        ps.executeUpdate();
        System.out.println("Mot de passe changé avec succès");
    }

    @Override
    public User recherparemail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        User user = null;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setNum_telephone(rs.getInt("num_telephone"));
                    user.setDate_naissance(rs.getDate("date_naissance"));
                    user.setLocalisation(rs.getString("localisation"));
                    user.setImage((rs.getString("image")));
                    user.setPassword(rs.getString("password"));
                    user.setVerification_code(rs.getString("verification_code"));
                    user.setBanned(rs.getInt("banned"));
                }
            }
        }
        return user;
    }

    // Optional: Shutdown scheduler when service is no longer needed
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}