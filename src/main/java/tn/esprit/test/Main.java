package tn.esprit.test;

import tn.esprit.models.Categorie;
import tn.esprit.models.Matiere;
import tn.esprit.models.Cours;
import tn.esprit.models.Chapitre;
import tn.esprit.services.ServiceCategorie;
import tn.esprit.services.ServiceMatiere;
import tn.esprit.services.ServiceCours;
import tn.esprit.services.ServiceChapitre;

public class Main {
    public static void main(String[] args) {
        ServiceCategorie sc = new ServiceCategorie();
        ServiceMatiere sm = new ServiceMatiere();
        ServiceCours sco = new ServiceCours();
        ServiceChapitre sch = new ServiceChapitre();

        // Ajouter une catégorie
        Categorie cat = new Categorie("Informatique", "Catégorie pour les cours d'informatique", "fa-laptop", "informatique, tech", "étudiants");
        sc.add(cat);

        // Ajouter une matière
        Matiere mat = new Matiere("Programmation Java", cat, "Connaissances de base en programmation", "Introduction à Java", "#FF5733");
        sm.add(mat);

        // Ajouter un cours
        Cours cours = new Cours("Java pour débutants", "Apprendre les bases de Java", "java.jpg", mat, "fr", 100, "https://payment.link");
        sco.add(cours);

        // Ajouter un chapitre


        // Afficher les données
        System.out.println("Catégories : " + sc.getAll());
        System.out.println("Matières : " + sm.getAll());
        System.out.println("Cours : " + sco.getAll());
        System.out.println("Chapitres : " + sch.getAll());
    }
}