package tn.esprit.models;

import java.util.ArrayList;
import java.util.List;

public class Categorie {
    private Integer id;
    private String name;
    private String descrption;
    private String icone;
    private String motsCles;
    private String publicCible;
    private List<Matiere> matieres;

    public Categorie() {
        this.matieres = new ArrayList<>();
    }

    public Categorie(int id, String name, String descrption, String icone, String motsCles, String publicCible) {
        this.id = id;
        this.name = name;
        this.descrption = descrption;
        this.icone = icone;
        this.motsCles = motsCles;
        this.publicCible = publicCible;
        this.matieres = new ArrayList<>();
    }

    public Categorie(String name, String descrption, String icone, String motsCles, String publicCible) {
        this.name = name;
        this.descrption = descrption;
        this.icone = icone;
        this.motsCles = motsCles;
        this.publicCible = publicCible;
        this.matieres = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescrption() {
        return descrption;
    }

    public void setDescrption(String descrption) {
        this.descrption = descrption;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public String getMotsCles() {
        return motsCles;
    }

    public void setMotsCles(String motsCles) {
        this.motsCles = motsCles;
    }

    public String getPublicCible() {
        return publicCible;
    }

    public void setPublicCible(String publicCible) {
        this.publicCible = publicCible;
    }

    public List<Matiere> getMatieres() {
        return matieres;
    }

    public void setMatieres(List<Matiere> matieres) {
        this.matieres = matieres;
    }

    @Override
    public String toString() {
        return name;
    }
}