package tn.esprit.models;

public class Chapitre {
    private int id;
    private String title;
    private int chapOrder;
    private Cours cours;
    private String contenu;
    private int dureeEstimee;
    private int nbrVues;
    private String brochure;
    private String description;
    private String creationDate;

    public Chapitre() {
    }

    public String getBrochure() {
        return brochure;
    }

    public Chapitre(int id, String title, int chapOrder, Cours cours, String contenu, int dureeEstimee, int nbrVues, String description, String creationDate) {
        this.id = id;
        this.title = title;
        this.chapOrder = chapOrder;
        this.cours = cours;
        this.contenu = contenu;
        this.dureeEstimee = dureeEstimee;
        this.nbrVues = nbrVues;
        this.description = description;
        this.creationDate = creationDate;
    }

    public Chapitre(String title, int chapOrder, Cours cours, String contenu, int dureeEstimee, int nbrVues, String description, String creationDate) {
        this.title = title;
        this.chapOrder = chapOrder;
        this.cours = cours;
        this.contenu = contenu;
        this.dureeEstimee = dureeEstimee;
        this.nbrVues = nbrVues;
        this.description = description;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getChapOrder() {
        return chapOrder;
    }

    public void setChapOrder(int chapOrder) {
        this.chapOrder = chapOrder;
    }

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        this.cours = cours;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public int getDureeEstimee() {
        return dureeEstimee;
    }

    public void setDureeEstimee(int dureeEstimee) {
        this.dureeEstimee = dureeEstimee;
    }

    public int getNbrVues() {
        return nbrVues;
    }

    public void setNbrVues(int nbrVues) {
        this.nbrVues = nbrVues;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "Chapitre{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", chapOrder=" + chapOrder +
                ", cours=" + (cours != null ? cours.getTitle() : "null") +
                ", contenu='" + contenu + '\'' +
                ", dureeEstimee=" + dureeEstimee +
                ", nbrVues=" + nbrVues +
                ", description='" + description + '\'' +
                ", creationDate='" + creationDate + '\'' +
                "}\n";
    }

    public void setBrochure(String brochure) {
        this.brochure = brochure;
    }
}