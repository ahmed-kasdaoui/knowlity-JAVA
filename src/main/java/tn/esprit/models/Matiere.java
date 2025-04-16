package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Matiere {
    private int id;
    private String titre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Categorie categorie;
    private List<Cours> cours;
    private String prerequis;
    private String description;
    private String couleurTheme;

    public Matiere() {
        this.cours = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Matiere(int id, String titre, LocalDateTime createdAt, LocalDateTime updatedAt, Categorie categorie, String prerequis, String description, String couleurTheme) {
        this.id = id;
        this.titre = titre;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.categorie = categorie;
        this.prerequis = prerequis;
        this.description = description;
        this.couleurTheme = couleurTheme;
        this.cours = new ArrayList<>();
    }

    public Matiere(String titre, Categorie categorie, String prerequis, String description, String couleurTheme) {
        this.titre = titre;
        this.categorie = categorie;
        this.prerequis = prerequis;
        this.description = description;
        this.couleurTheme = couleurTheme;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.cours = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public List<Cours> getCours() {
        return cours;
    }

    public void setCours(List<Cours> cours) {
        this.cours = cours;
    }

    public String getPrerequis() {
        return prerequis;
    }

    public void setPrerequis(String prerequis) {
        this.prerequis = prerequis;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCouleurTheme() {
        return couleurTheme;
    }

    public void setCouleurTheme(String couleurTheme) {
        this.couleurTheme = couleurTheme;
    }

    @Override
    public String toString() {
        return "Matiere{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", categorie=" + (categorie != null ? categorie.getName() : "null") +
                ", prerequis='" + prerequis + '\'' +
                ", description='" + description + '\'' +
                ", couleurTheme='" + couleurTheme + '\'' +
                "}\n";
    }
}