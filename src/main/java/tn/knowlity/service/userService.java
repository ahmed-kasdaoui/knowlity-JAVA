package tn.knowlity.service;

import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;
import tn.knowlity.entity.User;
import tn.knowlity.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class userService implements IService {
    Connection cnx;

    public userService() {
        cnx = MyDataBase.getDataBase().getConnection();
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
        ps.setString(7, user.getImage());
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
        ps.setString(7, user.getImage());
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
            user.setImage(rs.getString("image"));
            user.setPassword(rs.getString("password"));
            user.setGoogle_id(rs.getString("google_id"));
            String rolesStr = rs.getString("roles");
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
            user.setImage(rs.getString("image"));
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
            user.setImage(rs.getString("image"));
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
                    user.setImage(rs.getString("image"));
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
        ps.setString(6, image);
        ps.setInt(7, id);
        ps.executeUpdate();
        System.out.println("Utilisateur modifié avec succès");
    }
    @Override
    public void ajouterverificationcode(String verifcode,String email) throws SQLException{
        String sql="update user  set verification_code=? where email=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, verifcode);
        ps.setString(2, email);

        ps.executeUpdate();
        System.out.println("code ajouté avec succées ");

    }
    @Override
    public void modifierpassword(String password,String confirmpassword,String mail) throws SQLException{

        String sql="update user  set password=?,confirm_password=? where email=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, password);
        ps.setString(2, confirmpassword);
        ps.setString(3, mail);

        ps.executeUpdate();
        System.out.println("PASSWORD CHANGé avec succés ");

    }

    @Override
    public User recherparemail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email=?";
        User user = null;

        try (PreparedStatement ps1 = cnx.prepareStatement(sql)) {
            ps1.setString(1, email);

            try (ResultSet rs = ps1.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setNum_telephone(rs.getInt("num_telephone"));
                    user.setDate_naissance(rs.getDate("date_naissance"));
                    user.setLocalisation(rs.getString("localisation"));
                    user.setImage(rs.getString("image"));
                    user.setPassword(rs.getString("password"));
                    user.setVerification_code(rs.getString("verification_code"));

                }
            }
        }
        return user;
    }
}