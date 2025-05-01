package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeCovoiturage {
    private int id;
    private int passagerId;
    private String depart;
    private String destination;
    private LocalDateTime date;
    private String statut;
    private float budget;
    private List<PropositionCovoiturage> propositionCovoiturages;

    public DemandeCovoiturage() {
        this.date = LocalDateTime.now();
        this.propositionCovoiturages = new ArrayList<>();
    }

    public DemandeCovoiturage(int id, int passagerId, String depart, String destination, LocalDateTime date, String statut, float budget) {
        this.id = id;
        this.passagerId = passagerId;
        this.depart = depart;
        this.destination = destination;
        this.date = date;
        this.statut = statut;
        this.budget = budget;
        this.propositionCovoiturages = new ArrayList<>();
    }

    public DemandeCovoiturage(int passagerId, String depart, String destination, LocalDateTime date, String statut, float budget) {
        this.passagerId = passagerId;
        this.depart = depart;
        this.destination = destination;
        this.date = date;
        this.statut = statut;
        this.budget = budget;
        this.propositionCovoiturages = new ArrayList<>();
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

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
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

    public float getBudget() {
        return budget;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }

    public List<PropositionCovoiturage> getPropositionCovoiturages() {
        return propositionCovoiturages;
    }

    public void setPropositionCovoiturages(List<PropositionCovoiturage> propositionCovoiturages) {
        this.propositionCovoiturages = propositionCovoiturages;
    }

    @Override
    public String toString() {
        return "DemandeCovoiturage{" +
                "id=" + id +
                ", passagerId=" + passagerId +
                ", depart='" + depart + '\'' +
                ", destination='" + destination + '\'' +
                ", date=" + date +
                ", statut='" + statut + '\'' +
                ", budget=" + budget +
                "}\n";
    }
}