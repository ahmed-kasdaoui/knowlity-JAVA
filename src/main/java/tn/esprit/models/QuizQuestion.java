package tn.esprit.models;

import java.util.ArrayList;
import java.util.List;

public class QuizQuestion {
    private int id;
    private String type;
    private int points;
    private String texte;
    private int ordre;
    private Quiz quiz;
    private List<QuizResponse> reponses;

    public QuizQuestion() {
        this.reponses = new ArrayList<>();
    }

    public QuizQuestion(int id, String type, int points, String texte, int ordre, Quiz quiz) {
        this.id = id;
        this.type = type;
        this.points = points;
        this.texte = texte;
        this.ordre = ordre;
        this.quiz = quiz;
        this.reponses = new ArrayList<>();
    }

    public QuizQuestion(String type, int points, String texte, int ordre, Quiz quiz) {
        this.type = type;
        this.points = points;
        this.texte = texte;
        this.ordre = ordre;
        this.quiz = quiz;
        this.reponses = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public List<QuizResponse> getReponses() {
        return reponses;
    }

    public void setReponses(List<QuizResponse> reponses) {
        this.reponses = reponses;
    }

    @Override
    public String toString() {
        return "QuizQuestion{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", points=" + points +
                ", texte='" + texte + '\'' +
                ", ordre=" + ordre +
                ", quiz=" + (quiz != null ? quiz.getTitre() : "null") +
                "}\n";
    }
}