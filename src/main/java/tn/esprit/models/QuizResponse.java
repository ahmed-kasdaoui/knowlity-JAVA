package tn.esprit.models;

public class QuizResponse {
    private int id;
    private String texte;
    private boolean estCorrecte;
    private QuizQuestion question;

    public QuizResponse() {
    }

    public QuizResponse(int id, String texte, boolean estCorrecte, QuizQuestion question) {
        this.id = id;
        this.texte = texte;
        this.estCorrecte = estCorrecte;
        this.question = question;
    }

    public QuizResponse(String texte, boolean estCorrecte, QuizQuestion question) {
        this.texte = texte;
        this.estCorrecte = estCorrecte;
        this.question = question;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public boolean isEstCorrecte() {
        return estCorrecte;
    }

    public void setEstCorrecte(boolean estCorrecte) {
        this.estCorrecte = estCorrecte;
    }

    public QuizQuestion getQuestion() {
        return question;
    }

    public void setQuestion(QuizQuestion question) {
        this.question = question;
    }

    @Override
    public String toString() {
        return "QuizResponse{" +
                "id=" + id +
                ", texte='" + texte + '\'' +
                ", estCorrecte=" + estCorrecte +
                ", question=" + (question != null ? question.getTexte() : "null") +
                "}\n";
    }
}