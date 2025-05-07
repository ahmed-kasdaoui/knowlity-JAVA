package tn.knowlity.service;

import tn.knowlity.entity.User;

import java.sql.SQLException;
import java.util.List;

public interface IService {
    public void ajouterEtudiant(User user) throws SQLException;
    public void ajouterEnseignant(User user) throws SQLException;
    public void supprimerUser(User user) throws SQLException;
    public User Authentification(String prenom,String nom) throws SQLException;
    public List<User> afficherdetailsuser() throws SQLException;
    public  User recherparid(int id) throws SQLException;
    public void modifier(String nom,String prenom,String email,String localisation,int numtel,String image,int id) throws SQLException;
    public List<User> recherparnom(String nom) throws SQLException;
    public void ajouterverificationcode(String verifcode,String email) throws SQLException;
    public void modifierpassword(String password,String confirmpassword,String mail) throws SQLException;
    public User recherparemail(String email) throws SQLException;
    public void bannneruser(User user) throws SQLException;
}
