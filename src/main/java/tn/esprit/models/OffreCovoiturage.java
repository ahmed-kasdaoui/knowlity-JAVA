package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OffreCovoiturage {
    private int id;
    private String depart;
    private int conducteurId;
    private String destination;
    private int matVehicule;
    private int placesDispo;
    private LocalDateTime date;
    private String statut; // Enum remplacé par String pour simplifier
    private float prix;
    private String img;
    private List<Reservation> reservations;

    public OffreCovoiturage() {
        this.date = LocalDateTime.now();
        this.reservations = new ArrayList<>();
    }

    public OffreCovoiturage(int id, String depart, int conducteurId, String destination, int matVehicule, int placesDispo, LocalDateTime date, String statut, float prix, String img) {
        this.id = id;
        this.depart = depart;
        this.conducteurId = conducteurId;
        this.destination = destination;
        this.matVehicule = matVehicule;
        this.placesDispo = placesDispo;
        this.date = date;
        this.statut = statut;
        this.prix = prix;
        this.img = img;
        this.reservations = new ArrayList<>();
    }

    public OffreCovoiturage(String depart, int conducteurId, String destination, int matVehicule, int placesDispo, LocalDateTime date, String statut, float prix, String img) {
        this.depart = depart;
        this.conducteurId = conducteurId;
        this.destination = destination;
        this.matVehicule = matVehicule;
        this.placesDispo = placesDispo;
        this.date = date;
        this.statut = statut;
        this.prix = prix;
        this.img = img;
        this.reservations = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public int getConducteurId() {
        return conducteurId;
    }

    public void setConducteurId(int conducteurId) {
        this.conducteurId = conducteurId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getMatVehicule() {
        return matVehicule;
    }

    public void setMatVehicule(int matVehicule) {
        this.matVehicule = matVehicule;
    }

    public int getPlacesDispo() {
        return placesDispo;
    }

    public void setPlacesDispo(int placesDispo) {
        this.placesDispo = placesDispo;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public String toString() {
        return "OffreCovoiturage{" +
                "id=" + id +
                ", depart='" + depart + '\'' +
                ", conducteurId=" + conducteurId +
                ", destination='" + destination + '\'' +
                ", matVehicule=" + matVehicule +
                ", placesDispo=" + placesDispo +
                ", date=" + date +
                ", statut='" + statut + '\'' +
                ", prix=" + prix +
                ", img='" + img + '\'' +
                "}\n";
    }
}