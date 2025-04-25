package tn.esprit.models;

public class UserEventPreference {
    private int id;
    private int user_id;
    private String category;
    private String type;
    private int preferenceScore;

    public UserEventPreference() {
    }

    public UserEventPreference(int id, int user_id, String category , String type, int preferenceScore) {
        this.id = id;
        this.user_id = user_id;
        this.type = type;
        this.category = category;
        this.preferenceScore = preferenceScore;
    }

    public UserEventPreference(int user_id, String category, String type, int preferenceScore) {
        this.user_id = user_id;
        this.category = category;
        this.type = type;
        this.preferenceScore = preferenceScore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getCategory() {
        return category;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPreferenceScore() {
        return preferenceScore;
    }

    public void setPreferenceScore(int preferenceScore) {
        this.preferenceScore = preferenceScore;
    }

    @Override
    public String toString() {
        return "UserEventPreference{" +
                "id=" + id +
                ", user=" + user_id  +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                ", preferenceScore=" + preferenceScore +
                "}\n";
    }
}