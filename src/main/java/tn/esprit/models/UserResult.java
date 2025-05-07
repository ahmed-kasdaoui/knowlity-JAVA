package tn.esprit.models;

public class UserResult {
    private int id;
    private int userId;
    private int quizId;
    private int result;

    // Constructors
    public UserResult() {
    }

    public UserResult(int userId, int quizId, int result) {
        this.userId = userId;
        this.quizId = quizId;
        this.result = result;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}