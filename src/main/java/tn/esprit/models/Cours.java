package tn.esprit.models;

import java.util.ArrayList;
import java.util.List;
import tn.esprit.services.ServiceCours;
import tn.knowlity.entity.User;

public class Cours {
    private User enseignant;
    private int id;
    private String title;
    private String description;
    private String urlImage;
    private List<Chapitre> chapitres;
    private Matiere matiere;
    private String langue;
    private int prix;
    private String lienDePaiment;

    public Cours() {
        this.chapitres = new ArrayList<>();
    }

    public User getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(User enseignant) {
        this.enseignant = enseignant;
    }

    public Cours(int id, String title, String description, String urlImage, Matiere matiere, String langue, int prix, String lienDePaiment) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.urlImage = urlImage;
        this.matiere = matiere;
        this.langue = langue;
        this.prix = prix;
        this.lienDePaiment = lienDePaiment;
        this.chapitres = new ArrayList<>();
    }

    public Cours(String title, String description, String urlImage, Matiere matiere, String langue, int prix, String lienDePaiment) {
        this.title = title;
        this.description = description;
        this.urlImage = urlImage;
        this.matiere = matiere;
        this.langue = langue;
        this.prix = prix;
        this.lienDePaiment = lienDePaiment;
        this.chapitres = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public List<Chapitre> getChapitres() {
        if (chapitres == null || chapitres.isEmpty()) {
            // Utiliser ServiceCours pour récupérer les chapitres
            ServiceCours serviceCours = new ServiceCours();
            chapitres = serviceCours.getChapitres(this);
        }
        return chapitres;
    }

    public void setChapitres(List<Chapitre> chapitres) {
        this.chapitres = chapitres;
    }

    public Matiere getMatiere() {
        return matiere;
    }

    public void setMatiere(Matiere matiere) {
        this.matiere = matiere;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public String getLienDePaiment() {
        return lienDePaiment;
    }

    public void setLienDePaiment(String lienDePaiment) {
        this.lienDePaiment = lienDePaiment;
    }

    @Override
    public String toString() {
        return "Cours{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", urlImage='" + urlImage + '\'' +
                ", matiere=" + (matiere != null ? matiere.getTitre() : "null") +
                ", langue='" + langue + '\'' +
                ", prix=" + prix +
                ", lienDePaiment='" + lienDePaiment + '\'' +
                "}\n";
    }


}