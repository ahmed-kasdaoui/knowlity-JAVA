package tn.esprit.models;

import java.time.LocalDateTime;

public class UserQuizResult {
    private int id;
    private int score;
    private LocalDateTime soumisLe;
    private String feedback;
    private int userId; // Remplacement de User par userId
    private Quiz quiz;
    private int duration;

    public UserQuizResult() {
    }

    public UserQuizResult(int id, int score, LocalDateTime soumisLe, String feedback, int userId, Quiz quiz, int duration) {
        this.id = id;
        this.score = score;
        this.soumisLe = soumisLe;
        this.feedback = feedback;
        this.userId = userId;
        this.quiz = quiz;
        this.duration = duration;
    }

    public UserQuizResult(int score, LocalDateTime soumisLe, String feedback, int userId, Quiz quiz, int duration) {
        this.score = score;
        this.soumisLe = soumisLe;
        this.feedback = feedback;
        this.userId = userId;
        this.quiz = quiz;
        this.duration = duration;
    }

    // Backward-compatible constructor (without duration)
    public UserQuizResult(int id, int score, LocalDateTime soumisLe, String feedback, int userId, Quiz quiz) {
        this(id, score, soumisLe, feedback, userId, quiz, 0); // Default duration to 0
    }

    public UserQuizResult(int score, LocalDateTime soumisLe, String feedback, int userId, Quiz quiz) {
        this(score, soumisLe, feedback, userId, quiz, 0); // Default duration to 0
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getSoumisLe() {
        return soumisLe;
    }

    public void setSoumisLe(LocalDateTime soumisLe) {
        this.soumisLe = soumisLe;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "UserQuizResult{" +
                "id=" + id +
                ", score=" + score +
                ", soumisLe=" + soumisLe +
                ", feedback='" + feedback + '\'' +
                ", userId=" + userId +
                ", quiz=" + (quiz != null ? quiz.getTitre() : "null") +
                ", duration=" + duration +
                "}\n";
    }
}