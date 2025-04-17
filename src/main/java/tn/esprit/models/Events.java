package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Events {
    private int id;
    private int organizer_id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String type;
    private Integer maxParticipants;
    private Integer seatsAvailable;
    private String location;
    private LocalDateTime createdAt;
    private String image;
    private String category;
    private Float longitude;
    private Float latitude;
    private List<EventRegistration> registrations;
    private List<UserEventPreference> userEventPreferences;

    public Events() {
        this.createdAt = LocalDateTime.now();
        this.registrations = new ArrayList<>();
        this.userEventPreferences = new ArrayList<>();
    }

    public Events(int id, String title, String description, LocalDateTime startDate, LocalDateTime endDate, String type, Integer maxParticipants, Integer seatsAvailable, String location, LocalDateTime createdAt, String image, String category, Float longitude, Float latitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.maxParticipants = maxParticipants;
        this.seatsAvailable = seatsAvailable;
        this.location = location;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.image = image;
        this.category = category;
        this.longitude = longitude;
        this.latitude = latitude;
        this.registrations = new ArrayList<>();
        this.userEventPreferences = new ArrayList<>();
    }

    public Events(String title, String description, LocalDateTime startDate, LocalDateTime endDate, String type, Integer maxParticipants, Integer seatsAvailable, String location, String image, String category,Float longitude, Float latitude) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
        this.maxParticipants = maxParticipants;
        this.seatsAvailable = seatsAvailable;
        this.location = location;
        this.createdAt = LocalDateTime.now();
        this.image = image;
        this.category = category;
        this.registrations = new ArrayList<>();
        this.userEventPreferences = new ArrayList<>();
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getOrganizerId() {
        return organizer_id;
    }

    public void setOrganizerId(int id) {
        this.organizer_id = id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(Integer seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public List<EventRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<EventRegistration> registrations) {
        this.registrations = registrations;
    }

    public List<UserEventPreference> getUserEventPreferences() {
        return userEventPreferences;
    }

    public void setUserEventPreferences(List<UserEventPreference> userEventPreferences) {
        this.userEventPreferences = userEventPreferences;
    }

    @Override
    public String toString() {
        return "Events{" +
                "id=" + id +
                "organizer id="+organizer_id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", type='" + type + '\'' +
                ", maxParticipants=" + maxParticipants +
                ", seatsAvailable=" + seatsAvailable +
                ", location='" + location + '\'' +
                ", createdAt=" + createdAt +
                ", image='" + image + '\'' +
                ", category='" + category + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                "}\n";
    }
}