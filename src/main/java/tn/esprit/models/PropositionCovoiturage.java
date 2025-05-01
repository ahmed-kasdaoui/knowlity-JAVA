package tn.esprit.models;

import java.time.LocalDateTime;

public class PropositionCovoiturage {
    private int id;
    private int conducteurId;
    private String statut; // Enum remplacé par String pour simplifier
    private DemandeCovoiturage demande;
    private int placesDispo;
    private LocalDateTime createdAt;

    public PropositionCovoiturage() {
        this.createdAt = LocalDateTime.now();
    }

    public PropositionCovoiturage(int id, int conducteurId, String statut, DemandeCovoiturage demande, int placesDispo, LocalDateTime createdAt) {
        this.id = id;
        this.conducteurId = conducteurId;
        this.statut = statut;
        this.demande = demande;
        this.placesDispo = placesDispo;
        this.createdAt = createdAt;
    }

    public PropositionCovoiturage(int conducteurId, String statut, DemandeCovoiturage demande, int placesDispo) {
        this.conducteurId = conducteurId;
        this.statut = statut;
        this.demande = demande;
        this.placesDispo = placesDispo;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConducteurId() {
        return conducteurId;
    }

    public void setConducteurId(int conducteurId) {
        this.conducteurId = conducteurId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public DemandeCovoiturage getDemande() {
        return demande;
    }

    public void setDemande(DemandeCovoiturage demande) {
        this.demande = demande;
    }

    public int getPlacesDispo() {
        return placesDispo;
    }

    public void setPlacesDispo(int placesDispo) {
        this.placesDispo = placesDispo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PropositionCovoiturage{" +
                "id=" + id +
                ", conducteurId=" + conducteurId +
                ", statut='" + statut + '\'' +
                ", demande=" + (demande != null ? demande.getId() : "null") +
                ", placesDispo=" + placesDispo +
                ", createdAt=" + createdAt +
                "}\n";
    }
}