package com.esprit.knowlity.Model;

public class Cours {
    private int id;
    private int matiereId;
    private int enseignantId;
    private String title;
    private String description;
    private String urlImage;
    private String langue;
    private int prix;
    private String lienDePaiment;

    public Cours(int id, int matiereId, int enseignantId, String title, String description,
                 String urlImage, String langue, int prix, String lienDePaiment) {
        this.id = id;
        this.matiereId = matiereId;
        this.enseignantId = enseignantId;
        this.title = title;
        this.description = description;
        this.urlImage = urlImage;
        this.langue = langue;
        this.prix = prix;
        this.lienDePaiment = lienDePaiment;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMatiereId() { return matiereId; }
    public void setMatiereId(int matiereId) { this.matiereId = matiereId; }
    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrlImage() { return urlImage; }
    public void setUrlImage(String urlImage) { this.urlImage = urlImage; }
    public String getLangue() { return langue; }
    public void setLangue(String langue) { this.langue = langue; }
    public int getPrix() { return prix; }
    public void setPrix(int prix) { this.prix = prix; }
    public String getLienDePaiment() { return lienDePaiment; }
    public void setLienDePaiment(String lienDePaiment) { this.lienDePaiment = lienDePaiment; }

    // Override toString to display the course title in ComboBox
    @Override
    public String toString() {
        return title;
    }
}