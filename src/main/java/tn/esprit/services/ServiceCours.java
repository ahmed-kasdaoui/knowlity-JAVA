package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Chapitre;
import tn.esprit.models.Cours;
import tn.esprit.models.Matiere;
import tn.esprit.utils.MyDataBase;
import tn.esprit.services.ServiceChapitre;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.services.ServiceFavoris;
import tn.esprit.services.ServiceInscription;
import tn.knowlity.service.userService;

public class ServiceCours implements IService<Cours> {
    private Connection cnx;
    private ServiceMatiere serviceMatiere;
    private userService userService;

    public ServiceCours() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceMatiere = new ServiceMatiere();
        userService = new userService();
    }

    @Override
    public void add(Cours cours) {
        String qry = "INSERT INTO `cours` (`title`, `description`, `url_image`, `matiere_id`, `langue`, `prix`, `lien_de_paiment`,`enseignant_id`) VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, cours.getTitle());
            pstm.setString(2, cours.getDescription());
            pstm.setString(3, cours.getUrlImage());
            pstm.setInt(4, cours.getMatiere().getId());
            pstm.setString(5, cours.getLangue());
            pstm.setInt(6, cours.getPrix());
            pstm.setString(7, cours.getLienDePaiment());
            pstm.setInt(8, cours.getEnseignant().getId());
            pstm.executeUpdate();
            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                cours.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Cours> getAll() {
        List<Cours> coursList = new ArrayList<>();
        String qry = "SELECT * FROM `cours`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                c.setMatiere(matiere);
                c.setLangue(rs.getString("langue"));
                c.setPrix(rs.getInt("prix"));
                c.setLienDePaiment(rs.getString("lien_de_paiment"));
                
                // Set teacher information
                try {
                    int enseignantId = rs.getInt("enseignant_id");
                    if (enseignantId > 0) {
                        c.setEnseignant(userService.recherparid(enseignantId));
                    }
                } catch (SQLException e) {
                    System.out.println("Error retrieving teacher info: " + e.getMessage());
                }
                
                coursList.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coursList;
    }

    @Override
    public void update(Cours cours) {
        String qry = "UPDATE `cours` SET `title`=?, `description`=?, `url_image`=?, `matiere_id`=?, `langue`=?, `prix`=?, `lien_de_paiment`=?, `enseignant_id`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, cours.getTitle());
            pstm.setString(2, cours.getDescription());
            pstm.setString(3, cours.getUrlImage());
            pstm.setInt(4, cours.getMatiere().getId());
            pstm.setString(5, cours.getLangue());
            pstm.setInt(6, cours.getPrix());
            pstm.setString(7, cours.getLienDePaiment());
            pstm.setInt(8, cours.getEnseignant().getId());
            pstm.setInt(9, cours.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delete(Cours cours) {
        String qry = "DELETE FROM `cours` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, cours.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Helper method to get a course by ID
    public Cours getById(int id) {
        String qry = "SELECT * FROM `cours` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                Cours cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setTitle(rs.getString("title"));
                cours.setDescription(rs.getString("description"));
                cours.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                cours.setMatiere(matiere);
                cours.setLangue(rs.getString("langue"));
                cours.setPrix(rs.getInt("prix"));
                cours.setLienDePaiment(rs.getString("lien_de_paiment"));
                
                // Set teacher information
                try {
                    int enseignantId = rs.getInt("enseignant_id");
                    if (enseignantId > 0) {
                        cours.setEnseignant(userService.recherparid(enseignantId));
                    }
                } catch (SQLException e) {
                    System.out.println("Error retrieving teacher info: " + e.getMessage());
                }
                
                return cours;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Helper method to get chapters for a course
    public List<Chapitre> getChapitres(Cours cours) {
        List<Chapitre> chapitres = new ArrayList<>();
        String qry = "SELECT * FROM `chapitre` WHERE `cours_id`=? ORDER BY `chap_order`";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, cours.getId());
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Chapitre c = new Chapitre();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setChapOrder(rs.getInt("chap_order"));
                c.setCours(cours);
                c.setContenu(rs.getString("contenu"));
                c.setDureeEstimee(rs.getInt("duree_estimee"));
                c.setNbrVues(rs.getInt("nbr_vues"));
                chapitres.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return chapitres;
    }

    // Helper method to get courses by teacher ID
    public List<Cours> getByEnsignant(int id) {
        List<Cours> coursList = new ArrayList<>();
        String qry = "SELECT * FROM `cours` WHERE `enseignant_id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                c.setMatiere(matiere);
                c.setLangue(rs.getString("langue"));
                c.setPrix(rs.getInt("prix"));
                c.setLienDePaiment(rs.getString("lien_de_paiment"));
                
                // Set teacher information
                try {
                    int enseignantId = rs.getInt("enseignant_id");
                    if (enseignantId > 0) {
                        c.setEnseignant(userService.recherparid(enseignantId));
                    }
                } catch (SQLException e) {
                    System.out.println("Error retrieving teacher info: " + e.getMessage());
                }
                
                coursList.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coursList;
    }

    // Helper method to get favorite courses
    public List<Cours> getAllFavoris(int userId) {
        List<Cours> coursFavoris = new ArrayList<>();
        try {
            ServiceFavoris serviceFavoris = new ServiceFavoris();
            List<Integer> coursIds = serviceFavoris.getCoursIdsFavoris(userId);

            if (coursIds != null && !coursIds.isEmpty()) {
                for (Integer coursId : coursIds) {
                    Cours cours = getById(coursId);
                    if (cours != null) {
                        coursFavoris.add(cours);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving favorite courses: " + e.getMessage());
        }
        return coursFavoris;
    }

    public Cours getCoursById(int coursId) {
        String qry = "SELECT * FROM cours WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, coursId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setTitle(rs.getString("title"));
                c.setDescription(rs.getString("description"));
                c.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                c.setMatiere(matiere);
                c.setLangue(rs.getString("langue"));
                c.setPrix(rs.getInt("prix"));
                c.setLienDePaiment(rs.getString("lien_de_paiment"));

                // Set teacher information
                try {
                    int enseignantId = rs.getInt("enseignant_id");
                    if (enseignantId > 0) {
                        c.setEnseignant(userService.recherparid(enseignantId));
                    }
                } catch (SQLException e) {
                    System.out.println("Error retrieving teacher info: " + e.getMessage());
                }

                return c;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }}