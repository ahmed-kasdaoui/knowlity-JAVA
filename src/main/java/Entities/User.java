package Entities;

public class User extends tn.knowlity.entity.User {
    private int id;
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String numTelephone;
    private String genre;
    private java.sql.Date dateNaissance;
    private String verificationCode;
    private boolean banned;
    private boolean deleted;
    private int gradeLevel;
    private String specialite;
    private String role; // Le rôle de l'utilisateur (admin, user, etc.)

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.role = "user"; // Par défaut, un nouvel utilisateur est un utilisateur normal
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNumTelephone() {
        return numTelephone;
    }

    public void setNumTelephone(String numTelephone) {
        this.numTelephone = numTelephone;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public java.sql.Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(java.sql.Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }



    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        // Retourner le nom complet comme username
        return String.format("%s %s", prenom, nom).trim();
    }

    // Helper method to check if user is admin
    public boolean isAdmin() {
        return role != null && "admin".equalsIgnoreCase(role);
    }
}
