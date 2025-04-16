package com.esprit.knowlity.Model;

import java.sql.Timestamp;

public class Evaluation {
    private int id;
    private int coursId;
    private String title;
    private String description;
    private int maxScore;
    private Timestamp createAt;
    private Timestamp deadline;
    private Integer badgeThreshold;
    private String badgeImage;
    private String badgeTitle;

    // Constructor
    public Evaluation(int id, int coursId, String title, String description, int maxScore,
                      Timestamp createAt, Timestamp deadline, Integer badgeThreshold,
                      String badgeImage, String badgeTitle) {
        this.id = id;
        this.coursId = coursId;
        this.title = title;
        this.description = description;
        this.maxScore = maxScore;
        this.createAt = createAt;
        this.deadline = deadline;
        this.badgeThreshold = badgeThreshold;
        this.badgeImage = badgeImage;
        this.badgeTitle = badgeTitle;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }
    public Timestamp getCreateAt() { return createAt; }
    public void setCreateAt(Timestamp createAt) { this.createAt = createAt; }
    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }
    public Integer getBadgeThreshold() { return badgeThreshold; }
    public void setBadgeThreshold(Integer badgeThreshold) { this.badgeThreshold = badgeThreshold; }
    public String getBadgeImage() { return badgeImage; }
    public void setBadgeImage(String badgeImage) { this.badgeImage = badgeImage; }
    public String getBadgeTitle() { return badgeTitle; }
    public void setBadgeTitle(String badgeTitle) { this.badgeTitle = badgeTitle; }
}