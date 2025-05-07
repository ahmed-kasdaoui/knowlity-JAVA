package com.esprit.knowlity.Model;

import java.sql.Timestamp;

public class Reponse {
    private int id;
    private Integer questionId;
    private Integer evaluationId;
    private Integer resultatId;
    private String text;
    private Integer userId;
    private Integer note;
    private Timestamp startTime;
    private Timestamp submitTime;
    private String commentaire;
    private String status;
    private Timestamp correctedAt;
    private boolean isCorrect;
    private boolean plagiatSuspect;

    public Reponse() {
        // Default constructor
        this.plagiatSuspect = false; // Valeur par défaut
    }

    // Constructor
    public Reponse(int id, Integer questionId, Integer evaluationId, Integer resultatId, String text,
                   Integer userId, Integer note, Timestamp startTime, Timestamp submitTime,
                   String commentaire, String status, Timestamp correctedAt, boolean isCorrect,
                   boolean plagiatSuspect) {
        this.id = id;
        this.questionId = questionId;
        this.evaluationId = evaluationId;
        this.resultatId = resultatId;
        this.text = text;
        this.userId = userId;
        this.note = note;
        this.startTime = startTime;
        this.submitTime = submitTime;
        this.commentaire = commentaire;
        this.status = status;
        this.correctedAt = correctedAt;
        this.isCorrect = isCorrect;
        this.plagiatSuspect = plagiatSuspect;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Integer evaluationId) { this.evaluationId = evaluationId; }
    public Integer getResultatId() { return resultatId; }
    public void setResultatId(Integer resultatId) { this.resultatId = resultatId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getNote() { return note; }
    public void setNote(Integer note) { this.note = note; }
    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
    public Timestamp getSubmitTime() { return submitTime; }
    public void setSubmitTime(Timestamp submitTime) { this.submitTime = submitTime; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCorrectedAt() { return correctedAt; }
    public void setCorrectedAt(Timestamp correctedAt) { this.correctedAt = correctedAt; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    public boolean isPlagiatSuspect() {
        return plagiatSuspect;
    }
    public void setPlagiatSuspect(boolean plagiatSuspect) {
        this.plagiatSuspect = plagiatSuspect;
    }

    @Override
    public String toString() {
        String textPreview = text != null && text.length() > 25 ? text.substring(0, 25) + "..." : text;
        return "User " + userId + " | Q" + questionId + ": " + (textPreview != null ? textPreview : "[No Answer]");
    }
}