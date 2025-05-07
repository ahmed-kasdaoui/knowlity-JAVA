package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private int id;
    private String titre;
    private String description;
    private int scoreMax;
    private LocalDateTime dateLimite;
    private List<QuizQuestion> questions;
    private int cours_id;

    public Quiz() {
        this.questions = new ArrayList<>();
    }

    public Quiz(int id, String titre, String description, int scoreMax, LocalDateTime dateLimite) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.scoreMax = scoreMax;
        this.dateLimite = dateLimite;
        this.questions = new ArrayList<>();

    }

    public Quiz(String titre, String description, int scoreMax, LocalDateTime dateLimite,int cours_id) {
        this.titre = titre;
        this.description = description;
        this.scoreMax = scoreMax;
        this.dateLimite = dateLimite;
        this.questions = new ArrayList<>();
        this.cours_id = cours_id;
    }

    // Getters et Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(int scoreMax) {
        this.scoreMax = scoreMax;
    }

    public LocalDateTime getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public int getCours_id() {
        return cours_id;
    }

    public void setCours_id(int cours_id) {
        this.cours_id = cours_id;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", scoreMax=" + scoreMax +
                ", dateLimite=" + dateLimite +
                "}\n";
    }
}