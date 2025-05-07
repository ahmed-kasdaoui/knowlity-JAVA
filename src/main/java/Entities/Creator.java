package Entities;

import java.sql.Timestamp;

public class Creator {
    private int id;
    private String name;
    private String profile;
    private String achievements;
    private String image;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Creator() {}

    public Creator(String name, String profile, String achievements, String image) {
        this.name = name;
        this.profile = profile;
        this.achievements = achievements;
        this.image = image;
    }

    public Creator(int id, String name, String profile, String achievements, String image, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.profile = profile;
        this.achievements = achievements;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getProfile() { return profile; }
    public String getAchievements() { return achievements; }
    public String getImage() { return image; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setProfile(String profile) { this.profile = profile; }
    public void setAchievements(String achievements) { this.achievements = achievements; }
    public void setImage(String image) { this.image = image; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Creator{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", profile='" + profile + '\'' +
                ", achievements='" + achievements + '\'' +
                ", image='" + image + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
