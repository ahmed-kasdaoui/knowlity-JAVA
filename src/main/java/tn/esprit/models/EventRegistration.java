package tn.esprit.models;

import java.time.LocalDateTime;

public class EventRegistration {
    private int id;
    private Events event;
    private LocalDateTime registrationDate;
    private String status;
    private boolean disabledParking;
    private String comingFrom;
    private String name;
    private Integer placesReserved;
    private int user_id ;

    public EventRegistration() {
        this.registrationDate = LocalDateTime.now();
        this.status = "pending";
    }

    public EventRegistration(int id,int user_id, Events event, LocalDateTime registrationDate, String status, boolean disabledParking, String comingFrom, String name, Integer placesReserved) {
        this.id = id;
        this.user_id=user_id;
        this.event = event;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDateTime.now();
        this.status = status != null ? status : "pending";
        this.disabledParking = disabledParking;
        this.comingFrom = comingFrom;
        this.name = name;
        this.placesReserved = placesReserved;
    }

    public EventRegistration(Events event,int user_id, String comingFrom, String name, Integer placesReserved) {
        this.event = event;
        this.user_id=user_id;
        this.registrationDate = LocalDateTime.now();
        this.status = "pending";
        this.comingFrom = comingFrom;
        this.name = name;
        this.placesReserved = placesReserved;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }
    public Events getEvent() {
        return event;
    }

    public void setEvent(Events event) {
        this.event = event;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDisabledParking() {
        return disabledParking;
    }

    public void setDisabledParking(boolean disabledParking) {
        this.disabledParking = disabledParking;
    }

    public String getComingFrom() {
        return comingFrom;
    }

    public void setComingFrom(String comingFrom) {
        this.comingFrom = comingFrom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPlacesReserved() {
        return placesReserved;
    }

    public void setPlacesReserved(Integer placesReserved) {
        this.placesReserved = placesReserved;
    }

    @Override
    public String toString() {
        return "EventRegistration{" +
                "id=" + id +
                "user id="+user_id +
                ", event=" + (event != null ? event.getTitle() : "null") +
                ", registrationDate=" + registrationDate +
                ", status='" + status + '\'' +
                ", disabledParking=" + disabledParking +
                ", comingFrom='" + comingFrom + '\'' +
                ", name='" + name + '\'' +
                ", placesReserved=" + placesReserved +
                "}\n";
    }
}