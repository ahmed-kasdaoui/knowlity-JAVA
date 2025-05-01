package tn.esprit.models;

public class UserQuizSelection {
    private int id;
    private QuizResponse responseSelectionnee;
    private boolean estCorrecte;
    private QuizQuestion question;

    public UserQuizSelection() {
    }

    public UserQuizSelection(int id, QuizResponse responseSelectionnee, boolean estCorrecte, QuizQuestion question) {
        this.id = id;
        this.responseSelectionnee = responseSelectionnee;
        this.estCorrecte = estCorrecte;
        this.question = question;
    }

    public UserQuizSelection(QuizResponse responseSelectionnee, boolean estCorrecte, QuizQuestion question) {
        this.responseSelectionnee = responseSelectionnee;
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

    public QuizResponse getResponseSelectionnee() {
        return responseSelectionnee;
    }

    public void setResponseSelectionnee(QuizResponse responseSelectionnee) {
        this.responseSelectionnee = responseSelectionnee;
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
        return "UserQuizSelection{" +
                "id=" + id +
                ", responseSelectionnee=" + (responseSelectionnee != null ? responseSelectionnee.getTexte() : "null") +
                ", estCorrecte=" + estCorrecte +
                ", question=" + (question != null ? question.getTexte() : "null") +
                "}\n";
    }
}