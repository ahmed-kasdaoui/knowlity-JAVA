package tn.esprit.models;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private int passagerId;
    private String statut; // Enum remplacé par String pour simplifier
    private OffreCovoiturage offre;
    private LocalDateTime createdAt;

    public Reservation() {
        this.createdAt = LocalDateTime.now();
    }

    public Reservation(int id, int passagerId, String statut, OffreCovoiturage offre, LocalDateTime createdAt) {
        this.id = id;
        this.passagerId = passagerId;
        this.statut = statut;
        this.offre = offre;
        this.createdAt = createdAt;
    }

    public Reservation(int passagerId, String statut, OffreCovoiturage offre) {
        this.passagerId = passagerId;
        this.statut = statut;
        this.offre = offre;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPassagerId() {
        return passagerId;
    }

    public void setPassagerId(int passagerId) {
        this.passagerId = passagerId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public OffreCovoiturage getOffre() {
        return offre;
    }

    public void setOffre(OffreCovoiturage offre) {
        this.offre = offre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", passagerId=" + passagerId +
                ", statut='" + statut + '\'' +
                ", offre=" + (offre != null ? offre.getId() : "null") +
                ", createdAt=" + createdAt +
                "}\n";
    }
}