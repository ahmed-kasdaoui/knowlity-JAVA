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
public class ServiceCours implements IService<Cours> {
    private Connection cnx;
    private ServiceMatiere serviceMatiere;

    public ServiceCours() {
        cnx = MyDataBase.getInstance().getCnx();
        serviceMatiere = new ServiceMatiere();
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
    public List<Cours> getAllFavoris(int userId) {
        List<Cours> coursFavoris = new ArrayList<>(); // Initialisation de la liste des cours favoris
        try {
            // Récupération des IDs des cours favoris de l'utilisateur
            ServiceFavoris serviceFavoris = new ServiceFavoris();
            List<Integer> coursIds = serviceFavoris.getCoursIdsFavoris(userId);

            // Si des IDs sont trouvés, récupération des détails des cours correspondants
            if (coursIds != null && !coursIds.isEmpty()) {
                for (Integer coursId : coursIds) {
                    String qry = "SELECT * FROM `cours` WHERE id = ?";
                    try (PreparedStatement pst = cnx.prepareStatement(qry)) {
                        pst.setInt(1, coursId);
                        ResultSet rs = pst.executeQuery();
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
                            coursFavoris.add(c);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
        return coursFavoris;
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
                coursList.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coursList;
    }

    @Override
    public void update(Cours cours) {
        String qry = "UPDATE `cours` SET `title`=?, `description`=?, `url_image`=?, `matiere_id`=?, `langue`=?, `prix`=?, `lien_de_paiment`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, cours.getTitle());
            pstm.setString(2, cours.getDescription());
            pstm.setString(3, cours.getUrlImage());
            pstm.setInt(4, cours.getMatiere().getId());
            pstm.setString(5, cours.getLangue());
            pstm.setInt(6, cours.getPrix());
            pstm.setString(7, cours.getLienDePaiment());
            pstm.setInt(8, cours.getId());
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

    public Cours getById(int id) {
        Cours cours = null;
        String qry = "SELECT * FROM `cours` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, id);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setTitle(rs.getString("title"));
                cours.setDescription(rs.getString("description"));
                cours.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                cours.setMatiere(matiere);
                cours.setLangue(rs.getString("langue"));
                cours.setPrix(rs.getInt("prix"));
                cours.setLienDePaiment(rs.getString("lien_de_paiment"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cours;
    }
    public List<Chapitre> getChapitres(Cours cours) {
        List<Chapitre> chapitres = new ArrayList<>();


        String qry = "SELECT * FROM `chapitre` WHERE `cours_id` = ?";
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
            System.out.println("Retrieved " + chapitres.size() + " chapters for course ID: " + cours.getId());
        } catch (SQLException e) {
            System.out.println("Error retrieving chapters: " + e.getMessage());
        }
        return chapitres;
    }

    public List<Cours> getByEnsignant(int id ) {
        List<Cours> coursList = new ArrayList<>();
        String qry = "SELECT * FROM `cours` where enseignant_id="+id;
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
                coursList.add(c);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return coursList;
    }

    public Cours getCoursById(int coursId) {
        Cours cours = null;
        String qry = "SELECT * FROM `cours` WHERE `id` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, coursId);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                cours = new Cours();
                cours.setId(rs.getInt("id"));
                cours.setTitle(rs.getString("title"));
                cours.setDescription(rs.getString("description"));
                cours.setUrlImage(rs.getString("url_image"));
                Matiere matiere = serviceMatiere.getById(rs.getInt("matiere_id"));
                cours.setMatiere(matiere);
                cours.setLangue(rs.getString("langue"));
                cours.setPrix(rs.getInt("prix"));
                cours.setLienDePaiment(rs.getString("lien_de_paiment"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération du cours par ID : " + e.getMessage());
        }
        return cours;
    }

}