package tn.esprit.models;

public class UserEventPreference {
    private int id;
    private Events event;
    private String category;
    private int preferenceScore;

    public UserEventPreference() {
    }

    public UserEventPreference(int id, Events event, String category, int preferenceScore) {
        this.id = id;
        this.event = event;
        this.category = category;
        this.preferenceScore = preferenceScore;
    }

    public UserEventPreference(Events event, String category, int preferenceScore) {
        this.event = event;
        this.category = category;
        this.preferenceScore = preferenceScore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Events getEvent() {
        return event;
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    public String getCategory() {
        return category;
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
                ", event=" + (event != null ? event.getTitle() : "null") +
                ", category='" + category + '\'' +
                ", preferenceScore=" + preferenceScore +
                "}\n";
    }
}