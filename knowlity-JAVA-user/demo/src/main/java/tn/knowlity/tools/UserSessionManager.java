package tn.knowlity.tools;

import tn.knowlity.entity.User;

public class UserSessionManager {
    // Instance unique de la classe (Singleton)
    private static UserSessionManager instance;


    private User currentUser;


    public UserSessionManager() {}

    // Méthode pour obtenir l'instance unique
    public static UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
    }


    public User getCurrentUser() {
        return this.currentUser;
    }


    public boolean isLoggedIn() {
        return this.currentUser != null;
    }

    // Méthode pour déconnecter l'utilisateur
    public void logout() {
        this.currentUser = null;
    }
}